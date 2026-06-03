package com.codespace.tutorias.Services;

import com.codespace.tutorias.DTO.Mapping.UserMapping;
import com.codespace.tutorias.DTO.Request.LoginRequest;
import com.codespace.tutorias.DTO.Request.RegisterRequest;
import com.codespace.tutorias.DTO.Responsive.TokenLogin;
import com.codespace.tutorias.Exceptions.BusinessException;
import com.codespace.tutorias.Helpers.MatriculaHelper;
import com.codespace.tutorias.Models.Rol;
import com.codespace.tutorias.Models.Usuario;
import com.codespace.tutorias.Repositories.RolRepository;
import com.codespace.tutorias.Repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

        @Autowired
        private UsuarioRepository usuarioRepository;

        @Autowired
        private UserMapping userMapping;

        @Autowired
        private RolRepository rolRepository;

        public void register(RegisterRequest register){

            // El frontend envia la matricula con prefijo institucional (zS/S);
            // aqui la normalizamos a Integer y validamos formato.
            Integer matricula = MatriculaHelper.parse(register.getMatricula());

            if(usuarioRepository.existsById(matricula)){
                throw new BusinessException("La matrícula ya está registrada");
            }

            if(usuarioRepository.existsByCorreo(register.getCorreo())){
                throw new BusinessException("El correo ya está registrado");
            }

            Rol rol = rolRepository.findById(register.getRol())
                    .orElseThrow(() -> new BusinessException("El rol no existe"));

            List<String> rolesPermitidos = List.of("TUTOR", "TUTORADO");
            if (!rolesPermitidos.contains(rol.getRol().toUpperCase())) {
                throw new BusinessException("No puedes registrarte con ese rol");
            }

            Usuario usuario = userMapping.toEntity(register, matricula, rol);

            usuarioRepository.save(usuario);
        }
        public TokenLogin login(LoginRequest login){

            Integer matricula = MatriculaHelper.parse(login.getMatricula());

            Usuario user = usuarioRepository.findById(matricula)
                    .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

            if(!userMapping.matchesPassword(login.getPwd(), user.getPwd())){
                throw new BusinessException("Contraseña incorrecta");
            }

            return userMapping.generateToken(user);
        }
}
