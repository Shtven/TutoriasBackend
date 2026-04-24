package com.codespace.tutorias.Services;

import com.codespace.tutorias.DTO.Mapping.UserMapping;
import com.codespace.tutorias.DTO.Request.LoginRequest;
import com.codespace.tutorias.DTO.Request.RegisterRequest;
import com.codespace.tutorias.DTO.Responsive.JWT;
import com.codespace.tutorias.JWT.JWTUtils;
import com.codespace.tutorias.Models.Usuario;
import com.codespace.tutorias.Repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private UserMapping userMapping;
    @Autowired
    private JWTUtils jwtUtils;


    public void register(RegisterRequest register){

        if(usuarioRepository.findById(register.getMatricula()) != null){
            throw new RuntimeException("La matricula ya esta registrada");
        }

        if(usuarioRepository.findByCorreo(register.getCorreo()) != null){
            throw new RuntimeException("El correo ya esta registrado");
        }

        usuarioRepository.save(userMapping.toEntity(register));
    }

    public JWT login(LoginRequest login){
        Optional<Usuario> user = usuarioRepository.findById(login.getMatricula());

        if(user.isPresent()){
            if(userMapping.matchesPassword(login.getPwd(), user.get().getPwd())){
                userMapping.generateToken(user.get());
            }
        }
        return null;
    }



}
