package com.codespace.tutorias.Controllers;

import com.codespace.tutorias.DTO.Request.HorarioRequest;
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

    @PostMapping("/")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<?> crearHorario(@RequestAttribute("matricula") String matricula, @RequestBody HorarioRequest horarioRequest){
        horarioService.crearTutoria(horarioRequest, matricula);
        return ResponseEntity.ok(200);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<?> actualizarHorario(@PathVariable int id, @RequestBody HorarioRequest horario){
        return  ResponseEntity.ok(200);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<?> eliminarHorario(@PathVariable int id){
        horarioService.eliminarHorario(id);
        return ResponseEntity.ok(200);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<?> obtenerHorario(@PathVariable int id){
        return ResponseEntity.ok(horarioService.listarHorario(id));
    }

    @GetMapping("/")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<?> listarHorarios(@RequestAttribute("matricula") String matricula){
        return ResponseEntity.ok(horarioService.listarHorarios(matricula));
    }
}

