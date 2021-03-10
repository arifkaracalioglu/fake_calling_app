package com.hae.callyourself;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

public class SettingsActivity extends AppCompatActivity {
    public static SharedPreferences mSharedPreferencesSettings;
    public static SharedPreferences.Editor mEditorSettings;
    ImageView mImageViewSS;
    NumberPicker mNumberPicker;
    private Bitmap myPictureForShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        //SharedPreferences Settings
        mSharedPreferencesSettings = getSharedPreferences("settings_activity", MODE_PRIVATE);
        mEditorSettings = mSharedPreferencesSettings.edit();
        //View Settings
        mImageViewSS = findViewById(R.id.mSSView);
        mNumberPicker = findViewById(R.id.mNumberPicker);
        mNumberPicker.setMinValue(0);
        mNumberPicker.setMaxValue(180);
        //NumberPicker value change listener for delay time
        mNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mEditorSettings.putInt("delay", newVal).apply();
            }
        });
        mNumberPicker.setValue(mSharedPreferencesSettings.getInt("delay", 1));
        readFile();
        mImageViewSS.setImageBitmap(myPictureForShow);
        mImageViewSS.setScaleX(1);
        mImageViewSS.setScaleY(1);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    //Save preferences for number and name
    public void clickSaveBtn(View view) {
        try {
            Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
    //ReadFile for get calling screen image
    private void readFile(){
        File file = new File(Environment.getExternalStorageDirectory()+"/saved_images/profile.jpg");
        myPictureForShow = BitmapFactory.decodeFile(file.getAbsolutePath());
    }
    //Turn back and finish this activity
    public void clickBackBtn(View view) {
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    //For Select An Image
    public void clickPickImage(View view) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, 10);
    }

    //ActivityResult for selected image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                mImageViewSS.setImageBitmap(selectedImage);
                //Store image locally
                saveToInternalStorage(selectedImage);
                //EOF Store image locally
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, getString(R.string.image_error), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.didnt_pick_image), Toast.LENGTH_LONG).show();
        }
    }

    //Store image local method
    private void saveToInternalStorage(Bitmap bitmapImage) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_images");
        if (!myDir.exists()) {
            myDir.mkdirs();
        }
        String fname = "profile.jpg";
        File file = new File(myDir, fname);
        if (file.exists()){
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}