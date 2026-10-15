package com.codespace.tutorias.Services;

import com.codespace.tutorias.Exceptions.DemasiadosIntentosException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Freno a la fuerza bruta en POST /auth/signin. Cuenta los intentos
 * fallidos por cuenta y por IP en una ventana fija; al llegar al máximo,
 * esa llave queda bloqueada hasta que termina la ventana. Un login correcto
 * limpia el contador de la cuenta.
 *
 * Vive en memoria: alcanza para una sola instancia (Render) y se reinicia
 * con cada despliegue.
 */
@Component
public class LimiteIntentos {

    /** Por encima de este tamaño se purgan las ventanas vencidas. */
    private static final int PURGAR_DESDE = 10_000;

    private final Clock clock;
    private final int maxPorCuenta;
    private final int maxPorIp;
    private final Duration ventana;

    private final Map<String, Ventana> fallos = new ConcurrentHashMap<>();

    private record Ventana(Instant inicio, int fallos) {}

    public LimiteIntentos(Clock clock,
                          @Value("${auth.intentos.max-por-cuenta:5}") int maxPorCuenta,
                          @Value("${auth.intentos.max-por-ip:30}") int maxPorIp,
                          @Value("${auth.intentos.ventana-minutos:15}") long ventanaMinutos) {
        this.clock = clock;
        this.maxPorCuenta = maxPorCuenta;
        this.maxPorIp = maxPorIp;
        this.ventana = Duration.ofMinutes(ventanaMinutos);
    }

    /** Lanza DemasiadosIntentosException si la cuenta o la IP están bloqueadas. */
    public void verificar(String cuenta, String ip) {
        Instant ahora = clock.instant();
        bloqueo(llaveCuenta(cuenta), maxPorCuenta, ahora);
        bloqueo(llaveIp(ip), maxPorIp, ahora);
    }

    public void registrarFallo(String cuenta, String ip) {
        Instant ahora = clock.instant();
        if (fallos.size() > PURGAR_DESDE) {
            fallos.values().removeIf(v -> vencida(v, ahora));
        }
        sumar(llaveCuenta(cuenta), ahora);
        sumar(llaveIp(ip), ahora);
    }

    public void registrarExito(String cuenta) {
        fallos.remove(llaveCuenta(cuenta));
    }

    /** Solo pruebas: el reloj de pruebas no avanza entre casos. */
    public void reiniciar() {
        fallos.clear();
    }

    // ------------------------------------------------------------------

    private void bloqueo(String llave, int maximo, Instant ahora) {
        Ventana v = fallos.get(llave);
        if (v == null || vencida(v, ahora) || v.fallos() < maximo) return;

        long segundos = Duration.between(ahora, v.inicio().plus(ventana)).toSeconds();
        throw new DemasiadosIntentosException(Math.max(1, (segundos + 59) / 60));
    }

    private void sumar(String llave, Instant ahora) {
        fallos.compute(llave, (k, v) -> v == null || vencida(v, ahora)
                ? new Ventana(ahora, 1)
                : new Ventana(v.inicio(), v.fallos() + 1));
    }

    private boolean vencida(Ventana v, Instant ahora) {
        return !ahora.isBefore(v.inicio().plus(ventana));
    }

    private static String llaveCuenta(String cuenta) {
        return "cuenta:" + cuenta;
    }

    private static String llaveIp(String ip) {
        return "ip:" + ip;
    }
}
