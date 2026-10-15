package com.codespace.tutorias.Services;

import com.codespace.tutorias.DTO.Request.MateriaRequest;
import com.codespace.tutorias.DTO.Responsive.MateriaCatalogoResponsive;
import com.codespace.tutorias.Exceptions.BusinessException;
import com.codespace.tutorias.Helpers.EstadosTutoria;
import com.codespace.tutorias.Helpers.Pagina;
import com.codespace.tutorias.Helpers.Paginado;
import com.codespace.tutorias.Helpers.Texto;
import com.codespace.tutorias.Models.Materia;
import com.codespace.tutorias.Repositories.MateriaRepository;
import com.codespace.tutorias.Repositories.TutoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Catálogo de Experiencias Educativas (pantalla 16). */
@Service
public class MateriaService {

    @Autowired
    private MateriaRepository materiaRepository;

    @Autowired
    private TutoriaRepository tutoriaRepository;

    @Transactional(readOnly = true)
    public Paginado<MateriaCatalogoResponsive> listar(String q, Pagina pagina) {
        Map<Integer, Long> activas = new HashMap<>();
        for (Object[] fila : tutoriaRepository.contarActivasPorMateria()) {
            activas.put((Integer) fila[0], (Long) fila[1]);
        }
        List<Materia> filtradas = materiaRepository.findAllByOrderByMateriaAsc().stream()
                .filter(m -> coincide(m, q))
                .toList();
        List<Materia> porcion = pagina == null ? filtradas : pagina.recortar(filtradas);
        return new Paginado<>(porcion.stream()
                .map(m -> toDTO(m, activas.getOrDefault(m.getIdMateria(), 0L)))
                .toList(), filtradas.size());
    }

    @Transactional(readOnly = true)
    public MateriaCatalogoResponsive obtener(int idMateria) {
        Materia materia = buscar(idMateria);
        long activas = tutoriaRepository.findByEstado(EstadosTutoria.ACTIVA).stream()
                .filter(t -> t.getMateria().getIdMateria() == idMateria).count();
        return toDTO(materia, activas);
    }

    @Transactional
    public MateriaCatalogoResponsive crear(MateriaRequest request) {
        validarNrcLibre(request.nrc(), null);
        Materia materia = new Materia();
        materia.setMateria(request.nombre().trim());
        materia.setIdentificador(request.nrc());
        return toDTO(materiaRepository.save(materia), 0);
    }

    @Transactional
    public MateriaCatalogoResponsive actualizar(int idMateria, MateriaRequest request) {
        Materia materia = buscar(idMateria);
        validarNrcLibre(request.nrc(), idMateria);
        materia.setMateria(request.nombre().trim());
        materia.setIdentificador(request.nrc());
        materiaRepository.save(materia);
        return obtener(idMateria);
    }

    @Transactional
    public void eliminar(int idMateria) {
        Materia materia = buscar(idMateria);
        if (tutoriaRepository.existsByMateria_IdMateria(idMateria)) {
            throw new BusinessException("No se puede eliminar: la experiencia educativa tiene tutorías registradas.");
        }
        materiaRepository.delete(materia);
    }

    public Materia buscar(int idMateria) {
        return materiaRepository.findById(idMateria)
                .orElseThrow(() -> new BusinessException("Experiencia educativa no encontrada."));
    }

    private void validarNrcLibre(int nrc, Integer idPropio) {
        materiaRepository.findByIdentificador(nrc)
                .filter(otra -> idPropio == null || otra.getIdMateria() != idPropio)
                .ifPresent(otra -> {
                    throw new BusinessException("Este NRC ya está registrado para " + otra.getMateria() + ".");
                });
    }

    private static boolean coincide(Materia materia, String q) {
        if (q == null || q.isBlank()) return true;
        return Texto.contiene(materia.getMateria(), q)
                || String.valueOf(materia.getIdentificador()).contains(q.trim());
    }

    private static MateriaCatalogoResponsive toDTO(Materia materia, long tutoriasActivas) {
        return new MateriaCatalogoResponsive(
                materia.getIdMateria(), materia.getMateria(), materia.getIdentificador(), tutoriasActivas);
    }
}
