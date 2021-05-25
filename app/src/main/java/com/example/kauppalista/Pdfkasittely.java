package com.example.kauppalista;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
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

    File root;
    AssetManager assetManager;
    TextView tv;

    public ListView listaus;
    public static String FILE_NAME = null;
    public static String tiedostonNimi = null;

    ArrayList<String> tekstirivit;

    private static final int OPEN_DIRECTORY_REQUEST_CODE = 1;
    private static final int PICKFILE_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfkasittely);



        PDFBoxResourceLoader.init(getApplicationContext());

        //setListaus();

        Button pdfOK = (Button) findViewById(R.id.pdfOK);

        Button tiedostot = (Button) findViewById(R.id.tiedostot);

        pdfOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stripText(view);
            }
        });

        tiedostot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tiedostonValinta();
            }
        });
    }

    /**
     * Strips the text from a PDF and displays the text on screen
     */
    public void stripText(View v) {
        //File input = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), tiedostonNimi);
        File input = new File("/data/data/com.example.kauppalista/files", tiedostonNimi);
        System.out.println(input);
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

    /*
    public void setListaus(){

        File tiedostot = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()); //Hakee tiedostokansion oletustiedoston nimellä.
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

     */

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
        //intent.setType("application/pdf");
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Pdf"), PICKFILE_REQUEST_CODE);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
/*
    public void uusiTiedosto(Uri uri){
        BufferedReader br;
        FileOutputStream os;
        try {
            br = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)));
            //the name NewFileName on internal app storage?
            os = openFileOutput(tiedostonNimi, Context.MODE_PRIVATE);
            String line = null;
            while ((line = br.readLine()) != null) {
                os.write(line.getBytes());
                Log.w("jotain",line);
            }
            br.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

 */

    public void uusiTiedosto(Context context, Uri uri) throws IOException {
        String destinationDir = "/data/data/com.example.kauppalista/files";
        String destFileName = tiedostonNimi;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        InputStream input = null;
        boolean hasError = false;

        try {
            input = context.getContentResolver().openInputStream(uri);

            boolean directorySetupResult;
            File destDir = new File(destinationDir);
        System.out.println(destDir);
            if (!destDir.exists()) {
                directorySetupResult = destDir.mkdirs();
            } else if (!destDir.isDirectory()) {
                directorySetupResult = replaceFileWithDir(destinationDir);
            } else {
                directorySetupResult = true;
            }

            if (!directorySetupResult) {
                hasError = true;
            } else {
                String destination = destinationDir + File.separator + destFileName;
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
            hasError = true;
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

    private static boolean replaceFileWithDir(String path) {
        File file = new File(path);
        if (!file.exists()) {
            if (file.mkdirs()) {
                return true;
            }
        } else if (file.delete()) {
            File folder = new File(path);
            if (folder.mkdirs()) {
                return true;
            }
        }
        return false;
    }

}