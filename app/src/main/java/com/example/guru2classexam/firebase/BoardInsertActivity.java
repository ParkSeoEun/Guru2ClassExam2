package com.example.guru2classexam.firebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.guru2classexam.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class BoardInsertActivity extends AppCompatActivity {

    public static final String STORAGE_DB_URL ="gs://swu-class-project.appspot.com";

    private ImageView mImgProfile;
    private EditText mEdtMemo;

    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance(STORAGE_DB_URL);
    private FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();

    private Uri mCaptureUri;
    //사진이 저장된 단말기상의 실제 경로
    public String mPhotoPath;
    //startActivityForResult() 에 넘겨주는 값, 이 값이 나중에 onActivityResult()로 돌아와서
    //내가 던진값인지를 구별할 때 사용하는 상수이다.
    public static final int REQUEST_IMAGE_CAPTURE = 200;

    private BoardBean mBoardBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_insert);

        //카메라를 사용하기 위한 퍼미션을 요청한다.
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        }, 0);

        mImgProfile = findViewById(R.id.imgTitle);
        mEdtMemo = findViewById(R.id.edtMemo);

        mBoardBean = (BoardBean)getIntent().getSerializableExtra(BoardBean.class.getName());
        if(mBoardBean != null) {
            mBoardBean.bmpTitle = getIntent().getParcelableExtra("titleBitmap");
            if(mBoardBean.bmpTitle != null) {
                mImgProfile.setImageBitmap(mBoardBean.bmpTitle);
            }
            mEdtMemo.setText(mBoardBean.contents);
        }

        mImgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });

        findViewById(R.id.btnReg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBoardBean == null) {
                    // 신규등록
                    upload();
                } else {
                    // 수정 업데이트
                    update();
                }
            }
        });
    } // end of onCreate)()

    // 게시물 수정
    private void update() {
        // 사진을 안찍었을 경우, DB만 업데이트
        if(mPhotoPath == null) {
            mBoardBean.contents = mEdtMemo.getText().toString();
            // DB 업로드
            DatabaseReference dbRef = mFirebaseDatabase.getReference();
            String uuid = getUserIdFromUUID(mBoardBean.userId);
            // 동일 ID로 데이터 수정
            dbRef.child("memo").child(uuid).child(mBoardBean.id).setValue(mBoardBean);
            Toast.makeText(this, "수정되었습니다", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // 사진을 찍었을 경우, 사진부터 업로드하고 DB 업데이트 한다
        StorageReference storageRef =  mFirebaseStorage.getReference();
        final StorageReference imagesRef = storageRef.child("images/" + mCaptureUri.getLastPathSegment());
        UploadTask uploadTask = imagesRef.putFile(mCaptureUri);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if(!task.isSuccessful()) {
                    throw task.getException();
                }
                return imagesRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                // 파일 업로드 완료 후 호출된다.
                // 기존 이미지 파일을 삭제한다.
                if(mBoardBean.imgName != null) {
                    try {
                        mFirebaseStorage.getReference().child("images").child(mBoardBean.imgName)
                                .delete();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                mBoardBean.imgUrl = task.getResult().toString();
                mBoardBean.imgName = mCaptureUri.getLastPathSegment();
                mBoardBean.contents = mEdtMemo.getText().toString();
                // 수정된 날짜로
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                mBoardBean.date = sdf.format(new Date());

                String uuid = getUserIdFromUUID(mBoardBean.userId);
                mFirebaseDatabase.getReference().child("memo").child(uuid).child(mBoardBean.id)
                        .setValue(mBoardBean);

                Toast.makeText(getBaseContext(),"수정 되었습니다",Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    // 새메모 등록
    private void upload() {

        if(mPhotoPath == null) {
            Toast.makeText(this, "사진을 찍어주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        // 사진부터 Storage 에 업로드
        StorageReference storageRef = mFirebaseStorage.getReference();
        final StorageReference imagesRef = storageRef.child("images/"+mCaptureUri.getLastPathSegment());
        // images/파일날짜.jpg

        UploadTask uploadTask = imagesRef.putFile(mCaptureUri);
        // 파일 업로드 실패에 따른 콜백 처리르 한다.
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if(!task.isSuccessful()) {
                    throw task.getException();
                }
                return imagesRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                // database upload 를 호출한다.
                uploadDB(task.getResult().toString(), mCaptureUri.getLastPathSegment());
            }
        });
    }

    private void uploadDB(String imgUri, String imgName) {
        // FireBase 데이터베이스에 메모를 등록한다,
        DatabaseReference dbRef = mFirebaseDatabase.getReference();
        String id = dbRef.push().getKey(); // key를 메모의 고유 ID로 사용한다.

        // 데이터베이스에 저장
        BoardBean boardBean = new BoardBean();
        boardBean.id = id;
        boardBean.userId = mFirebaseAuth.getCurrentUser().getEmail();
        boardBean.title = "메모 title";
        boardBean.contents = mEdtMemo.getText().toString();
        boardBean.imgUrl = imgUri;
        boardBean.imgName = imgName;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        boardBean.date = sdf.format(new Date());

        // 고유번호를 생성한다.
        String guid = getUserIdFromUUID(boardBean.userId);
        dbRef.child("memo").child( guid ).child(boardBean.id).setValue(boardBean);
        Toast.makeText(this, "게시물이 등록 되었습니다." , Toast.LENGTH_SHORT)
                .show();
        finish();
    }

    public static String getUserIdFromUUID(String userEmail) {
        long val = UUID.nameUUIDFromBytes(userEmail.getBytes()).getMostSignificantBits();
        return String.valueOf(val);
    }

    private void takePicture() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mCaptureUri = Uri.fromFile( getOutPutMediaFile() );
        } else {
            mCaptureUri = FileProvider.getUriForFile(this,
                    "com.example.guru2classexam", getOutPutMediaFile());
        }

        i.putExtra(MediaStore.EXTRA_OUTPUT, mCaptureUri);

        //내가 원하는 액티비티로 이동하고, 그 액티비티가 종료되면 (finish되면)
        //다시금 나의 액티비티의 onActivityResult() 메서드가 호출되는 구조이다.
        //내가 어떤 데이터를 받고 싶을때 상대 액티비티를 호출해주고 그 액티비티에서
        //호출한 나의 액티비티로 데이터를 넘겨주는 구조이다. 이때 호출되는 메서드가
        //onActivityResult() 메서드 이다.
        startActivityForResult(i, REQUEST_IMAGE_CAPTURE);
    }
    private File getOutPutMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "cameraDemo");
        if(!mediaStorageDir.exists()) {
            if(!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File file = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

        mPhotoPath = file.getAbsolutePath();

        return file;
    }
    private void sendPicture() {
        Bitmap bitmap = BitmapFactory.decodeFile(mPhotoPath);
        Bitmap resizedBmp = getResizedBitmap(bitmap, 4, 100, 100);

        bitmap.recycle();

        //사진이 캡쳐되서 들어오면 뒤집어져 있다. 이애를 다시 원상복구 시킨다.
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(mPhotoPath);
        } catch(Exception e) {
            e.printStackTrace();
        }
        int exifOrientation;
        int exifDegree;
        if(exif != null) {
            exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            exifDegree = exifOrientToDegree(exifOrientation);
        } else {
            exifDegree = 0;
        }
        Bitmap rotatedBmp = roate(resizedBmp, exifDegree);
        mImgProfile.setImageBitmap( rotatedBmp );
        //줄어든 이미지를 다시 저장한다
        saveBitmapToFileCache(resizedBmp, mPhotoPath);

        Toast.makeText(this, "사진경로: " + mPhotoPath, Toast.LENGTH_LONG).show();
    }

    private void saveBitmapToFileCache(Bitmap bitmap, String strFilePath) {

        File fileCacheItem = new File(strFilePath);
        OutputStream out = null;

        try
        {
            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                out.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private int exifOrientToDegree(int exifOrientation) {
        if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        }
        else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        }
        else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    private Bitmap roate(Bitmap bmp, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(),
                matrix, true);
    }

    //비트맵의 사이즈를 줄여준다.
    public static Bitmap getResizedBitmap(Bitmap srcBmp, int size, int width, int height){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = size;
        Bitmap resized = Bitmap.createScaledBitmap(srcBmp, width, height, true);
        return resized;
    }

    public static Bitmap getResizedBitmap(Resources resources, int id, int size, int width, int height){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = size;
        Bitmap src = BitmapFactory.decodeResource(resources, id, options);
        Bitmap resized = Bitmap.createScaledBitmap(src, width, height, true);
        return resized;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //카메라로부터 오는 데이터를 취득한다.
        if(resultCode == RESULT_OK) {
            if(requestCode == REQUEST_IMAGE_CAPTURE) {
                sendPicture();
            }
        }
    }
}
