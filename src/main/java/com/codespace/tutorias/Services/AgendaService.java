package com.codespace.tutorias.Services;

import com.codespace.tutorias.Helpers.*;
import com.codespace.tutorias.Models.Horario;
import com.codespace.tutorias.Models.Sesion;
import com.codespace.tutorias.Models.Tutoria;
import com.codespace.tutorias.Repositories.AsistenciaRepository;
import com.codespace.tutorias.Repositories.InscripcionRepository;
import com.codespace.tutorias.Repositories.SesionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Cálculos de calendario compartidos: ocurrencias de cada horario (sesiones
 * registradas + virtuales, RN-6), próxima sesión, cupo y choques (RN-1/2/4).
 */
@Service
public class AgendaService {

    /** Orden de presentación: día de la semana y hora de inicio. */
    public static final Comparator<Horario> ORDEN_HORARIOS = Comparator
            .comparing((Horario h) -> Dia.de(h.getDia()).ordinal())
            .thenComparing(Horario::getHoraInicio);

    /** Ocurrencia de un horario en una fecha; sesion = null si es virtual. */
    public record Ocurrencia(Horario horario, LocalDate fecha, Sesion sesion) {

        public String estado() {
            return sesion == null ? EstadosSesion.PROGRAMADA : sesion.estado();
        }

        public boolean cuenta() {
            return sesion == null || sesion.getFechaCancelada() == null && sesion.getFechaListaCerrada() == null;
        }
    }

    /** Un horario candidato que se traslapa con uno existente. */
    public record Choque(Horario candidato, Horario existente) {}

    @Autowired
    private SesionRepository sesionRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private Reloj reloj;

    public static List<Horario> activos(Tutoria tutoria) {
        return tutoria.getHorarios().stream()
                .filter(h -> EstadosHorario.ACTIVO.equals(h.getEstado()))
                .sorted(ORDEN_HORARIOS)
                .toList();
    }

    // ------------------------------------------------------------------
    // Cupo (RN-3)
    // ------------------------------------------------------------------

    public long ocupados(int idTutoria) {
        return inscripcionRepository.countByTutoria_IdTutoriaAndEstado(idTutoria, EstadosInscripcion.ACTIVA);
    }

    public Map<Integer, Long> ocupados(Collection<Integer> idsTutoria) {
        Map<Integer, Long> resultado = new HashMap<>();
        if (idsTutoria.isEmpty()) return resultado;
        for (Object[] fila : inscripcionRepository.contarActivasPorTutoria(idsTutoria)) {
            resultado.put((Integer) fila[0], (Long) fila[1]);
        }
        return resultado;
    }

    // ------------------------------------------------------------------
    // Inasistencias (RN-10)
    // ------------------------------------------------------------------

    public long inasistencias(int idInscripcion) {
        return asistenciaRepository.contarInasistencias(idInscripcion);
    }

    public Map<Integer, Long> inasistencias(Collection<Integer> idsInscripcion) {
        Map<Integer, Long> resultado = new HashMap<>();
        if (idsInscripcion.isEmpty()) return resultado;
        for (Object[] fila : asistenciaRepository.contarInasistenciasPorInscripcion(idsInscripcion)) {
            resultado.put((Integer) fila[0], (Long) fila[1]);
        }
        return resultado;
    }

    // ------------------------------------------------------------------
    // Ocurrencias
    // ------------------------------------------------------------------

    /**
     * Próxima ocurrencia de los horarios que no esté cancelada ni completada y
     * cuya hora de fin aún no pasa (incluye una sesión en curso).
     */
    public Optional<Ocurrencia> proxima(List<Horario> horarios) {
        if (horarios.isEmpty()) return Optional.empty();

        LocalDate hoy = reloj.hoy();
        ZonedDateTime ahora = reloj.ahora();
        Map<String, Sesion> registradas = registradas(horarios, hoy, hoy.plusWeeks(6));

        Ocurrencia mejor = null;
        for (Horario horario : horarios) {
            LocalDate fecha = Dia.de(horario.getDia()).siguienteDesde(hoy);
            for (int semana = 0; semana < 6; semana++, fecha = fecha.plusWeeks(1)) {
                if (fecha.equals(hoy) && !ahora.isBefore(reloj.en(fecha, horario.getHoraFin()))) continue;
                Ocurrencia ocurrencia = new Ocurrencia(horario, fecha, registradas.get(clave(horario, fecha)));
                if (!ocurrencia.cuenta()) continue;
                if (mejor == null || antes(ocurrencia, mejor)) mejor = ocurrencia;
                break;
            }
        }
        return Optional.ofNullable(mejor);
    }

    /**
     * Ocurrencias en [desde, hasta]: las registradas de cualquier horario
     * recibido y las virtuales solo de horarios ACTIVO, sin duplicar.
     */
    public List<Ocurrencia> ocurrencias(List<Horario> horarios, LocalDate desde, LocalDate hasta) {
        List<Ocurrencia> resultado = new ArrayList<>();
        if (horarios.isEmpty() || hasta.isBefore(desde)) return resultado;

        Map<String, Sesion> registradas = registradas(horarios, desde, hasta);
        Set<String> usadas = new HashSet<>();

        for (Horario horario : horarios) {
            boolean activo = EstadosHorario.ACTIVO.equals(horario.getEstado());
            for (LocalDate fecha = Dia.de(horario.getDia()).siguienteDesde(desde);
                 !fecha.isAfter(hasta); fecha = fecha.plusWeeks(1)) {
                String clave = clave(horario, fecha);
                Sesion sesion = registradas.get(clave);
                if (sesion != null || activo) {
                    resultado.add(new Ocurrencia(horario, fecha, sesion));
                    usadas.add(clave);
                }
            }
        }
        // Sesiones registradas en fechas que ya no coinciden con el día (horario editado).
        registradas.forEach((clave, sesion) -> {
            if (!usadas.contains(clave)) resultado.add(new Ocurrencia(sesion.getHorario(), sesion.getFecha(), sesion));
        });

        resultado.sort(Comparator.comparing(Ocurrencia::fecha)
                .thenComparing(o -> o.horario().getHoraInicio()));
        return resultado;
    }

    private Map<String, Sesion> registradas(List<Horario> horarios, LocalDate desde, LocalDate hasta) {
        List<Integer> ids = horarios.stream().map(Horario::getIdHorario).toList();
        Map<String, Sesion> mapa = new HashMap<>();
        for (Sesion sesion : sesionRepository.findByHorario_IdHorarioInAndFechaBetween(ids, desde, hasta)) {
            mapa.put(clave(sesion.getHorario(), sesion.getFecha()), sesion);
        }
        return mapa;
    }

    private static String clave(Horario horario, LocalDate fecha) {
        return horario.getIdHorario() + "|" + fecha;
    }

    private static boolean antes(Ocurrencia a, Ocurrencia b) {
        int porFecha = a.fecha().compareTo(b.fecha());
        return porFecha != 0 ? porFecha < 0 : a.horario().getHoraInicio().isBefore(b.horario().getHoraInicio());
    }

    // ------------------------------------------------------------------
    // Choques
    // ------------------------------------------------------------------

    /** Pares (candidato, existente) del mismo día que se traslapan. */
    public static List<Choque> choques(List<Horario> candidatos, List<Horario> existentes) {
        List<Choque> resultado = new ArrayList<>();
        for (Horario candidato : candidatos) {
            for (Horario existente : existentes) {
                if (candidato.getDia().equals(existente.getDia())
                        && DateHelper.haySolapamiento(candidato.getHoraInicio(), candidato.getHoraFin(),
                        existente.getHoraInicio(), existente.getHoraFin())) {
                    resultado.add(new Choque(candidato, existente));
                }
            }
        }
        return resultado;
    }
}
