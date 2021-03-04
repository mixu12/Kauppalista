package com.example.kauppalista;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class latausikkuna extends Activity {
    public static String nimikeryhma = null;

    Tietokanta tietokanta;

    final ArrayList<String> ryhmat = new ArrayList<>();
    ArrayAdapter arrayAdapter;

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

        // Hakee tietokannan, tekee ListView näkymän, jonka nimi on listaus ja vie tiedot tietokannasta sinne
        tietokanta = new Tietokanta(latausikkuna.this);
        listaus = (ListView) findViewById(R.id.listaus);
        paivitaLista();

        listaus.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {

                Nimike klikattu = (Nimike) parent.getItemAtPosition(i);

                String ryhma = klikattu.getRyhma();
                EditText tiedostonNimi = (EditText) findViewById(R.id.listanNimi);
                tiedostonNimi.setText(ryhma);
            }
        });
    }


    //Paluu etusivulle
    public void mainNakyma(View view) {
        EditText tiedostonNimi = (EditText) findViewById(R.id.listanNimi);
        if(tiedostonNimi.getText().toString().equals("")){
            nimikeryhma = null;
        } else {
            nimikeryhma = tiedostonNimi.getText().toString();
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("NIMI", nimikeryhma);
        startActivity(intent);
    }

    private void paivitaLista() {
        arrayAdapter = new ArrayAdapter<Nimike>(latausikkuna.this, R.layout.muut_listviewit_paitsi_etusivu, tietokanta.getRyhmat());
        listaus.setAdapter(arrayAdapter);
        }

}
