package com.codespace.tutorias.Services;

import com.codespace.tutorias.DTO.Responsive.ComentarioResponsive;
import com.codespace.tutorias.Exceptions.BusinessException;
import com.codespace.tutorias.Helpers.EstadosInscripcion;
import com.codespace.tutorias.Helpers.EstadosTutoria;
import com.codespace.tutorias.Helpers.Reloj;
import com.codespace.tutorias.Helpers.Roles;
import com.codespace.tutorias.Models.Comentario;
import com.codespace.tutorias.Models.Tutoria;
import com.codespace.tutorias.Repositories.ComentarioRepository;
import com.codespace.tutorias.Repositories.InscripcionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Comentarios y sugerencias por tutoría (RN-16). */
@Service
public class ComentarioService {

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private TutoriaService tutoriaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private Reloj reloj;

    @Transactional(readOnly = true)
    public List<ComentarioResponsive> listar(int idTutoria, String matricula) {
        tutoriaService.buscar(idTutoria);
        return comentarioRepository.findByTutoria_IdTutoriaOrderByFechaComentarioDescIdComentarioDesc(idTutoria)
                .stream().map(c -> toDTO(c, matricula)).toList();
    }

    @Transactional
    public ComentarioResponsive publicar(int idTutoria, String matricula, String texto) {
        Tutoria tutoria = tutoriaService.buscar(idTutoria);
        if (!EstadosTutoria.ACTIVA.equals(tutoria.getEstado())) {
            throw new BusinessException("La tutoría no está activa.");
        }
        if (!inscripcionRepository.existsByTutoria_IdTutoriaAndTutorado_MatriculaAndEstado(
                idTutoria, matricula, EstadosInscripcion.ACTIVA)) {
            throw new BusinessException("Inscríbete para dejar comentarios.");
        }

        Comentario comentario = new Comentario();
        comentario.setTutoria(tutoria);
        comentario.setTutorado(usuarioService.buscar(matricula));
        comentario.setComentario(texto.trim());
        comentario.setFechaComentario(reloj.marca());
        return toDTO(comentarioRepository.save(comentario), matricula);
    }

    @Transactional
    public void eliminar(int idComentario, String matricula, String rol) {
        Comentario comentario = comentarioRepository.findById(idComentario)
                .orElseThrow(() -> new BusinessException("Comentario no encontrado."));
        if (!Roles.ADMIN.equals(rol) && !comentario.getTutorado().getMatricula().equals(matricula)) {
            throw new BusinessException("No tienes permiso sobre este recurso.");
        }
        comentarioRepository.delete(comentario);
    }

    private static ComentarioResponsive toDTO(Comentario c, String matricula) {
        return new ComentarioResponsive(
                c.getIdComentario(),
                c.getTutorado().getMatricula(),
                c.getTutorado().nombreCompleto(),
                c.getComentario(),
                Reloj.enZona(c.getFechaComentario()),
                c.getTutorado().getMatricula().equals(matricula));
    }
}
