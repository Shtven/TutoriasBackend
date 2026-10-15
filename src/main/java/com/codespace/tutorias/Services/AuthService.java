package com.codespace.tutorias.Services;

import com.codespace.tutorias.DTO.Mapping.UsuarioMapping;
import com.codespace.tutorias.DTO.Request.SigninRequest;
import com.codespace.tutorias.DTO.Request.SignupRequest;
import com.codespace.tutorias.DTO.Responsive.LoginResponsive;
import com.codespace.tutorias.Exceptions.BusinessException;
import com.codespace.tutorias.Exceptions.CredencialesInvalidasException;
import com.codespace.tutorias.Exceptions.ValidacionException;
import com.codespace.tutorias.Helpers.MatriculaHelper;
import com.codespace.tutorias.Helpers.Proveedores;
import com.codespace.tutorias.Helpers.Roles;
import com.codespace.tutorias.JWT.JWTUtils;
import com.codespace.tutorias.Models.Rol;
import com.codespace.tutorias.Models.Usuario;
import com.codespace.tutorias.Repositories.RolRepository;
import com.codespace.tutorias.Repositories.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Login con matrícula o correo y contraseña, y alta de cuentas
 * (docs/AUTENTICACION.md).
 */
@Service
public class AuthService {

    /** BCrypt solo usa los primeros 72 bytes. */
    private static final int MAX_BYTES_PWD = 72;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private UsuarioMapping usuarioMapping;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LimiteIntentos limite;

    @PersistenceContext
    private EntityManager entityManager;

    /** Dominios de correo aceptados en el signup; vacío = cualquiera. */
    @Value("${auth.local.dominios:}")
    private String[] dominios;

    /** Se compara contra él cuando no hay cuenta, para que tarde lo mismo que un fallo real. */
    private volatile String hashSenuelo;

    @Transactional(readOnly = true)
    public LoginResponsive signin(SigninRequest request, String ip) {
        boolean porCorreo = request.correo() != null && !request.correo().isBlank();
        boolean porMatricula = request.matricula() != null && !request.matricula().isBlank();
        if (!porCorreo && !porMatricula) {
            throw new ValidacionException("matricula", "Escribe tu matrícula o correo.");
        }

        String identificador = porCorreo
                ? request.correo().trim().toLowerCase()
                : MatriculaHelper.paraBusqueda(request.matricula());
        Optional<Usuario> usuario = porCorreo
                ? usuarioRepository.findByCorreo(identificador)
                : usuarioRepository.findById(identificador);

        // Por matrícula, no por lo escrito: entrar por correo o por matrícula comparte contador.
        String cuenta = usuario.map(Usuario::getMatricula).orElse(identificador);
        limite.verificar(cuenta, ip);

        Optional<Usuario> conPwd = usuario.filter(u -> !u.esReclamable());
        if (conPwd.isEmpty() || !coincide(request.pwd(), conPwd.get().getPasswordHash())) {
            if (conPwd.isEmpty()) coincide(request.pwd(), senuelo());
            limite.registrarFallo(cuenta, ip);
            throw new CredencialesInvalidasException();
        }

        limite.registrarExito(cuenta);
        return sesion(conPwd.get());
    }

    /**
     * Si la matrícula ya existe y no tiene contraseña (pre-registro o cuenta
     * de Microsoft/Google), se reclama cuando el correo coincide. Un TUTOR
     * solo puede darse de alta así: el admin pre-registra su matrícula.
     */
    @Transactional
    public LoginResponsive signup(SignupRequest request) {
        String matricula = MatriculaHelper.normalizar(request.matricula());
        String correo = request.correo().trim().toLowerCase();
        String rol = request.rol().trim().toUpperCase();
        if (request.pwd().getBytes(StandardCharsets.UTF_8).length > MAX_BYTES_PWD) {
            throw new ValidacionException("pwd", "La contraseña es demasiado larga.");
        }
        validarDominio(correo);

        Optional<Usuario> existente = usuarioRepository.bloquear(matricula);
        Usuario usuario = existente.isPresent()
                ? reclamar(existente.get(), correo, rol)
                : nuevo(matricula, correo, rol);

        usuario.setNombre(request.nombre().trim());
        usuario.setApellidos(request.apellidos().trim());
        usuario.setProveedor(Proveedores.LOCAL);
        usuario.setIdExterno(null);
        usuario.setPasswordHash(passwordEncoder.encode(request.pwd()));

        if (existente.isEmpty()) {
            // persist y no save(): con id asignado, save() haría merge y podría
            // pisar una fila creada en paralelo con la misma matrícula.
            // Un choque de índices único sale al hacer commit como
            // DataIntegrityViolationException (400 en GlobalExceptionHandler).
            entityManager.persist(usuario);
        }
        return sesion(usuario);
    }

    /** Solo pruebas (auth.dev.habilitado): entra por correo sin contraseña. */
    @Transactional(readOnly = true)
    public LoginResponsive loginDesarrollo(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo.trim().toLowerCase())
                .orElseThrow(() -> new BusinessException("No existe un usuario con ese correo."));
        return sesion(usuario);
    }

    // ------------------------------------------------------------------

    private Usuario nuevo(String matricula, String correo, String rol) {
        if (Roles.TUTOR.equals(rol)) {
            throw new ValidacionException("rol",
                    "Tu matrícula no está autorizada como tutor. Pide al administrador que te registre.");
        }
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new ValidacionException("correo", "El correo ya está registrado.");
        }
        Rol entidad = rolRepository.findByRol(rol)
                .orElseThrow(() -> new BusinessException("Rol inválido."));

        Usuario usuario = new Usuario();
        usuario.setMatricula(matricula);
        usuario.setCorreo(correo);
        usuario.setRol(entidad);
        return usuario;
    }

    private Usuario reclamar(Usuario usuario, String correo, String rol) {
        if (!usuario.esReclamable()) {
            throw new ValidacionException("matricula", "La matrícula ya está registrada.");
        }
        if (!usuario.getCorreo().equals(correo)) {
            throw new ValidacionException("correo", "El correo no coincide con el registrado para esta matrícula.");
        }

        String rolActual = usuario.getRol().getRol();
        if (rolActual.equals(rol)) {
            return usuario;
        }
        if (Roles.TUTOR.equals(rol)) {
            throw new ValidacionException("rol",
                    "Tu matrícula no está autorizada como tutor. Pide al administrador que te registre.");
        }
        if (Roles.TUTOR.equals(rolActual)) {
            throw new ValidacionException("rol", "Tu matrícula está autorizada como tutor: elige el rol TUTOR.");
        }
        // Un ADMIN no se reclama por signup (su contraseña se asigna en la BD).
        throw new ValidacionException("matricula", "La matrícula ya está registrada.");
    }

    private void validarDominio(String correo) {
        List<String> permitidos = Arrays.stream(dominios)
                .map(d -> d.trim().toLowerCase())
                .filter(d -> !d.isEmpty())
                .toList();
        String dominio = correo.substring(correo.lastIndexOf('@') + 1);
        if (!permitidos.isEmpty() && !permitidos.contains(dominio)) {
            throw new ValidacionException("correo", "El correo debe ser @" + String.join(" o @", permitidos) + ".");
        }
    }

    private boolean coincide(String pwd, String hash) {
        if (pwd.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES_PWD) {
            return false;
        }
        return passwordEncoder.matches(pwd, hash);
    }

    private String senuelo() {
        if (hashSenuelo == null) {
            hashSenuelo = passwordEncoder.encode("sin-cuenta-" + System.nanoTime());
        }
        return hashSenuelo;
    }

    private LoginResponsive sesion(Usuario usuario) {
        return new LoginResponsive(
                jwtUtils.generarToken(usuario.getMatricula(), usuario.getRol().getRol()),
                usuarioMapping.toDTO(usuario));
    }
}
