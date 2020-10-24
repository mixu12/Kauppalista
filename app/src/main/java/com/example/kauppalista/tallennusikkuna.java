package com.example.kauppalista;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class tallennusikkuna extends Activity {
    public ArrayList<Nimike> arrayList;
    public static String FILE_NAME = null;

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

        //OK-napin painalluksen ohjaus. Tallentaa muuttujaan FILE_NAME annetun tiedoston nimen ja lisää tiedostopäätteen.
        final Button ok = (Button) findViewById(R.id.ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText tiedostonNimi = (EditText) findViewById(R.id.tiedostonNimi);
                FILE_NAME = tiedostonNimi.getText().toString() +".txt";
                tallenna();
            }
        });
    }

    //Etusivulle siirtyminen
    public void mainNakyma(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    //Tallentaminen
    public void tallenna() {
        FileOutputStream fos = null;
        try {
            //Tämä määrittää tallennuspaikan
            fos = openFileOutput(FILE_NAME, MODE_PRIVATE);

            //Tallennettavan listan läpikäynti
            for (int i = 0; i < this.arrayList.size(); i++) {
                fos.write(this.arrayList.get(i).getNimi().getBytes());
                fos.write("\n".getBytes());
            }
            //Ilmotus talletuksesta
            Toast.makeText(this, "Saved to " + getFilesDir() + "/" + FILE_NAME, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            System.out.println("Virhe: " + e.getMessage());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
