package jp.techacademy.wakabayashi.kojiro.tochaku;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    SharedPreferences sp;
    TextView mUsername;


    //memo: preferenceから現在登録されているユーザーを受け取る為の変数
    String username;
    String email;
    String access_token;


    //memo: preferenceから現在登録されている目的地を受け取る為の変数
    String address;
    String latitude;
    String longitude;
    String destname;
    String destemail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        Log.d("Loginユーザー名",String.valueOf(username));


        //memo: 現在設定されている目的地の取得

        destname = sp.getString(Const.DestnameKEY,"");
        address = sp.getString(Const.DestaddressKEY,"");
        destemail = sp.getString(Const.DestemailKEY,"");
        latitude = sp.getString(Const.DestLatitudeKEY,"");
        longitude = sp.getString(Const.DestLongitudeKEY,"");
        Log.d("目的地名",String.valueOf(destname));
        Log.d("目的地住所",String.valueOf(address));
        Log.d("緯度",String.valueOf(latitude));
        Log.d("経度",String.valueOf(longitude));

        //memo: 開発用。ログインしているかどうかを判別しやすくするため。後で消す
        mUsername = (TextView) findViewById(R.id.username);
        mUsername.setText(String.valueOf(username));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //memo: ログインできていない場合はLogin画面へ
                if (username.equals("") && email.equals("") && access_token.equals("")){
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


    //memo: preferencceの書き換えを検知するListener
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("変更","MainActivityに書かれているLogです。");

        username = sp.getString(Const.UnameKEY, "");
        email = sp.getString(Const.EmailKEY, "");
        access_token = sp.getString(Const.TokenKey, "");
        //memo: 現在設定されている目的地の取得

        destname = sp.getString(Const.DestnameKEY,"");
        address = sp.getString(Const.DestaddressKEY,"");
        destemail = sp.getString(Const.DestemailKEY,"");
        latitude = sp.getString(Const.DestLatitudeKEY,"");
        longitude = sp.getString(Const.DestLongitudeKEY,"");
        Log.d("目的地名",String.valueOf(destname));
        Log.d("目的地住所",String.valueOf(address));
        Log.d("目的地メールアドレス",String.valueOf(destemail));
        Log.d("緯度",String.valueOf(latitude));
        Log.d("経度",String.valueOf(longitude));

        //memo: 開発用。ログインしているかどうかを判別しやすくするため。後で消す
        mUsername = (TextView) findViewById(R.id.username);
        mUsername.setText(String.valueOf(username));
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
            if (username.equals("") && email.equals("") && access_token.equals("")){
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
