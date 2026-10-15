package com.codespace.tutorias.Services;

import com.codespace.tutorias.DTO.Mapping.TutoriaMapping;
import com.codespace.tutorias.DTO.Responsive.*;
import com.codespace.tutorias.Exceptions.BusinessException;
import com.codespace.tutorias.Helpers.*;
import com.codespace.tutorias.Models.*;
import com.codespace.tutorias.Repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Inscripciones del tutorado (pantallas 05, 07 y 08; RN-3, RN-4, RN-12). */
@Service
public class InscripcionService {

    public static final String MI_ASISTIO = "ASISTIO";
    public static final String MI_NO_ASISTIO = "NO_ASISTIO";
    public static final String MI_PENDIENTE = "PENDIENTE";
    public static final String MI_NO_CUENTA = "NO_CUENTA";
    public static final String MI_SIN_MARCA = "SIN_MARCA";

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private TutoriaRepository tutoriaRepository;

    @Autowired
    private HorarioRepository horarioRepository;

    @Autowired
    private SesionRepository sesionRepository;

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private TutoriaService tutoriaService;

    @Autowired
    private AgendaService agenda;

    @Autowired
    private NotificacionService notificaciones;

    @Autowired
    private TutoriaMapping mapping;

    @Autowired
    private Reloj reloj;

    @Transactional
    public InscripcionCreadaResponsive inscribir(String matricula, int idTutoria) {
        // Bloquea la tutoría: dos inscripciones simultáneas no pueden pasar el cupo.
        Tutoria tutoria = tutoriaRepository.findByIdParaActualizar(idTutoria)
                .orElseThrow(() -> new BusinessException("Tutoría no encontrada."));
        List<Horario> horarios = AgendaService.activos(tutoria);

        if (!EstadosTutoria.ACTIVA.equals(tutoria.getEstado()) || horarios.isEmpty()) {
            throw new BusinessException("Esta tutoría ya no está disponible.");
        }
        if (inscripcionRepository.existsByTutoria_IdTutoriaAndTutorado_MatriculaAndEstado(
                idTutoria, matricula, EstadosInscripcion.ACTIVA)) {
            throw new BusinessException("Ya estás inscrito en esta tutoría.");
        }
        if (inscripcionRepository.findTutoriasConBajaAutomatica(matricula).contains(idTutoria)) {
            throw new BusinessException("No puedes volver a inscribirte: tuviste baja automática en esta tutoría.");
        }
        if (agenda.ocupados(idTutoria) >= Reglas.CUPO_MAXIMO) {
            throw new BusinessException("La tutoría ya no tiene lugares disponibles.");
        }
        AgendaService.choques(horarios, horarioRepository.findVigentesDeTutorado(matricula)).stream()
                .findFirst().ifPresent(c -> {
                    Horario mio = c.existente();
                    throw new BusinessException("El " + Dia.de(c.candidato().getDia()).nombre() + " de "
                            + c.candidato().getHoraInicio().format(HORA) + " a " + c.candidato().getHoraFin().format(HORA)
                            + " choca con tu inscripción en " + mio.getTutoria().getMateria().getMateria()
                            + " (" + mio.getHoraInicio().format(HORA) + "–" + mio.getHoraFin().format(HORA) + ").");
                });

        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setTutoria(tutoria);
        inscripcion.setTutorado(usuarioService.buscar(matricula));
        inscripcion.setFechaInscripcion(reloj.marca());
        inscripcion.setEstado(EstadosInscripcion.ACTIVA);
        inscripcionRepository.save(inscripcion);

        notificaciones.confirmacionInscripcion(inscripcion);
        return new InscripcionCreadaResponsive(idTutoria, inscripcion.getEstado(),
                Reloj.enZona(inscripcion.getFechaInscripcion()));
    }

    /** Mis tutorías (pantalla 07). */
    @Transactional(readOnly = true)
    public MisInscripcionesResponsive misInscripciones(String matricula) {
        List<Inscripcion> todas = inscripcionRepository.findByTutorado_MatriculaOrderByFechaInscripcionDesc(matricula);
        Map<Integer, Long> faltas = agenda.inasistencias(todas.stream().map(Inscripcion::getIdInscripcion).toList());

        List<InscripcionActivaResponsive> activas = new ArrayList<>();
        List<InscripcionAnteriorResponsive> anteriores = new ArrayList<>();
        Set<Integer> vistas = new HashSet<>();
        AgendaService.Ocurrencia masProxima = null;

        for (Inscripcion inscripcion : todas) {
            Tutoria tutoria = inscripcion.getTutoria();
            if (!vistas.add(tutoria.getIdTutoria())) continue; // solo la más reciente por tutoría
            long inasistencias = faltas.getOrDefault(inscripcion.getIdInscripcion(), 0L);
            TutoriaResumenResponsive resumen = mapping.toResumen(tutoria, agenda.ocupados(tutoria.getIdTutoria()), false);

            boolean activa = EstadosInscripcion.ACTIVA.equals(inscripcion.getEstado())
                    && EstadosTutoria.ACTIVA.equals(tutoria.getEstado());
            if (!activa) {
                anteriores.add(new InscripcionAnteriorResponsive(resumen, info(inscripcion, null), inasistencias));
                continue;
            }

            boolean nueva = !tuvoSesionCompletada(inscripcion);
            Optional<AgendaService.Ocurrencia> proxima = agenda.proxima(AgendaService.activos(tutoria));
            ProximaSesionResponsive proximaDTO = proxima.map(o -> new ProximaSesionResponsive(
                    null, null, null, o.fecha(), o.horario().getDia(), o.horario().getHoraInicio(),
                    o.horario().getHoraFin(), null, null, null, null, nueva)).orElse(null);
            if (proxima.isPresent() && (masProxima == null || antes(proxima.get(), masProxima))) {
                masProxima = proxima.get();
            }

            activas.add(new InscripcionActivaResponsive(resumen, info(inscripcion, nueva), inasistencias,
                    Reglas.INASISTENCIAS_PERMITIDAS, proximaDTO));
        }

        return new MisInscripcionesResponsive(
                masProxima == null ? null : proximaConDetalle(masProxima),
                activas,
                anteriores);
    }

    /** Mi asistencia en una tutoría (pantalla 08). */
    @Transactional(readOnly = true)
    public MiInscripcionResponsive miInscripcion(String matricula, int idTutoria) {
        Inscripcion inscripcion = inscripcionRepository
                .findFirstByTutoria_IdTutoriaAndTutorado_MatriculaOrderByFechaInscripcionDesc(idTutoria, matricula)
                .orElseThrow(() -> new BusinessException("No tienes inscripción en esta tutoría."));
        Tutoria tutoria = inscripcion.getTutoria();

        LocalDate desde = inscripcion.getFechaInscripcion().atZoneSameInstant(Reloj.ZONA).toLocalDate();
        LocalDate hasta = inscripcion.getFechaBaja() == null ? null
                : inscripcion.getFechaBaja().atZoneSameInstant(Reloj.ZONA).toLocalDate();

        Map<Integer, Boolean> misMarcas = new HashMap<>();
        for (Asistencia a : asistenciaRepository.findByInscripcion_IdInscripcion(inscripcion.getIdInscripcion())) {
            misMarcas.put(a.getSesion().getIdSesion(), a.isAsistio());
        }

        List<MiSesionResponsive> historial = new ArrayList<>();
        long conAsistencia = 0;
        long canceladas = 0;
        for (Sesion s : sesionRepository.findByHorario_Tutoria_IdTutoriaOrderByFechaAscHoraInicioAsc(idTutoria)) {
            if (s.getFecha().isBefore(desde) || hasta != null && s.getFecha().isAfter(hasta)) continue;
            String estado = s.estado();
            Boolean marca = misMarcas.get(s.getIdSesion());
            String mia;
            if (EstadosSesion.CANCELADA.equals(estado)) {
                mia = MI_NO_CUENTA;
                canceladas++;
            } else if (EstadosSesion.COMPLETADA.equals(estado)) {
                mia = marca == null ? MI_SIN_MARCA : marca ? MI_ASISTIO : MI_NO_ASISTIO;
                if (Boolean.TRUE.equals(marca)) conAsistencia++;
            } else {
                mia = MI_PENDIENTE;
            }
            historial.add(new MiSesionResponsive(s.getIdSesion(), s.getFecha(), s.getHorario().getDia(),
                    s.getHoraInicio(), s.getHoraFin(), estado, mia));
        }

        ProximaSesionResponsive proximaDTO = null;
        boolean vigente = EstadosInscripcion.ACTIVA.equals(inscripcion.getEstado())
                && EstadosTutoria.ACTIVA.equals(tutoria.getEstado());
        if (vigente) {
            Optional<AgendaService.Ocurrencia> proxima = agenda.proxima(AgendaService.activos(tutoria));
            if (proxima.isPresent()) {
                AgendaService.Ocurrencia o = proxima.get();
                Horario h = o.horario();
                proximaDTO = new ProximaSesionResponsive(null, null, h.getIdHorario(), o.fecha(), h.getDia(),
                        h.getHoraInicio(), h.getHoraFin(), tutoria.getEdificio(), tutoria.getAula(), null,
                        h.getHoraInicio().minusHours(Reglas.HORAS_RECORDATORIO), null);
                if (o.sesion() == null) {
                    historial.add(new MiSesionResponsive(null, o.fecha(), h.getDia(), h.getHoraInicio(),
                            h.getHoraFin(), EstadosSesion.PROGRAMADA, MI_PENDIENTE));
                }
            }
        }

        historial.sort(Comparator.comparing(MiSesionResponsive::fecha)
                .thenComparing(MiSesionResponsive::horaInicio).reversed());

        return new MiInscripcionResponsive(
                tutoriaService.detalle(idTutoria, matricula, Roles.TUTORADO),
                info(inscripcion, null),
                new MiResumenAsistenciaResponsive(agenda.inasistencias(inscripcion.getIdInscripcion()),
                        Reglas.INASISTENCIAS_PERMITIDAS, conAsistencia, canceladas),
                proximaDTO,
                historial);
    }

    @Transactional
    public void darseDeBaja(String matricula, int idTutoria) {
        Inscripcion inscripcion = inscripcionRepository
                .findByTutoria_IdTutoriaAndTutorado_MatriculaAndEstado(idTutoria, matricula, EstadosInscripcion.ACTIVA)
                .orElseThrow(() -> new BusinessException("No tienes una inscripción activa en esta tutoría."));
        inscripcion.setEstado(EstadosInscripcion.BAJA);
        inscripcion.setFechaBaja(reloj.marca());
        inscripcionRepository.save(inscripcion);
        notificaciones.bajaVoluntaria(inscripcion);
    }

    /** "Tu semana" (pantalla 04). */
    @Transactional(readOnly = true)
    public List<SemanaItemResponsive> semana(String matricula) {
        return horarioRepository.findVigentesDeTutorado(matricula).stream()
                .sorted(AgendaService.ORDEN_HORARIOS)
                .map(h -> new SemanaItemResponsive(h.getDia(), h.getHoraInicio(), h.getHoraFin(),
                        h.getTutoria().getIdTutoria(), mapping.toDTO(h.getTutoria().getMateria())))
                .toList();
    }

    // ------------------------------------------------------------------

    private static InscripcionInfoResponsive info(Inscripcion inscripcion, Boolean nueva) {
        return new InscripcionInfoResponsive(inscripcion.getEstado(),
                Reloj.enZona(inscripcion.getFechaInscripcion()), Reloj.enZona(inscripcion.getFechaBaja()), nueva);
    }

    /** "NUEVA": aún no hay sesiones completadas desde que se inscribió. */
    private boolean tuvoSesionCompletada(Inscripcion inscripcion) {
        return sesionRepository.findByHorario_Tutoria_IdTutoriaOrderByFechaAscHoraInicioAsc(
                        inscripcion.getTutoria().getIdTutoria()).stream()
                .anyMatch(s -> EstadosSesion.COMPLETADA.equals(s.estado())
                        && s.getFechaListaCerrada().isAfter(inscripcion.getFechaInscripcion()));
    }

    private ProximaSesionResponsive proximaConDetalle(AgendaService.Ocurrencia o) {
        Horario h = o.horario();
        Tutoria t = h.getTutoria();
        return new ProximaSesionResponsive(t.getIdTutoria(), mapping.toDTO(t.getMateria()), h.getIdHorario(),
                o.fecha(), h.getDia(), h.getHoraInicio(), h.getHoraFin(), t.getEdificio(), t.getAula(),
                h.getTemas().stream().map(Tema::getTema).toList(),
                h.getHoraInicio().minusHours(Reglas.HORAS_RECORDATORIO), null);
    }

    private static boolean antes(AgendaService.Ocurrencia a, AgendaService.Ocurrencia b) {
        int porFecha = a.fecha().compareTo(b.fecha());
        return porFecha != 0 ? porFecha < 0 : a.horario().getHoraInicio().isBefore(b.horario().getHoraInicio());
    }
}
