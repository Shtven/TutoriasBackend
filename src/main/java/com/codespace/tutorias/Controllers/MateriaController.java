package com.codespace.tutorias.Controllers;

import com.codespace.tutorias.DTO.Request.MateriaRequest;
import com.codespace.tutorias.DTO.Responsive.MateriaCatalogoResponsive;
import com.codespace.tutorias.Exceptions.ApiResponse;
import com.codespace.tutorias.Services.MateriaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Experiencias Educativas (pantalla 16). */
@RestController
@RequestMapping("/materias")
public class MateriaController {

    @Autowired
    private MateriaService materiaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<MateriaCatalogoResponsive>>> listar(@RequestParam(required = false) String q) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Experiencias educativas", materiaService.listar(q)));
    }

    @GetMapping("/{idMateria}")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<MateriaCatalogoResponsive>> obtener(@PathVariable int idMateria) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Experiencia educativa", materiaService.obtener(idMateria)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MateriaCatalogoResponsive>> crear(@Valid @RequestBody MateriaRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Experiencia educativa creada.", materiaService.crear(request)));
    }

    @PutMapping("/{idMateria}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MateriaCatalogoResponsive>> actualizar(@PathVariable int idMateria, @Valid @RequestBody MateriaRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Experiencia educativa actualizada.",
                materiaService.actualizar(idMateria, request)));
    }

    @DeleteMapping("/{idMateria}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable int idMateria) {
        materiaService.eliminar(idMateria);
        return ResponseEntity.ok(new ApiResponse<>(true, "Experiencia educativa eliminada.", null));
    }
}
