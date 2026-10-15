package com.codespace.tutorias.Controllers;

import com.codespace.tutorias.DTO.Request.DevLoginRequest;
import com.codespace.tutorias.DTO.Responsive.LoginResponsive;
import com.codespace.tutorias.Exceptions.ApiResponse;
import com.codespace.tutorias.Services.AuthService;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * SOLO PRUEBAS. Login por correo sin contraseña, para probar la API
 * con usuarios de datos mock. La ruta no existe si auth.dev.habilitado=false.
 */
@RestController
@RequestMapping("/auth")
@ConditionalOnProperty(name = "auth.dev.habilitado", havingValue = "true")
public class DevAuthController {

    private static final Logger log = LoggerFactory.getLogger(DevAuthController.class);

    @Autowired
    private AuthService authService;

    @PostConstruct
    void advertir() {
        log.warn("auth.dev.habilitado=true: POST /auth/dev permite entrar sin contraseña. No usar en producción.");
    }

    @PostMapping("/dev")
    public ResponseEntity<ApiResponse<LoginResponsive>> loginDesarrollo(@Valid @RequestBody DevLoginRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Login", authService.loginDesarrollo(request.correo())));
    }
}
