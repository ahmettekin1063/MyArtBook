package com.example.myartbook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.KeyListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Main2Activity extends AppCompatActivity {
    Bitmap selectedImage;
    ImageView imageView;
    EditText artNameText, painterNameText, yearText;

    Button saveButton;
    Button deleteButton;
    Button saveButton2;
    Button changeButton;

    SQLiteDatabase database;

    byte[] byteArray;

    KeyListener artNameTextListener;
    KeyListener yearTextListener;
    KeyListener painterNameTextListener;
    int artId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        imageView = findViewById(R.id.imageView);

        artNameText = findViewById(R.id.artNameText);
        painterNameText = findViewById(R.id.painterNameText);
        yearText = findViewById(R.id.yearText);

        saveButton2 = findViewById(R.id.saveButton2);
        saveButton = findViewById(R.id.saveButton);
        deleteButton =findViewById(R.id.deleteButton);
        changeButton =findViewById(R.id.changeButton);

        artNameTextListener = artNameText.getKeyListener();
        yearTextListener = yearText.getKeyListener();
        painterNameTextListener = painterNameText.getKeyListener();


        database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null);
        Intent intent= getIntent();
        String info = intent.getStringExtra("info");

        if(info.matches("new")){

            artNameText.setText("");
            painterNameText.setText("");
            yearText.setText("");

            saveButton.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.INVISIBLE);
            saveButton2.setVisibility(View.INVISIBLE);
            changeButton.setVisibility(View.INVISIBLE);

            selectedImage = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.selectimage);
            imageView.setImageBitmap(selectedImage);

        }
        else{
            artId = intent.getIntExtra("artId",1);

            saveButton.setVisibility(View.INVISIBLE);
            changeButton.setVisibility(View.VISIBLE);
            saveButton2.setVisibility(View.INVISIBLE);
            deleteButton.setVisibility(View.VISIBLE);

            artNameText.setKeyListener(null);
            painterNameText.setKeyListener(null);
            yearText.setKeyListener(null);
            imageView.setClickable(false);


            try {


                Cursor cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?", new String[] {String.valueOf(artId)});



                int artNameIx = cursor.getColumnIndex( "artname" );

                int painterNameIx = cursor.getColumnIndex( "paintername" );
                int yearIx = cursor.getColumnIndex( "year" );
                int imageIx = cursor.getColumnIndex( "image" );



                while (cursor.moveToNext()) {

                    artNameText.setText(cursor.getString(artNameIx));
                    painterNameText.setText(cursor.getString(painterNameIx));
                    yearText.setText(cursor.getString(yearIx));

                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    imageView.setImageBitmap(bitmap);


            }
                cursor.close();
            }
            catch (Exception e)
            {

            }
        }


    }

    public void selectImage(View view){

      if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
           {
               ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},1 );
           }

      else {
          Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
          startActivityForResult(intentToGallery,2);
      }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 1)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intentToGallery,2);
            }
        }



        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 2 && resultCode == RESULT_OK && data != null) {
            Uri imageData = data.getData();

            try {

                if (Build.VERSION.SDK_INT >= 28) {
                    ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), imageData);
                    selectedImage = ImageDecoder.decodeBitmap(source);
                    imageView.setImageBitmap(selectedImage);

                } else {
                    selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageData);

                    imageView.setImageBitmap(selectedImage);

                }


            } catch (IOException e) {
                e.printStackTrace();
            }


            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void save (View view){

        if(selectedImage == null){
            selectedImage = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.selectimage);
        }

        String artName = artNameText.getText().toString();
        String painterName = painterNameText.getText().toString();
        String year = yearText.getText().toString();


        Bitmap smallImage = makeSmallerImage(selectedImage, 300);




        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);



        byteArray = outputStream.toByteArray();


        try {


            database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null);
            database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY , artname VARCHAR, paintername VARCHAR, year VARCHAR, image BLOB)");

            String sqlString = "INSERT INTO arts (artname, paintername, year, image) VALUES (?, ?, ?, ?)";
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
            sqLiteStatement.bindString(1, artName);
            sqLiteStatement.bindString(2, painterName);
            sqLiteStatement.bindString(3, year);
            sqLiteStatement.bindBlob(4, byteArray);
            sqLiteStatement.execute();

        }

        catch (Exception e){

        }
        Intent intent = new Intent(Main2Activity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        //finish();

    }

    public void delete (View view){

        database = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);
        Intent intent= getIntent();
        int artId = intent.getIntExtra("artId",1);

        database.execSQL("DELETE FROM arts WHERE id = ?", new String[] {String.valueOf(artId)}  );
        Intent intent2 = new Intent(Main2Activity.this,MainActivity.class);
        intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent2);



    }
    public void change(View view){

        artNameText.setKeyListener(artNameTextListener);
        painterNameText.setKeyListener(painterNameTextListener);
        yearText.setKeyListener(yearTextListener);
        imageView.setClickable(true);

        saveButton.setVisibility(View.INVISIBLE);
        changeButton.setVisibility(View.INVISIBLE);
        saveButton2.setVisibility(View.VISIBLE);
        deleteButton.setVisibility(View.VISIBLE);




    }

    public void save2(View view){

        if(selectedImage == null){
            Cursor cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?", new String[] {String.valueOf(artId)});
            int imageIx = cursor.getColumnIndex( "image" );
            while (cursor.moveToNext()) {
                byte[] bytes = cursor.getBlob(imageIx);
                selectedImage =BitmapFactory.decodeByteArray(bytes,0,bytes.length);
            }

        }

        String artName = artNameText.getText().toString();
        String painterName = painterNameText.getText().toString();
        String year = yearText.getText().toString();


        Bitmap smallImage = makeSmallerImage(selectedImage, 300);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);

        byteArray = outputStream.toByteArray();

        try {

            database = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);
            Intent intent= getIntent();
            int artId = intent.getIntExtra("artId",1);

            String sqlString = "UPDATE arts SET artname = ?, paintername =?, year=?, image=? WHERE id = ?";
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
            sqLiteStatement.bindString(1, artName);
            sqLiteStatement.bindString(2, painterName);
            sqLiteStatement.bindString(3, year);
            sqLiteStatement.bindBlob(4, byteArray);
            sqLiteStatement.bindString(5, String.valueOf(artId));//soru işaretleri index
            sqLiteStatement.execute();


        }

        catch (Exception e){

        }
        Intent intent3 = new Intent(Main2Activity.this,MainActivity.class);
        intent3.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent3);

    }
    public Bitmap makeSmallerImage(Bitmap image, int maximumSize){

        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if(bitmapRatio > 1){

            width = maximumSize;
            height = (int) (width / bitmapRatio);
        }

        else{
            height = maximumSize;
            width =(int) (height * bitmapRatio);

        }


        return Bitmap.createScaledBitmap(image,width,height,true);

    }





}