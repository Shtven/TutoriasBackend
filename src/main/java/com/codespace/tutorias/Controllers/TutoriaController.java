package com.codespace.tutorias.Controllers;

import com.codespace.tutorias.DTO.Request.TutoriaRequest;
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

    @PostMapping("/")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<?> crearTutoria(@RequestBody TutoriaRequest tutoriaRequest) {
        tutoriaService.crearTutoria(tutoriaRequest);
        return ResponseEntity.ok(200);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<?> eliminarTutoria(@PathVariable int id) {
        tutoriaService.eliminarTutoria(id);
        return ResponseEntity.ok(200);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<?> actualizarTutoria(@PathVariable int id, @RequestBody TutoriaRequest tutoriaRequest) {
        tutoriaService.actualizarTutoria(id, tutoriaRequest);
        return ResponseEntity.ok(200);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<?> obtenerTutoria(@PathVariable int id) {
        return ResponseEntity.ok(tutoriaService.obtenerTutoriaPorId(id));
    }

    @GetMapping("/")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<?> listarTutorias(@RequestAttribute("matricula") String matricula) {
        return ResponseEntity.ok(tutoriaService.obtenerTutorias(matricula));
    }
}
