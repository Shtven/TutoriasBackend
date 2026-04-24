package com.codespace.tutorias.Controllers;

import com.codespace.tutorias.DTO.Request.HorarioRequest;
import com.codespace.tutorias.Models.Horario;
import com.codespace.tutorias.Services.HorarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/horario")
public class HorarioController {

    @Autowired
    private HorarioService horarioService;
//
//    @PostMapping("/")
//    public ResponseEntity<?> crearHorario(@RequestAttribute("matricula") String matricula, @RequestBody HorarioRequest horarioRequest){
//        horarioService.crearTutoria(horarioRequest, matricula);
//        return ResponseEntity.ok(200);
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<?> actualizarHorario(@PathVariable int id, @RequestBody HorarioRequest horario){
//        return  ResponseEntity.ok(200);
//    }


    @GetMapping("/all")
    public List<HorariosMostrarDTO> obtenerHorarios() {
        return horarioService.listarHorariosPublicos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<HorariosMostrarDTO> obtenerHorario(@PathVariable("id") int id) {
        return horarioService.buscarHorarioPublico(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/crear-horario")
    public ResponseEntity<?> crearHorario(@Valid @RequestBody CrearHorarioDTO dto, HttpServletRequest request) {
        String rol = (String) request.getAttribute("rol");
        String matricula = (String) request.getAttribute("matricula");

        if (!"TUTOR".equals(rol) && !"ADMIN".equals(rol)) {
            return ResponseEntity.status(403).body(new ApiResponse<>(false, "Acceso denegado", null));
        }

        return ResponseEntity.ok(new ApiResponse<>(true, "Horario creado correctamente", horarioService.crearHorario(dto, matricula)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizarHorario(@PathVariable("id") int id, @Valid @RequestBody HorariosDTO dto) {
        return horarioService.actualizarHorario(id, dto)
                .map(h -> ResponseEntity.noContent().<Void>build())
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarHorario(@PathVariable("id") int id) {
        horarioService.eliminarHorario(id);
        return ResponseEntity.noContent().build();
    }
}

