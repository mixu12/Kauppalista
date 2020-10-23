package com.example.kauppalista;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;

public class latausikkuna extends Activity {
    public ArrayList<String> arrayList;
    public static String FILE_NAME = null;

    public ListView listaus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Ulkonäön määrittelyä
        setContentView(R.layout.latausikkunan_layout);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width*.7),(int)(height*.7));

        //Tiedostojen haku
        setListaus();
    }

    //Paluu etusivulle
    public void mainNakyma(View view) {

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("NIMI", FILE_NAME);
        startActivity(intent);
    }

    //Tiedostojen haku
    public void setListaus(){
        File tiedostot = new File(getFilesDir().getPath()); //Hakee tiedostokansion oletustiedoston nimellä.
        File[] tiedostolista = tiedostot.listFiles(); //Muodostaa tiedostoista taulukon.
        final String[] tiedostojenNimet = new String[tiedostolista.length];
        for (int i = 0; i < tiedostojenNimet.length; i++) { //Tiedostojen läpikäynti
            tiedostojenNimet[i] = tiedostolista[i].getName();
        }
        //Tästä eteenpäin lisää tiedostot näkyviin ListViewiin
        listaus = (ListView) findViewById(R.id.listaus);
        listaus.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        final ArrayAdapter arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, tiedostojenNimet);

        listaus.setAdapter(arrayAdapter);

        //Tällä valitaan haluttu tiedosto ja siirtää tiedosotn nimen EditText-kenttään.
        listaus.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                EditText tiedostonNimi = (EditText) findViewById(R.id.tiedostonNimi);
                FILE_NAME = tiedostojenNimet[i].toString();
                tiedostonNimi.setText(FILE_NAME);
            }
        });

    }
}
