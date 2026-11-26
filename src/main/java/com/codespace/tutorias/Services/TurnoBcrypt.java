package com.codespace.tutorias.Services;

import com.codespace.tutorias.Exceptions.ServicioOcupadoException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Turnos para la comparación BCrypt de POST /auth/signin. BCrypt gasta CPU a
 * propósito: si entran 50 personas a la vez, 50 comparaciones simultáneas
 * dejan sin CPU a quienes ya entraron. Aquí solo corren unas pocas y el resto
 * espera por orden de llegada, sin ocupar conexiones de la BD.
 */
@Component
public class TurnoBcrypt {

    private final Semaphore turnos;
    private final long esperaSegundos;

    public TurnoBcrypt(@Value("${auth.bcrypt.concurrencia:2}") int concurrencia,
                       @Value("${auth.bcrypt.espera-segundos:45}") long esperaSegundos) {
        this.turnos = new Semaphore(Math.max(1, concurrencia), true);
        this.esperaSegundos = esperaSegundos;
    }

    /** Ejecuta la tarea con un turno; ServicioOcupadoException si no llega a tiempo. */
    public void ejecutar(Runnable tarea) {
        if (!hayTurno()) throw new ServicioOcupadoException();
        try {
            tarea.run();
        } finally {
            turnos.release();
        }
    }

    private boolean hayTurno() {
        try {
            return turnos.tryAcquire(esperaSegundos, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
