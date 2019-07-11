package com.example.guru2classexam;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GoogleMapActivity extends AppCompatActivity {

    private SupportMapFragment mMapFragment;
    private LocationManager mLocationManager;
    private LatLng mCurPosLatLng;    //현재위치 저장 위도,경도 변수
    private int mBtnClickIndex = 0; //어떤 버튼의 index 가 클릭됐는지를 저장

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map);

        findViewById(R.id.btnMap1).setOnClickListener(mBtnClicks);
        findViewById(R.id.btnMap2).setOnClickListener(mBtnClicks);
        findViewById(R.id.btnMap3).setOnClickListener(mBtnClicks);

        mMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        //구글맵이 로딩이 완료되면 아래의 이벤트가 발생한다.
        mMapFragment.getMapAsync(mapReadyCallback);

        //GSP 가 켜져 있는지 확인한다.
        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if(!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //GSP 설정하는 Setting 화면으로 이동한다.
            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            i.addCategory(Intent.CATEGORY_DEFAULT);
            startActivity(i);
        }

        if(
             ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
             ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            )
        {
            return;
        }

        //GPS 위치를 0.1초마다 10m 간격범위안에서 이동하면 위치를 listener 로 보내주도록 등록한다.
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 10, locationListener);
        //WIFI 위치를 0.1초마다 10m 간격범위안에서 이동하면 위치를 listener 로 보내주도록 등록한다.
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 10, locationListener);
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //위치 변경시 위도, 경도 정보 update 수신
            mCurPosLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            Toast.makeText(getBaseContext(), "현재 위치가 갱신 되었습니다. " + mCurPosLatLng.latitude + ", " + mCurPosLatLng.longitude, Toast.LENGTH_SHORT).show();
            //구글맵을 현재 위치로 이동시킨다.
            mMapFragment.getMapAsync(mapReadyCallback);
            //현재 위치를 한번만 호출하기 위해 리스너 해지
            mLocationManager.removeUpdates(locationListener);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) { }

        @Override
        public void onProviderEnabled(String s) { }

        @Override
        public void onProviderDisabled(String s) { }
    };

    //버튼 클릭 이벤트
    private View.OnClickListener mBtnClicks = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btnMap1: //세종대왕
                    mBtnClickIndex = 1;
                    mCurPosLatLng = new LatLng(37.572820, 126.976935);
                    break;

                case R.id.btnMap2: //해운대 해수욕장
                    mBtnClickIndex = 2;
                    mCurPosLatLng = new LatLng(35.158927, 129.160786);
                    break;

                case R.id.btnMap3: //SWU
                    mCurPosLatLng = new LatLng(37.628096, 127.090543);
                    mBtnClickIndex = 3;
                    break;
            }//end switch
            mMapFragment.getMapAsync(mapReadyCallback); //map refresh
        }
    };

    //구글맵 로딩완료후 이벤트
    private OnMapReadyCallback mapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(final GoogleMap googleMap) {

            if(
               ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
               ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            )
            {
                return;
            }
            //현재버튼 추가
            googleMap.setMyLocationEnabled(true);
            //줌인 줌아웃 버튼 추가
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            //나침반 추가
            googleMap.getUiSettings().setCompassEnabled(true);

            //맵을 클릭했을 때 이벤트를 등록한다.
            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title("클릭한 장소 ");
                    markerOptions.snippet("위도:" + latLng.latitude + ", 경도: " + latLng.longitude);
                    googleMap.addMarker(markerOptions).showInfoWindow();
                }
            });

            //snippet 클릭시 마커삭제
            googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    marker.remove();
                }
            });

            if(mCurPosLatLng != null) {
                //깃발 표시
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(mCurPosLatLng);

                if(mBtnClickIndex == 1) {
                    //세종대왕
                    markerOptions.title("광화문");
                    markerOptions.snippet("세종대왕 동상");
                }
                else if(mBtnClickIndex == 2) {
                    markerOptions.title("해운대");
                    markerOptions.snippet("해운대 해수욕장 백사장");
                }
                else if(mBtnClickIndex == 3) {
                    markerOptions.title("서울여대");
                    markerOptions.snippet("서울여대 만주벌판(들판)");
               }

                googleMap.addMarker(markerOptions).showInfoWindow();

                //구글맵을 위도,경도 위치로 이동시킨다.
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(mCurPosLatLng));
            }
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
        }
    };

}
