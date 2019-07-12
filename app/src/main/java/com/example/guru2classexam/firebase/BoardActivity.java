package com.example.guru2classexam.firebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.guru2classexam.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BoardActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase mFirebaseDB = FirebaseDatabase.getInstance();
    private ListView mListView;
    private List<BoardBean> mBoardList = new ArrayList<>();
    private BoardAdapter mBoardAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        mListView = findViewById(R.id.lstBoard);
        // 새메모 버튼
        findViewById(R.id.btnNewMemo).setOnClickListener(mClicks);
        // 로그아웃 버튼
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(mClicks);
        btnLogout.setText("로그아웃(" + mFirebaseAuth.getCurrentUser().getEmail()+")");

        // 최초 데이터 셋팅
        mBoardAdapter = new BoardAdapter(this, mBoardList);
        mListView.setAdapter(mBoardAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 데이터 취득
        String userEmail = mFirebaseAuth.getCurrentUser().getEmail();
        String uuid = BoardInsertActivity.getUserIdFromUUID(userEmail);
        mFirebaseDB.getReference().child("memo").child(uuid).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // 데이터를 받아와서 List에 저장
                        mBoardList.clear();

                        for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            BoardBean bean = snapshot.getValue(BoardBean.class);
                            mBoardList.add(0,bean);
                        }
                        // 바뀐 데이터로 Refresh 한다
                        if(mBoardAdapter != null) {
                            mBoardAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );
        // 어댑터 생성
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
