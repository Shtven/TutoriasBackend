package com.codespace.tutorias;

import com.codespace.tutorias.Exceptions.ServicioOcupadoException;
import com.codespace.tutorias.Services.TurnoBcrypt;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/** BCrypt en POST /auth/signin: con turnos y sin retener conexiones de la BD. */
class SigninConcurrenciaTest extends PruebaIntegracion {

    @MockitoSpyBean
    private PasswordEncoder codificador;

    @Autowired
    private HikariDataSource pool;

    @Test
    void veinteLoginsALaVezRespetanLosTurnosYNoRetienenConexiones() throws Exception {
        usuarioConPwd("S23014587", "a@prueba.mx", "TUTORADO", "secreta123");
        AtomicInteger dentro = new AtomicInteger();
        AtomicInteger maximo = new AtomicInteger();
        AtomicInteger conexiones = new AtomicInteger();
        doAnswer(inv -> {
            maximo.accumulateAndGet(dentro.incrementAndGet(), Math::max);
            Thread.sleep(40);
            conexiones.accumulateAndGet(pool.getHikariPoolMXBean().getActiveConnections(), Math::max);
            try {
                return inv.callRealMethod();
            } finally {
                dentro.decrementAndGet();
            }
        }).when(codificador).matches(any(), any());

        List<Integer> estados = simultaneos(20, "secreta123");

        assertThat(estados).hasSize(20).containsOnly(200);
        assertThat(maximo.get()).isBetween(1, 2);
        // Sin la transacción, los 18 que esperan turno no tienen conexión tomada.
        assertThat(conexiones.get()).isLessThanOrEqualTo(4);
    }

    @Test
    void losFallosEnParaleloNoSuperanElLimiteDeLaCuenta() throws Exception {
        usuarioConPwd("S23014587", "a@prueba.mx", "TUTORADO", "secreta123");

        List<Integer> estados = simultaneos(20, "equivocada");

        // 5 fallos bloquean; con 2 turnos puede colarse a lo mucho uno más.
        assertThat(estados.stream().filter(s -> s == 401).count()).isBetween(5L, 6L);
        assertThat(estados.stream().filter(s -> s == 429).count()).isBetween(14L, 15L);
    }

    @Test
    void sinTurnoATiempoSeRechaza() throws Exception {
        TurnoBcrypt turno = new TurnoBcrypt(1, 0);
        CountDownLatch ocupado = new CountDownLatch(1);
        CountDownLatch soltar = new CountDownLatch(1);
        ExecutorService hilo = Executors.newSingleThreadExecutor();
        Future<?> primero = hilo.submit(() -> turno.ejecutar(() -> {
            ocupado.countDown();
            try {
                soltar.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
        ocupado.await(5, TimeUnit.SECONDS);

        assertThatThrownBy(() -> turno.ejecutar(() -> { })).isInstanceOf(ServicioOcupadoException.class);

        soltar.countDown();
        primero.get(5, TimeUnit.SECONDS);
        AtomicInteger veces = new AtomicInteger();
        turno.ejecutar(veces::incrementAndGet);
        assertThat(veces.get()).isEqualTo(1);
        hilo.shutdown();
    }

    private List<Integer> simultaneos(int n, String pwd) throws Exception {
        ExecutorService hilos = Executors.newFixedThreadPool(n);
        CountDownLatch salida = new CountDownLatch(1);
        List<Future<Integer>> respuestas = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            respuestas.add(hilos.submit(() -> {
                salida.await();
                return mvc.perform(post("/auth/signin").contentType(MediaType.APPLICATION_JSON)
                                .content("{\"matricula\":\"S23014587\",\"pwd\":\"" + pwd + "\"}"))
                        .andReturn().getResponse().getStatus();
            }));
        }
        salida.countDown();
        List<Integer> estados = new ArrayList<>();
        for (Future<Integer> r : respuestas) estados.add(r.get(60, TimeUnit.SECONDS));
        hilos.shutdown();
        return estados;
    }
}
