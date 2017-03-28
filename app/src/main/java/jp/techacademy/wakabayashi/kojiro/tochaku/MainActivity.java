package jp.techacademy.wakabayashi.kojiro.tochaku;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.http.HttpResponseCache;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


//memo: GoogleMap追加 http://stackoverflow.com/questions/38878636/google-maps-in-middle-area-of-an-activity
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

//memo: 現在位置系追加　http://wisteriahill.sakura.ne.jp/CMS/WordPress/2016/08/04/google-maps-android-api-v2-lat-lon-alt/

import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;



/*
https://akira-watson.com/android/google-map-zoom-addmarker.html
 */


public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
        {

  //  private LocationManager locationManager;
    private final int REQUEST_PERMISSION = 10;

    SharedPreferences sp;
    TextView mUsername;


    //memo: preferenceから現在登録されているユーザーを受け取る為の変数
    String username;
    String email;
    String access_token;


    //memo: preferenceから現在登録されている目的地を受け取る為の変数
    String address;
    String latitude; //StringにしているけどFloat
    String longitude;//StringにしているけどFloat
    String destname;
    String destemail;

    //memo: Googlemap追加
    private GoogleMap mMap = null;
    private SupportMapFragment mapFragment;

    private LatLng latlng;
    private int width, height;
    private Double herelatitude, herelongitude;
    private Double destlatitude, destlongitude;

    //memo: 現在位置関連 LocationClient の代わりにGoogleApiClientを使います
    GoogleApiClient mGoogleApiClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("MainActivity", "onCreate()");
    //    textView = (TextView) findViewById(R.id.text_view);

        if(Build.VERSION.SDK_INT >= 23){
            checkPermission();
        }
        else{
            requestLocationPermission();
        }


        //memo: Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        //memo: PreferenceでLogin判定
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sp.registerOnSharedPreferenceChangeListener(this);

        //memo: ログインユーザー情報を取得しておく。
        username = sp.getString(Const.UnameKEY, "");
        email = sp.getString(Const.EmailKEY, "");
        access_token = sp.getString(Const.TokenKey, "");
        Log.d("Loginユーザー名", String.valueOf(username));


        //memo: 現在設定されている目的地の取得

        destname = sp.getString(Const.DestnameKEY, "");
        address = sp.getString(Const.DestaddressKEY, "");
        destemail = sp.getString(Const.DestemailKEY, "");
        latitude = sp.getString(Const.DestLatitudeKEY,"");
        longitude = sp.getString(Const.DestLongitudeKEY,"");
        Log.d("目的地名", String.valueOf(destname));
        Log.d("目的地住所", String.valueOf(address));
        Log.d("緯度", String.valueOf(latitude));
        Log.d("経度", String.valueOf(longitude));




        //memo: 開発用。ログインしているかどうかを判別しやすくするため。後で消す
        mUsername = (TextView) findViewById(R.id.username);
        mUsername.setText(String.valueOf(username));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //memo: ログインできていない場合はLogin画面へ
                if (username.equals("") && email.equals("") && access_token.equals("")) {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    //スタートボタンとして利用する予定
                    Snackbar.make(view, "スタートボタンとして利用予定です。まだ未実装", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }

    // 位置情報許可の確認
    public void checkPermission() {
        // 既に許可している
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
           Log.d("許可","Granted");


            mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            //memo: 位置情報取得のために必要GoogleApiClient.ConnectionCallbacks　GoogleApiClient.OnConnectionFailedListener
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            mGoogleApiClient.connect();



            // 測位開始
            Button buttonStart = (Button)findViewById(R.id.button_start);
            buttonStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            // 測位終了
            Button buttonStop = (Button)findViewById(R.id.button_stop);
            buttonStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

        }
        // 拒否していた場合
        else{
            requestLocationPermission();
        }
    }



    // 許可を求める
    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION);

        } else {
            Toast toast = Toast.makeText(this, "許可されないとアプリが実行できません", Toast.LENGTH_SHORT);
            toast.show();

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, REQUEST_PERMISSION);

        }
    }

    // 結果の受け取り
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("許可","Granted");
                return;

            } else {
                // それでも拒否された時の対応
                Toast toast = Toast.makeText(this, "これ以上なにもできません", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    // onResumeフェーズに入ったら接続
    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();


    }

    // onPauseで切断
    @Override
    public void onPause() {
        super.onPause();
       mGoogleApiClient.disconnect();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {


        // check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("debug", "permission granted");

            mMap = googleMap;
            // default の LocationSource から自前のsourceに変更する
          //  mMap.setLocationSource(this);
           // mMap.setMyLocationEnabled(true);
           // mMap.setOnMyLocationButtonClickListener(this);

            //map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
            // Add a marker in Sydney and move the camera
        /*
        LatLng sydney = new LatLng(-34, 151);
        map.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        map.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        */

            if (latitude.equals("") && longitude.equals("")) {

                //memo: defaultの地図
                LatLng JAPAN = new LatLng(36, 139);

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(JAPAN, 4));

                Toast.makeText(this, "目的地が設定されていません。", Toast.LENGTH_LONG).show();


            } else {

                //memo: defalutの地図を表示

                destlatitude = Double.parseDouble(latitude);
                destlongitude = Double.parseDouble(longitude);

                latlng = new LatLng(destlatitude, destlongitude);

                //memo: マーカをクリックした時にデフォルトで出てしまうGoogleMapへのツールバーを削除
                UiSettings us = mMap.getUiSettings();
                us.setMapToolbarEnabled(false);
                // 標準のマーカー

                setMarker(destlatitude, destlongitude);

                // アイコン画像をマーカーに設定
                //setIcon(herelatitude, herelongitude);

            }
        }else{
            Log.d("debug", "permission error");
            return;
        }

    }

    private void setMarker(double destlatitude, double destlongitude) {

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latlng);
        markerOptions.title(destname);
        mMap.addMarker(markerOptions);



        // ズーム
        zoomMap(destlatitude, destlongitude);


    }

    /*
    private void setIcon(double destlatitude, double destlongitude) {

        // マップに貼り付ける BitmapDescriptor生成
        //BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.droid);
        BitmapDescriptor descriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);

        // 貼り付設定
        GroundOverlayOptions overlayOptions = new GroundOverlayOptions();
        overlayOptions.image(descriptor);

        //　public GroundOverlayOptions anchor (float u, float v)
        // (0,0):top-left, (0,1):bottom-left, (1,0):top-right, (1,1):bottom-right
        overlayOptions.anchor(0.5f, 0.5f);

        // 張り付け画像の大きさ メートル単位
        // public GroundOverlayOptions	position(LatLng location, float width, float height)
        overlayOptions.position(latlng, 300f, 300f);

        // マップに貼り付け・アルファを設定
        GroundOverlay overlay = mMap.addGroundOverlay(overlayOptions);
        // ズーム
        zoomMap(herelatitude, herelongitude);

        // 透明度
        overlay.setTransparency(0.0F);

    }
    */
    private void zoomMap(double destlatitude, double destlongitude) {
        // 表示する東西南北の緯度経度を設定


        /*
        memo: 1 ドアップ　0.1　何も見えない　10 海？何も見えない　0.9
         */
        double south = destlatitude * (1 - 0.00005);
        double west = destlongitude * (1 - 0.00005);
        double north = destlatitude * (1 + 0.00005);
        double east = destlongitude * (1 + 0.00005);


        // LatLngBounds (LatLng southwest, LatLng northeast)
        LatLngBounds bounds = LatLngBounds.builder()
                .include(new LatLng(south, west))
                .include(new LatLng(north, east))
                .build();

        width = getResources().getDisplayMetrics().widthPixels;
        height = getResources().getDisplayMetrics().heightPixels;

        // static CameraUpdate.newLatLngBounds(LatLngBounds bounds, int width, int height, int padding)
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, 0));

    }

    //memo: preferencceの書き換えを検知するListener
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("変更", "MainActivityに書かれているLogです。");

        username = sp.getString(Const.UnameKEY, "");
        email = sp.getString(Const.EmailKEY, "");
        access_token = sp.getString(Const.TokenKey, "");
        //memo: 現在設定されている目的地の取得

        if(sp.getString(Const.DestnameKEY,"") != null) {
            destname = sp.getString(Const.DestnameKEY, "");
            address = sp.getString(Const.DestaddressKEY, "");
            destemail = sp.getString(Const.DestemailKEY, "");
            latitude = sp.getString(Const.DestLatitudeKEY, "");
            longitude = sp.getString(Const.DestLongitudeKEY, "");
            Log.d("目的地名", String.valueOf(destname));
            Log.d("目的地住所", String.valueOf(address));
            Log.d("目的地メールアドレス", String.valueOf(destemail));
            Log.d("緯度", String.valueOf(latitude));
            Log.d("経度", String.valueOf(longitude));

            //memo: 開発用。ログインしているかどうかを判別しやすくするため。後で消す
            mUsername = (TextView) findViewById(R.id.username);
            mUsername.setText(String.valueOf(username));

/*
            //memo: 目的地が変更されたら即座に変更
            if (latitude != null && longitude != null) {
                destlatitude = Double.parseDouble(latitude);
                destlongitude = Double.parseDouble(longitude);

                latlng = new LatLng(destlatitude, destlongitude);

                // 標準のマーカー
                setMarker(destlatitude, destlongitude);
            }
*/
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.item1) {
            if (username.equals("") && email.equals("") && access_token.equals("")) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onConnected(Bundle bundle) {


        Log.d("API","Connected??");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("debug", "permission granted");


            }

        else{
            Log.d("debug", "permission error");
            return;
        }

    }



    @Override
    public void onConnectionSuspended(int i) {
        Log.d("API","Suspended??");


    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("API","connectionFaild??");


    }

}
