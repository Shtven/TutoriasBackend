package com.codespace.tutorias.Controllers;

import com.codespace.tutorias.DTO.Request.AsistenciaRequest;
import com.codespace.tutorias.Services.AsistenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/asistencias")
public class AsistenciaController {

    @Autowired
    private AsistenciaService asistenciaService;

    @PostMapping
    @PreAuthorize("hasRole('TUTORADO')")
    public ResponseEntity<?> crearAsistencia(@RequestAttribute("matricula") String matricula, @RequestBody AsistenciaRequest request) {

        asistenciaService.crearAsistencia(request, matricula);

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('TUTOR')")
    @GetMapping("/tutoria/{idTutoria}")
    public ResponseEntity<?> listarPorTutoria(@PathVariable int idTutoria) {

        return ResponseEntity.ok(asistenciaService.listarAsistencias(idTutoria));
    }

    @PreAuthorize("hasRole('TUTOR')")
    @GetMapping("/{idAsistencia}")
    public ResponseEntity<?> listarUna(@PathVariable int idAsistencia) {
        return ResponseEntity.ok(asistenciaService.listarAsistencia(idAsistencia));
    }

    @PatchMapping("/{idAsistencia}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<?> actualizarAsistencia(@PathVariable int idAsistencia, @RequestParam boolean asistio) {

        asistenciaService.actualizarAsistencia(asistio, idAsistencia);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{idAsistencia}")
    @PreAuthorize("hasRole('TUTORADO')")
    public ResponseEntity<?> eliminarAsistencia(
            @PathVariable int idAsistencia,
            @RequestAttribute("matricula") String matricula) {

        asistenciaService.eliminarAsistencia(idAsistencia, matricula);

        return ResponseEntity.ok().build();
    }
}
