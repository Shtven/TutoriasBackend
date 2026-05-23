package com.codespace.tutorias.Controllers;

import com.codespace.tutorias.DTO.Request.ActualizarTutoriaRequest;
import com.codespace.tutorias.DTO.Request.TutoriaRequest;
import com.codespace.tutorias.Exceptions.ApiResponse;
import com.codespace.tutorias.Services.TutoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tutoria")
public class TutoriaController {

    @Autowired
    private TutoriaService tutoriaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public ResponseEntity<?> crearTutoria(@RequestBody TutoriaRequest tutoriaRequest) {
        tutoriaService.crearTutoria(tutoriaRequest);
        return ResponseEntity.ok(new ApiResponse<>(true, "Tutoria creada.", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public ResponseEntity<?> eliminarTutoria(
            @PathVariable int id,
            @RequestAttribute("matricula") String matricula) {
        tutoriaService.eliminarTutoria(id, matricula);
        return ResponseEntity.ok(new ApiResponse<>(true, "Tutoria cancelada.", null));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public ResponseEntity<?> actualizarTutoria(
            @PathVariable int id,
            @RequestBody ActualizarTutoriaRequest tutoriaRequest,
            @RequestAttribute("matricula") String matricula) {
        tutoriaService.actualizarTutoria(id, tutoriaRequest, matricula);
        return ResponseEntity.ok(new ApiResponse<>(true, "Tutoria actualizada.", null));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public ResponseEntity<?> obtenerTutoria(@PathVariable int id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Tutoria", tutoriaService.obtenerTutoriaPorId(id)));
    }

    @GetMapping("/mis-tutorias")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public ResponseEntity<?> listarTutorias(@RequestAttribute("matricula") String matricula) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Mis tutorias", tutoriaService.obtenerTutorias(matricula)));
    }

    @GetMapping("/disponibles")
    @PreAuthorize("hasAnyRole('TUTORADO', 'ADMIN')")
    public ResponseEntity<?> listarTutoriasTutorado() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Tutorias", tutoriaService.obtenerTutoriasTutorado()));
    }

    @PutMapping("/completar/{id}")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public ResponseEntity<?> completarTutoria(
            @PathVariable int id,
            @RequestAttribute("matricula") String matricula) {
        tutoriaService.completarTutoria(id, matricula);
        return ResponseEntity.ok(new ApiResponse<>(true, "Tutoria completa.", null));
    }
}
