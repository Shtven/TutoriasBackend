package com.codespace.tutorias.DTO.Mapping;

import com.codespace.tutorias.DTO.Request.RegisterRequest;
import com.codespace.tutorias.DTO.Responsive.TokenLogin;
import com.codespace.tutorias.JWT.JWTUtils;
import com.codespace.tutorias.Models.Rol;
import com.codespace.tutorias.Models.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserMapping {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTUtils jwtUtils;

    public Usuario toEntity(RegisterRequest request, Integer matricula, Rol rol){
        Usuario entity = new Usuario();
        entity.setMatricula(matricula);
        entity.setNombre(request.getNombre());
        entity.setApellidoP(request.getApellidoP());
        entity.setApellidoM(request.getApellidoM());
        entity.setCorreo(request.getCorreo());
        entity.setPwd(passwordEncoder.encode(request.getPwd()));
        entity.setRol(rol);

        return entity;
    }

    public Boolean matchesPassword(String pwd, String encodedPwd){
        return passwordEncoder.matches(pwd, encodedPwd);
    }

    public TokenLogin generateToken(Usuario usuario){
        TokenLogin tokenLogin = new TokenLogin();

        String token = jwtUtils.generateToken(
                usuario.getMatricula(),
                usuario.getRol().getRol()
        );

        tokenLogin.setToken(token);
        tokenLogin.setRol(usuario.getRol().getRol());

        return tokenLogin;
    }
}
