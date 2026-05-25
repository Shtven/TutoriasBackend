package com.codespace.tutorias.Controllers;

import com.codespace.tutorias.DTO.Request.CalificacionRequest;
import com.codespace.tutorias.Exceptions.ApiResponse;
import com.codespace.tutorias.Services.CalificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/calificaciones")
public class CalificacionController {

    @Autowired
    private CalificacionService calificacionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('TUTORADO', 'ADMIN')")
    public ResponseEntity<?> calificar(
            @RequestAttribute("matricula") String matricula,
            @RequestBody CalificacionRequest request) {

        calificacionService.calificarTutoria(request, matricula);

        return ResponseEntity.ok(new ApiResponse<>(true, "Calificación registrada.", null));
    }

    @GetMapping("/promedio/{matricula}")
    @PreAuthorize("hasAnyRole('TUTOR', 'TUTORADO', 'ADMIN')")
    public ResponseEntity<?> promedioPorMatricula(@PathVariable String matricula) {

        return ResponseEntity.ok(new ApiResponse<>(
                true, "Promedio del tutor", calificacionService.obtenerPromedioTutor(matricula)));
    }

    @GetMapping("/promedio")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public ResponseEntity<?> miPromedio(@RequestAttribute("matricula") String matricula) {

        return ResponseEntity.ok(new ApiResponse<>(
                true, "Mi promedio", calificacionService.obtenerPromedioTutor(matricula)));
    }
}
