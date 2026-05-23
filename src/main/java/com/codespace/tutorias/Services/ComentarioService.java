package com.codespace.tutorias.Services;

import com.codespace.tutorias.DTO.Mapping.ComentarioMapping;
import com.codespace.tutorias.DTO.Request.ComentarioRequest;
import com.codespace.tutorias.DTO.Responsive.ComentarioResponsive;
import com.codespace.tutorias.Exceptions.BusinessException;
import com.codespace.tutorias.Models.Comentario;
import com.codespace.tutorias.Models.Tutoria;
import com.codespace.tutorias.Models.Usuario;
import com.codespace.tutorias.Repositories.AsistenciaRepository;
import com.codespace.tutorias.Repositories.ComentarioRepository;
import com.codespace.tutorias.Repositories.TutoriaRepository;
import com.codespace.tutorias.Repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComentarioService {

    private static final int LIMITE_CARACTERES = 255;

    @Autowired
    private ComentarioRepository comentarioRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private TutoriaRepository tutoriaRepository;
    @Autowired
    private AsistenciaRepository asistenciaRepository;
    @Autowired
    private ComentarioMapping comentarioMapping;

    public void crearComentario(ComentarioRequest request, String matricula) {

        if (request.getComentario() == null || request.getComentario().trim().isEmpty()) {
            throw new BusinessException("El comentario no puede estar vacío.");
        }

        String texto = request.getComentario().trim();

        if (texto.length() > LIMITE_CARACTERES) {
            throw new BusinessException(
                    "El comentario supera el límite de " + LIMITE_CARACTERES + " caracteres.");
        }

        Usuario usuario = usuarioRepository.findById(matricula)
                .orElseThrow(() -> new BusinessException("El usuario no existe"));

        Tutoria tutoria = tutoriaRepository.findById(request.getIdTutoria())
                .orElseThrow(() -> new BusinessException("La tutoría no existe"));

        if (!"PROGRAMADA".equals(tutoria.getEstado())) {
            throw new BusinessException("Solo puedes comentar tutorías programadas.");
        }

        if (asistenciaRepository.findByMatriculaTutoria(matricula, request.getIdTutoria()).isEmpty()) {
            throw new BusinessException("Debes estar inscrito en la tutoría para comentar.");
        }

        comentarioRepository.save(comentarioMapping.toEntity(tutoria, usuario, texto));
    }

    public List<ComentarioResponsive> listarPorTutoria(int idTutoria) {

        if (!tutoriaRepository.existsById(idTutoria)) {
            throw new BusinessException("La tutoría no existe");
        }

        return comentarioRepository.findByTutoriaIdTutoria(idTutoria)
                .stream().map(comentarioMapping::toDTO).toList();
    }

    public List<ComentarioResponsive> listarPorTutorado(String matricula) {
        return comentarioRepository.findByUsuarioMatricula(matricula)
                .stream().map(comentarioMapping::toDTO).toList();
    }

    public void eliminarComentario(int idComentario, String matricula) {

        Comentario comentario = comentarioRepository.findById(idComentario)
                .orElseThrow(() -> new BusinessException("El comentario no existe"));

        if (!comentario.getUsuario().getMatricula().equals(matricula)) {
            throw new BusinessException("No puedes eliminar comentarios de otros usuarios.");
        }

        comentarioRepository.delete(comentario);
    }
}
