package jp.techacademy.wakabayashi.kojiro.tochaku;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
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
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

//memo: 現在位置系追加　http://wisteriahill.sakura.ne.jp/CMS/WordPress/2016/08/04/google-maps-android-api-v2-lat-lon-alt/

import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;



/*


https://akira-watson.com/android/google-map-zoom-addmarker.html
https://github.com/googlesamples/android-play-location/tree/master/LocationUpdates

View系の参考に
http://dev.classmethod.jp/smartphone/android/android-tips-25-google-maps-android-api-v2/#RetainMap
http://qiita.com/daisy1754/items/aa9ad75d1a84b745469b
http://qiita.com/nbkn/items/41b3dd5a86be6e2b57bf
http://qiita.com/droibit/items/3a4706ec01cb34672d12

2拠点系
http://qiita.com/a_nishimura/items/6c2642343c0af832acd4
https://akira-watson.com/android/trace-google-map.html
http://foonyan.sakura.ne.jp/wisteriahill/gmap_androidapiv2II_memo5/index.html
http://seesaawiki.jp/w/moonlight_aska/d/2%C3%CF%C5%C0%B4%D6%A4%CE%BA%C7%C3%BB%B5%F7%CE%A5%A4%F2%B5%E1%A4%E1%A4%EB
 */


public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {


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
    boolean isFirst = true;
    private GoogleMap mMap = null;

    private LatLng latlng;
    int width, height;
    private Double herelatitude, herelongitude;
    private Double destlatitude, destlongitude;

    //memo: 現在位置関連 LocationClient の代わりにGoogleApiClientを使います
    GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError = false;

    //memo: 計測スタートで切り替える
    boolean isStart = false;


    //memo: ボタンの状態管理(

    Integer mStatus = 0;
    //0:現在位置を取得します（現在位置を取得＆目的地があればセットされた名前を表示）　　
    //1:出発します（地図や距離を表示して計測スタート）
    //2:停止します（地図をDefaultに切り替えて、
    //3:停止中（０へ）


    //memo: 現在位置を求めるための変数群
    protected static final String TAG = "MainActivity";
    protected static final int REQUEST_CHECK_SETTINGS = 123; //適当なリクエストコード
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Bundle にStoreしているやつを読み込む時のために使うらしい　Keys for storing activity state in the Bundle.(未実装）
    protected final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    protected final static String KEY_LOCATION = "location";
    protected final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";

    protected LocationRequest mLocationRequest;
    protected LocationSettingsRequest mLocationSettingsRequest;
    protected Location mCurrentLocation;

    private final int REQUEST_PERMISSION = 10;
    // UI Widgets.
    protected Button mStartUpdatesButton;
    protected Button mStopUpdatesButton;

    /*
    protected TextView mLastUpdateTimeTextView;
    protected TextView mLatitudeTextView;
    protected TextView mLongitudeTextView;
    protected TextView mLocationInadequateWarning;
    */
    protected TextView mDestTextView;

    // Labels.
    protected String mLatitudeLabel;
    protected String mLongitudeLabel;
    protected String mLastUpdateTimeLabel;

    protected LatLng currentlatlng;

    //memo: Tracks the status of the location updates request. Value changes when the user presses the Start Updates and Stop Updates buttons.
    protected Boolean mRequestingLocationUpdates;
    protected String mLastUpdateTime;

    Marker destmarker;
    Marker currentMarker;
    MarkerOptions currentMarkerOptions = new MarkerOptions();
    Polyline polylineFinal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("Debug", "onCreate()");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("到着予報");
        toolbar.setTitleTextColor(Color.WHITE);
        // ナビゲーションアイコンの設定、クリック処理
        /*
        toolbar.findViewById(R.id.toolbar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("タグ", "Clicked");
            }
        });
        */
        setSupportActionBar(toolbar);

        //memo: 位置情報取得のために必要GoogleApiClient.ConnectionCallbacks　GoogleApiClient.OnConnectionFailedListener
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        //memo: Permission 必要なし　map の表示　
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //memo: UI
        mStartUpdatesButton = (Button) findViewById(R.id.button_start);
        mStopUpdatesButton = (Button) findViewById(R.id.button_stop);
        /*
        mLatitudeTextView = (TextView) findViewById(R.id.latitude_text);
        mLongitudeTextView = (TextView) findViewById(R.id.longitude_text);
        mLastUpdateTimeTextView = (TextView) findViewById(R.id.last_update_time_text);
        mLocationInadequateWarning = (TextView) findViewById(R.id.location_inadequate_warning);
        */
        mDestTextView = (TextView) findViewById(R.id.dest_text);

        /*
        mLatitudeTextView.setText("緯度");
        mLongitudeTextView.setText("経度");
        mLastUpdateTimeTextView.setText("最終取得時刻");

        // Set labels.
        mLatitudeLabel = getResources().getString(R.string.latitude_label);
        mLongitudeLabel = getResources().getString(R.string.longitude_label);
        mLastUpdateTimeLabel = getResources().getString(R.string.last_update_time_label);
        */

        //memo: true
        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";

        //memo: PreferenceでLogin判定
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sp.registerOnSharedPreferenceChangeListener(this);

        //memo: ログインユーザー情報を取得しておく。
        username = sp.getString(Const.UnameKEY, "");
        email = sp.getString(Const.EmailKEY, "");
        access_token = sp.getString(Const.TokenKey, "");

        //memo: 現在設定されている目的地の取得
        destname = sp.getString(Const.DestnameKEY, "");
        address = sp.getString(Const.DestaddressKEY, "");
        destemail = sp.getString(Const.DestemailKEY, "");
        latitude = sp.getString(Const.DestLatitudeKEY, "");
        longitude = sp.getString(Const.DestLongitudeKEY, "");

        //memo: 開発用。ログインしているかどうかを判別しやすくするため。後で消す
        mUsername = (TextView) findViewById(R.id.username);
        mUsername.setText(String.valueOf(username));

        //memo: Buttonを実装
        mStartUpdatesButton.setText("現在位置を取得");
        mStartUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (username.equals("") && email.equals("") && access_token.equals("")) {
                    Toast toast = Toast.makeText(MainActivity.this, "会員登録またはログインしてください。", Toast.LENGTH_LONG);
                    toast.show();

                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else if (longitude.equals("") && latitude.equals("")) {
                    Toast toast = Toast.makeText(MainActivity.this, "目的地を設定してください。", Toast.LENGTH_LONG);
                    toast.show();

                    Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                    startActivity(intent);

                } else {
                    checkPermission();
                }
            }
        });
        mStopUpdatesButton.setVisibility(View.GONE);
        mStopUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Debug", "mStopUpdatesButton");
                mStatus= 0;
                mMap.clear();
                updateUI();
                defaultMap();


                stopLocationUpdates();

                //updateUI();


            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //memo: ログインできていない場合はLogin画面へ
                if (username.equals("") && email.equals("") && access_token.equals("")) {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    //スタートボタンとして利用する予定
                    Intent intent = new Intent(getApplicationContext(), DestActivity.class);
                    startActivity(intent);

                    Snackbar.make(view, "目的地を登録", Snackbar.LENGTH_LONG).setAction("Action", null).show();

                }
            }
        });
    }

    //memo: Permissionの状態で切り分け　ここのシーケンスがいけていない（Permissionがあるか、ログインしているか、目的地が設定されているかを）
    public void checkPermission() {
        // 既に許可している
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("Debug", "CheckPermission");



            //memo: Locationをリクエストするためのインターバルなどをセット
            createLocationRequest();
            buildLocationSettingsRequest();

            //memo: ここで計測Startされる　こいつ→startLocationUpdates();
            /*requests start of location updates. Does nothing if
            * updates have already been requested.
            */
            if (!mRequestingLocationUpdates) {
                Log.d("debug", String.valueOf(mRequestingLocationUpdates));
                //memo:trueはボタンが押された状態
                mRequestingLocationUpdates = true;
                setButtonsEnabledState();
                startLocationUpdates();
            }


            switch (mStatus){
                case 0:
                    break;
                case 1:

                    break;
                case 2:

                    break;

            }
        }
        // 拒否していた場合
        else {
            requestLocationPermission();
        }
    }


    //memo: 許可を求める
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

    //memo: Permission結果の受け取り
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("許可", "Granted");
                //memo: 目的地を設定するよう促す
                Toast toast = Toast.makeText(this, "目的地を設定してください", Toast.LENGTH_SHORT);
                toast.show();
                //memo: Permissionが取れたらそのまま目的地設定画面に
                //  Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
                //  startActivity(intent);


                //isFirst = false;
                return;

            } else {
                // それでも拒否された時の対応
                Toast toast = Toast.makeText(this, "これ以上なにもできません", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    //memo: 一定時間ごとに現在位置を取得する方法
    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(20000);//位置更新を受け取る間隔(msec)　２０秒おき
        mLocationRequest.setFastestInterval(10000);//速くて10 秒おき
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//正確さ優先
    }

    //memo: 位置情報取得のデバイス側の設定を確認するリクエスト？
    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.setAlwaysShow(true); //added 20170330 http://qiita.com/nbkn/items/41b3dd5a86be6e2b57bf
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
        Toast.makeText(this, "buildLocationSettingRequest fired", Toast.LENGTH_LONG).show();
        Log.d("buildLocationSettingRequest", "発火している？");
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    // memo: onResume(= Another Activity comes into the foreground)フェーズに入ったら接続
    @Override
    protected void onResume() {
        super.onResume();
        //  isFirst = false;

        /*
        onPauseですでにConnectionが切られているからほとんどの場合、StartLocationUpdatesは発火しないはず。
        mGoogleApiClient.isConnected() 且つ　mRequestingLocationUpdatesがTrue状態の場合、startLocationUpdatesが実行される。

         */

        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        updateUI();
    }

    //memo: onPause(= Another Activity comes into the foreground = this activity goes to the background)フェーズに入ったら接続で切断
    @Override
    public void onPause() {
        super.onPause();
        // mGoogleApiClient.disconnect();
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
        // mMap.clear();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("onMapReady", "when do you call me?");

        mMap = googleMap;
        defaultMap();



    }


    private void defaultMap() {

        UiSettings us = mMap.getUiSettings();
        us.setMapToolbarEnabled(false);

        LatLng JAPAN = new LatLng(36, 139);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(JAPAN, (float) 4.8));

        mDestTextView.setVisibility(View.INVISIBLE);

    }

    private void liveMap() {
        mDestTextView.setVisibility(View.VISIBLE);
        if (currentMarker != null) {
            currentMarker.remove();
        }

        switch (mStatus){
            case 0:
             //memo: 現在位置を取得する　目的地があればそれをTextViewに表示する
                if(destname != null){
                    mDestTextView.setText("目的地に［"+destname+"］がセットされました。目的地を変更するには［設定］画面から変更できます。");
                }
                currentlatlng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLng(currentlatlng));
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mMap.setMyLocationEnabled(true);

                mStatus = 1;
                Toast.makeText(this,"ステータス"+String.valueOf(mStatus),Toast.LENGTH_LONG).show();
                break;

            case 1:
            //memo: 全体図を表示する

                // 設定の取得
                UiSettings settings = mMap.getUiSettings();
                settings.setZoomControlsEnabled(true);

                mDestTextView.setText("目的地に［"+destname+"］がセットされました。目的地を変更するには［設定］画面から変更できます。");

                //memo: 目的地をセット
                destlatitude = Double.parseDouble(latitude);
                destlongitude = Double.parseDouble(longitude);

                latlng = new LatLng(destlatitude, destlongitude);
                setMarker(destlatitude, destlongitude);

                //memo:　現在位置をセット
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mMap.setMyLocationEnabled(true);
                currentlatlng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                currentMarkerOptions.position(currentlatlng);
                currentMarkerOptions.title("現在位置");
                currentMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                currentMarker = mMap.addMarker(currentMarkerOptions);


               // mMap.animateCamera(CameraUpdateFactory.newLatLng(currentlatlng));

                //memo:　目的地と現在位置に線を引く（Routeでは無いからあんまり意味ない）
                PolylineOptions options = new PolylineOptions();
                options.add(currentlatlng); // 東京
                options.add(latlng); // ロサンゼルス
                options.color(0xcc00ffff);
                options.width(10);
                // options.geodesic(true); // 測地線で表示
                polylineFinal = mMap.addPolyline(options);


                //memo:　目的地と現在位置の距離を取る
                float[] results = new float[1];
                Location.distanceBetween(destlatitude, destlongitude, mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), results);
                Toast.makeText(getApplicationContext(), "距離：" + ( (Float)(results[0]/1000) ).toString() + "Km", Toast.LENGTH_LONG).show();

                String destance = String.valueOf(results[0]/1000);

                mDestTextView.setText("目的地までの距離：" + destance + "Km");

/*
                LatLngBounds destmap = new LatLngBounds(
                     // new LatLng(destlongitude, destlatitude), new LatLng(mCurrentLocation.getLongitude(),mCurrentLocation.getLatitude()));
                     new LatLng(36, 113), new LatLng(-10, 154));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destmap.getCenter(), 10));
                */


                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(destmarker.getPosition());
                builder.include(currentMarker.getPosition());
                LatLngBounds bounds = builder.build();
                mMap.setPadding( 50,250,50,250); //   left,        top,       right,  bottom
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 120);

                mMap.moveCamera(cu);

                new commingmail().execute(destname,destemail, String.valueOf(mCurrentLocation.getLatitude()),String.valueOf(mCurrentLocation.getLongitude()));

                mStatus=2;

                break;

            case 2:
                //memo: 程よいズームにする
                //memo:目的地がないということはこの段階ではない、ということにしないといけない。

                // 設定の取得
                settings = mMap.getUiSettings();
                settings.setZoomControlsEnabled(true);

                //memo: 目的地をセット
                destlatitude = Double.parseDouble(latitude);
                destlongitude = Double.parseDouble(longitude);

                latlng = new LatLng(destlatitude, destlongitude);
                setMarker(destlatitude, destlongitude);

                //memo:　現在位置をセット
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mMap.setMyLocationEnabled(true);
                currentlatlng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                currentMarkerOptions.position(currentlatlng);
                currentMarkerOptions.title("現在位置");
                currentMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                currentMarker = mMap.addMarker(currentMarkerOptions);

                polylineFinal.remove();


                //memo:　目的地と現在位置の距離を取る
                results = new float[1];
                Location.distanceBetween(destlatitude, destlongitude, mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), results);
                Toast.makeText(getApplicationContext(), "距離：" + ( (Float)(results[0]/1000) ).toString() + "Km", Toast.LENGTH_LONG).show();

                destance = String.valueOf(results[0]/1000);

                mDestTextView.setText("目的地までの距離：" + destance + "Km");

                zoomMap(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());



                break;

        }
        updateUI();


  /*
        mDestTextView.setText("aaaとか");
        //memo: 地図の中心を現在位置にしてみる
        currentlatlng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        // mMap.animateCamera(CameraUpdateFactory.newLatLng(curr));

       // currentMarkerOptions.position(currentlatlng);
       // currentMarkerOptions.title("現在位置");
        //currentMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(currentlatlng));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        if(destname != null){

            mDestTextView.setText("目的地に"+destname+"がセットされました。");
        }


        //currentMarker = mMap.addMarker(currentMarkerOptions);
        zoomMap(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());


        //memo: Preferenceから取得した情報から目的地を中心とした地図をつくる
        destlatitude = Double.parseDouble(latitude);
        destlongitude = Double.parseDouble(longitude);

        latlng = new LatLng(destlatitude, destlongitude);

        //memo: マーカをクリックした時にデフォルトで出てしまうGoogleMapへのツールバーを削除
        UiSettings us = mMap.getUiSettings();
        us.setMapToolbarEnabled(false);


        //memo: 標準のマーカー
        setMarker(destlatitude, destlongitude);

        //memo: アイコン画像をマーカーに設定
        //setIcon(herelatitude, herelongitude);

*/
     /*

        ボタンを押した時に計測スタートできる状態（つまり、目的地も設定されている状態）であるため、ここでは

        ・２つの地点の中間点からズームされた地図を出す
        ・２つの距離を計算しViewに出す。
        ・メールが送信される。




         */



    }

    //URL
    //     https://rails5api-wkojiro1.c9users.io/trackings.json?email=test00@test.com&token=1:NzjRgLCTwpd9ED7HoLTz
/*
        {
            "id": 2,
                "destname": "浅草寺",
                "destemail": "wkojiro22@gmail.com",
                "destaddress": "台東区浅草２丁目３−1",
                "nowlatitude": 35.714765,
                "nowlongitude": 139.796655,
                "created_at": "2017-03-31T12:42:49.982Z",
                "updated_at": "2017-03-31T12:42:49.982Z",
                "url": "https://rails5api-wkojiro1.c9users.io/trackings/2.json"
        }
*/






    private void setMarker(double destlatitude, double destlongitude) {

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latlng);
        markerOptions.title(destname);
        destmarker = mMap.addMarker(markerOptions);

        // ズーム
        //zoomMap(destlatitude, destlongitude);
    }

    private void zoomMap(double destlatitude, double destlongitude) {
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


    private class commingmail extends AsyncTask<String, Void, Void>{
        @Override
        protected Void doInBackground(String... params) {

            PostMail(params);

            return null;
        }

        @Override
        protected void onPostExecute(Void result){



        }
    }

    public String PostMail(String[] params){
        HttpURLConnection con = null;//httpURLConnectionのオブジェクトを初期化している。
        BufferedReader reader = null;
        StringBuilder jsonData = new StringBuilder();
        String urlString = "https://rails5api-wkojiro1.c9users.io/trackings.json?email="+ email +"&token="+ access_token +"";

        InputStream inputStream = null;
        String result = "";

        final String json =
                "{" +
                        "\"destname\":\"" + params[0] + "\"," +
                        "\"destemail\":\"" + params[1] + "\"," +
                        "\"destaddress\":\"\"," +
                        "\"nowlatitude\":\"" + params[2] + "\"," +
                        "\"nowlongitude\":\"" + params[3] + "\"" +
                 "}";

        try {
            URL url = new URL(urlString); //URLを生成
            con = (HttpURLConnection) url.openConnection(); //HttpURLConnectionを取得する
            con.setRequestMethod("POST");
            con.setInstanceFollowRedirects(false); // HTTP リダイレクト (応答コード 3xx の要求) を、この HttpURLConnection インスタンスで自動に従うかどうかを設定します。
            con.setRequestProperty("Accept-Language", "jp");
            con.setDoOutput(true); //この URLConnection の doOutput フィールドの値を、指定された値に設定します。→イマイチよく理解できない（URL 接続は、入力または出力、あるいはその両方に対して使用できます。URL 接続を出力用として使用する予定である場合は doOutput フラグを true に設定し、そうでない場合は false に設定します。デフォルトは false です。）
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            // リスエストの送信
            OutputStream os = con.getOutputStream(); //この接続に書き込みを行う出力ストリームを返します
            con.connect();
            // con.getResponseCode();


            PrintStream ps = new PrintStream(os); //行の自動フラッシュは行わずに、指定のファイルで新しい出力ストリームを作成します。
            ps.print(json);// JsonをPOSTする
            ps.close();
            final int status = con.getResponseCode();
            Log.d("結果",String.valueOf(status));
            if(status == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));//デフォルトサイズのバッファーでバッファリングされた、文字型入力ストリームを作成します。
                String line = reader.readLine();
                while (line != null) {
                    jsonData.append(line);
                    line = reader.readLine();
                }
                System.out.println(jsonData.toString());
            }
            con.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d("メール送信", "Postしてみました");
        // mProgress.dismiss();

        result = "OK";



        return result;
    }


    //memo: preferencceの書き換えを検知するListener
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("変更", "MainActivityに書かれているLogです。");

        username = sp.getString(Const.UnameKEY, "");
        email = sp.getString(Const.EmailKEY, "");
        access_token = sp.getString(Const.TokenKey, "");
        //memo: 現在設定されている目的地の取得

        if (sp.getString(Const.DestnameKEY, "") != null) {
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

            //memo: 目的地が変更されたら即座に変更
            if (latitude != "" && longitude != "") {
                destlatitude = Double.parseDouble(latitude);
                destlongitude = Double.parseDouble(longitude);

                latlng = new LatLng(destlatitude, destlongitude);
                Log.d("debug", "onSharedPreferenceChangedListner_setMarkerが呼ばれる");
                // 標準のマーカー
                //setMarker(destlatitude, destlongitude);
            }
        }
    }

//memo: ここからLocationUpdates(定期取得がはじまる。onConnectedしておいて、startLocationUpdatesで要求を出したのちに
// LocationChangedで居場所が返ってくるという流れ）
    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        // 1. ユーザが必要な位置情報設定を満たしているか確認する
        LocationServices.SettingsApi.checkLocationSettings(
                mGoogleApiClient,
                mLocationSettingsRequest
        ).setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {

                final Status status = locationSettingsResult.getStatus();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // 設定が有効になっているので現在位置を取得する
                        Log.i(TAG, "All location settings are satisfied.");

                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            Log.d("TODo", "Consider Calling");
                            return;
                        }
                        LocationServices.FusedLocationApi.requestLocationUpdates(
                                mGoogleApiClient, mLocationRequest, MainActivity.this);

                        Log.d("TODoの次", "RequestLocationUpdatesをやっています");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                "location settings ");
                        try {
                            // 2. ユーザに位置情報設定を変更してもらうためのダイアログを表示する
                            // Show the dialog by calling startResolutionForResult(), and check the
                            // result in onActivityResult().
                            status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // 位置情報が取得できず、なおかつその状態からの復帰も難しい時呼ばれるらしい
                        String errorMessage = "Location settings are inadequate, and cannot be " +
                                "fixed here. Fix in Settings.";
                        Log.e(TAG, errorMessage);
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        mRequestingLocationUpdates = false;
                }
                updateUI();
            }
        });
    }


    //memo:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                // ユーザのダイアログに対する応答をここで確認できる
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        // Nothing to do. startLocationupdates() gets called in onResume again.
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        updateUI();
                        break;
                }
                break;
        }
    }


    private void updateUI() {
        setButtonsEnabledState();
        updateLocationUI();
    }


    private void setButtonsEnabledState() {

        /*
        if (mRequestingLocationUpdates) {
            mStartUpdatesButton.setEnabled(false);
            mStopUpdatesButton.setEnabled(true);
        } else {
            mStartUpdatesButton.setEnabled(true);
            mStopUpdatesButton.setEnabled(false);
        }*/
        switch (mStatus){
            case 0:
                mStopUpdatesButton.setVisibility(View.GONE);
                mStartUpdatesButton.setVisibility(View.VISIBLE);
                mStartUpdatesButton.setText("現在位置を取得");
                break;
            case 1:
                mStartUpdatesButton.setText("出発します！");
                break;
            case 2:
                mStopUpdatesButton.setText("停止");
                mStopUpdatesButton.setVisibility(View.VISIBLE);
                mStartUpdatesButton.setVisibility(View.GONE);
                break;


        }
    }

    /**
     * Sets the value of the UI fields for the location latitude, longitude and last update time.
     */
    private void updateLocationUI() {
        /*
        if (mCurrentLocation != null) {
            mLatitudeTextView.setText(String.format("%s: %f", mLatitudeLabel,mCurrentLocation.getLatitude()));
            mLongitudeTextView.setText(String.format("%s: %f", mLongitudeLabel,mCurrentLocation.getLongitude()));
            mLastUpdateTimeTextView.setText(String.format("%s: %s", mLastUpdateTimeLabel,mLastUpdateTime));
        }*/
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                mRequestingLocationUpdates = false;
                setButtonsEnabledState();
            }
        });
    }


    //memo: Bundle bundle から connectionHintに変えて見た
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d("API", "Connected??");



        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        //
        // Because we cache the value of the initial location in the Bundle, it means that if the
        // user launches the activity,
        // moves to a new location, and then changes the device orientation, the original location
        // is displayed as the activity is re-created.



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            //memo: パーミッションがあるが、ボタンが押されたタイミングか？
            if (mCurrentLocation == null) {

                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

                /*

                この段階では、APIがConnectされて、要求が出された段階？
                よくわからないが、取得できないときがあるってか。そりゃおちる。




                if(mCurrentLocation != null){

                    mMap.setMyLocationEnabled(true);
                    liveMap();
                } else {

                    Toast.makeText(this,"Location情報が取得できませんでした。", Toast.LENGTH_LONG).show();
                    Log.d("debug","mCurrentLocation still (null");

                }
                */


                updateLocationUI();
                Log.d("Current_location", String.valueOf(mCurrentLocation));
            }

            //memo: startLocationUpdatesとは、計測開始するということ。計測中であれば計測開始ということ？これいる？これはどう言う状態だ？
            if (mRequestingLocationUpdates) {
                Log.i(TAG, "in onConnected(), starting location updates");
                Log.d("debug", "onConnected_mRequestingLocationUpdates");
                startLocationUpdates();
            }
        } else {
            Log.d("debug", "permission error");
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("API", "Suspended??");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("API", "connectionFaild??");
    }

    @Override
    public void onLocationChanged(Location location) {

        if (currentMarker != null) {
            currentMarker.remove();
        }


        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

        liveMap();

        /*
        //memo: 地図の中心を現在位置にしてみる
        currentlatlng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        // mMap.animateCamera(CameraUpdateFactory.newLatLng(curr));

        currentMarkerOptions.position(currentlatlng);
        currentMarkerOptions.title("現在位置");
        currentMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(currentlatlng));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

        }

        currentMarker = mMap.addMarker(currentMarkerOptions);
      */
       updateLocationUI();
       Toast.makeText(this, "現在地の変更を感知しました"+ mStatus , Toast.LENGTH_LONG).show();


    }


    //memo: 右上のメニュー
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


}
