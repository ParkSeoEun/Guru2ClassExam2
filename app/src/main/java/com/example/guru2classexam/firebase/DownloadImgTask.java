package com.example.guru2classexam.firebase;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.List;

public class DownloadImgTask extends AsyncTask<URL, Void, Bitmap> {

    private Context mContext;
    private WeakReference<ImageView> mImageView = null;
    private List<BoardBean> mBoardList;
    private int mPosition;

    // 생성자
    public DownloadImgTask(Context context, ImageView imageView, List<BoardBean> boardList,
                           int position) {
        mContext = context;
        mImageView = new WeakReference<>(imageView);
        mBoardList = boardList;
        mPosition = position;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected Bitmap doInBackground(URL... params) {
        URL imageURL = params[0];
        Bitmap downloadedBitmap = null;

        try{
            InputStream inputStream = imageURL.openStream();
            downloadedBitmap = BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return downloadedBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if(bitmap != null) {
            // 이미지 다운로드 성공
            mImageView.get().setImageBitmap(bitmap);
            // 리스트 갱신
            mBoardList.get(mPosition).bmpTitle = bitmap;
        }
    }
}
