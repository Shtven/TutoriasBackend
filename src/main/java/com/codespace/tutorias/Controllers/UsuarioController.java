package com.codespace.tutorias.Controllers;

import com.codespace.tutorias.DTO.Responsive.UsuarioResponsive;
import com.codespace.tutorias.Exceptions.ApiResponse;
import com.codespace.tutorias.Services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UsuarioResponsive>> me(@RequestAttribute("matricula") String matricula) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Usuario", usuarioService.obtenerActual(matricula)));
    }
}
