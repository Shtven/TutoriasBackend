package com.codespace.tutorias.Models;

import com.codespace.tutorias.Helpers.EstadosSesion;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

/**
 * Ocurrencia de un horario en una fecha. Solo se persiste al abrir el pase
 * de lista o al cancelar; antes de eso la sesión es "virtual".
 */
@Entity
public class Sesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idSesion;

    @ManyToOne
    @JoinColumn(name = "id_horario")
    private Horario horario;

    private LocalDate fecha;

    /** Copiadas del horario al crear la fila. */
    private LocalTime horaInicio;
    private LocalTime horaFin;

    private OffsetDateTime fechaCancelada;
    private OffsetDateTime fechaListaAbierta;
    private OffsetDateTime fechaListaCerrada;
    private OffsetDateTime fechaAvisoCierre;

    public Sesion() {}

    /** El estado se deriva de las marcas de tiempo (RN-6). */
    public String estado() {
        if (fechaCancelada != null) return EstadosSesion.CANCELADA;
        if (fechaListaCerrada != null) return EstadosSesion.COMPLETADA;
        if (fechaListaAbierta != null) return EstadosSesion.LISTA_ABIERTA;
        return EstadosSesion.PROGRAMADA;
    }

    public int getIdSesion() {
        return idSesion;
    }

    public void setIdSesion(int idSesion) {
        this.idSesion = idSesion;
    }

    public Horario getHorario() {
        return horario;
    }

    public void setHorario(Horario horario) {
        this.horario = horario;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public OffsetDateTime getFechaCancelada() {
        return fechaCancelada;
    }

    public void setFechaCancelada(OffsetDateTime fechaCancelada) {
        this.fechaCancelada = fechaCancelada;
    }

    public OffsetDateTime getFechaListaAbierta() {
        return fechaListaAbierta;
    }

    public void setFechaListaAbierta(OffsetDateTime fechaListaAbierta) {
        this.fechaListaAbierta = fechaListaAbierta;
    }

    public OffsetDateTime getFechaListaCerrada() {
        return fechaListaCerrada;
    }

    public void setFechaListaCerrada(OffsetDateTime fechaListaCerrada) {
        this.fechaListaCerrada = fechaListaCerrada;
    }

    public OffsetDateTime getFechaAvisoCierre() {
        return fechaAvisoCierre;
    }

    public void setFechaAvisoCierre(OffsetDateTime fechaAvisoCierre) {
        this.fechaAvisoCierre = fechaAvisoCierre;
    }
}
