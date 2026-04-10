package it.unibs;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Cella {
    private int riga, colonna;
    private TipoCella tipo;
    @JsonProperty("frontiera")
    private boolean isFrontiera;
    @JsonProperty("landMark")
    private boolean isLandMark;

    // Costruttore di copia deep copy per Cella
    public Cella(Cella other) {
        this.riga = other.riga;
        this.colonna = other.colonna;
        this.tipo = other.tipo; // TipoCella è enum, quindi è immutabile
        this.isFrontiera = other.isFrontiera;
        this.isLandMark = other.isLandMark;
    }

    // Costruttore con tipo di default

    public Cella(int riga, int colonna) {
        this.riga = riga;
        this.colonna = colonna;
        this.tipo = TipoCella.LIBERA;
        this.isFrontiera = false;
        this.isLandMark = false;
    }

    @JsonCreator
    public Cella(
            @JsonProperty("riga") int riga,
            @JsonProperty("colonna") int colonna,
            @JsonProperty("tipo") TipoCella tipo,
            @JsonProperty("frontiera") boolean isFrontiera,
            @JsonProperty("landmark") boolean isLandMark) {
        this.riga = riga;
        this.colonna = colonna;
        this.tipo = tipo;
        this.isFrontiera = false;
        this.isLandMark = false;

    }

    // Costruttore con tipo personalizzato
    public Cella(int riga, int colonna, TipoCella tipo) {
        this.riga = riga;
        this.colonna = colonna;
        this.tipo = tipo;
        this.isFrontiera = false;
        this.isLandMark = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Cella))
            return false;
        Cella other = (Cella) o;
        return this.riga == other.riga && this.colonna == other.colonna;
    }

    public int calcolaPosizione(int numMaxRighe) {
        return riga * numMaxRighe + colonna;

    }

    // Setters and Getters
    public int getRiga() {
        return riga;
    }

    public int getColonna() {
        return colonna;
    }

    public void setRiga(int riga) {
        this.riga = riga;
    }

    public void setColonna(int colonna) {
        this.colonna = colonna;
    }

    public void setCoordinate(int riga, int colonna) {
        this.riga = riga;
        this.colonna = colonna;
    }

    public TipoCella getTipo() {
        return tipo;
    }

    public void setTipo(TipoCella tipo) {
        this.tipo = tipo;
    }

    public boolean isLandMark() {
        return this.isLandMark;
    }

    public boolean isFrontiera() {
        return this.isFrontiera;
    }

    public void setIsFrontiera(boolean isFrontiera) {
        this.isFrontiera = isFrontiera;
    }

    public void setIsLandmark(boolean isLandMark) {
        this.isLandMark = isLandMark;
    }

    @Override
    public String toString() {
        return "Cella(riga=" + riga + ", colonna=" + colonna + ",  tipo=" + (tipo == TipoCella.LIBERA ? "ATTRAVERSABILE)" : tipo + ")");
    }
}