package com.codespace.tutorias.Services;

import com.codespace.tutorias.Helpers.EmailHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Envía los correos después del commit y fuera del hilo de la petición.
 * Un fallo de SendGrid se registra pero no revierte nada (RN-18).
 */
@Component
public class EnvioCorreos {

    private static final Logger log = LoggerFactory.getLogger(EnvioCorreos.class);

    @Autowired
    private EmailHelper emailHelper;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void enviar(NotificacionService.CorreoPendiente correo) {
        try {
            emailHelper.enviarCorreo(correo.destinatario(), correo.asunto(), correo.html());
        } catch (Exception e) {
            log.error("No se pudo enviar el correo '{}' a {}", correo.asunto(), correo.destinatario(), e);
        }
    }
}
