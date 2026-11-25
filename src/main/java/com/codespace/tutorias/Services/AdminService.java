package com.codespace.tutorias.Services;

import com.codespace.tutorias.DTO.Mapping.TutoriaMapping;
import com.codespace.tutorias.DTO.Mapping.UsuarioMapping;
import com.codespace.tutorias.DTO.Request.PreRegistroTutorRequest;
import com.codespace.tutorias.DTO.Responsive.AdminResumenResponsive;
import com.codespace.tutorias.DTO.Responsive.AdminTutoriaResponsive;
import com.codespace.tutorias.DTO.Responsive.UsuarioResponsive;
import com.codespace.tutorias.DTO.Responsive.UsuarioResumenResponsive;
import com.codespace.tutorias.Exceptions.BusinessException;
import com.codespace.tutorias.Exceptions.ValidacionException;
import com.codespace.tutorias.Helpers.*;
import com.codespace.tutorias.Models.Tutoria;
import com.codespace.tutorias.Models.Usuario;
import com.codespace.tutorias.Repositories.AsistenciaRepository;
import com.codespace.tutorias.Repositories.InscripcionRepository;
import com.codespace.tutorias.Repositories.RolRepository;
import com.codespace.tutorias.Repositories.SesionRepository;
import com.codespace.tutorias.Repositories.TutoriaRepository;
import com.codespace.tutorias.Repositories.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/** Panel de administración (pantalla 15). */
@Service
public class AdminService {

    @Autowired
    private TutoriaRepository tutoriaRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private SesionRepository sesionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TutoriaService tutoriaService;

    @Autowired
    private AgendaService agenda;

    @Autowired
    private TutoriaMapping mapping;

    @Autowired
    private UsuarioMapping usuarioMapping;

    @Autowired
    private RolRepository rolRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public AdminResumenResponsive resumen(LocalDate desde) {
        long tutorias = tutoriaRepository.countByEstado(EstadosTutoria.ACTIVA);
        long inscripciones = inscripcionRepository.contarActivasEnTutoriasActivas();
        long lugares = tutorias * Reglas.CUPO_MAXIMO;
        long bajas = desde == null
                ? inscripcionRepository.countByEstado(EstadosInscripcion.BAJA_AUTOMATICA)
                : inscripcionRepository.countByEstadoAndFechaBajaGreaterThanEqual(
                        EstadosInscripcion.BAJA_AUTOMATICA, desde.atStartOfDay(Reloj.ZONA).toOffsetDateTime());
        return new AdminResumenResponsive(tutorias, inscripciones, lugares - inscripciones, lugares, bajas);
    }

    @Transactional(readOnly = true)
    public List<AdminTutoriaResponsive> tutorias(String q, String matriculaTutor, String estado) {
        String filtro = estado == null || estado.isBlank() ? EstadosTutoria.ACTIVA : estado.trim().toUpperCase();
        List<Tutoria> tutorias = switch (filtro) {
            case "TODAS" -> tutoriaRepository.findAll();
            case EstadosTutoria.ACTIVA, EstadosTutoria.INACTIVA -> tutoriaRepository.findByEstado(filtro);
            default -> throw new BusinessException("Estado inválido: usa ACTIVA, INACTIVA o TODAS.");
        };

        List<Tutoria> filtradas = tutorias.stream()
                .filter(t -> matriculaTutor == null || matriculaTutor.isBlank()
                        || t.getTutor().getMatricula().equalsIgnoreCase(matriculaTutor.trim()))
                .filter(t -> q == null || q.isBlank()
                        || Texto.contiene(t.getMateria().getMateria(), q)
                        || Texto.contiene(t.getTutor().nombreCompleto(), q)
                        || String.valueOf(t.getMateria().getIdentificador()).contains(q.trim()))
                .sorted(Comparator.comparing((Tutoria t) -> Texto.normalizar(t.getMateria().getMateria())))
                .toList();

        List<Integer> ids = filtradas.stream().map(Tutoria::getIdTutoria).toList();
        Map<Integer, Long> ocupados = agenda.ocupados(ids);
        Map<Integer, Integer> porcentajes = new HashMap<>();
        if (!ids.isEmpty()) {
            for (Object[] fila : asistenciaRepository.contarMarcasPorTutoria(ids)) {
                long asistio = ((Number) fila[1]).longValue();
                long total = ((Number) fila[2]).longValue();
                if (total > 0) porcentajes.put((Integer) fila[0], (int) Math.round(asistio * 100.0 / total));
            }
        }

        return filtradas.stream()
                .map(t -> new AdminTutoriaResponsive(
                        mapping.toResumen(t, ocupados.getOrDefault(t.getIdTutoria(), 0L), false),
                        porcentajes.get(t.getIdTutoria())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioResumenResponsive> tutores() {
        return usuarioRepository.findByRol_RolOrderByNombreAscApellidosAsc(Roles.TUTOR).stream()
                .map(usuarioMapping::toResumen)
                .toList();
    }

    /**
     * Lista de tutores autorizados: deja la matrícula como TUTOR sin
     * contraseña. La persona la reclama con POST /auth/signup (rol TUTOR y
     * el mismo correo).
     */
    @Transactional
    public UsuarioResponsive preRegistrarTutor(PreRegistroTutorRequest request) {
        String matricula = MatriculaHelper.normalizar(request.matricula());
        String correo = request.correo().trim().toLowerCase();
        if (usuarioRepository.existsById(matricula)) {
            throw new ValidacionException("matricula", "La matrícula ya está registrada.");
        }
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ValidacionException("correo", "El correo ya está registrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setMatricula(matricula);
        usuario.setNombre(request.nombre().trim());
        usuario.setApellidos(request.apellidos().trim());
        usuario.setCorreo(correo);
        usuario.setRol(rolRepository.findByRol(Roles.TUTOR)
                .orElseThrow(() -> new BusinessException("Rol inválido.")));
        entityManager.persist(usuario);
        return usuarioMapping.toDTO(usuario);
    }

    /** Solo un pre-registro que todavía no crea su cuenta (p. ej. para corregir un error). */
    @Transactional
    public void quitarPreRegistro(String matricula) {
        Usuario usuario = usuarioRepository.findById(MatriculaHelper.paraBusqueda(matricula))
                .filter(u -> Roles.TUTOR.equals(u.getRol().getRol()) && u.getProveedor() == null)
                .orElseThrow(() -> new BusinessException(
                        "Solo se puede quitar un tutor pre-registrado que aún no crea su cuenta."));
        usuarioRepository.delete(usuario);
    }

    /** Desactiva todas las tutorías activas; se rechaza completo si alguna tiene lista abierta. */
    @Transactional
    public long cierrePeriodo(boolean confirmar) {
        if (!confirmar) {
            throw new BusinessException("Confirma el cierre del periodo.");
        }
        List<Tutoria> activas = tutoriaRepository.findByEstado(EstadosTutoria.ACTIVA);
        List<String> conLista = activas.stream()
                .filter(t -> sesionRepository.existeListaAbierta(t.getIdTutoria()))
                .map(t -> t.getMateria().getMateria())
                .toList();
        if (!conLista.isEmpty()) {
            throw new BusinessException("No se puede cerrar el periodo: hay pases de lista abiertos en "
                    + String.join(", ", conLista) + ".");
        }
        activas.forEach(tutoriaService::desactivar);
        return activas.size();
    }
}
