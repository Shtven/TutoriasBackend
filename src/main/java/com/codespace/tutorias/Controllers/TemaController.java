package com.codespace.tutorias.Controllers;

import com.codespace.tutorias.DTO.Request.TemaRequest;
import com.codespace.tutorias.DTO.Responsive.TemaResponsive;
import com.codespace.tutorias.Exceptions.ApiResponse;
import com.codespace.tutorias.Services.TemaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class TemaController {

    @Autowired
    private TemaService temaService;

    @PostMapping("/horarios/{idHorario}/temas")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<ApiResponse<TemaResponsive>> agregar(@PathVariable int idHorario,
                                                               @Valid @RequestBody TemaRequest request,
                                                               @RequestAttribute("matricula") String matricula) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Tema agregado.",
                temaService.agregar(idHorario, matricula, request.tema())));
    }

    @DeleteMapping("/temas/{idTema}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable int idTema,
                                                      @RequestAttribute("matricula") String matricula) {
        temaService.eliminar(idTema, matricula);
        return ResponseEntity.ok(new ApiResponse<>(true, "Tema eliminado.", null));
    }
}
