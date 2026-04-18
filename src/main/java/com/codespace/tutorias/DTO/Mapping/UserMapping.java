package com.codespace.tutorias.DTO.Mapping;

import com.codespace.tutorias.DTO.Request.RegisterRequest;
import com.codespace.tutorias.Models.User;
import com.codespace.tutorias.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserMapping {

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User toEntity(RegisterRequest request){
        User entity = new User();
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
}
