package jp.techacademy.wakabayashi.kojiro.tochaku;

/*

１　このActivityがonCreateされた時にAPIを叩いて、登録されているDestの一覧をGetする。
２　一覧表示する

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
import android.widget.AdapterView;
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


//Realm関連
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class SettingActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    //パーツの定義
    TextView mUserNameText;
    TextView mEmailText;
    TextView mDestCountText;
    Button mProfileButton;
    Button mLogoutButton;


    private Realm mRealm;
    private RealmResults<Dest> mDestRealmResults;
    private RealmChangeListener mRealmListener = new RealmChangeListener() {
        @Override
        public void onChange(Object element) {
            reloadListView();
        }
    };

    //リストView
    private ListView mListView;
    //GetのResponseを受けるパラメータ
    private DestAdapter mDestAdapter;

    ProgressDialog mProgress;

    //API通信のための会員Email及びToken(Preferenseから取得）
    static String apiusername;
    static String apiemail;
    static String apitoken;

    String preusername;
    String preemail;
    String pretoken;


    Dest dest;
    int railsid;
    String name;
    String email;
    String address;
    float latitude;
    float longitude;
    String url;







    //Responseを受け取るためのパラメータ
    static Dest res_dest;
    static Integer res_destid;
    static String res_destname;
    static String res_destemail;
    static String res_destaddress;
    static Float res_destlatitude;
    static Float res_destlongitude;
    static String res_desturl;

    public static String GetdestList(){


        HttpURLConnection con = null;//httpURLConnectionのオブジェクトを初期化している。
        BufferedReader reader = null;
        StringBuilder jsonData = new StringBuilder();
        String urlString = "https://rails5api-wkojiro1.c9users.io/destinations.json?email="+ apiemail +"&token="+ apitoken +"";

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
                System.out.println(line);

            }
            //ここからRealm 及び　Arrayにいれる
            //con.getResponseCode();


            // JSON to Java
            /*
            Gson gson = new Gson();
            res_dest = gson.fromJson(jsonData.toString(),Dest.class);

            if(res_dest != null) {

                res_destid = res_dest.getRailsId();
                res_destname = res_dest.getDestName();
                res_destemail = res_dest.getDestEmail();
                res_destaddress = res_dest.getDestAddress();
                res_destlatitude = res_dest.getDestLatitude();
                res_destlongitude = res_dest.getDestLongitude();
                res_desturl = res_dest.getDestUrl();


                System.out.println("id = " + res_dest.getRailsId());

            }*/


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


        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sp.registerOnSharedPreferenceChangeListener(this);

       // Log.d("user name",String.valueOf(sp));
        apiusername = sp.getString(Const.UnameKEY, "");
        apiemail = sp.getString(Const.EmailKEY, "");
        apitoken = sp.getString(Const.TokenKey, "");


            new getDestinations().execute();



        setTitle("設定画面");
        mUserNameText = (TextView) findViewById(R.id.userNameText);

        mUserNameText.setText(apiusername);
        mEmailText = (TextView) findViewById(R.id.EmailText);
        mEmailText.setText(apiemail);
        mDestCountText = (TextView) findViewById(R.id.DestsText);




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


        // Realmの設定
        mRealm = Realm.getDefaultInstance();
        mDestRealmResults = mRealm.where(Dest.class).findAll();
        mRealm.addChangeListener(mRealmListener);

        // ListViewの設定
        mDestAdapter = new DestAdapter(SettingActivity.this);
        mListView = (ListView) findViewById(R.id.listView);

        // ListViewをタップしたときの処理
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // ①入力・編集する画面に遷移させる
                // ②トグルかチェックを表示して、この目的地を選択し、選択されたものはPreferenceに保存される。

            }
        });

        // ListViewを長押ししたときの処理
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                // タスクを削除する

                return true;
            }
        });

        if (mDestRealmResults.size() == 0) {
            // アプリ起動時にタスクの数が0であった場合は表示テスト用のタスクを作成する
            addDestForTest();
        }

        reloadListView();


    }
    private void reloadListView() {

        // 後でTaskクラスに変更する

        ArrayList<Dest> destArrayList = new ArrayList<>();
         /*
        destArrayList.add("aaa");
        destArrayList.add("bbb");
        destArrayList.add("ccc");
        */
        for (int i = 0; i < mDestRealmResults.size(); i++){
            if(!mDestRealmResults.get(i).isValid()) continue;

            Dest dest = new Dest();

            dest.setId(mDestRealmResults.get(i).getId());
            dest.setRailsId(mDestRealmResults.get(i).getRailsId());
            dest.setDestName(mDestRealmResults.get(i).getDestName());
            dest.setDestEmail(mDestRealmResults.get(i).getDestEmail());
            dest.setDestAddress(mDestRealmResults.get(i).getDestAddress());
            dest.setDestLatitude(mDestRealmResults.get(i).getDestLatitude());
            dest.setDestLongitude(mDestRealmResults.get(i).getDestLongitude());
            dest.setDestUrl(mDestRealmResults.get(i).getDestUrl());

            destArrayList.add(dest);
        }

        mDestAdapter.setDestArrayList(destArrayList);
        mListView.setAdapter(mDestAdapter);
        mDestAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRealm.close();
    }

    private void addDestForTest() {
        Dest dest = new Dest();
        dest.setDestName("東京タワー");
        dest.setDestEmail("wkojiro22@gmail.com");
        dest.setDestAddress("東京都港区芝公園４丁目２−８");
        dest.setDestUrl("http://www.yahoo.co,jp");
        dest.setDestLatitude(35.658581f);
        dest.setDestLongitude(139.745433f);
        dest.setRailsId(0);
        dest.setId(0);
        mRealm.beginTransaction();
        mRealm.copyToRealmOrUpdate(dest);
        mRealm.commitTransaction();
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // handle the preference change here
        Log.d("変更","あった");

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
            finish();
        }

    }
    private void deleteUserdata() {
        // Preferenceを削除する
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sp.edit().clear().commit();
        Log.d("Delete","done");
        apiemail = null;
        apiusername = null;
        apitoken = null;


    }
    private class getDestinations extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {

            //API通信のための会員Email及びToken(Preferenseから取得）
            String username;
            String email;
            String token;





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
