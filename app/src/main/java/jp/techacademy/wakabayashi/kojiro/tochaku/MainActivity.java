package jp.techacademy.wakabayashi.kojiro.tochaku;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.http.HttpResponseCache;
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

import java.text.DateFormat;
import java.util.Date;



/*
https://akira-watson.com/android/google-map-zoom-addmarker.html
https://github.com/googlesamples/android-play-location/tree/master/LocationUpdates
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


    //memo: 現在位置を求めるための変数群

    protected static final String TAG = "MainActivity";
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

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
    protected TextView mLastUpdateTimeTextView;
    protected TextView mLatitudeTextView;
    protected TextView mLongitudeTextView;
    protected TextView mLocationInadequateWarning;

    // Labels.
    protected String mLatitudeLabel;
    protected String mLongitudeLabel;
    protected String mLastUpdateTimeLabel;

    protected LatLng currentlatlng;

    protected Boolean mRequestingLocationUpdates;
    protected String mLastUpdateTime;

    Marker currentMarker;
    MarkerOptions currentMarkerOptions = new MarkerOptions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("Debug", "onCreate()");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        ActionBar bar = this.getSupportActionBar();

// タイトルを設定
        toolbar.setTitle("タイトル");
        toolbar.setTitleTextColor(Color.WHITE);

// ナビゲーションアイコンの設定、クリック処理

        toolbar.findViewById(R.id.toolbar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("タグ","Clicked");



            }
        });


        if(isFirst){
            Log.d("","");
        }


        //memo: 位置情報取得のために必要GoogleApiClient.ConnectionCallbacks　GoogleApiClient.OnConnectionFailedListener
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        //memo: Permission 必要なし　map の表示　現状は超デフォルト本来はここで日本の地図にしたい
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //memo: Toolbar

        setSupportActionBar(toolbar);

        //memo: UI
        mStartUpdatesButton = (Button) findViewById(R.id.button_start);
        mStopUpdatesButton = (Button) findViewById(R.id.button_stop);
        mLatitudeTextView = (TextView) findViewById(R.id.latitude_text);
        mLongitudeTextView = (TextView) findViewById(R.id.longitude_text);
        mLastUpdateTimeTextView = (TextView) findViewById(R.id.last_update_time_text);
        mLocationInadequateWarning = (TextView) findViewById(R.id.location_inadequate_warning);

        mLatitudeTextView.setText("緯度");
        mLongitudeTextView.setText("経度");
        mLastUpdateTimeTextView.setText("最終取得時刻");

        // Set labels.
        mLatitudeLabel = getResources().getString(R.string.latitude_label);
        mLongitudeLabel = getResources().getString(R.string.longitude_label);
        mLastUpdateTimeLabel = getResources().getString(R.string.last_update_time_label);

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
        mStopUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Debug", "mStopUpdatesButton");
                mMap.clear();
                defaultMap();


                stopLocationUpdates();


            }
        });
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

           // isStart = true;

            //memo: Locationをリクエストするためのインターバルなどをセット
            createLocationRequest();
            buildLocationSettingsRequest();

            //memo: ここで計測Startされる
            if (!mRequestingLocationUpdates) {
                Log.d("debug", String.valueOf(mRequestingLocationUpdates));

                mRequestingLocationUpdates = true;
                setButtonsEnabledState();
                startLocationUpdates();


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
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(20000);//位置更新を受け取る間隔(msec)　3秒おき
        mLocationRequest.setFastestInterval(10000);//速くて１秒おき
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//正確さ優先
    }

    //memo: 続一定時間ごとに現在位置を取得する方法
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

        /*
        //memo: check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("debug", "permission granted");
            Log.d("debug", "onMapReady:"+String.valueOf(mRequestingLocationUpdates));
            if (latitude != "" && longitude != "") {


                liveMap();
                mMap.setMyLocationEnabled(true);

            } else {
                defaultMap();
            }

        } else {

            defaultMap();
            Log.d("debug", "permission error");
            Toast.makeText(this, "このアプリをご利用になるには許可が必要です。", Toast.LENGTH_LONG).show();
        }
        */


    }


    private void defaultMap() {

        UiSettings us = mMap.getUiSettings();
        us.setMapToolbarEnabled(false);

        LatLng JAPAN = new LatLng(36, 139);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(JAPAN, (float) 4.8));

    }

    private void liveMap(){

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


    }


    private void setMarker(double destlatitude, double destlongitude) {

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latlng);
        markerOptions.title(destname);
        mMap.addMarker(markerOptions);

        // ズーム
        zoomMap(destlatitude, destlongitude);
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
                setMarker(destlatitude, destlongitude);
            }
        }
    }


    protected void startLocationUpdates() {
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
                            // Show the dialog by calling startResolutionForResult(), and check the
                            // result in onActivityResult().
                            status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
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

    private void updateUI() {
        setButtonsEnabledState();
        updateLocationUI();
    }


    private void setButtonsEnabledState() {
        if (mRequestingLocationUpdates) {
            mStartUpdatesButton.setEnabled(false);
            mStopUpdatesButton.setEnabled(true);
        } else {
            mStartUpdatesButton.setEnabled(true);
            mStopUpdatesButton.setEnabled(false);
        }
    }

    /**
     * Sets the value of the UI fields for the location latitude, longitude and last update time.
     */
    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            mLatitudeTextView.setText(String.format("%s: %f", mLatitudeLabel,mCurrentLocation.getLatitude()));
            mLongitudeTextView.setText(String.format("%s: %f", mLongitudeLabel,mCurrentLocation.getLongitude()));
            mLastUpdateTimeTextView.setText(String.format("%s: %s", mLastUpdateTimeLabel,mLastUpdateTime));
        }
    }

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

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("API", "Connected??");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("Current_location", String.valueOf(mCurrentLocation));
            if (mCurrentLocation == null) {
                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                updateLocationUI();
                Log.d("Current_location", String.valueOf(mCurrentLocation));
            }
            if (mRequestingLocationUpdates) {
                Log.i(TAG, "in onConnected(), starting location updates");
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

       updateLocationUI();
       Toast.makeText(this, "現在地の変更を感知しました", Toast.LENGTH_LONG).show();

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
