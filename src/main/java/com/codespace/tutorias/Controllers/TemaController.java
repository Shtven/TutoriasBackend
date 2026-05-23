package com.codespace.tutorias.Controllers;

import com.codespace.tutorias.DTO.Request.TemaRequest;
import com.codespace.tutorias.Exceptions.ApiResponse;
import com.codespace.tutorias.Services.TemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/temas")
public class TemaController {

    @Autowired
    private TemaService temaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public ResponseEntity<?> crearTema(
            @RequestAttribute("matricula") String matricula,
            @RequestBody TemaRequest request) {

        temaService.crearTema(request, matricula);

        return ResponseEntity.ok(new ApiResponse<>(true, "Tema agregado.", null));
    }

    @GetMapping("/tutoria/{idTutoria}")
    @PreAuthorize("hasAnyRole('TUTOR', 'TUTORADO', 'ADMIN')")
    public ResponseEntity<?> listarPorTutoria(@PathVariable int idTutoria) {

        return ResponseEntity.ok(new ApiResponse<>(
                true, "Temas de la tutoría", temaService.listarPorTutoria(idTutoria)));
    }

    @DeleteMapping("/{idTema}")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public ResponseEntity<?> eliminarTema(
            @PathVariable int idTema,
            @RequestAttribute("matricula") String matricula) {

        temaService.eliminarTema(idTema, matricula);

        return ResponseEntity.ok(new ApiResponse<>(true, "Tema eliminado.", null));
    }
}
