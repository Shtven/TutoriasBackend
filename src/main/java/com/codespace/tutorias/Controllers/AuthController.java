package com.codespace.tutorias.Controllers;

import com.codespace.tutorias.DTO.Request.LoginRequest;
import com.codespace.tutorias.DTO.Request.RegisterRequest;
import com.codespace.tutorias.Services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    UsuarioService usuarioService;

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody RegisterRequest register){
        usuarioService.register(register);
        return ResponseEntity.ok(200);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody LoginRequest login){
        return ResponseEntity.ok(usuarioService.login(login));
    }


}
