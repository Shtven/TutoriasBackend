package com.codespace.tutorias.Controllers;

import com.codespace.tutorias.DTO.Request.ComentarioRequest;
import com.codespace.tutorias.Exceptions.ApiResponse;
import com.codespace.tutorias.Services.ComentarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comentarios")
public class ComentarioController {

    @Autowired
    private ComentarioService comentarioService;

    @PostMapping
    @PreAuthorize("hasAnyRole('TUTORADO', 'ADMIN')")
    public ResponseEntity<?> crearComentario(
            @RequestAttribute("matricula") String matricula,
            @RequestBody ComentarioRequest request) {

        comentarioService.crearComentario(request, matricula);

        return ResponseEntity.ok(new ApiResponse<>(true, "Comentario registrado.", null));
    }

    @GetMapping("/tutoria/{idTutoria}")
    @PreAuthorize("hasAnyRole('TUTOR', 'TUTORADO', 'ADMIN')")
    public ResponseEntity<?> listarPorTutoria(@PathVariable int idTutoria) {

        return ResponseEntity.ok(new ApiResponse<>(
                true, "Comentarios de la tutoría", comentarioService.listarPorTutoria(idTutoria)));
    }

    @GetMapping("/mis-comentarios")
    @PreAuthorize("hasAnyRole('TUTORADO', 'ADMIN')")
    public ResponseEntity<?> misComentarios(@RequestAttribute("matricula") String matricula) {

        return ResponseEntity.ok(new ApiResponse<>(
                true, "Mis comentarios", comentarioService.listarPorTutorado(matricula)));
    }

    @DeleteMapping("/{idComentario}")
    @PreAuthorize("hasAnyRole('TUTORADO', 'ADMIN')")
    public ResponseEntity<?> eliminarComentario(
            @PathVariable int idComentario,
            @RequestAttribute("matricula") String matricula) {

        comentarioService.eliminarComentario(idComentario, matricula);

        return ResponseEntity.ok(new ApiResponse<>(true, "Comentario eliminado.", null));
    }
}
