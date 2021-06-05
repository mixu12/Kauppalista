package com.example.kauppalista;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Pdfkasittely extends AppCompatActivity {

    public static String tiedostonNimi = null;

    ArrayList<String> tekstirivit;

    private static final int PICKFILE_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfkasittely);

        PDFBoxResourceLoader.init(getApplicationContext());

        Button pdfOK = (Button) findViewById(R.id.pdfOK);

        Button tiedostot = (Button) findViewById(R.id.tiedostot);

        pdfOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stripText(view);
                poistaLuodutTiedostot();
            }
        });

        tiedostot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tiedostonValinta();
            }
        });
    }

     //Tekstin erottelu pdf-tiedostosta
    public void stripText(View v) {
        File input = new File(getFilesDir().getPath(), tiedostonNimi);
        String parsedText = null;
        PDDocument document = null;
        try {
            document = PDDocument.load(input);
        } catch(IOException e) {
            Log.e("PdfBox-Android", "Virhe ladattaessa dokumenttia", e);
        }

        try {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setStartPage(0);
            pdfStripper.setEndPage(1);
            parsedText = pdfStripper.getText(document);
        }
        catch (IOException e)
        {
            Log.e("PdfBox-Android", "Virhe tekstirivien käsittelyssä", e);
        } finally {
            try {
                if (document != null) document.close();
            }
            catch (IOException e)
            {
                Log.e("PdfBox-Android", "Virhe dokumentin sulkemisessa", e);
            }
        }
        setlistaan(parsedText);
        mainNakyma(v);
    }

    //Eroteltu teksti siirretään listaan
    public ArrayList<String> setlistaan(String teksti){
        String[] pilkottu = teksti.split("\n");
        tekstirivit = new ArrayList<>();
        for (int i = 0; i < pilkottu.length; i++){
            tekstirivit.add(pilkottu[i]);
        }
        return tekstirivit;
    }

    public void mainNakyma(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("PDF", tekstirivit);
        startActivity(intent);
        finish();
    }

    public void tiedostonValinta(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Pdf"), PICKFILE_REQUEST_CODE);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == PICKFILE_REQUEST_CODE) {
                Uri uri = data.getData();
                tiedostonNimi = haeTiedostonNimi(uri);
                Context context = getApplicationContext();
                try {
                    uusiTiedosto(getApplicationContext(), uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                TextView pdfNimi = (TextView) findViewById(R.id.pdfNimi);
                pdfNimi.setText(tiedostonNimi);
                return;
            }
            super.onActivityResult(requestCode, resultCode, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String haeTiedostonNimi(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    // Tähän metodiin saatu ohjeita täältä https://stackoverflow.com/questions/13133579/android-save-a-file-from-an-existing-uri
    public void uusiTiedosto(Context context, Uri uri) throws IOException {
        String kohdekansio = getFilesDir().getPath(); //Tämä vie kansioon /data/user/0/com.example.kauppalista/files
        String uudenTiedostonNimi = tiedostonNimi;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        InputStream input = null;
        boolean virhe = false;

        try {
            input = context.getContentResolver().openInputStream(uri);

            boolean directorySetupResult;
            File destDir = new File(kohdekansio);
            if (!destDir.exists()) {
                directorySetupResult = destDir.mkdirs();
            } else {
                directorySetupResult = true;
            }

            if (!directorySetupResult) {
                virhe = true;
            } else {
                String destination = kohdekansio + File.separator + uudenTiedostonNimi;
                int originalsize = input.available();

                bis = new BufferedInputStream(input);
                bos = new BufferedOutputStream(new FileOutputStream(destination));
                byte[] buf = new byte[originalsize];
                bis.read(buf);
                do {
                    bos.write(buf);
                } while (bis.read(buf) != -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            virhe = true;
        } finally {
            try {
                if (bos != null) {
                    bos.flush();
                    bos.close();
                }
            } catch (Exception ignored) {

            }
        }
    }

    private void poistaLuodutTiedostot(){
        File poistettavat = new File(getFilesDir().getPath());
        String[] tiedostot = poistettavat.list();
        for(int i = 0; i < tiedostot.length; i++){
            File tiedosto = new File(poistettavat, tiedostot[i]);
            tiedosto.delete();
            System.out.println("Tiedostot poistettu");
        }
    }

}