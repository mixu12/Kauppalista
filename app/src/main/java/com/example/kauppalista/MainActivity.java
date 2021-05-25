package com.example.kauppalista;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public ListView listView;
    //Kaiki nimikkeet tallentuvat myös listaan arrayList, koska olioita sisältävän listan siirto bluetooth-ikkunaan ei vielä toimi.
    final ArrayList<String> arrayList = new ArrayList<>();
    public ArrayList<String> vastaanotettu = new ArrayList<>();

    // Luo tyhjän ArrayAdapterin ja tietokannan
    CustomAdapter arrayAdapter;
    Tietokanta tietokanta;

    public static String nimikeryhma = "tyhjä";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//listViewiin tulee näkyviin tallennetut ostokset
        listView = (ListView) findViewById(R.id.listview);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        registerForContextMenu(listView);

        tietokanta = new Tietokanta(MainActivity.this);

        if (nimikeryhma.equals("tyhjä") && tietokanta.getViimeisinRyhma() != null) {
            nimikeryhma = tietokanta.getViimeisinRyhma();
            paivitaLista();

        }

        // Tarkistaa onko intentin kautta tullut tietoja eli bluetooth tai pdf. Tyhjentää vastaanotettu-listan heti kauppalistaan siirron jälkeen.
        haeIntentillaLahetetyt();

        if (vastaanotettu != null) {
            if (vastaanotettu.size() > 0) {
                vastaanotetutDatat();
                vastaanotettu.clear();
            }
        }

    //lisää-napin määrittely. Jos EditText-kentässä ei ole mitään ja painaa "Lisää", niin yrittää hakea bluetoothilla siirretyn listan.
        Button leikepoyta = findViewById(R.id.leikepoyta);
        final EditText sana = findViewById(R.id.tekstikentta);

        if (!sana.getText().toString().equals("")) {
            lisaysNapinPainallus(sana);
        }

        // Sanan lisäys enteria painamalla. Palauttaa falsen, koska silloin ei näppäimistö mene piiloon.
        sana.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
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
        Button poistaValitut = (Button) findViewById(R.id.poistaValitut);
        poistaValitut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tietokanta.poistaKaikkiKeratyt(nimikeryhma);
                paivitaLista();
            }

        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {

                Nimike klikattu = (Nimike) parent.getItemAtPosition(i);

                if (klikattu.getKeratty() == false) {
                    tietokanta.paivitaKeratyksi(klikattu);

                } else {
                    tietokanta.paivitaKeraamattomaksi(klikattu);
                }

                paivitaLista();

            }
        });

        Intent intent = getIntent();
        String nimikeryhmaLatausikkunasta = (String) intent.getSerializableExtra("NIMI");
        if (nimikeryhmaLatausikkunasta != null) {
            nimikeryhma = nimikeryhmaLatausikkunasta;
            ViimeisinLista viimeisinLista = new ViimeisinLista(-1, nimikeryhma);

            // Lisäys SQLite kantaan.
            Tietokanta tietokanta = new Tietokanta(MainActivity.this);

            boolean viimeisimmanLisays = tietokanta.lisaaViimeisinLista(viimeisinLista);

            // Hakee koko tietokannan listaan ja laittaa sen näkyville.
            paivitaLista();
            arrayAdapter.notifyDataSetChanged();
        }

        paivitaLista();

    }

    private void haeIntentillaLahetetyt() {
        //tämä tuo arraylistan BluetoothinHallinta-luokasta ja PDF-valikosta
        Intent intentBluetooth = getIntent();
        vastaanotettu = (ArrayList<String>) intentBluetooth.getSerializableExtra("Nimikkeet");

        Intent intentPDF = getIntent();
        vastaanotettu = (ArrayList<String>) intentPDF.getSerializableExtra("PDF");
    }

    //Tämä käy läpi vastaanotettu-listan ja lähettää ne metodille lisaa.
    public void vastaanotetutDatat() {
        for (int i = 0; i < vastaanotettu.size(); i++) {
            String sana = vastaanotettu.get(i);
            lisaa(sana);
        }
    }

    private void paivitaLista() {
        // Näiden avulla lista palautuu samalle paikalle kuin se olikin.
        int lastViewedPosition = listView.getFirstVisiblePosition();
        View v = listView.getChildAt(0);
        int topOffset = (v == null) ? 0 : v.getTop();

        if (nimikeryhma != null) {
            //käytössä on oma ListView, joka tukee checkboxeja
            arrayAdapter = new CustomAdapter<Nimike>(MainActivity.this, R.layout.listview_checkboxilla, tietokanta.ryhmanNimikkeet(nimikeryhma));
            listView.setAdapter(arrayAdapter);
            checkboxit();
            //Tämä tekee lopullisen siirron listassa oikeaan paikkaan.
            listView.setSelectionFromTop(lastViewedPosition, topOffset);
        } else {
            arrayAdapter = new CustomAdapter<Nimike>(MainActivity.this, R.layout.listview_checkboxilla, tietokanta.kaikkiNimikkeet());
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

    // Valmistelee sanan lisäyksen ennen lopullista lisäämistä tauluun hakemalla lisätyn sanan.
    public void lisaysNapinPainallus(EditText sana) {
        Nimike nimike;

        String uusiTuote = sana.getText().toString();
        try {
            lisaa(uusiTuote);
            sana.setText(null);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Virhe tallennuksessa", Toast.LENGTH_SHORT).show();
            nimike = new Nimike(-1, "virhe", false, "tyhjä");
        }
    }

    //Nimikkeet lisäävä metodi. Jos tuote on jo listassa, niin sitä ei lisätä. Jos EditText-kentässä ei ole mitään ja painaa "Lisää", niin yrittää hakea bluetoothilla siirretyn listan.
    public void lisaa(String uusiTuote) {
        if (uusiTuote.contains("\n")) {
            lisaaUseitaKerralla(uusiTuote);
            uusiTuote = "";
        }
        arrayList.clear(); //Lista pitää tyhjentää nimikkeistä, jotta historia ei jää talteen ja ei estä tallentamasta uusia tuotteita.
        for (int i = 0; i < tietokanta.ryhmanNimikkeet(nimikeryhma).size(); i++) {
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
        }
    }

    public void checkboxit() {
        List<Nimike> listaus = new ArrayList<>();
        listaus = tietokanta.ryhmanNimikkeet(nimikeryhma);
        for (int i = 0; i < listaus.size(); i++) {
            if (listaus.get(i).getKeratty() == true) {
                listView.setItemChecked(i, true);
            }
        }
    }

    public void lisaaUseitaKerralla(String uusiaTuotteita) {
        String[] pilkottu = uusiaTuotteita.split("\n");

        for (int i = 0; i < pilkottu.length; i++) {
            String sana = pilkottu[i];
            lisaa(sana);
        }
    }

    public void tyhjennys() {
        if (nimikeryhma != null) {
            tietokanta.poistaKaikki(nimikeryhma);
        } else {
            tietokanta.poistaKaikki();
        }
        paivitaLista();
    }

    // Tallentaa näkyvillä olevat nimikkeet leikepöydälle.
    public void leikepoyta() {
        StringBuilder tuotteet = new StringBuilder();
        for (int i = 0; i < tietokanta.ryhmanNimikkeet(nimikeryhma).size(); i++) {
            if (i < tietokanta.ryhmanNimikkeet((nimikeryhma)).size() - 1) {
                tuotteet.append(tietokanta.ryhmanNimikkeet(nimikeryhma).get(i).getNimi() + "\n");
            } else {
                tuotteet.append(tietokanta.ryhmanNimikkeet(nimikeryhma).get(i).getNimi());
            }
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("tuotteet", tuotteet);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(MainActivity.this, "Tallennettu leikepöydälle", Toast.LENGTH_SHORT).show();
    }

    // Tästä alkaa listview-menun ja ylämenun rakentaminen
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        getMenuInflater().inflate(R.menu.menu, menu);
        menu.setHeaderTitle("Valitse toiminto");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final Nimike klikattu = (Nimike) listView.getItemAtPosition(menuInfo.position);
        switch (item.getItemId()) {
            case R.id.poista:
                tietokanta.poistaYksi(klikattu);
                paivitaLista();
                //Toast.makeText(this, "poistettu", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.muokkaa:
                final EditText sana = (EditText) findViewById(R.id.tekstikentta);
                sana.setText(klikattu.getNimi());

                // Sanan lisäys enteria painamalla. Palauttaa falsen, koska silloin ei näppäimistö mene piiloon.
                sana.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                        if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                            String muokattuTuote = sana.getText().toString();
                            if (!muokattuTuote.isEmpty()) {
                                tietokanta.muokkaaTuotetta(klikattu, muokattuTuote);
                            }
                            paivitaLista();
                            sana.setText("");
                            Toast.makeText(MainActivity.this, "muokattu", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                            return false;
                        }
                        return true;
                    }
                });
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ylamenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.paivita_nimi_valikko:
                paivitaNimiValikko();
                return true;
            case R.id.Bluetooth_valikko:
                bluetoothNakyma();
                return true;
            case R.id.tyhjennaLista:
                tyhjennys();
                return true;
            case R.id.Pdfkasittely:
                pdfKasittely();
                return true;
            case R.id.Ocr:
                ocr();
                return true;
        }
        ;

        return super.onOptionsItemSelected(item);
    }

    //INTENTIT

    //Siirtyminen bluetooth-ikkunaan.
    public void bluetoothNakyma() {
        arrayList.clear();
        for (int i = 0; i < tietokanta.ryhmanNimikkeet(nimikeryhma).size(); i++) {
            arrayList.add(tietokanta.ryhmanNimikkeet(nimikeryhma).get(i).getNimi());
        }
        Intent intent = new Intent(this, BluetoothinHallinta.class);
        intent.putExtra("Nimikkeet", arrayList);
        startActivity(intent);
    }

    public void paivitaNimiValikko() {
        Intent intent = new Intent(MainActivity.this, tallennusikkuna.class);
        intent.putExtra("nimikeryhmä", nimikeryhma);
        startActivity(intent);
    }

    public void pdfKasittely() {
        Intent intent = new Intent(this, Pdfkasittely.class);
        startActivity(intent);
    }

    public void ocr() {
        Intent intent = new Intent(this, Ocr.class);
        startActivity(intent);
    }

}

