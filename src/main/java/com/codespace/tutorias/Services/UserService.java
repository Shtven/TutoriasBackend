package com.codespace.tutorias.Services;

import com.codespace.tutorias.DTO.Mapping.UserMapping;
import com.codespace.tutorias.DTO.Request.LoginRequest;
import com.codespace.tutorias.DTO.Request.RegisterRequest;
import com.codespace.tutorias.Models.User;
import com.codespace.tutorias.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserMapping userMapping;


    public void register(RegisterRequest register){

        if(userRepository.findById(register.getMatricula()) != null){
            throw new RuntimeException("La matricula ya esta registrada");
        }

        if(userRepository.findByCorreo(register.getCorreo()) != null){
            throw new RuntimeException("El correo ya esta registrado");
        }

        userRepository.save(userMapping.toEntity(register));
    }

    public String login(LoginRequest login){
        Optional<User> user = userRepository.findById(login.getMatricula());

        if(user.isPresent()){
            if(userMapping.matchesPassword(login.getPwd(), user.get().getPwd())){
                return "JWT_TOKEN...";
            }
        }
        return "Usuario no encontrado o contraseña incorrecta";
    }
}
