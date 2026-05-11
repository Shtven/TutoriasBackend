package com.codespace.tutorias.Controllers;

import com.codespace.tutorias.DTO.Request.LoginRequest;
import com.codespace.tutorias.DTO.Request.RegisterRequest;
import com.codespace.tutorias.Exceptions.ApiResponse;
import com.codespace.tutorias.Services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody RegisterRequest register){
        usuarioService.register(register);
        return ResponseEntity.ok(new ApiResponse<>(true, "Registro exitoso.", null));
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody LoginRequest login){
        return ResponseEntity.ok(new ApiResponse<>(true, "Login", usuarioService.login(login)));
    }
}
