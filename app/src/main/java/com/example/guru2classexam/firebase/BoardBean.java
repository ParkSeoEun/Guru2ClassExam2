package com.example.guru2classexam.firebase;

import android.graphics.Bitmap;

import java.io.Serializable;

public class BoardBean implements Serializable { //Bean자체를 putExtra 할 때는 implement serialli....해야됨

    public String id;
    public String userId; // 이메일
    public String imgUrl;
    public String imgName;
    public String title;
    public String contents;
    public String date;
    public transient Bitmap bmpTitle; // 제외
}
