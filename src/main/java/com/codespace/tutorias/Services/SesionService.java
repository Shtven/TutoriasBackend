package com.codespace.tutorias.Services;

import com.codespace.tutorias.DTO.Mapping.TutoriaMapping;
import com.codespace.tutorias.DTO.Request.MarcaRequest;
import com.codespace.tutorias.DTO.Responsive.*;
import com.codespace.tutorias.Exceptions.BusinessException;
import com.codespace.tutorias.Helpers.*;
import com.codespace.tutorias.Models.*;
import com.codespace.tutorias.Repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Sesiones y pase de lista (RN-6 a RN-11) e historial de asistencia.
 * Una sesión solo se guarda al abrir la lista o al cancelar.
 */
@Service
public class SesionService {

    @Autowired
    private SesionRepository sesionRepository;

    @Autowired
    private HorarioRepository horarioRepository;

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private RecordatorioRepository recordatorioRepository;

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

    // ------------------------------------------------------------------
    // Consultas del tutor
    // ------------------------------------------------------------------

    /** Pantalla 09: sesiones de hoy + listas de días anteriores sin cerrar. */
    @Transactional(readOnly = true)
    public SesionesHoyResponsive hoy(String matricula) {
        LocalDate hoy = reloj.hoy();
        Dia dia = Dia.de(hoy);

        List<SesionResponsive> sesiones = List.of();
        if (dia != null) {
            List<Horario> deHoy = horarioRepository.findVigentesDeTutor(matricula).stream()
                    .filter(h -> h.getDia().equals(dia.name()))
                    .toList();
            sesiones = agenda.ocurrencias(deHoy, hoy, hoy).stream().map(this::toDTO).toList();
        }

        List<SesionResponsive> pendientes = sesionRepository.findListasAbiertasDeTutor(matricula).stream()
                .filter(s -> s.getFecha().isBefore(hoy))
                .map(s -> toDTO(new AgendaService.Ocurrencia(s.getHorario(), s.getFecha(), s)))
                .toList();

        return new SesionesHoyResponsive(hoy, sesiones, pendientes);
    }

    /** Pantalla 11: sesiones registradas + virtuales de los próximos `dias`. */
    @Transactional(readOnly = true)
    public List<SesionResponsive> deTutoria(int idTutoria, String matricula, String rol, int dias) {
        if (dias < 0 || dias > 28) {
            throw new BusinessException("El parámetro dias debe estar entre 0 y 28.");
        }
        Tutoria tutoria = tutoriaService.buscar(idTutoria);
        tutoriaService.verificarDueno(tutoria, matricula, rol);

        List<AgendaService.Ocurrencia> ocurrencias = new ArrayList<>();
        Set<String> registradas = new HashSet<>();
        for (Sesion sesion : sesionRepository.findByHorario_Tutoria_IdTutoriaOrderByFechaAscHoraInicioAsc(idTutoria)) {
            ocurrencias.add(new AgendaService.Ocurrencia(sesion.getHorario(), sesion.getFecha(), sesion));
            registradas.add(sesion.getHorario().getIdHorario() + "|" + sesion.getFecha());
        }

        if (dias > 0 && EstadosTutoria.ACTIVA.equals(tutoria.getEstado())) {
            LocalDate hoy = reloj.hoy();
            agenda.ocurrencias(AgendaService.activos(tutoria), hoy, hoy.plusDays(dias - 1)).stream()
                    .filter(o -> o.sesion() == null)
                    .filter(o -> !registradas.contains(o.horario().getIdHorario() + "|" + o.fecha()))
                    .forEach(ocurrencias::add);
        }

        ocurrencias.sort(Comparator.comparing(AgendaService.Ocurrencia::fecha)
                .thenComparing(o -> o.horario().getHoraInicio())
                .reversed());
        return ocurrencias.stream().map(this::toDTO).toList();
    }

    // ------------------------------------------------------------------
    // Abrir / cancelar (RN-7, RN-8)
    // ------------------------------------------------------------------

    @Transactional
    public ListaResponsive abrirLista(String matricula, int idHorario, LocalDate fecha) {
        Horario horario = horarioDelTutor(idHorario, matricula);
        validarFechaDelHorario(horario, fecha);

        if (!fecha.equals(reloj.hoy())) {
            throw new BusinessException("Solo puedes pasar lista el día de la sesión.");
        }
        if (!reloj.yaLlego(fecha, horario.getHoraInicio())) {
            throw new BusinessException("La sesión aún no inicia.");
        }

        Optional<Sesion> existente = sesionRepository.findByHorario_IdHorarioAndFecha(idHorario, fecha);
        if (existente.isPresent()) {
            Sesion sesion = existente.get();
            switch (sesion.estado()) {
                case EstadosSesion.CANCELADA -> throw new BusinessException("La sesión fue cancelada.");
                case EstadosSesion.COMPLETADA -> throw new BusinessException("El pase de lista ya se cerró.");
                default -> {
                    return armarLista(sesion);
                }
            }
        }

        Sesion sesion = nuevaSesion(horario, fecha);
        sesion.setFechaListaAbierta(reloj.marca());
        return armarLista(sesionRepository.save(sesion));
    }

    @Transactional
    public SesionResponsive cancelar(String matricula, int idHorario, LocalDate fecha) {
        Horario horario = horarioDelTutor(idHorario, matricula);
        validarFechaDelHorario(horario, fecha);

        Optional<Sesion> existente = sesionRepository.findByHorario_IdHorarioAndFecha(idHorario, fecha);
        if (existente.isPresent()) {
            throw new BusinessException(EstadosSesion.CANCELADA.equals(existente.get().estado())
                    ? "La sesión ya está cancelada."
                    : "No se puede cancelar una sesión con pase de lista.");
        }
        ZonedDateTime limite = reloj.en(fecha, horario.getHoraInicio())
                .minusMinutes(Reglas.MINUTOS_LIMITE_CANCELACION);
        if (reloj.ahora().isAfter(limite)) {
            throw new BusinessException("Ya no puedes cancelar: faltan menos de "
                    + Reglas.MINUTOS_LIMITE_CANCELACION + " minutos para la sesión.");
        }

        Sesion sesion = nuevaSesion(horario, fecha);
        sesion.setFechaCancelada(reloj.marca());
        sesionRepository.save(sesion);

        notificaciones.sesionCancelada(sesion, tutoriaService.inscripcionesActivas(horario.getTutoria().getIdTutoria()));
        return toDTO(new AgendaService.Ocurrencia(horario, fecha, sesion));
    }

    // ------------------------------------------------------------------
    // Pase de lista (RN-9, RN-10, RN-11)
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public ListaResponsive lista(int idSesion, String matricula, String rol) {
        Sesion sesion = buscar(idSesion);
        tutoriaService.verificarDueno(sesion.getHorario().getTutoria(), matricula, rol);
        return armarLista(sesion);
    }

    @Transactional
    public ListaResponsive marcar(int idSesion, String matricula, List<MarcaRequest> marcas) {
        Sesion sesion = buscar(idSesion);
        Tutoria tutoria = sesion.getHorario().getTutoria();
        tutoriaService.verificarDueno(tutoria, matricula, Roles.TUTOR);
        if (!EstadosSesion.LISTA_ABIERTA.equals(sesion.estado())) {
            throw new BusinessException("El pase de lista no está abierto.");
        }

        Map<Integer, Asistencia> porInscripcion = asistenciasPorInscripcion(sesion);
        Map<String, Inscripcion> marcables = new HashMap<>();
        porInscripcion.values().forEach(a -> marcables.put(a.getInscripcion().getTutorado().getMatricula(), a.getInscripcion()));
        tutoriaService.inscripcionesActivas(tutoria.getIdTutoria())
                .forEach(i -> marcables.put(i.getTutorado().getMatricula(), i));

        for (MarcaRequest marca : marcas) {
            Inscripcion inscripcion = marcables.get(marca.matricula().trim().toUpperCase());
            if (inscripcion == null) {
                throw new BusinessException(marca.matricula() + " no está inscrito en esta tutoría.");
            }
            Asistencia asistencia = porInscripcion.get(inscripcion.getIdInscripcion());
            if (asistencia == null) {
                asistencia = new Asistencia(sesion, inscripcion, marca.asistio());
                porInscripcion.put(inscripcion.getIdInscripcion(), asistencia);
            } else {
                asistencia.setAsistio(marca.asistio());
            }
            asistenciaRepository.save(asistencia);
        }
        return armarLista(sesion);
    }

    @Transactional
    public CierreResultadoResponsive cerrarLista(int idSesion, String matricula) {
        Sesion sesion = buscar(idSesion);
        Tutoria tutoria = sesion.getHorario().getTutoria();
        tutoriaService.verificarDueno(tutoria, matricula, Roles.TUTOR);

        if (!EstadosSesion.LISTA_ABIERTA.equals(sesion.estado())) {
            throw new BusinessException("El pase de lista no está abierto.");
        }
        if (!reloj.yaLlego(sesion.getFecha(), sesion.getHoraInicio())) {
            throw new BusinessException("La sesión aún no inicia.");
        }
        Map<Integer, Asistencia> marcas = asistenciasPorInscripcion(sesion);
        List<Inscripcion> activas = tutoriaService.inscripcionesActivas(tutoria.getIdTutoria());
        long faltantes = activas.stream().filter(i -> !marcas.containsKey(i.getIdInscripcion())).count();
        if (faltantes > 0) {
            throw new BusinessException("Faltan " + faltantes + (faltantes == 1 ? " tutorado" : " tutorados") + " por marcar.");
        }

        sesion.setFechaListaCerrada(reloj.marca());
        sesionRepository.saveAndFlush(sesion);

        List<BajaResponsive> bajas = new ArrayList<>();
        for (Inscripcion inscripcion : activas) {
            long inasistencias = agenda.inasistencias(inscripcion.getIdInscripcion());
            if (inasistencias > Reglas.INASISTENCIAS_PERMITIDAS) {
                inscripcion.setEstado(EstadosInscripcion.BAJA_AUTOMATICA);
                inscripcion.setFechaBaja(reloj.marca());
                inscripcionRepository.save(inscripcion);
                notificaciones.bajaAutomatica(inscripcion, inasistencias);
                bajas.add(new BajaResponsive(inscripcion.getTutorado().getMatricula(),
                        inscripcion.getTutorado().nombreCompleto(), inasistencias));
            }
        }

        long asistieron = marcas.values().stream().filter(Asistencia::isAsistio).count();
        return new CierreResultadoResponsive(sesion.getIdSesion(), sesion.estado(),
                asistieron, marcas.size() - asistieron, bajas);
    }

    // ------------------------------------------------------------------
    // Historial (pantalla 14)
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public HistorialAsistenciaResponsive historial(int idTutoria, String matricula, String rol) {
        Tutoria tutoria = tutoriaService.buscar(idTutoria);
        tutoriaService.verificarDueno(tutoria, matricula, rol);

        List<Sesion> sesiones = sesionRepository.findByHorario_Tutoria_IdTutoriaOrderByFechaAscHoraInicioAsc(idTutoria);
        List<Asistencia> asistencias = asistenciaRepository.findBySesion_Horario_Tutoria_IdTutoria(idTutoria);
        List<Inscripcion> inscripciones = inscripcionRepository.findByTutoria_IdTutoria(idTutoria);

        // Por tutorado: su inscripción más reciente y sus marcas de todas sus inscripciones.
        Map<String, Inscripcion> vigente = new HashMap<>();
        for (Inscripcion inscripcion : inscripciones) {
            vigente.merge(inscripcion.getTutorado().getMatricula(), inscripcion,
                    (a, b) -> a.getFechaInscripcion().isAfter(b.getFechaInscripcion()) ? a : b);
        }
        Map<String, Map<Integer, Boolean>> marcasPorTutorado = new HashMap<>();
        for (Asistencia a : asistencias) {
            marcasPorTutorado.computeIfAbsent(a.getInscripcion().getTutorado().getMatricula(), m -> new HashMap<>())
                    .put(a.getSesion().getIdSesion(), a.isAsistio());
        }

        Set<Integer> completadas = new HashSet<>();
        long canceladas = 0;
        for (Sesion s : sesiones) {
            if (EstadosSesion.COMPLETADA.equals(s.estado())) completadas.add(s.getIdSesion());
            if (EstadosSesion.CANCELADA.equals(s.estado())) canceladas++;
        }

        long marcasAsistio = 0;
        long marcasTotales = 0;
        List<FilaHistorialResponsive> filas = new ArrayList<>();
        List<Inscripcion> ordenadas = new ArrayList<>(vigente.values());
        ordenadas.sort(Comparator.comparing((Inscripcion i) -> Texto.normalizar(i.getTutorado().nombreCompleto())));

        for (Inscripcion inscripcion : ordenadas) {
            Usuario tutorado = inscripcion.getTutorado();
            Map<Integer, Boolean> propias = marcasPorTutorado.getOrDefault(tutorado.getMatricula(), Map.of());
            Map<Integer, Boolean> marcas = new LinkedHashMap<>();
            long asistio = 0;
            long falto = 0;
            Integer idSesionBaja = null;
            for (Sesion s : sesiones) {
                Boolean marca = propias.get(s.getIdSesion());
                marcas.put(s.getIdSesion(), marca);
                if (marca != null && completadas.contains(s.getIdSesion())) {
                    if (marca) asistio++;
                    else {
                        falto++;
                        idSesionBaja = s.getIdSesion();
                    }
                }
            }
            marcasAsistio += asistio;
            marcasTotales += asistio + falto;
            boolean baja = EstadosInscripcion.BAJA_AUTOMATICA.equals(inscripcion.getEstado());
            filas.add(new FilaHistorialResponsive(tutorado.getMatricula(), tutorado.nombreCompleto(),
                    inscripcion.getEstado(), Reloj.enZona(inscripcion.getFechaBaja()), marcas,
                    baja ? idSesionBaja : null, asistio, falto));
        }

        long bajasAutomaticas = inscripciones.stream()
                .filter(i -> EstadosInscripcion.BAJA_AUTOMATICA.equals(i.getEstado())).count();
        Integer pct = marcasTotales == 0 ? null : (int) Math.round(marcasAsistio * 100.0 / marcasTotales);

        HistorialTutoriaResponsive datos = new HistorialTutoriaResponsive(
                tutoria.getIdTutoria(), mapping.toDTO(tutoria.getMateria()), tutoria.getEdificio(), tutoria.getAula(),
                AgendaService.activos(tutoria).stream().map(h -> mapping.toDTO(h, false)).toList(),
                mapping.cupo(agenda.ocupados(idTutoria)));

        return new HistorialAsistenciaResponsive(
                datos,
                new ResumenHistorialResponsive(completadas.size(), canceladas, marcasAsistio, marcasTotales, pct, bajasAutomaticas),
                sesiones.stream().map(s -> new SesionColumnaResponsive(
                        s.getIdSesion(), s.getFecha(), s.getHorario().getDia(), s.estado())).toList(),
                filas);
    }

    // ------------------------------------------------------------------
    // Procesos automáticos (RN-17, RN-9)
    // ------------------------------------------------------------------

    /** Recordatorio 2 horas antes, una vez por inscripción, horario y fecha. */
    @Transactional
    public int procesarRecordatorios() {
        LocalDate hoy = reloj.hoy();
        Dia dia = Dia.de(hoy);
        if (dia == null) return 0;

        ZonedDateTime ahora = reloj.ahora();
        int enviados = 0;
        for (Horario horario : horarioRepository.findVigentesPorDia(dia.name())) {
            ZonedDateTime inicio = reloj.en(hoy, horario.getHoraInicio());
            if (ahora.isBefore(inicio.minusHours(Reglas.HORAS_RECORDATORIO)) || !ahora.isBefore(inicio)) continue;

            Optional<Sesion> sesion = sesionRepository.findByHorario_IdHorarioAndFecha(horario.getIdHorario(), hoy);
            if (sesion.isPresent() && sesion.get().getFechaCancelada() != null) continue;

            for (Inscripcion inscripcion : tutoriaService.inscripcionesActivas(horario.getTutoria().getIdTutoria())) {
                if (recordatorioRepository.existsByInscripcion_IdInscripcionAndHorario_IdHorarioAndFecha(
                        inscripcion.getIdInscripcion(), horario.getIdHorario(), hoy)) continue;
                recordatorioRepository.save(new Recordatorio(inscripcion, horario, hoy, reloj.marca()));
                notificaciones.recordatorio(inscripcion, horario, hoy);
                enviados++;
            }
        }
        return enviados;
    }

    /** Aviso único al tutor cuando termina una sesión con la lista aún abierta. */
    @Transactional
    public int procesarAvisosDeCierre() {
        int enviados = 0;
        for (Sesion sesion : sesionRepository.findListasAbiertasSinAviso()) {
            if (!reloj.yaLlego(sesion.getFecha(), sesion.getHoraFin())) continue;
            sesion.setFechaAvisoCierre(reloj.marca());
            sesionRepository.save(sesion);
            notificaciones.listaSinCerrar(sesion);
            enviados++;
        }
        return enviados;
    }

    // ------------------------------------------------------------------
    // Utilidades
    // ------------------------------------------------------------------

    private Sesion buscar(int idSesion) {
        return sesionRepository.findById(idSesion)
                .orElseThrow(() -> new BusinessException("Sesión no encontrada."));
    }

    private Horario horarioDelTutor(int idHorario, String matricula) {
        Horario horario = horarioRepository.findById(idHorario)
                .orElseThrow(() -> new BusinessException("Horario no encontrado."));
        tutoriaService.verificarDueno(horario.getTutoria(), matricula, Roles.TUTOR);
        if (!EstadosTutoria.ACTIVA.equals(horario.getTutoria().getEstado())
                || !EstadosHorario.ACTIVO.equals(horario.getEstado())) {
            throw new BusinessException("El horario no está activo.");
        }
        return horario;
    }

    private static void validarFechaDelHorario(Horario horario, LocalDate fecha) {
        if (!Dia.de(horario.getDia()).coincide(fecha)) {
            throw new BusinessException("La fecha no corresponde al día del horario.");
        }
    }

    private static Sesion nuevaSesion(Horario horario, LocalDate fecha) {
        Sesion sesion = new Sesion();
        sesion.setHorario(horario);
        sesion.setFecha(fecha);
        sesion.setHoraInicio(horario.getHoraInicio());
        sesion.setHoraFin(horario.getHoraFin());
        return sesion;
    }

    private Map<Integer, Asistencia> asistenciasPorInscripcion(Sesion sesion) {
        Map<Integer, Asistencia> mapa = new HashMap<>();
        for (Asistencia a : asistenciaRepository.findBySesion_IdSesion(sesion.getIdSesion())) {
            mapa.put(a.getInscripcion().getIdInscripcion(), a);
        }
        return mapa;
    }

    /** Pantallas 12 y 13: tutorados con su marca, resumen y condiciones de cierre. */
    private ListaResponsive armarLista(Sesion sesion) {
        Tutoria tutoria = sesion.getHorario().getTutoria();
        Map<Integer, Asistencia> marcas = asistenciasPorInscripcion(sesion);
        List<Inscripcion> activas = tutoriaService.inscripcionesActivas(tutoria.getIdTutoria());

        Map<Integer, Inscripcion> enLista = new LinkedHashMap<>();
        activas.forEach(i -> enLista.put(i.getIdInscripcion(), i));
        marcas.values().forEach(a -> enLista.putIfAbsent(a.getInscripcion().getIdInscripcion(), a.getInscripcion()));

        boolean abierta = EstadosSesion.LISTA_ABIERTA.equals(sesion.estado());
        boolean completada = EstadosSesion.COMPLETADA.equals(sesion.estado());
        Map<Integer, Long> faltas = agenda.inasistencias(enLista.keySet());

        List<TutoradoListaResponsive> tutorados = new ArrayList<>();
        List<BajaResponsive> bajasPrevistas = new ArrayList<>();
        for (Inscripcion inscripcion : enLista.values()) {
            Asistencia marca = marcas.get(inscripcion.getIdInscripcion());
            Boolean asistio = marca == null ? null : marca.isAsistio();
            long previas = faltas.getOrDefault(inscripcion.getIdInscripcion(), 0L);
            if (completada && Boolean.FALSE.equals(asistio)) previas--; // ya cuenta esta sesión
            boolean causaraBaja = abierta && Boolean.FALSE.equals(asistio)
                    && EstadosInscripcion.ACTIVA.equals(inscripcion.getEstado())
                    && previas + 1 > Reglas.INASISTENCIAS_PERMITIDAS;
            Usuario tutorado = inscripcion.getTutorado();
            tutorados.add(new TutoradoListaResponsive(tutorado.getMatricula(), tutorado.nombreCompleto(),
                    previas, asistio, causaraBaja));
            if (causaraBaja) {
                bajasPrevistas.add(new BajaResponsive(tutorado.getMatricula(), tutorado.nombreCompleto(), previas + 1));
            }
        }
        tutorados.sort(Comparator.comparing(t -> Texto.normalizar(t.nombreCompleto())));

        long marcados = tutorados.stream().filter(t -> t.asistio() != null).count();
        long asistieron = tutorados.stream().filter(t -> Boolean.TRUE.equals(t.asistio())).count();
        boolean todosMarcados = activas.stream().allMatch(i -> marcas.containsKey(i.getIdInscripcion()));
        boolean iniciada = reloj.yaLlego(sesion.getFecha(), sesion.getHoraInicio());
        boolean noCancelada = sesion.getFechaCancelada() == null;

        return new ListaResponsive(
                toDTO(new AgendaService.Ocurrencia(sesion.getHorario(), sesion.getFecha(), sesion)),
                tutorados,
                new ResumenListaResponsive(tutorados.size(), marcados, asistieron, marcados - asistieron,
                        tutorados.size() - marcados),
                new CierreListaResponsive(todosMarcados, iniciada, noCancelada,
                        abierta && todosMarcados && iniciada && noCancelada, bajasPrevistas));
    }

    /** Objeto Sesion de la API (docs/API.md § Objetos comunes). */
    public SesionResponsive toDTO(AgendaService.Ocurrencia ocurrencia) {
        Horario horario = ocurrencia.horario();
        Tutoria tutoria = horario.getTutoria();
        Sesion sesion = ocurrencia.sesion();
        String estado = ocurrencia.estado();
        LocalDate fecha = ocurrencia.fecha();

        var inicio = sesion != null ? sesion.getHoraInicio() : horario.getHoraInicio();
        var fin = sesion != null ? sesion.getHoraFin() : horario.getHoraFin();

        long marcados = 0;
        long asistieron = 0;
        boolean todosMarcados = false;
        long ocupados = agenda.ocupados(tutoria.getIdTutoria());
        if (sesion != null) {
            Map<Integer, Asistencia> marcas = asistenciasPorInscripcion(sesion);
            marcados = marcas.size();
            asistieron = marcas.values().stream().filter(Asistencia::isAsistio).count();
            todosMarcados = tutoriaService.inscripcionesActivas(tutoria.getIdTutoria()).stream()
                    .allMatch(i -> marcas.containsKey(i.getIdInscripcion()));
        }
        long inscritos = EstadosSesion.COMPLETADA.equals(estado) ? marcados : ocupados;

        boolean vigente = EstadosTutoria.ACTIVA.equals(tutoria.getEstado())
                && EstadosHorario.ACTIVO.equals(horario.getEstado());
        boolean iniciada = reloj.yaLlego(fecha, inicio);
        boolean programada = EstadosSesion.PROGRAMADA.equals(estado);

        AccionesSesionResponsive acciones = new AccionesSesionResponsive(
                vigente && programada && fecha.equals(reloj.hoy()) && iniciada,
                EstadosSesion.LISTA_ABIERTA.equals(estado) && iniciada && todosMarcados,
                vigente && programada && !reloj.ahora().isAfter(
                        reloj.en(fecha, inicio).minusMinutes(Reglas.MINUTOS_LIMITE_CANCELACION)));

        return new SesionResponsive(
                sesion == null ? null : sesion.getIdSesion(),
                horario.getIdHorario(),
                tutoria.getIdTutoria(),
                mapping.toDTO(tutoria.getMateria()),
                fecha,
                horario.getDia(),
                inicio,
                fin,
                tutoria.getEdificio(),
                tutoria.getAula(),
                estado,
                inscritos,
                marcados,
                asistieron,
                inicio.minusHours(Reglas.HORAS_RECORDATORIO),
                reloj.yaLlego(fecha, fin),
                acciones);
    }
}
