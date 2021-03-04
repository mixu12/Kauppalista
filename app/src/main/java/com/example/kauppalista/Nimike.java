package com.example.kauppalista;

import java.io.Serializable;
import java.util.Objects;

public class Nimike implements Serializable {
    private int id;
    private String nimi;
    private Boolean keratty;
    private String ryhma;


    public Nimike(int id, String nimi, Boolean onkoKeratty, String ryhma){
        this.id = id;
        this.nimi = nimi;
        this.keratty = onkoKeratty;
        this.ryhma = ryhma;
    }

    public String getNimi(){
        return this.nimi;
    }

    public int getId(){
        return this.id;
    }

    public void setKeratty(){
        if(this.keratty == false){
            this.keratty = true;
        } else{
            this.keratty = false;
        }

    }

    public Boolean getKeratty(){
        return this.keratty;
    }

    public String toString(){
/*
        if (this.keratty == false) {
            return this.nimi + " " + this.ryhma;
        } else {
            return "";
        }
 */
        return this.nimi + " " + this.ryhma;
    }

    public String getRyhma(){
        return this.ryhma;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Nimike nimike = (Nimike) o;
        return nimi.equals(nimike.nimi) &&
                ryhma.equals(nimike.ryhma);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nimi);
    }

}
