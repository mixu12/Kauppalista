package com.example.kauppalista;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
//Tätä ei vielä käytetä mihinkään. Tarkoitus on koota kaikki ostoslistat tämän alle ja yhdistää nimike ja ostoslista erikseen.
public class Listat {
    private int id;
    private String lista;
    private String muokkauspaiva;

    public Listat(int id, String lista){
        this.id = id;
        this.lista = lista;
        this.muokkauspaiva = "a";
    }

    public String getPaiva(){
        if (this.muokkauspaiva.equals("a")) {
            ZonedDateTime aika = ZonedDateTime.now();
            DateTimeFormatter muokkaaja = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            return aika.format(muokkaaja);
        } else{
            return this.muokkauspaiva;
        }
    }

    public int getId(){
        return this.id;
    }

    public String getLista(){
        return this.lista;
    }
}
