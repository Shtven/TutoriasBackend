package com.codespace.tutorias.Controllers;

import com.codespace.tutorias.DTO.Request.CierrePeriodoRequest;
import com.codespace.tutorias.DTO.Request.PreRegistroTutorRequest;
import com.codespace.tutorias.DTO.Responsive.CierrePeriodoResponsive;
import com.codespace.tutorias.Exceptions.ApiResponse;
import com.codespace.tutorias.Services.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/** Panel de administración (pantalla 15). */
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/resumen")
    public ResponseEntity<?> resumen(@RequestParam(required = false)
                                     @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Resumen", adminService.resumen(desde)));
    }

    @GetMapping("/tutorias")
    public ResponseEntity<?> tutorias(@RequestParam(required = false) String q,
                                      @RequestParam(required = false) String matriculaTutor,
                                      @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Tutorías", adminService.tutorias(q, matriculaTutor, estado)));
    }

    @GetMapping("/tutores")
    public ResponseEntity<?> tutores() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Tutores", adminService.tutores()));
    }

    /** Autoriza una matrícula como tutor (docs/AUTENTICACION.md § Alta de tutores). */
    @PostMapping("/tutores")
    public ResponseEntity<?> preRegistrarTutor(@Valid @RequestBody PreRegistroTutorRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Tutor autorizado.", adminService.preRegistrarTutor(request)));
    }

    @DeleteMapping("/tutores/{matricula}")
    public ResponseEntity<?> quitarPreRegistro(@PathVariable String matricula) {
        adminService.quitarPreRegistro(matricula);
        return ResponseEntity.ok(new ApiResponse<>(true, "Pre-registro eliminado.", null));
    }

    @PostMapping("/tutorias/cierre-periodo")
    public ResponseEntity<?> cierrePeriodo(@Valid @RequestBody CierrePeriodoRequest request) {
        long desactivadas = adminService.cierrePeriodo(request.confirmar());
        return ResponseEntity.ok(new ApiResponse<>(true, "Periodo cerrado.", new CierrePeriodoResponsive(desactivadas)));
    }
}
