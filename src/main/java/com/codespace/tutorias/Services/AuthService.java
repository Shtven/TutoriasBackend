package com.codespace.tutorias.Services;

import com.codespace.tutorias.DTO.Mapping.UsuarioMapping;
import com.codespace.tutorias.DTO.Request.LoginManualRequest;
import com.codespace.tutorias.DTO.Request.RegistroManualRequest;
import com.codespace.tutorias.DTO.Request.RegistroRequest;
import com.codespace.tutorias.DTO.Responsive.LoginResponsive;
import com.codespace.tutorias.DTO.Responsive.PerfilProveedorResponsive;
import com.codespace.tutorias.Exceptions.BusinessException;
import com.codespace.tutorias.Helpers.MatriculaHelper;
import com.codespace.tutorias.Helpers.Proveedores;
import com.codespace.tutorias.Helpers.Roles;
import com.codespace.tutorias.JWT.IdentidadVerificada;
import com.codespace.tutorias.JWT.JWTUtils;
import com.codespace.tutorias.Models.Rol;
import com.codespace.tutorias.Models.Usuario;
import com.codespace.tutorias.Repositories.RolRepository;
import com.codespace.tutorias.Repositories.UsuarioRepository;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Dos formas de entrar (docs/AUTENTICACION.md):
 * con proveedor externo (§1 y §5) o con matrícula/correo y contraseña (§9).
 */
@Service
public class AuthService {

    private static final Set<String> ROLES_REGISTRABLES = Set.of(Roles.TUTORADO, Roles.TUTOR);

    private static final String CREDENCIALES_INCORRECTAS = "Matrícula, correo o contraseña incorrectos.";

    /** BCrypt solo usa los primeros 72 bytes; más largos se rechazan. */
    private static final int MAX_BYTES_PASSWORD = 72;

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

    @Autowired
    private PasswordEncoder passwordEncoder;

    /** Dominios aceptados en el registro con contraseña; vacío = cualquiera. */
    @Value("${auth.local.dominios:}")
    private String[] dominiosLocales;

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
     * 2. Si no, por correo solo si la fila no tiene proveedor (pre-registro o
     *    cuenta con contraseña) y la liga.
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
            // El proveedor demuestra que el correo es de esta persona. La contraseña
            // se desactiva para que quien haya registrado ese correo a mano antes
            // no conserve acceso a la cuenta.
            usuario.setPasswordHash(null);
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

        Rol rol = rolRegistrable(request.rol());
        String matricula = MatriculaHelper.normalizar(request.matricula());
        validarDisponibles(matricula, identidad.correo());

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

    /** Registro con datos capturados a mano (POST /auth/signup). Devuelve la sesión. */
    @Transactional
    public LoginResponsive registrarManual(RegistroManualRequest request) {
        Rol rol = rolRegistrable(request.rol());
        String matricula = MatriculaHelper.normalizar(request.matricula());
        String correo = normalizarCorreo(request.correo());
        validarDominioLocal(correo);
        String apellidos = apellidos(request);
        if (excedeLimiteBcrypt(request.password())) {
            throw new BusinessException("La contraseña es demasiado larga: usa menos letras con acento o símbolos.");
        }
        validarDisponibles(matricula, correo);

        Usuario usuario = new Usuario();
        usuario.setMatricula(matricula);
        usuario.setNombre(request.nombre().trim());
        usuario.setApellidos(apellidos);
        usuario.setCorreo(correo);
        usuario.setRol(rol);
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));

        return sesion(usuarioRepository.save(usuario));
    }

    /** Login con contraseña (POST /auth/signin) por matrícula o por correo. */
    @Transactional(readOnly = true)
    public LoginResponsive loginManual(LoginManualRequest request) {
        Usuario usuario = buscarParaLogin(request)
                .orElseThrow(() -> new BusinessException(CREDENCIALES_INCORRECTAS));

        if (usuario.getPasswordHash() == null) {
            throw new BusinessException(sinContrasena(usuario));
        }
        if (excedeLimiteBcrypt(request.password())
                || !passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            throw new BusinessException(CREDENCIALES_INCORRECTAS);
        }
        return sesion(usuario);
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

    /** TUTORADO o TUTOR; ADMIN nunca se asigna desde la API. */
    private Rol rolRegistrable(String valor) {
        String rol = valor.trim().toUpperCase(Locale.ROOT);
        if (!ROLES_REGISTRABLES.contains(rol)) {
            throw new BusinessException("Rol inválido.");
        }
        return rolRepository.findByRol(rol)
                .orElseThrow(() -> new BusinessException("Rol inválido."));
    }

    private void validarDisponibles(String matricula, String correo) {
        if (usuarioRepository.existsById(matricula)) {
            throw new BusinessException("La matrícula ya está registrada con otra cuenta.");
        }
        if (usuarioRepository.existsByCorreo(correo)) {
            throw new BusinessException("El correo ya está registrado.");
        }
    }

    private void validarDominioLocal(String correo) {
        List<String> dominios = Arrays.stream(dominiosLocales)
                .map(d -> d.trim().toLowerCase(Locale.ROOT))
                .filter(d -> !d.isEmpty())
                .toList();
        String dominio = correo.substring(correo.lastIndexOf('@') + 1);
        if (!dominios.isEmpty() && !dominios.contains(dominio)) {
            throw new BusinessException("El correo debe ser @" + String.join(" o @", dominios) + ".");
        }
    }

    /** apellidos, o apellidoP + apellidoM como en la versión anterior. */
    private static String apellidos(RegistroManualRequest request) {
        String apellidos = tieneTexto(request.apellidos())
                ? request.apellidos().trim()
                : String.join(" ", Stream.of(request.apellidoP(), request.apellidoM())
                        .filter(AuthService::tieneTexto)
                        .map(String::trim)
                        .toList());
        if (apellidos.isEmpty()) {
            throw new BusinessException("Los apellidos son obligatorios.");
        }
        if (apellidos.length() > 200) {
            throw new BusinessException("Los apellidos no pueden exceder 200 caracteres.");
        }
        return apellidos;
    }

    private Optional<Usuario> buscarParaLogin(LoginManualRequest request) {
        if (tieneTexto(request.correo())) {
            return usuarioRepository.findByCorreo(normalizarCorreo(request.correo()));
        }
        if (tieneTexto(request.matricula())) {
            return usuarioRepository.findById(MatriculaHelper.normalizarSiAplica(request.matricula()));
        }
        throw new BusinessException("Escribe tu matrícula o tu correo.");
    }

    private static String sinContrasena(Usuario usuario) {
        if (Proveedores.MICROSOFT.equals(usuario.getProveedor())) {
            return "Esta cuenta inicia sesión con Microsoft.";
        }
        if (Proveedores.GOOGLE.equals(usuario.getProveedor())) {
            return "Esta cuenta inicia sesión con Google.";
        }
        return "Esta cuenta no tiene contraseña: inicia sesión con Microsoft o Google.";
    }

    private static boolean excedeLimiteBcrypt(String password) {
        return password.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES_PASSWORD;
    }

    private static String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
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
