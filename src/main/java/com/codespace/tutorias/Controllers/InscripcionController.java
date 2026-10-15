package com.codespace.tutorias.Controllers;

import com.codespace.tutorias.DTO.Request.InscripcionRequest;
import com.codespace.tutorias.DTO.Responsive.InscripcionCreadaResponsive;
import com.codespace.tutorias.DTO.Responsive.MiInscripcionResponsive;
import com.codespace.tutorias.DTO.Responsive.MisInscripcionesResponsive;
import com.codespace.tutorias.DTO.Responsive.SemanaItemResponsive;
import com.codespace.tutorias.Exceptions.ApiResponse;
import com.codespace.tutorias.Services.InscripcionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inscripciones")
@PreAuthorize("hasRole('TUTORADO')")
public class InscripcionController {

    @Autowired
    private InscripcionService inscripcionService;

    @PostMapping
    public ResponseEntity<ApiResponse<InscripcionCreadaResponsive>> inscribir(@Valid @RequestBody InscripcionRequest request,
                                                                              @RequestAttribute("matricula") String matricula) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Inscripción confirmada.",
                inscripcionService.inscribir(matricula, request.idTutoria())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<MisInscripcionesResponsive>> misInscripciones(@RequestAttribute("matricula") String matricula) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Mis inscripciones",
                inscripcionService.misInscripciones(matricula)));
    }

    @GetMapping("/tutorias/{idTutoria}")
    public ResponseEntity<ApiResponse<MiInscripcionResponsive>> miInscripcion(@PathVariable int idTutoria,
                                                                              @RequestAttribute("matricula") String matricula) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Mi inscripción",
                inscripcionService.miInscripcion(matricula, idTutoria)));
    }

    @DeleteMapping("/tutorias/{idTutoria}")
    public ResponseEntity<ApiResponse<Void>> darseDeBaja(@PathVariable int idTutoria,
                                                         @RequestAttribute("matricula") String matricula) {
        inscripcionService.darseDeBaja(matricula, idTutoria);
        return ResponseEntity.ok(new ApiResponse<>(true, "Te diste de baja de la tutoría.", null));
    }

    @GetMapping("/semana")
    public ResponseEntity<ApiResponse<List<SemanaItemResponsive>>> semana(@RequestAttribute("matricula") String matricula) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Mi semana", inscripcionService.semana(matricula)));
    }
}
