package com.codespace.tutorias.Controllers;

import com.codespace.tutorias.DTO.Request.SigninRequest;
import com.codespace.tutorias.DTO.Request.SignupRequest;
import com.codespace.tutorias.DTO.Responsive.LoginResponsive;
import com.codespace.tutorias.Exceptions.ApiResponse;
import com.codespace.tutorias.Services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /** IP real detrás del proxy de Render: server.forward-headers-strategy=native. */
    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<LoginResponsive>> signin(@Valid @RequestBody SigninRequest request,
                                                               HttpServletRequest http) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Login", authService.signin(request, http.getRemoteAddr())));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<LoginResponsive>> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Cuenta creada.", authService.signup(request)));
    }
}
