package com.example.kauppalista;

public class ViimeisinLista {
    private int id;
    private String listanNimi;

    public ViimeisinLista(int id, String listanNimi){
        this.id = id;
        this.listanNimi = listanNimi;
    }

    public String getListanNimi(){
        return this.listanNimi;
    }
}
