package com.example.guru2classexam.firebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.guru2classexam.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    // 구글 로그인
    private GoogleSignInClient mGoogleSignInClient;
    // Firebsae 인증 객체
    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById(R.id.btnGoogleSignIn).setOnClickListener(mClicks);

        // 구글 로그인 객체 선언
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mFirebaseAuth.getCurrentUser() != null
                && mFirebaseAuth.getCurrentUser().getEmail() != null) {
            // 이미 로그인 되어 있다. 따라서 메인화면으로 바로 이동한다.
            Toast.makeText(this, "로그인 성공 - 메인화면 이동", Toast.LENGTH_SHORT)
                    .show();
            goBoardMainActivity();
        }
    }

    // 게시판 메인 화면으로 이동
    private void goBoardMainActivity() {
        Intent i = new Intent(this, BoardActivity.class);
        startActivity(i);
        finish();
    }

    private View.OnClickListener mClicks = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            switch(view.getId()) {
                case R.id.btnGoogleSignIn:
                    googleSingIn();
                    break;
            }
        }
    };
    // 구글 로그인 처리
    private void googleSingIn() {
        Intent i = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(i, 1004);
    }

    private void firebaseAuthWIthGoogle(GoogleSignInAccount account) {
        // Firebase 인증
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),
                null);
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            // Firebase 로그인 성공
                            Toast.makeText(getBaseContext(), "Firebase 로그인 성공",
                                    Toast.LENGTH_SHORT).show();
                            // 메인화면으로 이동
                            goBoardMainActivity();
                        } else {
                            // 로그인 실패
                            Toast.makeText(getBaseContext(), "Firebase 로그인 실패",
                                    Toast.LENGTH_SHORT).show();
                            Log.w("TEST", "인증실패: "+task.getException());

                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 구글 로그인 버튼 응답
        if (requestCode == 1004) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // 구글 로그인 성공
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Toast.makeText(getBaseContext(), "로그인에 성공 하였습니다."
                        , Toast.LENGTH_SHORT).show();
                // Firebase 인증하러 가기
                firebaseAuthWIthGoogle(account);

            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }// end onCreate
}
