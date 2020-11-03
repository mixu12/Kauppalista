package com.example.kauppalista;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class tallennusikkuna extends Activity {
    public ArrayList<Nimike> arrayList;

    Tietokanta tietokanta;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //tämä vastaanottaa talletettavan listan
        Intent intent = getIntent();
        arrayList = (ArrayList<Nimike>) intent.getSerializableExtra("nimikkeet");

        //Tästä eteenpäin on ulkonäön määritelyä
        setContentView(R.layout.tallennusikkunan_layout);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width*.5),(int)(height*.4));

        tietokanta = new Tietokanta(tallennusikkuna.this);

        final String nimikeryhma = (String) intent.getSerializableExtra("nimikeryhmä");
        System.out.println(nimikeryhma);

        //OK-napin painalluksen ohjaus. Tallentaa muuttujaan FILE_NAME annetun tiedoston nimen ja lisää tiedostopäätteen.
        final Button ok = (Button) findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText tiedostonNimi = (EditText) findViewById(R.id.listanNimi);
                tietokanta.paivitaRyhma(tiedostonNimi.getText().toString(), nimikeryhma);
                mainNakyma(view);
            }
        });
    }

    //Etusivulle siirtyminen
    public void mainNakyma(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
