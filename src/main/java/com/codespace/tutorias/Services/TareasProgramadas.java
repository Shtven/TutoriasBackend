package com.codespace.tutorias.Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Procesos que corren cada minuto: recordatorio 2 h antes (RN-17) y aviso
 * de lista sin cerrar (RN-9). Se apagan con app.tareas.habilitadas=false.
 */
@Component
@ConditionalOnProperty(name = "app.tareas.habilitadas", havingValue = "true", matchIfMissing = true)
public class TareasProgramadas {

    private static final Logger log = LoggerFactory.getLogger(TareasProgramadas.class);

    @Autowired
    private SesionService sesionService;

    @Scheduled(fixedRate = 60_000)
    public void recordatorios() {
        try {
            int enviados = sesionService.procesarRecordatorios();
            if (enviados > 0) log.info("Recordatorios encolados: {}", enviados);
        } catch (Exception e) {
            log.error("Fallo el proceso de recordatorios", e);
        }
    }

    @Scheduled(fixedRate = 60_000)
    public void avisosDeCierre() {
        try {
            int enviados = sesionService.procesarAvisosDeCierre();
            if (enviados > 0) log.info("Avisos de lista sin cerrar encolados: {}", enviados);
        } catch (Exception e) {
            log.error("Fallo el proceso de avisos de cierre", e);
        }
    }
}
