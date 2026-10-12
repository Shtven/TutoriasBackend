package com.codespace.tutorias.Controllers;

import com.codespace.tutorias.DTO.Request.ActualizarTutoriaRequest;
import com.codespace.tutorias.DTO.Request.EstadoTutoriaRequest;
import com.codespace.tutorias.DTO.Request.TutoriaRequest;
import com.codespace.tutorias.Exceptions.ApiResponse;
import com.codespace.tutorias.Services.TutoriaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tutorias")
public class TutoriaController {

    @Autowired
    private TutoriaService tutoriaService;

    @GetMapping
    @PreAuthorize("hasRole('TUTORADO')")
    public ResponseEntity<?> explorar(@RequestAttribute("matricula") String matricula,
                                      @RequestParam(required = false) String q,
                                      @RequestParam(required = false) String dia,
                                      @RequestParam(defaultValue = "false") boolean soloConLugar,
                                      @RequestParam(defaultValue = "false") boolean ocultarChoques) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Tutorías disponibles",
                tutoriaService.explorar(matricula, q, dia, soloConLugar, ocultarChoques)));
    }

    @GetMapping("/{idTutoria:\\d+}")
    @PreAuthorize("hasAnyRole('TUTORADO', 'TUTOR', 'ADMIN')")
    public ResponseEntity<?> detalle(@PathVariable int idTutoria,
                                     @RequestAttribute("matricula") String matricula,
                                     @RequestAttribute("rol") String rol) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Tutoría", tutoriaService.detalle(idTutoria, matricula, rol)));
    }

    @GetMapping("/mis-tutorias")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<?> misTutorias(@RequestAttribute("matricula") String matricula,
                                         @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Mis tutorías", tutoriaService.misTutorias(matricula, estado)));
    }

    @PostMapping
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<?> crear(@Valid @RequestBody TutoriaRequest request,
                                   @RequestAttribute("matricula") String matricula) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Tutoría creada.", tutoriaService.crear(matricula, request)));
    }

    @PutMapping("/{idTutoria}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<?> actualizar(@PathVariable int idTutoria,
                                        @Valid @RequestBody ActualizarTutoriaRequest request,
                                        @RequestAttribute("matricula") String matricula) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Tutoría actualizada.",
                tutoriaService.actualizar(idTutoria, matricula, request)));
    }

    @PatchMapping("/{idTutoria}/estado")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public ResponseEntity<?> cambiarEstado(@PathVariable int idTutoria,
                                           @Valid @RequestBody EstadoTutoriaRequest request,
                                           @RequestAttribute("matricula") String matricula,
                                           @RequestAttribute("rol") String rol) {
        tutoriaService.cambiarEstado(idTutoria, matricula, rol, request.estado());
        return ResponseEntity.ok(new ApiResponse<>(true, "Tutoría desactivada.", null));
    }

    @GetMapping("/{idTutoria}/inscritos")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public ResponseEntity<?> inscritos(@PathVariable int idTutoria,
                                       @RequestAttribute("matricula") String matricula,
                                       @RequestAttribute("rol") String rol) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Inscritos", tutoriaService.inscritos(idTutoria, matricula, rol)));
    }
}
