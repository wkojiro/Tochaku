package jp.techacademy.wakabayashi.kojiro.tochaku;

/*



 */

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;

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
import java.util.ArrayList;
import java.util.Map;

public class SettingActivity extends AppCompatActivity {

    //パーツの定義
    TextView mUserNameText;
    TextView mEmailText;
    TextView mDestCountText;
    Button mProfileButton;
    Button mLogoutButton;
    private ListView mListView;


    //パーツから受け取るためのパラメータ



    //GetのResponseを受けるパラメータ
    static Dest dest;
    private ArrayList<Dest> mDestArrayList;
    private DestsListAdapter mDestAdapter;

    ProgressDialog mProgress;

    //API通信のための会員Email及びToken(Preferenseから取得）
    static String username;
    static String email;
    static String token;

    //Responseを受け取るためのパラメータ

    static String res_destid;
    static String res_destname;
    static String res_destemail;
    static String res_destaddress;
    static Float res_destlatitude;
    static Float res_destlongitude;



    public static String GetdestList(){
        HttpURLConnection con = null;//httpURLConnectionのオブジェクトを初期化している。
        BufferedReader reader = null;
        StringBuilder jsonData = new StringBuilder();
        String urlString = "https://rails5api-wkojiro1.c9users.io/destinations.json?email="+ email +"&token="+ token +"";

        InputStream inputStream = null;
        String result = "";




        try {



            URL url = new URL(urlString); //URLを生成
            con = (HttpURLConnection) url.openConnection(); //HttpURLConnectionを取得する
            con.setRequestMethod("GET");
            con.setInstanceFollowRedirects(false); // HTTP リダイレクト (応答コード 3xx の要求) を、この HttpURLConnection インスタンスで自動に従うかどうかを設定します。
            con.setRequestProperty("Accept-Language", "jp");
           // con.setDoInput(true); //この URLConnection の doOutput フィールドの値を、指定された値に設定します。→イマイチよく理解できない（URL 接続は、入力または出力、あるいはその両方に対して使用できます。URL 接続を出力用として使用する予定である場合は doOutput フラグを true に設定し、そうでない場合は false に設定します。デフォルトは false です。）
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");


            // //////////////////////////////////////
            // リスエストの送信
            // //////////////////////////////////////
            InputStream is = con.getInputStream(); //この接続に書き込みを行う出力ストリームを返します
            con.connect();

            final int status = con.getResponseCode();
            Log.d("結果",String.valueOf(status));
            if(status == HttpURLConnection.HTTP_OK){

                //多分ここからResponseのための器をつくっている。
                //戻り値の指定をしないと動かないのかな？

                reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));//デフォルトサイズのバッファーでバッファリングされた、文字型入力ストリームを作成します。
                String line = reader.readLine();
                while (line != null) {
                    jsonData.append(line);
                    line = reader.readLine();
                }

                System.out.println(jsonData.toString());

            }
            //con.getResponseCode();


/*
            PrintStream ps = new PrintStream(os); //行の自動フラッシュは行わずに、指定のファイルで新しい出力ストリームを作成します。
            ps.print(json);// JsonをPOSTする
            ps.close();
*/


            //多分ここからResponseのための器をつくっている。
            //戻り値の指定をしないと動かないのかな？
/*
            reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));//デフォルトサイズのバッファーでバッファリングされた、文字型入力ストリームを作成します。
            String line = reader.readLine();
            while (line != null) {
                jsonData.append(line);
                line = reader.readLine();
            }

            System.out.println(jsonData.toString());

*/
            // JSON to Java
            Gson gson = new Gson();
            dest = gson.fromJson(jsonData.toString(),Dest.class);

            if(dest != null) {
                /*
                res_id = user.getUid();
                res_token = user.getToken();
                res_username = user.getUserName();
                res_email = user.getEmail();
*/
                System.out.println("id = " + dest.getDestId());


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



        Log.d("目的地登録", "Postしてみました");
        // mProgress.dismiss();

        result = "OK";


        // return result
        return result;


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        setTitle("設定画面");

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(sp != null) {
            username = sp.getString(Const.UnameKEY, "");
            email = sp.getString(Const.EmailKEY, "");
            token = sp.getString(Const.TokenKey, "");
            Log.d("user name", String.valueOf(username));
            Log.d("Email", String.valueOf(username));
            Log.d("トークン", String.valueOf(token));

            new getDestinations().execute();
        }


        Button profileButton = (Button) findViewById(R.id.ProfileButton);
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // キーボードが出てたら閉じる
                InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);


            }
        });

        Button logoutButton = (Button) findViewById(R.id.LogoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // キーボードが出てたら閉じる
                InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                Log.d("ログアウトボタン","ログアウト");

                new logout().execute();


            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.option_menu2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.item2) {
            Intent intent = new Intent(getApplicationContext(), DestActivity.class);
            startActivity(intent);


            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private class logout extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {


            //ここにDeleteのAPIをたたくString urlString = "https://rails5api-wkojiro1.c9users.io/destinations.json?email="+ email +"&token="+ token +"";
        Log.d("ログアウト","logout");
            try {

                URL url = new URL("https://rails5api-wkojiro1.c9users.io/users.json");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setDoOutput(true);
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded" );
                con.setRequestMethod("DELETE");
                con.connect();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }




            return null;


        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Void result) {
           

            //response();


            deleteUserdata();
            View v = findViewById(android.R.id.content);
            Snackbar.make(v, "ログアウトしました", Snackbar.LENGTH_LONG).show();

            //finish();
        }

    }
    private void deleteUserdata() {
        // Preferenceを削除する
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().clear();
        Log.d("Delete","done");


    }



    private class getDestinations extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {



            //return POST();
            GetdestList();
            Log.d("user","");
            return null;

        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Void result) {
            Log.d("Post","done");

            //response();



            View v = findViewById(android.R.id.content);
            Snackbar.make(v, "目的地の一覧を取得しました", Snackbar.LENGTH_LONG).show();

           // finish();
        }
    }
}
