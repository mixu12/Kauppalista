package com.example.kauppalista;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public ListView listView;
    final ArrayList<String> arrayList = new ArrayList<>();
    public ArrayList<String> vastaanotettu = new ArrayList<>();
    public static String FILE_NAME = null;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//listViewiin tulee näkyviin tallennetut ostokset
        listView = (ListView) findViewById(R.id.listview);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setLongClickable(true);
//jos haluaa saada checkboxin käyttöön, niin vaihtaa kohdan android.R.layout.simple_list_item_1 muotoon R.layout.rowlayout
        final ArrayAdapter arrayAdapter = new ArrayAdapter(this, R.layout.rowlayout, arrayList);

        listView.setAdapter(arrayAdapter);


//lisää-napin määrittely. Jos EditText-kentässä ei ole mitään ja painaa "Lisää", niin yrittää hakea bluetoothilla siirretyn listan.
        Button lisaa = (Button) findViewById(R.id.lisaa);
        lisaa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText sana = (EditText) findViewById(R.id.tekstikentta);
                String uusiTuote = sana.getText().toString();

                if (!(uusiTuote.equals(""))) {
                    if(!(arrayList.contains(uusiTuote))) {
                        arrayList.add(uusiTuote);
                        arrayAdapter.notifyDataSetChanged();
                        sana.setText(null);
                    }else{
                        sana.setText(null);
                    }
                }else{
                    if(vastaanotettu != null){
                        //Bluetoothilla siirretyssä listassa on puolipiste sanoja erottavana merkkinä
                        String[] osat = vastaanotettu.get(0).split(";");
                        for(int i = 0; i < osat.length; i++) {
                            arrayList.add(osat[i]);
                        }
                        arrayAdapter.notifyDataSetChanged();
                    }

                }
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
                for (int i = 0; i < arrayList.size(); i++){
                    listView.setItemChecked(i, false);
                }
                arrayList.clear();
                arrayAdapter.notifyDataSetChanged();
            }

        });

        //Tämä ohjaa jo kerätyksi merkattujen laatikoiden merkkejä, jos jotain poistaa välistä.
        //Ilman tätä vain listassa oleva teksti poistuu, mutta merkki jää muistiin ja siirtyy väärään paikkaan.
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                for(int i = 0; i < arrayList.size(); i++){
                    if(listView.isItemChecked(position + i + 1)  == true){
                        listView.setItemChecked(position + i, true);
                    }else{
                        listView.setItemChecked(position + i, false);
                    }
                }
                int poistettava = position;
                arrayList.remove(poistettava);
                arrayAdapter.notifyDataSetChanged();
                return true;
            }
        });

        //Aukaisee tallennus-ikkunan. Intent.putExtra-komennolla siirretään arrayList-lista tallennus-luokkaan.
        final Button tallenna = (Button) findViewById(R.id.tallenna);
            tallenna.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, tallennusikkuna.class);
                intent.putExtra("ITEMS", arrayList);
                startActivity(intent);
            }
        });

        //tämä tuo arraylistan BluetoothinHallinta-luokasta
        Intent intent = getIntent();
        vastaanotettu = (ArrayList<String>) intent.getSerializableExtra("ITEMS");

        FILE_NAME = (String) intent.getSerializableExtra("NIMI");

        //tämä tuo latauskansion latausikkuna-luokasta aina kun avataan pääsivu, jos FILE_NAME:ssa on jokin tiedostonimi.
        if(FILE_NAME != null){
            lataa();
            arrayAdapter.notifyDataSetChanged();
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

 /*   public void tallenna() {
        FileOutputStream fos = null;
        try {

            fos = openFileOutput(FILE_NAME, MODE_PRIVATE);

            for (int i = 0; i < this.arrayList.size(); i++) {
                fos.write(this.arrayList.get(i).getBytes());
                fos.write("\n".getBytes());
            }
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

  */

 // Tässä ladataan se tiedosto, joka on talletettuna muuttujaan FILE_NAME
    public void lataa() {
        FileInputStream fis = null;

        try {
            fis = openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;

            //Tämä käy jokaisen rivin läpi ja lisää ne listaan. Rivit pätkitään rivinvaihtojen perusteella.
            while ((text = br.readLine()) != null) {
                sb.append(text).append("\n");
                arrayList.add(text);
            }
            //Alla oleva antaa ilmoituksen siitä, mikä tiedosto on avattu ja mistä mistä kansiosta.
            Toast.makeText(this, "Load from " + getFilesDir() + "/" + FILE_NAME, Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Siirtyminen bluetooth-ikkunaan.
    public void bluetoothNakyma(View view) {
        Intent intent = new Intent(this, BluetoothinHallinta.class);
        intent.putExtra("ITEMS", arrayList);
        startActivity(intent);
    }


}

