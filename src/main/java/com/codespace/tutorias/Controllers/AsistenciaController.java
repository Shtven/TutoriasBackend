package com.codespace.tutorias.Controllers;

import com.codespace.tutorias.DTO.Request.AsistenciaRequest;
import com.codespace.tutorias.Exceptions.ApiResponse;
import com.codespace.tutorias.Services.AsistenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/asistencia")
public class AsistenciaController {

    @Autowired
    private AsistenciaService asistenciaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('TUTORADO', 'ADMIN')")
    public ResponseEntity<?> crearAsistencia(@RequestAttribute("matricula") Integer matricula, @RequestBody AsistenciaRequest request) {

        asistenciaService.crearAsistencia(request, matricula);

        return ResponseEntity.ok(new ApiResponse<>(true, "Asistencia marcada.", null));
    }

    @GetMapping("/tutoria/{idTutoria}")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public ResponseEntity<?> listarPorTutoria(@PathVariable int idTutoria) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Asistencias", asistenciaService.listarAsistencias(idTutoria)));
    }

    @GetMapping("/{idAsistencia}")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public ResponseEntity<?> listarUna(@PathVariable int idAsistencia) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Asistencia", asistenciaService.listarAsistencia(idAsistencia)));
    }

    @PatchMapping("/{idAsistencia}")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public ResponseEntity<?> actualizarAsistencia(@PathVariable int idAsistencia, @RequestParam boolean asistio) {

        asistenciaService.actualizarAsistencia(asistio, idAsistencia);

        return ResponseEntity.ok(new ApiResponse<>(true, "Asistencia actualizada.", null));
    }

    @GetMapping("/mis-inscripciones")
    @PreAuthorize("hasAnyRole('TUTORADO', 'ADMIN')")
    public ResponseEntity<?> misInscripciones(@RequestAttribute("matricula") Integer matricula) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Mis inscripciones", asistenciaService.listarPorTutorado(matricula)));
    }

    @GetMapping("/historial")
    @PreAuthorize("hasAnyRole('TUTORADO', 'ADMIN')")
    public ResponseEntity<?> historial(@RequestAttribute("matricula") Integer matricula) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Historial de tutorías", asistenciaService.listarHistorial(matricula)));
    }

    @DeleteMapping("/{idAsistencia}")
    @PreAuthorize("hasAnyRole('TUTORADO', 'ADMIN')")
    public ResponseEntity<?> eliminarAsistencia(
            @PathVariable int idAsistencia,
            @RequestAttribute("matricula") Integer matricula) {

        asistenciaService.eliminarAsistencia(idAsistencia, matricula);

        return ResponseEntity.ok(new ApiResponse<>(true, "Asistencia eliminada.", null));
    }
}
