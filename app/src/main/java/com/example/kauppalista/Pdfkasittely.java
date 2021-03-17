package com.example.kauppalista;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class Pdfkasittely extends AppCompatActivity {

    File root;
    AssetManager assetManager;
    TextView tv;

    public ListView listaus;
    public static String FILE_NAME = null;

    ArrayList<String> tekstirivit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfkasittely);



        PDFBoxResourceLoader.init(getApplicationContext());

        setListaus();

        Button pdfOK = (Button) findViewById(R.id.pdfOK);

        pdfOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stripText(view);
            }
        });
    }

    /**
     * Strips the text from a PDF and displays the text on screen
     */
    public void stripText(View v) {
        File input = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), FILE_NAME);
        String parsedText = null;
        PDDocument document = null;
        try {
            document = PDDocument.load(input);
        } catch(IOException e) {
            Log.e("PdfBox-Android-Sample", "Exception thrown while loading document to strip", e);
        }

        try {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setStartPage(0);
            pdfStripper.setEndPage(1);
            parsedText = pdfStripper.getText(document);
        }
        catch (IOException e)
        {
            Log.e("PdfBox-Android-Sample", "Exception thrown while stripping text", e);
        } finally {
            try {
                if (document != null) document.close();
            }
            catch (IOException e)
            {
                Log.e("PdfBox-Android-Sample", "Exception thrown while closing document", e);
            }
        }
        setlistaan(parsedText);
        mainNakyma(v);
    }

    public void setListaus(){

        File tiedostot = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()); //Hakee tiedostokansion oletustiedoston nimellä.
        System.out.println(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
        File[] tiedostolista = tiedostot.listFiles(); //Muodostaa tiedostoista taulukon.

        final ArrayList<String> tiedostojenNimetListassa = new ArrayList<>();

        for (int i = 0; i < tiedostolista.length; i++) { //Tiedostojen läpikäynti
            if (tiedostolista[i].getName().contains(".pdf")) { //Suodattaa vain pdf-tiedostot näkyville
                tiedostojenNimetListassa.add(tiedostolista[i].getName().toLowerCase());
            }
        }

        Collections.sort(tiedostojenNimetListassa); //Järjestää listan aakkosjärjestykseen

        //Tästä eteenpäin lisää tiedostot näkyviin ListViewiin
        listaus = (ListView) findViewById(R.id.listaus);
        listaus.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        final ArrayAdapter arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, tiedostojenNimetListassa);

        listaus.setAdapter(arrayAdapter);

        //Tällä valitaan haluttu tiedosto ja siirtää tiedosotn nimen EditText-kenttään.
        listaus.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                FILE_NAME = tiedostojenNimetListassa.get(i);
                TextView pdfNimi = (TextView) findViewById(R.id.pdfNimi);
                pdfNimi.setText(FILE_NAME);
            }
        });

    }

    public ArrayList<String> setlistaan(String teksti){
        String[] pilkottu = teksti.split("\n");
        tekstirivit = new ArrayList<>();
        for (int i = 0; i < pilkottu.length; i++){
            tekstirivit.add(pilkottu[i]);
        }
        System.out.println(tekstirivit);
        return tekstirivit;
    }

    public void mainNakyma(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("PDF", tekstirivit);
        startActivity(intent);
        finish();
    }

}