package com.example.guru2classexam.firebase;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.guru2classexam.R;
import com.google.firebase.auth.FirebaseAuth;

public class BoardActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        // 새메모 버튼
        findViewById(R.id.btnNewMemo).setOnClickListener(mClicks);
        // 로그아웃 버튼
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(mClicks);
        btnLogout.setText("로그아웃(" + mFirebaseAuth.getCurrentUser().getEmail()+")");
    }

    private View.OnClickListener mClicks = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId()) {
                case R.id.btnLogout:
                    logout();
                    break;
                case R.id.btnNewMemo:
                    Intent i = new Intent(getBaseContext(), BoardInsertActivity.class);
                    startActivity(i);
                    break;
            }
        }
    };

    // 로그아웃 메소드
    private void logout() {
        try {
            mFirebaseAuth.signOut();
            Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT)
                    .show();
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
