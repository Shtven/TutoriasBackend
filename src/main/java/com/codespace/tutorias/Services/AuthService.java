package com.codespace.tutorias.Services;

import com.codespace.tutorias.DTO.Mapping.UsuarioMapping;
import com.codespace.tutorias.DTO.Request.RegistroRequest;
import com.codespace.tutorias.DTO.Responsive.LoginResponsive;
import com.codespace.tutorias.DTO.Responsive.PerfilProveedorResponsive;
import com.codespace.tutorias.Exceptions.BusinessException;
import com.codespace.tutorias.Helpers.MatriculaHelper;
import com.codespace.tutorias.Helpers.Roles;
import com.codespace.tutorias.JWT.IdentidadVerificada;
import com.codespace.tutorias.JWT.JWTUtils;
import com.codespace.tutorias.Models.Rol;
import com.codespace.tutorias.Models.Usuario;
import com.codespace.tutorias.Repositories.RolRepository;
import com.codespace.tutorias.Repositories.UsuarioRepository;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

/**
 * Login con proveedor externo y registro en el primer acceso
 * (docs/AUTENTICACION.md §1 y §5).
 */
@Service
public class AuthService {

    private static final Set<String> ROLES_REGISTRABLES = Set.of(Roles.TUTORADO, Roles.TUTOR);

    @Autowired
    private VerificadorIdToken verificador;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private UsuarioMapping usuarioMapping;

    @Transactional
    public LoginResponsive loginMicrosoft(String idToken) {
        return login(verificador.verificarMicrosoft(idToken));
    }

    @Transactional
    public LoginResponsive loginGoogle(String idToken) {
        return login(verificador.verificarGoogle(idToken));
    }

    /**
     * 1. Busca por proveedor + id externo.
     * 2. Si no, por correo solo si la fila es un pre-registro (sin id externo) y la liga.
     * 3. Si tampoco, es primer acceso: devuelve tokenRegistro.
     */
    private LoginResponsive login(IdentidadVerificada identidad) {
        Optional<Usuario> existente = usuarioRepository
                .findByProveedorAndIdExterno(identidad.proveedor(), identidad.idExterno());
        if (existente.isPresent()) {
            return sesion(existente.get());
        }

        Optional<Usuario> porCorreo = usuarioRepository.findByCorreo(identidad.correo());
        if (porCorreo.isPresent()) {
            Usuario usuario = porCorreo.get();
            if (usuario.getIdExterno() != null) {
                throw new BusinessException("Este correo ya está ligado a otra cuenta.");
            }
            usuario.setProveedor(identidad.proveedor());
            usuario.setIdExterno(identidad.idExterno());
            return sesion(usuarioRepository.save(usuario));
        }

        return LoginResponsive.registroPendiente(
                jwtUtils.generarTokenRegistro(identidad),
                new PerfilProveedorResponsive(
                        identidad.nombre(), identidad.apellidos(), identidad.correo(), identidad.proveedor()));
    }

    @Transactional
    public LoginResponsive registrar(RegistroRequest request) {
        IdentidadVerificada identidad = leerTokenRegistro(request.tokenRegistro());

        // Doble envío del formulario: ya quedó registrado con esta identidad.
        Optional<Usuario> yaRegistrado = usuarioRepository
                .findByProveedorAndIdExterno(identidad.proveedor(), identidad.idExterno());
        if (yaRegistrado.isPresent()) {
            return sesion(yaRegistrado.get());
        }

        String rolSolicitado = request.rol().trim().toUpperCase();
        if (!ROLES_REGISTRABLES.contains(rolSolicitado)) {
            throw new BusinessException("Rol inválido.");
        }

        String matricula = MatriculaHelper.normalizar(request.matricula());
        if (usuarioRepository.existsById(matricula)) {
            throw new BusinessException("La matrícula ya está registrada con otra cuenta.");
        }
        if (usuarioRepository.existsByCorreo(identidad.correo())) {
            throw new BusinessException("El correo ya está registrado.");
        }

        Rol rol = rolRepository.findByRol(rolSolicitado)
                .orElseThrow(() -> new BusinessException("Rol inválido."));

        Usuario usuario = new Usuario();
        usuario.setMatricula(matricula);
        usuario.setNombre(identidad.nombre());
        usuario.setApellidos(identidad.apellidos());
        usuario.setCorreo(identidad.correo());
        usuario.setRol(rol);
        usuario.setProveedor(identidad.proveedor());
        usuario.setIdExterno(identidad.idExterno());

        return sesion(usuarioRepository.save(usuario));
    }

    /** Solo pruebas (auth.dev.habilitado): entra por correo sin proveedor. */
    @Transactional(readOnly = true)
    public LoginResponsive loginDesarrollo(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo.trim().toLowerCase())
                .orElseThrow(() -> new BusinessException("No existe un usuario con ese correo."));
        return sesion(usuario);
    }

    // ------------------------------------------------------------------

    private LoginResponsive sesion(Usuario usuario) {
        return LoginResponsive.sesion(
                jwtUtils.generarToken(usuario.getMatricula(), usuario.getRol().getRol()),
                usuarioMapping.toDTO(usuario));
    }

    private IdentidadVerificada leerTokenRegistro(String token) {
        Claims claims;
        try {
            claims = jwtUtils.leer(token);
        } catch (Exception e) {
            throw new BusinessException("El registro expiró. Vuelve a iniciar sesión.");
        }
        if (!JWTUtils.TIPO_REGISTRO.equals(claims.get("tipo", String.class))) {
            throw new BusinessException("El registro expiró. Vuelve a iniciar sesión.");
        }
        return new IdentidadVerificada(
                claims.get("proveedor", String.class),
                claims.getSubject(),
                claims.get("correo", String.class),
                claims.get("nombre", String.class),
                claims.get("apellidos", String.class));
    }
}
