package com.codespace.tutorias.Models;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

/** Catálogo de lugares (edificio + aula). */
@Entity
@IdClass(Aula.AulaId.class)
public class Aula {

    @Id
    private int edificio;

    @Id
    private int aula;

    public Aula() {}

    public int getEdificio() {
        return edificio;
    }

    public void setEdificio(int edificio) {
        this.edificio = edificio;
    }

    public int getAula() {
        return aula;
    }

    public void setAula(int aula) {
        this.aula = aula;
    }

    public static class AulaId implements Serializable {

        private int edificio;
        private int aula;

        public AulaId() {}

        public AulaId(int edificio, int aula) {
            this.edificio = edificio;
            this.aula = aula;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AulaId otro)) return false;
            return edificio == otro.edificio && aula == otro.aula;
        }

        @Override
        public int hashCode() {
            return Objects.hash(edificio, aula);
        }
    }
}
