package com.codespace.tutorias.Services;

import com.codespace.tutorias.Helpers.EmailHelper;
import com.codespace.tutorias.Models.Asistencia;
import com.codespace.tutorias.Models.Usuario;
import com.codespace.tutorias.Models.Tutoria;
import com.codespace.tutorias.Repositories.TutoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;

@Service
public class EmailService {

    @Autowired
    private TutoriaRepository tutoriaRepository;

    @Autowired
    private EmailHelper emailHelper;

    @Scheduled(fixedRate = 60000)
    public void enviarRecordatorios() {

        ZoneId zonaMexico = ZoneId.of("America/Mexico_City");
        ZonedDateTime ahoraZoned = ZonedDateTime.now(zonaMexico);

        LocalDate hoy = ahoraZoned.toLocalDate();
        LocalTime ahora = ahoraZoned.toLocalTime();

        List<Tutoria> tutorias = tutoriaRepository.findAll();

        for (Tutoria tutoria : tutorias) {

            if (!esValidaParaRecordatorio(tutoria, hoy)) continue;

            LocalTime inicio = tutoria.getHorario().getHoraInicio().toLocalTime();
            long minutosRestantes = Duration.between(ahora, inicio).toMinutes();

            if (minutosRestantes <= 15 && ahora.isBefore(inicio)) {

                List<Asistencia> asistencias = tutoria.getAsistencias();

                for (Asistencia a : asistencias) {

                    Usuario usuario = a.getUsuario();

                    //if (!usuario.isRecordatorio()) continue;

                    emailHelper.enviarCorreo(
                            usuario.getCorreo(),
                            "Recordatorio de tutoría próxima",
                            construirCorreoAlumno(usuario, tutoria)
                    );
                }

                Usuario tutor = tutoria.getHorario().getTutor();

                emailHelper.enviarCorreo(
                        tutor.getCorreo(),
                        "Recordatorio: tu tutoría comienza en 15 minutos",
                        construirCorreoTutor(tutor, tutoria)
                );

                tutoria.setEstado("A PUNTO DE INICIAR");
                tutoriaRepository.save(tutoria);
            }
        }
    }

    public void enviarCorreoCancelacion(Tutoria tutoria) {

        List<Asistencia> asistencias = tutoria.getAsistencias();

        if (asistencias.isEmpty()) return;

        for (Asistencia a : asistencias) {

            Usuario usuario = a.getUsuario();

            emailHelper.enviarCorreo(
                    usuario.getCorreo(),
                    "Cancelación de tutoría",
                    construirCorreoCancelacion(usuario, tutoria)
            );
        }
    }

    private boolean esValidaParaRecordatorio(Tutoria tutoria, LocalDate hoy) {
        return !("COMPLETADA".equalsIgnoreCase(tutoria.getEstado()) ||
                "CANCELADA".equalsIgnoreCase(tutoria.getEstado()))
                && tutoria.getFecha().equals(hoy)
                && "PENDIENTE".equalsIgnoreCase(tutoria.getEstado());
    }

    private String construirCorreoAlumno(Usuario usuario, Tutoria tutoria) {
        return plantillaBase(
                "¡Hola " + usuario.getNombre() + "!",
                "Te recordamos que tienes una tutoría próxima a iniciar.",
                tutoria
        );
    }

    private String construirCorreoTutor(Usuario tutor, Tutoria tutoria) {
        return plantillaBase(
                "¡Hola " + tutor.getNombre() + "!",
                "Tu tutoría está a punto de comenzar.",
                tutoria
        );
    }

    private String construirCorreoCancelacion(Usuario usuario, Tutoria tutoria) {
        return plantillaBase(
                "¡Hola " + usuario.getNombre() + "!",
                "La tutoría ha sido cancelada.",
                tutoria
        );
    }

    private String plantillaBase(String titulo, String mensaje, Tutoria tutoria) {

        return new StringBuilder()
                .append("<html>")
                .append("<body style='font-family: Segoe UI; padding:20px;'>")
                .append("<div style='max-width:600px;margin:auto;background:white;padding:20px;border-radius:10px;'>")
                .append("<h2>").append(titulo).append("</h2>")
                .append("<p>").append(mensaje).append("</p>")
                .append("<p><strong>Materia:</strong> ").append(tutoria.getMateria().getMateria()).append("</p>")
                .append("<p><strong>Hora:</strong> ").append(tutoria.getHorario().getHoraInicio()).append("</p>")
                .append("<p><strong>Aula:</strong> ").append(tutoria.getEdificio()).append(" / ").append(tutoria.getAula()).append("</p>")
                .append("</div>")
                .append("</body>")
                .append("</html>")
                .toString();
    }
}