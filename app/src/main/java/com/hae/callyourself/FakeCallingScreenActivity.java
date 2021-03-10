package com.hae.callyourself;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class FakeCallingScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_calling_screen);
    }
    //When click screen, activity and app will be destroyed with that method
    public void finishClick(View view){
        finish();
        System.exit(0);
    }
}