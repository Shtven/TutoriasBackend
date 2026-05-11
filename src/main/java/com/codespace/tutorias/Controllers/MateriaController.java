package com.codespace.tutorias.Controllers;

import com.codespace.tutorias.DTO.Request.MateriaRequest;
import com.codespace.tutorias.Exceptions.ApiResponse;
import com.codespace.tutorias.Models.Materia;
import com.codespace.tutorias.Services.MateriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/materia")
public class MateriaController {

    @Autowired
    private MateriaService materiaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> crearMateria(@RequestBody MateriaRequest materia) {
        materiaService.createMateria(materia);
        return ResponseEntity.ok(new ApiResponse<>(true, "Materia creada.", null));
    }

    @GetMapping("/{nrc}")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public ResponseEntity<?> obtenerMateria(@PathVariable int nrc) {
        Materia materia = materiaService.getMateria(nrc);
        return ResponseEntity.ok(new ApiResponse<>(true, "Materia", materia));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public ResponseEntity<?> listarMaterias() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Materias", materiaService.getMaterias()));
    }

    @DeleteMapping("/{nrc}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> eliminarMateria(@PathVariable int nrc) {
        materiaService.deleteMateria(nrc);
        return ResponseEntity.ok(new ApiResponse<>(true, "Materia eliminada.", null));
    }

    @PutMapping("/{nrc}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> actualizarMateria(@PathVariable int nrc, @RequestBody MateriaRequest materia) {
        materiaService.updateMateria(nrc, materia);
        return ResponseEntity.ok(new ApiResponse<>(true, "Materia actualizada.", null));
    }
}
