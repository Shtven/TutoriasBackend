package com.codespace.tutorias.Services;

import com.codespace.tutorias.DTO.Mapping.TutoriaMapping;
import com.codespace.tutorias.DTO.Request.ActualizarTutoriaRequest;
import com.codespace.tutorias.DTO.Request.HorarioRequest;
import com.codespace.tutorias.DTO.Request.TutoriaRequest;
import com.codespace.tutorias.DTO.Responsive.*;
import com.codespace.tutorias.Exceptions.BusinessException;
import com.codespace.tutorias.Helpers.*;
import com.codespace.tutorias.Models.*;
import com.codespace.tutorias.Repositories.HorarioRepository;
import com.codespace.tutorias.Repositories.InscripcionRepository;
import com.codespace.tutorias.Repositories.SesionRepository;
import com.codespace.tutorias.Repositories.TutoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Tutorías: explorar, detalle, alta, edición, desactivación e inscritos. */
@Service
public class TutoriaService {

    // Estados del panel de inscripción (pantalla 06).
    public static final String PANEL_BAJA_AUTOMATICA = "BAJA_AUTOMATICA";
    public static final String PANEL_NO_ACTIVA = "NO_ACTIVA";
    public static final String PANEL_INSCRIPCION_ACTIVA = "INSCRIPCION_ACTIVA";
    public static final String PANEL_LLENA = "LLENA";
    public static final String PANEL_CHOQUE_HORARIO = "CHOQUE_HORARIO";
    public static final String PANEL_CON_LUGAR = "CON_LUGAR";

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    /** Horario ya normalizado del request; posicion es 1-based para mensajes. */
    private record HorarioNuevo(Integer idHorario, String dia, LocalTime inicio, LocalTime fin,
                                List<String> temas, int posicion) {}

    @Autowired
    private TutoriaRepository tutoriaRepository;

    @Autowired
    private HorarioRepository horarioRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private SesionRepository sesionRepository;

    @Autowired
    private MateriaService materiaService;

    @Autowired
    private CatalogoService catalogoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private AgendaService agenda;

    @Autowired
    private NotificacionService notificaciones;

    @Autowired
    private TutoriaMapping mapping;

    // ------------------------------------------------------------------
    // Consultas
    // ------------------------------------------------------------------

    /** Explorar tutorías (pantalla 03, RN-12). */
    @Transactional(readOnly = true)
    public List<TutoriaExplorarResponsive> explorar(String matricula, String q, String dia,
                                                     boolean soloConLugar, boolean ocultarChoques) {
        String diaFiltro = dia == null || dia.isBlank() ? null : Dia.normalizar(dia);

        List<Horario> misHorarios = horarioRepository.findVigentesDeTutorado(matricula);
        Set<Integer> excluidas = new HashSet<>(inscripcionRepository.findTutoriasConBajaAutomatica(matricula));
        misHorarios.forEach(h -> excluidas.add(h.getTutoria().getIdTutoria()));

        List<Tutoria> candidatas = tutoriaRepository.findByEstado(EstadosTutoria.ACTIVA).stream()
                .filter(t -> !excluidas.contains(t.getIdTutoria()))
                .filter(t -> coincideBusqueda(t, q))
                .filter(t -> diaFiltro == null || AgendaService.activos(t).stream()
                        .anyMatch(h -> h.getDia().equals(diaFiltro)))
                .sorted(Comparator.comparing((Tutoria t) -> Texto.normalizar(t.getMateria().getMateria())))
                .toList();

        Map<Integer, Long> ocupados = agenda.ocupados(candidatas.stream().map(Tutoria::getIdTutoria).toList());

        List<TutoriaExplorarResponsive> resultado = new ArrayList<>();
        for (Tutoria tutoria : candidatas) {
            long ocupadosTutoria = ocupados.getOrDefault(tutoria.getIdTutoria(), 0L);
            if (soloConLugar && ocupadosTutoria >= Reglas.CUPO_MAXIMO) continue;

            List<ChoqueResponsive> choques = AgendaService.choques(AgendaService.activos(tutoria), misHorarios)
                    .stream().map(mapping::toDTO).toList();
            if (ocultarChoques && !choques.isEmpty()) continue;

            resultado.add(new TutoriaExplorarResponsive(mapping.toResumen(tutoria, ocupadosTutoria, false), choques));
        }
        return resultado;
    }

    /** Detalle (pantallas 04, 06 y 11). panelTutorado solo para TUTORADO. */
    @Transactional(readOnly = true)
    public TutoriaDetalleResponsive detalle(int idTutoria, String matricula, String rol) {
        Tutoria tutoria = buscar(idTutoria);
        long ocupados = agenda.ocupados(idTutoria);

        ProximaSesionResponsive proxima = EstadosTutoria.ACTIVA.equals(tutoria.getEstado())
                ? agenda.proxima(AgendaService.activos(tutoria)).map(mapping::proxima).orElse(null)
                : null;

        PanelTutoradoResponsive panel = Roles.TUTORADO.equals(rol)
                ? panelTutorado(tutoria, ocupados, matricula)
                : null;

        return new TutoriaDetalleResponsive(
                mapping.toResumen(tutoria, ocupados, true),
                proxima,
                tutoria.getTutor().getMatricula().equals(matricula),
                panel);
    }

    private PanelTutoradoResponsive panelTutorado(Tutoria tutoria, long ocupados, String matricula) {
        int idTutoria = tutoria.getIdTutoria();
        Optional<Inscripcion> ultima = inscripcionRepository
                .findFirstByTutoria_IdTutoriaAndTutorado_MatriculaOrderByFechaInscripcionDesc(idTutoria, matricula);
        boolean bajaAutomatica = inscripcionRepository.findTutoriasConBajaAutomatica(matricula).contains(idTutoria);
        boolean inscrito = ultima.filter(i -> EstadosInscripcion.ACTIVA.equals(i.getEstado())).isPresent();
        boolean activa = EstadosTutoria.ACTIVA.equals(tutoria.getEstado());

        List<ChoqueResponsive> choques = List.of();
        if (activa && !inscrito) {
            List<Horario> misHorarios = horarioRepository.findVigentesDeTutorado(matricula).stream()
                    .filter(h -> h.getTutoria().getIdTutoria() != idTutoria)
                    .toList();
            choques = AgendaService.choques(AgendaService.activos(tutoria), misHorarios)
                    .stream().map(mapping::toDTO).toList();
        }

        String estado;
        if (bajaAutomatica) estado = PANEL_BAJA_AUTOMATICA;
        else if (!activa) estado = PANEL_NO_ACTIVA;
        else if (inscrito) estado = PANEL_INSCRIPCION_ACTIVA;
        else if (ocupados >= Reglas.CUPO_MAXIMO) estado = PANEL_LLENA;
        else if (!choques.isEmpty()) estado = PANEL_CHOQUE_HORARIO;
        else estado = PANEL_CON_LUGAR;

        InscripcionEstadoResponsive info = null;
        if ((PANEL_BAJA_AUTOMATICA.equals(estado) || PANEL_INSCRIPCION_ACTIVA.equals(estado)) && ultima.isPresent()) {
            Inscripcion inscripcion = ultima.get();
            info = new InscripcionEstadoResponsive(
                    inscripcion.getEstado(),
                    Reloj.enZona(inscripcion.getFechaInscripcion()),
                    Reloj.enZona(inscripcion.getFechaBaja()),
                    agenda.inasistencias(inscripcion.getIdInscripcion()),
                    Reglas.INASISTENCIAS_PERMITIDAS);
        }

        return new PanelTutoradoResponsive(estado, PANEL_CON_LUGAR.equals(estado), choques, info);
    }

    /** Tutorías del tutor autenticado (pantallas 09 y 14). */
    @Transactional(readOnly = true)
    public List<MiTutoriaResponsive> misTutorias(String matricula, String estado) {
        String filtro = estado == null || estado.isBlank() ? EstadosTutoria.ACTIVA : estado.trim().toUpperCase();
        List<Tutoria> tutorias = switch (filtro) {
            case "TODAS" -> tutoriaRepository.findByTutor_Matricula(matricula);
            case EstadosTutoria.ACTIVA, EstadosTutoria.INACTIVA ->
                    tutoriaRepository.findByTutor_MatriculaAndEstado(matricula, filtro);
            default -> throw new BusinessException("Estado inválido: usa ACTIVA, INACTIVA o TODAS.");
        };

        Map<Integer, Long> ocupados = agenda.ocupados(tutorias.stream().map(Tutoria::getIdTutoria).toList());

        return tutorias.stream()
                .sorted(Comparator.comparing((Tutoria t) -> Texto.normalizar(t.getMateria().getMateria())))
                .map(t -> {
                    ProximaSesionResponsive proxima = EstadosTutoria.ACTIVA.equals(t.getEstado())
                            ? agenda.proxima(AgendaService.activos(t)).map(mapping::proxima).orElse(null)
                            : null;
                    return new MiTutoriaResponsive(
                            mapping.toResumen(t, ocupados.getOrDefault(t.getIdTutoria(), 0L), false),
                            proxima,
                            tutoradosEnRiesgo(t.getIdTutoria()));
                })
                .toList();
    }

    /** Panel "Inscritos" (pantalla 11). */
    @Transactional(readOnly = true)
    public InscritosResponsive inscritos(int idTutoria, String matricula, String rol) {
        Tutoria tutoria = buscar(idTutoria);
        verificarDueno(tutoria, matricula, rol);

        List<Inscripcion> activas = inscripcionesActivas(idTutoria);
        Map<Integer, Long> faltas = agenda.inasistencias(activas.stream().map(Inscripcion::getIdInscripcion).toList());

        List<InscritoResponsive> inscritos = activas.stream().map(i -> {
            long inasistencias = faltas.getOrDefault(i.getIdInscripcion(), 0L);
            return new InscritoResponsive(
                    i.getTutorado().getMatricula(),
                    i.getTutorado().nombreCompleto(),
                    i.getTutorado().getCorreo(),
                    Reloj.enZona(i.getFechaInscripcion()),
                    inasistencias,
                    inasistencias == Reglas.INASISTENCIAS_PERMITIDAS);
        }).toList();

        return new InscritosResponsive(mapping.cupo(activas.size()), inscritos);
    }

    // ------------------------------------------------------------------
    // Alta y edición (RN-1, RN-2, RN-5)
    // ------------------------------------------------------------------

    @Transactional
    public TutoriaDetalleResponsive crear(String matricula, TutoriaRequest request) {
        Usuario tutor = usuarioService.buscar(matricula);
        Materia materia = materiaService.buscar(request.idMateria());
        catalogoService.validarAula(request.edificio(), request.aula());

        List<HorarioNuevo> horarios = normalizar(request.horarios());
        validarChoques(horarios, matricula, request.edificio(), request.aula(), null);

        Tutoria tutoria = new Tutoria();
        tutoria.setMateria(materia);
        tutoria.setTutor(tutor);
        tutoria.setEdificio(request.edificio());
        tutoria.setAula(request.aula());
        tutoria.setEstado(EstadosTutoria.ACTIVA);
        for (HorarioNuevo nuevo : horarios) {
            tutoria.getHorarios().add(nuevoHorario(tutoria, nuevo));
        }

        tutoriaRepository.save(tutoria);
        return detalle(tutoria.getIdTutoria(), matricula, Roles.TUTOR);
    }

    /** Reemplaza lugar y horarios; solo sin inscritos activos. */
    @Transactional
    public TutoriaDetalleResponsive actualizar(int idTutoria, String matricula, ActualizarTutoriaRequest request) {
        Tutoria tutoria = buscar(idTutoria);
        verificarDueno(tutoria, matricula, Roles.TUTOR);

        if (!EstadosTutoria.ACTIVA.equals(tutoria.getEstado())) {
            throw new BusinessException("La tutoría no está activa.");
        }
        if (agenda.ocupados(idTutoria) > 0) {
            throw new BusinessException("No se pueden cambiar horarios ni lugar con tutorados inscritos.");
        }
        if (sesionRepository.existeListaAbierta(idTutoria)) {
            throw new BusinessException("Cierra el pase de lista abierto antes de editar.");
        }

        catalogoService.validarAula(request.edificio(), request.aula());
        List<HorarioNuevo> horarios = normalizar(request.horarios());
        validarChoques(horarios, tutoria.getTutor().getMatricula(), request.edificio(), request.aula(), idTutoria);

        Map<Integer, Horario> actuales = new HashMap<>();
        AgendaService.activos(tutoria).forEach(h -> actuales.put(h.getIdHorario(), h));
        Set<Integer> conservados = new HashSet<>();

        for (HorarioNuevo nuevo : horarios) {
            if (nuevo.idHorario() != null) {
                Horario actual = actuales.get(nuevo.idHorario());
                if (actual == null || !conservados.add(nuevo.idHorario())) {
                    throw new BusinessException("El horario " + nuevo.idHorario()
                            + " no pertenece a esta tutoría, no está activo o viene repetido.");
                }
                boolean mismoBloque = actual.getDia().equals(nuevo.dia())
                        && actual.getHoraInicio().equals(nuevo.inicio())
                        && actual.getHoraFin().equals(nuevo.fin());
                if (mismoBloque) {
                    reemplazarTemas(actual, nuevo.temas());
                    continue;
                }
                // Cambió día u horas: el actual se desactiva y se crea uno nuevo (conserva historial).
                conservados.remove(nuevo.idHorario());
            }
            tutoria.getHorarios().add(nuevoHorario(tutoria, nuevo));
        }

        actuales.values().stream()
                .filter(h -> !conservados.contains(h.getIdHorario()))
                .forEach(h -> h.setEstado(EstadosHorario.INACTIVO));

        tutoria.setEdificio(request.edificio());
        tutoria.setAula(request.aula());
        tutoriaRepository.save(tutoria);
        return detalle(idTutoria, matricula, Roles.TUTOR);
    }

    // ------------------------------------------------------------------
    // Estado (RN-13)
    // ------------------------------------------------------------------

    @Transactional
    public void cambiarEstado(int idTutoria, String matricula, String rol, String estado) {
        if (!EstadosTutoria.INACTIVA.equals(estado.trim().toUpperCase())) {
            throw new BusinessException("Estado inválido.");
        }
        Tutoria tutoria = buscar(idTutoria);
        verificarDueno(tutoria, matricula, rol);

        if (EstadosTutoria.INACTIVA.equals(tutoria.getEstado())) {
            throw new BusinessException("La tutoría ya está inactiva.");
        }
        if (sesionRepository.existeListaAbierta(idTutoria)) {
            throw new BusinessException("Cierra el pase de lista abierto antes de desactivar.");
        }
        desactivar(tutoria);
    }

    /** Desactiva y avisa a los inscritos; sus inscripciones conservan el estado (S3). */
    public void desactivar(Tutoria tutoria) {
        tutoria.setEstado(EstadosTutoria.INACTIVA);
        tutoriaRepository.save(tutoria);
        notificaciones.tutoriaDesactivada(tutoria, inscripcionesActivas(tutoria.getIdTutoria()));
    }

    // ------------------------------------------------------------------
    // Utilidades compartidas
    // ------------------------------------------------------------------

    public Tutoria buscar(int idTutoria) {
        return tutoriaRepository.findById(idTutoria)
                .orElseThrow(() -> new BusinessException("Tutoría no encontrada."));
    }

    /** El ADMIN siempre pasa; un tutor solo sobre sus tutorías. */
    public void verificarDueno(Tutoria tutoria, String matricula, String rol) {
        if (Roles.ADMIN.equals(rol)) return;
        if (!tutoria.getTutor().getMatricula().equals(matricula)) {
            throw new BusinessException("No tienes permiso sobre este recurso.");
        }
    }

    public List<Inscripcion> inscripcionesActivas(int idTutoria) {
        return inscripcionRepository.findByTutoria_IdTutoriaAndEstadoOrderByTutorado_NombreAscTutorado_ApellidosAsc(
                idTutoria, EstadosInscripcion.ACTIVA);
    }

    /** Inscritos activos con exactamente INASISTENCIAS_PERMITIDAS faltas (RN-11). */
    public long tutoradosEnRiesgo(int idTutoria) {
        List<Integer> ids = inscripcionesActivas(idTutoria).stream().map(Inscripcion::getIdInscripcion).toList();
        return agenda.inasistencias(ids).values().stream()
                .filter(n -> n == Reglas.INASISTENCIAS_PERMITIDAS)
                .count();
    }

    private static boolean coincideBusqueda(Tutoria tutoria, String q) {
        if (q == null || q.isBlank()) return true;
        return Texto.contiene(tutoria.getMateria().getMateria(), q)
                || Texto.contiene(tutoria.getTutor().nombreCompleto(), q)
                || String.valueOf(tutoria.getMateria().getIdentificador()).contains(q.trim());
    }

    private List<HorarioNuevo> normalizar(List<HorarioRequest> requests) {
        List<HorarioNuevo> horarios = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            HorarioRequest r = requests.get(i);
            String dia = Dia.normalizar(r.dia());
            if (!r.horaInicio().isBefore(r.horaFin())) {
                throw new BusinessException("La hora de fin debe ser posterior a la de inicio.");
            }
            horarios.add(new HorarioNuevo(r.idHorario(), dia, r.horaInicio(), r.horaFin(),
                    normalizarTemas(r.temas()), i + 1));
        }

        for (int i = 0; i < horarios.size(); i++) {
            for (int j = i + 1; j < horarios.size(); j++) {
                HorarioNuevo a = horarios.get(i);
                HorarioNuevo b = horarios.get(j);
                if (a.dia().equals(b.dia()) && DateHelper.haySolapamiento(a.inicio(), a.fin(), b.inicio(), b.fin())) {
                    throw new BusinessException("Los horarios " + a.posicion() + " y " + b.posicion() + " se traslapan.");
                }
            }
        }
        return horarios;
    }

    /** RN-15: trim, sin vacíos, máx. 60 caracteres, máx. 10, sin repetir. */
    private static List<String> normalizarTemas(List<String> temas) {
        List<String> resultado = new ArrayList<>();
        Set<String> vistos = new HashSet<>();
        if (temas == null) return resultado;
        for (String tema : temas) {
            if (tema == null || tema.isBlank()) continue;
            String limpio = tema.trim();
            if (limpio.length() > Reglas.MAX_CARACTERES_TEMA) {
                throw new BusinessException("El tema no puede exceder " + Reglas.MAX_CARACTERES_TEMA + " caracteres.");
            }
            if (!vistos.add(limpio.toLowerCase())) {
                throw new BusinessException("El tema '" + limpio + "' está repetido en el mismo horario.");
            }
            resultado.add(limpio);
        }
        if (resultado.size() > Reglas.MAX_TEMAS_POR_HORARIO) {
            throw new BusinessException("Máximo " + Reglas.MAX_TEMAS_POR_HORARIO + " temas por horario.");
        }
        return resultado;
    }

    private void validarChoques(List<HorarioNuevo> horarios, String matriculaTutor,
                                int edificio, int aula, Integer idTutoriaPropia) {
        List<Horario> candidatos = horarios.stream().map(h -> {
            Horario horario = new Horario();
            horario.setDia(h.dia());
            horario.setHoraInicio(h.inicio());
            horario.setHoraFin(h.fin());
            return horario;
        }).toList();

        List<Horario> delTutor = horarioRepository.findVigentesDeTutor(matriculaTutor).stream()
                .filter(h -> !Objects.equals(h.getTutoria().getIdTutoria(), idTutoriaPropia))
                .toList();
        AgendaService.choques(candidatos, delTutor).stream().findFirst().ifPresent(c -> {
            throw new BusinessException("El horario del " + nombreDia(c.candidato()) + " "
                    + rango(c.candidato()) + " choca con tu tutoría de "
                    + c.existente().getTutoria().getMateria().getMateria() + ".");
        });

        List<Horario> enAula = horarioRepository.findVigentesEnAula(edificio, aula).stream()
                .filter(h -> !Objects.equals(h.getTutoria().getIdTutoria(), idTutoriaPropia))
                .toList();
        AgendaService.choques(candidatos, enAula).stream().findFirst().ifPresent(c -> {
            throw new BusinessException("El Edificio " + edificio + " · Aula " + aula + " está ocupado el "
                    + nombreDia(c.existente()) + " de " + c.existente().getHoraInicio().format(HORA)
                    + " a " + c.existente().getHoraFin().format(HORA) + ".");
        });
    }

    private static Horario nuevoHorario(Tutoria tutoria, HorarioNuevo nuevo) {
        Horario horario = new Horario();
        horario.setTutoria(tutoria);
        horario.setDia(nuevo.dia());
        horario.setHoraInicio(nuevo.inicio());
        horario.setHoraFin(nuevo.fin());
        horario.setEstado(EstadosHorario.ACTIVO);
        for (String tema : nuevo.temas()) {
            horario.getTemas().add(new Tema(tema, horario));
        }
        return horario;
    }

    /** Quita los temas que ya no vienen y agrega los nuevos (sin reinsertar los que se conservan). */
    private static void reemplazarTemas(Horario horario, List<String> temas) {
        Set<String> deseados = new HashSet<>();
        temas.forEach(t -> deseados.add(t.toLowerCase()));
        horario.getTemas().removeIf(t -> !deseados.contains(t.getTema().toLowerCase()));

        Set<String> existentes = new HashSet<>();
        horario.getTemas().forEach(t -> existentes.add(t.getTema().toLowerCase()));
        for (String tema : temas) {
            if (!existentes.contains(tema.toLowerCase())) horario.getTemas().add(new Tema(tema, horario));
        }
    }

    private static String nombreDia(Horario horario) {
        return Dia.de(horario.getDia()).nombre();
    }

    private static String rango(Horario horario) {
        return horario.getHoraInicio().format(HORA) + "–" + horario.getHoraFin().format(HORA);
    }
}
