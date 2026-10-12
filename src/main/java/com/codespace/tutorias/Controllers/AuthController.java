package com.codespace.tutorias.Controllers;

import com.codespace.tutorias.DTO.Request.IdTokenRequest;
import com.codespace.tutorias.DTO.Request.LoginManualRequest;
import com.codespace.tutorias.DTO.Request.RegistroManualRequest;
import com.codespace.tutorias.DTO.Request.RegistroRequest;
import com.codespace.tutorias.DTO.Responsive.LoginResponsive;
import com.codespace.tutorias.Exceptions.ApiResponse;
import com.codespace.tutorias.Services.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/microsoft")
    public ResponseEntity<?> loginMicrosoft(@Valid @RequestBody IdTokenRequest request) {
        return respuestaLogin(authService.loginMicrosoft(request.idToken()));
    }

    @PostMapping("/google")
    public ResponseEntity<?> loginGoogle(@Valid @RequestBody IdTokenRequest request) {
        return respuestaLogin(authService.loginGoogle(request.idToken()));
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registro(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Registro exitoso.", authService.registrar(request)));
    }

    /** Registro con datos a mano y contraseña (alternativa al proveedor). */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody RegistroManualRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Registro exitoso.", authService.registrarManual(request)));
    }

    /** Login con matrícula o correo y contraseña. */
    @PostMapping("/signin")
    public ResponseEntity<?> signin(@Valid @RequestBody LoginManualRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Login", authService.loginManual(request)));
    }

    private ResponseEntity<?> respuestaLogin(LoginResponsive login) {
        String mensaje = login.registroPendiente() ? "Registro pendiente" : "Login";
        return ResponseEntity.ok(new ApiResponse<>(true, mensaje, login));
    }
}
