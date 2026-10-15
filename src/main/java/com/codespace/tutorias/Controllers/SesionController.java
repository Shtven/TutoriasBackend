package com.codespace.tutorias.Controllers;

import com.codespace.tutorias.DTO.Request.MarcasRequest;
import com.codespace.tutorias.DTO.Request.SesionRequest;
import com.codespace.tutorias.DTO.Responsive.CierreResultadoResponsive;
import com.codespace.tutorias.DTO.Responsive.HistorialAsistenciaResponsive;
import com.codespace.tutorias.DTO.Responsive.ListaResponsive;
import com.codespace.tutorias.DTO.Responsive.SesionResponsive;
import com.codespace.tutorias.DTO.Responsive.SesionesHoyResponsive;
import com.codespace.tutorias.Exceptions.ApiResponse;
import com.codespace.tutorias.Services.SesionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Sesiones, pase de lista e historial (pantallas 09, 11 a 14). */
@RestController
public class SesionController {

    @Autowired
    private SesionService sesionService;

    @GetMapping("/sesiones/hoy")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<ApiResponse<SesionesHoyResponsive>> hoy(@RequestAttribute("matricula") String matricula) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Sesiones de hoy", sesionService.hoy(matricula)));
    }

    @PostMapping("/sesiones/abrir-lista")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<ApiResponse<ListaResponsive>> abrirLista(@Valid @RequestBody SesionRequest request,
                                                                   @RequestAttribute("matricula") String matricula) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Pase de lista abierto.",
                sesionService.abrirLista(matricula, request.idHorario(), request.fecha())));
    }

    @PostMapping("/sesiones/cancelar")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<ApiResponse<SesionResponsive>> cancelar(@Valid @RequestBody SesionRequest request,
                                                                  @RequestAttribute("matricula") String matricula) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Sesión cancelada.",
                sesionService.cancelar(matricula, request.idHorario(), request.fecha())));
    }

    @GetMapping("/sesiones/{idSesion}/lista")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<ListaResponsive>> lista(@PathVariable int idSesion,
                                                              @RequestAttribute("matricula") String matricula,
                                                              @RequestAttribute("rol") String rol) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Pase de lista", sesionService.lista(idSesion, matricula, rol)));
    }

    @PutMapping("/sesiones/{idSesion}/lista")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<ApiResponse<ListaResponsive>> marcar(@PathVariable int idSesion,
                                                               @Valid @RequestBody MarcasRequest request,
                                                               @RequestAttribute("matricula") String matricula) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Asistencia guardada.",
                sesionService.marcar(idSesion, matricula, request.marcas())));
    }

    @PostMapping("/sesiones/{idSesion}/cerrar-lista")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<ApiResponse<CierreResultadoResponsive>> cerrarLista(@PathVariable int idSesion,
                                                                              @RequestAttribute("matricula") String matricula) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Pase de lista cerrado.",
                sesionService.cerrarLista(idSesion, matricula)));
    }

    @GetMapping("/tutorias/{idTutoria}/sesiones")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<SesionResponsive>>> deTutoria(@PathVariable int idTutoria,
                                                                         @RequestParam(defaultValue = "7") int dias,
                                                                         @RequestAttribute("matricula") String matricula,
                                                                         @RequestAttribute("rol") String rol) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Sesiones",
                sesionService.deTutoria(idTutoria, matricula, rol, dias)));
    }

    @GetMapping("/tutorias/{idTutoria}/historial-asistencia")
    @PreAuthorize("hasAnyRole('TUTOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<HistorialAsistenciaResponsive>> historial(@PathVariable int idTutoria,
                                                                                @RequestAttribute("matricula") String matricula,
                                                                                @RequestAttribute("rol") String rol) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Historial de asistencia",
                sesionService.historial(idTutoria, matricula, rol)));
    }
}
