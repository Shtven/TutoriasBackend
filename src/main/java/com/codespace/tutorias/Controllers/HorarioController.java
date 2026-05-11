package com.codespace.tutorias.Controllers;

import com.codespace.tutorias.DTO.Request.HorarioRequest;
import com.codespace.tutorias.Exceptions.ApiResponse;
import com.codespace.tutorias.Models.Horario;
import com.codespace.tutorias.Services.HorarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/horario")
public class HorarioController {

    @Autowired
    private HorarioService horarioService;

    @PostMapping
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public ResponseEntity<?> crearHorario(@RequestAttribute("matricula") String matricula, @RequestBody HorarioRequest horarioRequest){
        horarioService.crearHorario(horarioRequest, matricula);
        return ResponseEntity.ok(new ApiResponse<>(true, "Horario creado.", null));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public ResponseEntity<?> actualizarHorario(@RequestAttribute("matricula") String matricula, @PathVariable int id, @RequestBody HorarioRequest horario){
        horarioService.modificarHorario(id, horario, matricula);
        return ResponseEntity.ok(new ApiResponse<>(true, "Horario actualizado.", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public ResponseEntity<?> eliminarHorario(@PathVariable int id){
        horarioService.eliminarHorario(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Horario eliminado.", null));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public ResponseEntity<?> obtenerHorario(@PathVariable int id){
        return ResponseEntity.ok(new ApiResponse<>(true, "Horario", horarioService.listarHorario(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public ResponseEntity<?> listarHorarios(@RequestAttribute("matricula") String matricula){
        return ResponseEntity.ok(new ApiResponse<>(true, "Horarios", horarioService.listarHorarios(matricula)));
    }
}

