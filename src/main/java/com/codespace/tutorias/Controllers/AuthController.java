package com.codespace.tutorias.Controllers;

import com.codespace.tutorias.DTO.Request.LoginRequest;
import com.codespace.tutorias.DTO.Request.RegisterRequest;
import com.codespace.tutorias.Services.UserService;
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
    UserService userService;

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody RegisterRequest register){
        userService.register(register);
        return ResponseEntity.ok(200);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody LoginRequest login){
        return ResponseEntity.ok(userService.login(login));
    }
}
