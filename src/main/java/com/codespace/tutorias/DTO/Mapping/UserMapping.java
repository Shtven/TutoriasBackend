package com.codespace.tutorias.DTO.Mapping;

import com.codespace.tutorias.DTO.Request.RegisterRequest;
import com.codespace.tutorias.DTO.Responsive.TokenLogin;
import com.codespace.tutorias.JWT.JWTUtils;
import com.codespace.tutorias.Models.Rol;
import com.codespace.tutorias.Models.Usuario;
import com.codespace.tutorias.Repositories.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserMapping {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private RolRepository rolRepository;

    public Usuario toEntity(RegisterRequest request, Rol rol){
        Usuario entity = new Usuario();
        entity.setMatricula(request.getMatricula());
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
        Optional<Rol> rolEntity = rolRepository.findById(usuario.getRol().getIdRol());
        TokenLogin tokenLogin = new TokenLogin();
        if(rolEntity.isEmpty()){
            return null;
        }
        String token = jwtUtils.generateToken(usuario.getMatricula(), rolEntity.get().getRol());

        tokenLogin.setToken(token);
        tokenLogin.setRol(rolEntity.get().getRol());

        return tokenLogin;
    }
}
