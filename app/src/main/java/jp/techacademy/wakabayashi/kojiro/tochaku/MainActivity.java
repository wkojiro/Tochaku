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


    String user;
    String username;
    String email;
    String token;
    SharedPreferences sp;
    TextView mUsername;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*
        (new Thread(new Runnable() {
            @Override
            public void run() {

                sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                //this とはおそらくthis activityのこと ここはActivityの中だからthisでもいける。
                // getApplicationContext();　とも書ける。
                Log.d("user name",String.valueOf(sp));
                username = sp.getString(Const.UnameKEY, "");
                email = sp.getString(Const.EmailKEY, "");
                token = sp.getString(Const.TokenKey, "");
                Log.d("user name",String.valueOf(username));

                TextView mUsername = (TextView) findViewById(R.id.username);
                mUsername.setText(String.valueOf(username));

            }
        })).start();
        */

        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sp.registerOnSharedPreferenceChangeListener(this);

        //this とはおそらくthis activityのこと ここはActivityの中だからthisでもいける。
        // getApplicationContext();　とも書ける。
        Log.d("user name",String.valueOf(sp));
        username = sp.getString(Const.UnameKEY, "");
        email = sp.getString(Const.EmailKEY, "");
        token = sp.getString(Const.TokenKey, "");
        Log.d("user name",String.valueOf(username));

        mUsername = (TextView) findViewById(R.id.username);
        mUsername.setText(String.valueOf(username));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("user name",String.valueOf(username));
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Log.d("user name",String.valueOf(username));


                if (username == "" && email == "" && token == ""){
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    //スタートボタンとして利用する予定
                    Log.d("user name",String.valueOf(username));
                }


            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        mUsername.setText(String.valueOf(username));


    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // handle the preference change here
        Log.d("変更","あった");
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.item1) {
            if (username == "" && email == "" && token == ""){
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
