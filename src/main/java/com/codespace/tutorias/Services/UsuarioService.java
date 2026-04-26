package com.codespace.tutorias.Services;

import com.codespace.tutorias.DTO.Mapping.UserMapping;
import com.codespace.tutorias.DTO.Request.LoginRequest;
import com.codespace.tutorias.DTO.Request.RegisterRequest;
import com.codespace.tutorias.DTO.Responsive.TokenLogin;
import com.codespace.tutorias.JWT.JWTUtils;
import com.codespace.tutorias.Models.Rol;
import com.codespace.tutorias.Models.Usuario;
import com.codespace.tutorias.Repositories.RolRepository;
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
    @Autowired
    private RolRepository rolRepository;

    public void register(RegisterRequest register){
        Optional<Usuario> usuario = usuarioRepository.findById(register.getMatricula());
        Optional<Rol> rol = rolRepository.findById(register.getRol());

        if(usuario.isPresent()){
            throw new RuntimeException("La matricula ya esta registrada");
        }

        if(usuario.get().getCorreo().equals(register.getCorreo())){
            throw new RuntimeException("El correo ya esta registrado");
        }

        if(!rol.isPresent()){
            throw new RuntimeException("EL rol no existe");
        }

        usuarioRepository.save(userMapping.toEntity(register, rol.get()));
    }

    public TokenLogin login(LoginRequest login){
        Optional<Usuario> user = usuarioRepository.findById(login.getMatricula());

        if(user.isPresent()){
            if(userMapping.matchesPassword(login.getPwd(), user.get().getPwd())){
                userMapping.generateToken(user.get());
            }
        }
        return null;
    }



}
