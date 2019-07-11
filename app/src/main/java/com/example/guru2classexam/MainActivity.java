package com.example.guru2classexam;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.guru2classexam.firebase.LoginActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnGoogleMap).setOnClickListener(mBtnClick);
        findViewById(R.id.btnFirebase).setOnClickListener(mBtnClick);

        //GPS 퍼미션 요청
        ActivityCompat.requestPermissions(this,
                new String[] {
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, 0);

    }

    private View.OnClickListener mBtnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnGoogleMap:
                    Intent i = new Intent(MainActivity.this, GoogleMapActivity.class);
                    startActivity(i);
                    break;

                case R.id.btnFirebase:
                    Intent i2 = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(i2);
            }
        }
    };
}
