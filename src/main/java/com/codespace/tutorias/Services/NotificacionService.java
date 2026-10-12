package com.codespace.tutorias.Services;

import com.codespace.tutorias.Helpers.Dia;
import com.codespace.tutorias.Helpers.Reglas;
import com.codespace.tutorias.Models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Correos del sistema (RN-18). Arma el contenido dentro de la transacción y
 * lo publica como {@link CorreoPendiente}; {@link EnvioCorreos} lo envía en
 * segundo plano solo si la transacción se confirma.
 */
@Service
public class NotificacionService {

    /** Correo listo para enviar (solo texto: cruza de hilo sin entidades JPA). */
    public record CorreoPendiente(String destinatario, String asunto, String html) {}

    private static final Locale ES = Locale.forLanguageTag("es-MX");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", ES);
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    private ApplicationEventPublisher publisher;

    public void confirmacionInscripcion(Inscripcion inscripcion) {
        Tutoria t = inscripcion.getTutoria();
        enviar(inscripcion.getTutorado().getCorreo(),
                "Inscripción confirmada · " + t.getMateria().getMateria(),
                "Inscripción confirmada",
                "<p>Ya tienes lugar en <b>" + esc(t.getMateria().getMateria()) + "</b> con "
                        + esc(t.getTutor().nombreCompleto()) + ".</p>"
                        + horariosYLugar(t)
                        + "<p>Recibirás un recordatorio " + Reglas.HORAS_RECORDATORIO
                        + " horas antes de cada sesión. Con más de " + Reglas.INASISTENCIAS_PERMITIDAS
                        + " inasistencias se te da de baja automáticamente.</p>");
    }

    public void bajaVoluntaria(Inscripcion inscripcion) {
        Tutoria t = inscripcion.getTutoria();
        enviar(inscripcion.getTutorado().getCorreo(),
                "Baja de tutoría · " + t.getMateria().getMateria(),
                "Te diste de baja",
                "<p>Te diste de baja de <b>" + esc(t.getMateria().getMateria()) + "</b>. "
                        + "Tu lugar quedó libre para otro tutorado.</p>");
    }

    public void recordatorio(Inscripcion inscripcion, Horario horario, LocalDate fecha) {
        Tutoria t = horario.getTutoria();
        String temas = horario.getTemas().isEmpty() ? "" :
                "<p>Temas: " + horario.getTemas().stream().map(tm -> esc(tm.getTema()))
                        .collect(Collectors.joining(", ")) + ".</p>";
        enviar(inscripcion.getTutorado().getCorreo(),
                "Recordatorio · " + t.getMateria().getMateria() + " a las " + hora(horario.getHoraInicio()),
                "Tu sesión es hoy",
                "<p><b>" + esc(t.getMateria().getMateria()) + "</b> · " + fecha(fecha) + ", "
                        + hora(horario.getHoraInicio()) + "–" + hora(horario.getHoraFin())
                        + " · " + lugar(t) + ".</p>" + temas);
    }

    public void sesionCancelada(Sesion sesion, List<Inscripcion> inscritos) {
        Tutoria t = sesion.getHorario().getTutoria();
        for (Inscripcion inscripcion : inscritos) {
            enviar(inscripcion.getTutorado().getCorreo(),
                    "Sesión cancelada · " + t.getMateria().getMateria(),
                    "Sesión cancelada",
                    "<p>La sesión de <b>" + esc(t.getMateria().getMateria()) + "</b> del "
                            + fecha(sesion.getFecha()) + " (" + hora(sesion.getHoraInicio()) + "–"
                            + hora(sesion.getHoraFin()) + ") fue cancelada por el tutor. "
                            + "No cuenta para tus inasistencias.</p>");
        }
    }

    public void bajaAutomatica(Inscripcion inscripcion, long inasistencias) {
        Tutoria t = inscripcion.getTutoria();
        String materia = esc(t.getMateria().getMateria());
        enviar(inscripcion.getTutorado().getCorreo(),
                "Baja automática · " + t.getMateria().getMateria(),
                "Baja automática",
                "<p>Acumulaste <b>" + inasistencias + " inasistencias</b> en " + materia
                        + " y se te dio de baja automáticamente. Ya no puedes volver a inscribirte "
                        + "a esta tutoría.</p>");
        enviar(t.getTutor().getCorreo(),
                "Baja automática en " + t.getMateria().getMateria(),
                "Baja automática de un tutorado",
                "<p>" + esc(inscripcion.getTutorado().nombreCompleto()) + " ("
                        + esc(inscripcion.getTutorado().getMatricula()) + ") acumuló " + inasistencias
                        + " inasistencias en " + materia + " y quedó dado de baja. Su lugar quedó libre.</p>");
    }

    public void listaSinCerrar(Sesion sesion) {
        Tutoria t = sesion.getHorario().getTutoria();
        enviar(t.getTutor().getCorreo(),
                "Pase de lista sin cerrar · " + t.getMateria().getMateria(),
                "Cierra el pase de lista",
                "<p>La sesión de <b>" + esc(t.getMateria().getMateria()) + "</b> del "
                        + fecha(sesion.getFecha()) + " terminó y su pase de lista sigue abierto. "
                        + "Ciérralo para que la sesión cuente como completada.</p>");
    }

    public void tutoriaDesactivada(Tutoria tutoria, List<Inscripcion> inscritos) {
        for (Inscripcion inscripcion : inscritos) {
            enviar(inscripcion.getTutorado().getCorreo(),
                    "Tutoría finalizada · " + tutoria.getMateria().getMateria(),
                    "La tutoría ya no está activa",
                    "<p>La tutoría de <b>" + esc(tutoria.getMateria().getMateria()) + "</b> con "
                            + esc(tutoria.getTutor().nombreCompleto()) + " dejó de estar activa. "
                            + "Ya no habrá más sesiones.</p>");
        }
    }

    // ------------------------------------------------------------------

    private void enviar(String destinatario, String asunto, String titulo, String cuerpo) {
        publisher.publishEvent(new CorreoPendiente(destinatario, asunto, plantilla(titulo, cuerpo)));
    }

    private static String plantilla(String titulo, String cuerpo) {
        return "<div style=\"font-family:Arial,sans-serif;color:#1f2937;max-width:560px\">"
                + "<h2 style=\"color:#1e4482\">" + esc(titulo) + "</h2>" + cuerpo
                + "<p style=\"color:#6b7280;font-size:12px\">Sistema de Tutorías · Facultad de Negocios y Tecnologías, UV</p>"
                + "</div>";
    }

    private static String horariosYLugar(Tutoria t) {
        String horarios = AgendaService.activos(t).stream()
                .map(h -> Dia.de(h.getDia()).nombre() + " " + hora(h.getHoraInicio()) + "–" + hora(h.getHoraFin()))
                .collect(Collectors.joining(", "));
        return "<p>Horarios: " + horarios + " · " + lugar(t) + ".</p>";
    }

    private static String lugar(Tutoria t) {
        return "Edificio " + t.getEdificio() + " · Aula " + t.getAula();
    }

    private static String fecha(LocalDate fecha) {
        return fecha.format(FECHA);
    }

    private static String hora(LocalTime hora) {
        return hora.format(HORA);
    }

    private static String esc(String texto) {
        return HtmlUtils.htmlEscape(texto);
    }
}
