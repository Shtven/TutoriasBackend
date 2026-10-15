package com.codespace.tutorias.Controllers;

import com.codespace.tutorias.DTO.Request.ComentarioRequest;
import com.codespace.tutorias.DTO.Responsive.ComentarioResponsive;
import com.codespace.tutorias.Exceptions.ApiResponse;
import com.codespace.tutorias.Services.ComentarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ComentarioController {

    @Autowired
    private ComentarioService comentarioService;

    @GetMapping("/tutorias/{idTutoria}/comentarios")
    @PreAuthorize("hasAnyRole('TUTORADO', 'TUTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<ComentarioResponsive>>> listar(@PathVariable int idTutoria,
                                                                          @RequestAttribute("matricula") String matricula) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Comentarios", comentarioService.listar(idTutoria, matricula)));
    }

    @PostMapping("/tutorias/{idTutoria}/comentarios")
    @PreAuthorize("hasRole('TUTORADO')")
    public ResponseEntity<ApiResponse<ComentarioResponsive>> publicar(@PathVariable int idTutoria,
                                                                      @Valid @RequestBody ComentarioRequest request,
                                                                      @RequestAttribute("matricula") String matricula) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Comentario publicado.",
                comentarioService.publicar(idTutoria, matricula, request.comentario())));
    }

    @DeleteMapping("/comentarios/{idComentario}")
    @PreAuthorize("hasAnyRole('TUTORADO', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable int idComentario,
                                                      @RequestAttribute("matricula") String matricula,
                                                      @RequestAttribute("rol") String rol) {
        comentarioService.eliminar(idComentario, matricula, rol);
        return ResponseEntity.ok(new ApiResponse<>(true, "Comentario eliminado.", null));
    }
}
