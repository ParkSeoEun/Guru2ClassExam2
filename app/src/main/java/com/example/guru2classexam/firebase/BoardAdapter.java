package com.example.guru2classexam.firebase;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.guru2classexam.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.net.URL;
import java.util.List;

public class BoardAdapter extends BaseAdapter {

    private Context mContext;
    private List<BoardBean> mBoardList;

    public BoardAdapter(Context context, List<BoardBean> boardList) {
        mContext = context;
        mBoardList = boardList;
    }

    @Override
    public int getCount() {
        return mBoardList.size();
    }

    @Override
    public Object getItem(int i) {
        return mBoardList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater  inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.view_board_item, null);

        ImageView imgTitle = view.findViewById(R.id.imgTitle);
        TextView txtTitle = view.findViewById(R.id.txtTitle);
        TextView txtDate = view.findViewById(R.id.txtDate);
        Button btnModify = view.findViewById(R.id.btnModify);
        Button btnDel = view.findViewById(R.id.btnDel);

        final BoardBean boardBean= mBoardList.get(i);

        // imgTitle의 이미지를 표시할 때는 원격서버에 있는 이미지임을 비동기로 표시한다.
        try {
            if(boardBean.bmpTitle == null) {
                new DownloadImgTask(mContext, imgTitle, mBoardList, i).execute(new URL(boardBean.imgUrl));
            } else {
                imgTitle.setImageBitmap(boardBean.bmpTitle);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        txtTitle.setText(boardBean.title);
        txtDate.setText(boardBean.date);

        btnModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(mContext, BoardInsertActivity.class);
                i.putExtra(BoardBean.class.getName(),boardBean);
                i.putExtra("titleBitmap", boardBean.bmpTitle);
                mContext.startActivity(i);

            }
        });
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("삭제");
                builder.setMessage("삭제 하시겠습니까?");
                builder.setNegativeButton("아니오", null);
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                        String uuid = BoardInsertActivity.getUserIdFromUUID(email);

                        // DB에서 삭제처리
                        FirebaseDatabase.getInstance().getReference().child("memo").child(uuid)
                        .child(boardBean.id).removeValue();
                        // Storage 삭제처리
                        if(boardBean.imgName != null) {
                            try {
                                FirebaseStorage.getInstance().getReference().child("images")
                                        .child(boardBean.imgName).delete();
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                        Toast.makeText(mContext, "삭제 되었습니다.", Toast.LENGTH_SHORT)
                                .show();
                    }
                });
                builder.create().show();
            }
        });

        return view;
    }
}
