package com.example.kauppalista;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public ListView listView;
    //Kaiki nimikkeet tallentuvat myös listaan arrayList, koska olioita sisältävän listan siirto bluetooth-ikkunaan ei vielä toimi.
    final ArrayList<String> arrayList = new ArrayList<>();
    public ArrayList<String> vastaanotettu = new ArrayList<>();

    public static String nimikeryhma = "tyhjä";

    // Luo tyhjän ArrayAdapterin ja tietokannan
    ArrayAdapter arrayAdapter;
    Tietokanta tietokanta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//listViewiin tulee näkyviin tallennetut ostokset
        listView = (ListView) findViewById(R.id.listview);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setLongClickable(true);


        tietokanta = new Tietokanta(MainActivity.this);
        paivitaLista();


//lisää-napin määrittely. Jos EditText-kentässä ei ole mitään ja painaa "Lisää", niin yrittää hakea bluetoothilla siirretyn listan.
        Button leikepoyta = (Button) findViewById(R.id.leikepoyta);
        final EditText sana = (EditText) findViewById(R.id.tekstikentta);

        // Sanan lisäys enteria painamalla. Palauttaa falsen, koska silloin ei näppäimistö mene piiloon.
        sana.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                    lisaysNapinPainallus(sana);
                    return false;
                }
                return true;
            }
        });

        // Sanan lisäys lisää-nappia painamalla.
        leikepoyta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leikepoyta();
            }

        });


                //Aukaisee tallennettujen listojen valikon
                Button lataa = (Button) findViewById(R.id.lataa);
                lataa.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, latausikkuna.class);
                        startActivity(intent);

                    }

                });

                //Tyhjentää näkyvillä olevan listan kokonaan, mutta ei muokkaa jo tallennettua listaa
                Button tyhjenna = (Button) findViewById(R.id.tyhjenna);
                tyhjenna.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(nimikeryhma != null) {
                            tietokanta.poistaKaikki(nimikeryhma);
                            paivitaLista();
                        } else {
                            tietokanta.poistaKaikki();
                            paivitaLista();
                        }
                    }

                });

                //Tämä ohjaa jo kerätyksi merkattujen laatikoiden merkkejä, jos jotain poistaa välistä.
                //Ilman tätä vain listassa oleva teksti poistuu, mutta merkki jää muistiin ja siirtyy väärään paikkaan.
                listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                        Nimike klikattu = (Nimike) parent.getItemAtPosition(position);
                        tietokanta.poistaYksi(klikattu);
                        paivitaLista();
                        return true;
                    }
                });

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int i, long l) {

                        Nimike klikattu = (Nimike) parent.getItemAtPosition(i);


                        if (klikattu.getKeratty() == false) {
                            tietokanta.paivitaKeratyksi(klikattu);
                            //listView.setItemChecked(klikattu.getId(), true);


                        } else {
                            tietokanta.paivitaKeraamattomaksi(klikattu);
                           // listView.setItemChecked(klikattu.getId(), false);
                        }

                        paivitaLista();
                        checkboxit();
                    }
                });


                //Aukaisee tallennus-ikkunan. Intent.putExtra-komennolla siirretään arrayList-lista tallennus-luokkaan.
                final Button tallenna = (Button) findViewById(R.id.paivita_lista);
                tallenna.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, tallennusikkuna.class);
                        intent.putExtra("nimikeryhmä", nimikeryhma);
                        startActivity(intent);
                    }
                });

                //tämä tuo arraylistan BluetoothinHallinta-luokasta
                Intent intent = getIntent();
                vastaanotettu = (ArrayList<String>) intent.getSerializableExtra("ITEMS");

                String nimikeryhmaLatausikkunasta = (String) intent.getSerializableExtra("NIMI");
                if (nimikeryhmaLatausikkunasta != null){
                    nimikeryhma = nimikeryhmaLatausikkunasta;
                }

                paivitaLista();

            }

        private void paivitaLista() {
            if(nimikeryhma != null){
                //jos haluaa saada checkboxin käyttöön, niin vaihtaa kohdan android.R.layout.simple_list_item_1 muotoon R.layout.rowlayout
                arrayAdapter = new ArrayAdapter<Nimike>(MainActivity.this, android.R.layout.simple_list_item_1, tietokanta.ryhmanNimikkeet(nimikeryhma));
                listView.setAdapter(arrayAdapter);
            } else {
                arrayAdapter = new ArrayAdapter<Nimike>(MainActivity.this, android.R.layout.simple_list_item_1, tietokanta.kaikkiNimikkeet());
                listView.setAdapter(arrayAdapter);
            }
        }

    //Tällä koodilla voi sulkea automaattisesti näppäimistän. Ei tällä hetkellä käytössä
            private void suljeNappaimisto() {
                View view = this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }

            //Siirtyminen bluetooth-ikkunaan.
            public void bluetoothNakyma(View view) {
                Intent intent = new Intent(this, BluetoothinHallinta.class);
                intent.putExtra("ITEMS", arrayList);
                startActivity(intent);
            }

            // Valmistelee sanan lisäyksen ennen lopullista lisäämistä tauluun hakemalla lisätyn sanan.
            public void lisaysNapinPainallus(EditText sana){
                Nimike nimike;

                String uusiTuote = sana.getText().toString();
                try {
                    lisaa(uusiTuote);
                    sana.setText(null);
                } catch (Exception e){
                    Toast.makeText(MainActivity.this, "Virhe tallennuksessa", Toast.LENGTH_SHORT).show();
                    nimike = new Nimike(-1, "virhe", false, "tyhjä");
                }
            }

            //Nimikkeet lisäävä metodi. Jos tuote on jo listassa, niin sitä ei lisätä. Jos EditText-kentässä ei ole mitään ja painaa "Lisää", niin yrittää hakea bluetoothilla siirretyn listan.
            public void lisaa(String uusiTuote) {
                arrayList.clear(); //Lista pitää tyhjentää nimikkeistä, jotta historia ei jää talteen ja ei estä tallentamasta uusia tuotteita.
                for(int i = 0 ; i < tietokanta.ryhmanNimikkeet(nimikeryhma).size(); i++){
                    arrayList.add(tietokanta.ryhmanNimikkeet(nimikeryhma).get(i).getNimi());
                }

                if (!(uusiTuote.equals(""))) {
                    Nimike nimike = new Nimike(-1, uusiTuote, false, nimikeryhma);

                    // Lisäys SQLite kantaan.
                    Tietokanta tietokanta = new Tietokanta(MainActivity.this);

                    if (!(arrayList.contains(uusiTuote))) { // Tarkastaa onko nimikettä jo listassa
                        boolean onnistui = tietokanta.addOne(nimike);
                        }
                        // Hakee koko tietokannan listaan ja laittaa sen näkyville.
                        paivitaLista();
                        arrayAdapter.notifyDataSetChanged();
                } else {
                    if (vastaanotettu != null) {
                        //Bluetoothilla siirretyssä listassa on puolipiste sanoja erottavana merkkinä
                        String[] osat = vastaanotettu.get(0).split(";");
                        for (int i = 0; i < osat.length; i++) {
                            arrayList.add(osat[i]);
                        }
                    }

                }
            }

            public void leikepoyta(){
                StringBuilder tuotteet = new StringBuilder();
                for(int i = 0 ; i < tietokanta.ryhmanNimikkeet(nimikeryhma).size(); i++){
                    if (i < tietokanta.ryhmanNimikkeet((nimikeryhma)).size()-1) {
                        tuotteet.append(tietokanta.ryhmanNimikkeet(nimikeryhma).get(i).getNimi() + "\n");
                    } else{
                        tuotteet.append(tietokanta.ryhmanNimikkeet(nimikeryhma).get(i).getNimi());
                    }
                }
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("tuotteet", tuotteet);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, "Tallennettu leikepöydälle", Toast.LENGTH_SHORT).show();
            }

            public void checkboxit(){
                List<Nimike> listaus = new ArrayList<>();
                listaus = tietokanta.kaikkiNimikkeet();
                for (int i = 0; i < listaus.size(); i++){
                    if (listaus.get(i).getKeratty() == true){
                        listView.setItemChecked(i, true);
                        System.out.println("testi");
                    }

                }
            }
        }