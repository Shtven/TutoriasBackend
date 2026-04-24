package com.codespace.tutorias.DTO.Mapping;

import com.codespace.tutorias.DTO.Request.RegisterRequest;
import com.codespace.tutorias.DTO.Responsive.JWT;
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

    public Usuario toEntity(RegisterRequest request){
        Usuario entity = new Usuario();
        entity.setMatricula(request.getMatricula());
        entity.setNombre(request.getNombre());
        entity.setApellidoP(request.getApellidoP());
        entity.setApellidoM(request.getApellidoM());
        entity.setCorreo(request.getCorreo());
        entity.setPwd(passwordEncoder.encode(request.getPwd()));
        entity.setRol(request.getRol());

        return entity;
    }

    public Boolean matchesPassword(String pwd, String encodedPwd){
        return passwordEncoder.matches(pwd, encodedPwd);
    }

    public JWT generateToken(Usuario usuario){
        Optional<Rol> rolEntity = rolRepository.findById(usuario.getRol());
        JWT jwt = new JWT();
        if(rolEntity.isEmpty()){
            return null;
        }
        String token = jwtUtils.generateToken(usuario.getMatricula(), rolEntity.get().getRol());

        jwt.setToken(token);
        jwt.setRol(rolEntity.get().getRol());

        return jwt;
    }
}
