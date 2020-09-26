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

public class latausikkuna extends Activity {
    public ArrayList<String> arrayList;
    public static String FILE_NAME = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        arrayList = (ArrayList<String>) intent.getSerializableExtra("ITEMS");

        setContentView(R.layout.latausikkunan_layout);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width*.5),(int)(height*.4));
    }

    public void mainNakyma(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        EditText tiedostonNimi = (EditText) findViewById(R.id.tiedostonNimi);
        FILE_NAME = tiedostonNimi.getText().toString() +".txt";
        intent.putExtra("NIMI", FILE_NAME);
        startActivity(intent);
    }
}
