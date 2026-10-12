package com.codespace.tutorias.Controllers;

import com.codespace.tutorias.Exceptions.ApiResponse;
import com.codespace.tutorias.Services.CatalogoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/catalogos")
public class CatalogoController {

    @Autowired
    private CatalogoService catalogoService;

    @GetMapping("/edificios")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public ResponseEntity<?> edificios() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Edificios", catalogoService.edificios()));
    }
}
