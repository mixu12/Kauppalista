package com.example.kauppalista;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Tietokanta extends SQLiteOpenHelper {


    public static final String LISTA = "LISTA";
    public static final String COLUMN_TUOTTEEN_NIMI = "TUOTTEEN_NIMI";
    public static final String COLUMN_ONKO = "ONKO";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_RYHMA = "RYHMA";

    public Tietokanta(@Nullable Context context) {
        super(context, "lista.db", null, 1);
    }

    // Tätä kutsutaan kun ensimmäisen kerran tarvitaan tietokantaa. Tämä luo uuden sellaisen.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement = "CREATE TABLE " + LISTA + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_TUOTTEEN_NIMI + " TEXT, " + COLUMN_ONKO + " BOOL, " + COLUMN_RYHMA + " STRING)";
        db.execSQL(createTableStatement);
    }

    // Tämä huolehtaa tietokannan versionumeroinnista. Tarkoitus on estää tietokannan hajoaminen muutoksia tehdessä.
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public boolean addOne(Nimike nimike){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_TUOTTEEN_NIMI, nimike.getNimi());
        cv.put(COLUMN_ONKO, nimike.getKeratty());
        cv.put(COLUMN_RYHMA, nimike.getRyhma());

        long insert = db.insert(LISTA, null, cv);
        if (insert == -1){
            return false;
        } else {
            return true;
        }
    }

    public List<Nimike> kaikkiNimikkeet(){
        List<Nimike> nimikkeet = new ArrayList<>();

        // Datan haku kannasta

        String queryString = "SELECT * FROM " + LISTA;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);

        if (cursor.moveToFirst()) {
            // Käy läpi kaikki tallennetut tiedot ja luo uuden nimike-olion. Laittaa ne sitten listaan, joka palautetaan.
                do {
                int id = cursor.getInt(0);
                String nimike = cursor.getString(1);
                boolean onko = cursor.getInt(2) == 1 ? true: false; // Tämä on lyhenne if-else rakenteesta. Jos ensimmäinen on totta, valitaan kysymysmerkin jälkeen tuleva, jos ei, niin sitten kaksoispisteen jälkeen oleva.
                String ryhma = cursor.getString(3);

                Nimike uusiNimike = new Nimike(id, nimike, onko, "");
                nimikkeet.add(uusiNimike);

            } while (cursor.moveToNext());

        } else {
            // Jos tulee virhe
        }
        // Sulkee tietokannan
        cursor.close();
        db.close();
        return nimikkeet;
    }

    // Kun latausikkunassa on annettu jokin hakusana, niin tämä haku etsii kyseisen ryhmän nimikkeet.
    public List<Nimike> ryhmanNimikkeet(String valittu){
        List<Nimike> nimikkeet = new ArrayList<>();

        // Datan haku kannasta

        String queryString = "SELECT * FROM " + LISTA + " WHERE " + COLUMN_RYHMA + " = ?";
        String[] valinnat = {valittu};
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, valinnat);

        if (cursor.moveToFirst()) {
            // Käy läpi kaikki tallennetut tiedot ja luo uuden nimike-olion. Laittaa ne sitten listaan, joka palautetaan.
            do {
                int id = cursor.getInt(0);
                String nimike = cursor.getString(1);
                boolean onko = cursor.getInt(2) == 1 ? true: false; // Tämä on lyhenne if-else rakenteesta. Jos ensimmäinen on totta, valitaan kysymysmerkin jälkeen tuleva, jos ei, niin sitten kaksoispisteen jälkeen oleva.
                String ryhma = cursor.getString(3);

                Nimike uusiNimike = new Nimike(id, nimike, onko, "");
                nimikkeet.add(uusiNimike);

            } while (cursor.moveToNext());

        } else {
            // Jos tulee virhe
        }
        // Sulkee tietokannan
        cursor.close();
        db.close();
        return nimikkeet;
    }

    public boolean poistaYksi(Nimike nimike){

        SQLiteDatabase db = this.getWritableDatabase();
        String queryString = "DELETE FROM " + LISTA + " WHERE " + COLUMN_ID + " = " + nimike.getId();

        Cursor cursor = db.rawQuery(queryString, null);

        if (cursor.moveToFirst()){
            return true;
        } else {
            return false;
        }
    }

    public boolean paivitaKeratyksi(Nimike nimike){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        String queryString = "UPDATE " + LISTA + " SET " + COLUMN_ONKO + " = '1'" + " WHERE " + COLUMN_ID + " = " + nimike.getId();

        Cursor cursor = db.rawQuery(queryString, null);

        if (cursor.moveToFirst()){
            return true;
        } else {
            return false;
        }

    }

    public boolean paivitaKeraamattomaksi(Nimike nimike) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        String queryString = "UPDATE " + LISTA + " SET " + COLUMN_ONKO + " = '0'" + " WHERE " + COLUMN_ID + " = " + nimike.getId();

        Cursor cursor = db.rawQuery(queryString, null);

        if (cursor.moveToFirst()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean paivitaRyhma(String ryhmanNimiUusi, String ryhmanNimiVanha) {
        SQLiteDatabase db = this.getWritableDatabase();

        String queryString = "UPDATE " + LISTA + " SET " + COLUMN_RYHMA + " = ?" + " WHERE " + COLUMN_RYHMA + " = ?";
        String[] valinnat = {ryhmanNimiUusi, ryhmanNimiVanha};

        Cursor cursor = db.rawQuery(queryString, valinnat);

        if (cursor.moveToFirst()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean poistaKaikki(){

        SQLiteDatabase db = this.getWritableDatabase();
        String queryString = "DELETE FROM " + LISTA;

        Cursor cursor = db.rawQuery(queryString, null);

        if (cursor.moveToFirst()){
            return true;
        } else {
            return false;
        }
    }

    public boolean poistaKaikki(String poistettava_ryhma){

        SQLiteDatabase db = this.getWritableDatabase();
        String queryString = "DELETE FROM " + LISTA + " WHERE " + COLUMN_RYHMA + " = ?";
        String[] valittu = {poistettava_ryhma};

        Cursor cursor = db.rawQuery(queryString, valittu);

        if (cursor.moveToFirst()){
            return true;
        } else {
            return false;
        }
    }

    // Tämä vie latausikkuna-luokalle kaikki erilaiset ryhmät
    public List<Nimike> getRyhmat(){
        List<Nimike> nimikkeet = new ArrayList<>();

        // Datan haku kannasta

        String queryString = "SELECT " +  COLUMN_RYHMA + " FROM " + LISTA + " GROUP BY " + COLUMN_RYHMA;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);

        if (cursor.moveToFirst()) {
            // Käy läpi kaikki tallennetut tiedot ja luo uuden nimike-olion. Laittaa ne sitten listaan, joka palautetaan.
            do {
                int id = cursor.getInt(0);
             //   String nimike = cursor.getString(1);
              //  boolean onko = cursor.getInt(2) == 1 ? true: false; // Tämä on lyhenne if-else rakenteesta. Jos ensimmäinen on totta, valitaan kysymysmerkin jälkeen tuleva, jos ei, niin sitten kaksoispisteen jälkeen oleva.
                String ryhma = cursor.getString(0);

                Nimike uusiNimike = new Nimike(id, "", false, ryhma);
                nimikkeet.add(uusiNimike);

            } while (cursor.moveToNext());

        } else {
            // Jos tulee virhe
        }
        // Sulkee tietokannan
        cursor.close();
        db.close();
        return nimikkeet;
    }
}
