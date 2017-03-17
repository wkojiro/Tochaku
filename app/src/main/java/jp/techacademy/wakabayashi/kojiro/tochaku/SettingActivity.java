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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONObject;

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

    public final static String EXTRA_DEST = "jp.techacademy.wakabayashi.kojiro.tochaku.DEST";

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
    String apiusername;
    String apiemail;
    String apitoken;




    Dest dest;
    int railsid;
    String name;
    String email;
    String address;
    float latitude;
    float longitude;
    String url;




    SharedPreferences sp;


    //Responseを受け取るためのパラメータ
    Dest res_dest;
    Integer res_destid;
    String res_destname;
    String res_destemail;
    String res_destaddress;
    Float res_destlatitude;
    Float res_destlongitude;
    String res_desturl;


    //API用変数
    HttpURLConnection con = null;//httpURLConnectionのオブジェクトを初期化している。
    BufferedReader reader = null;
    StringBuilder jsonData = new StringBuilder();
    InputStream inputStream = null;
    String result = "";
    String result2 = "";
    int status = 0;

    public String Deletelogout(){

        String urlString = "https://rails5api-wkojiro1.c9users.io/users/sign_out.json";


        final String json =
                "{\"user\":{" +
                        "\"email\":\"" + apiemail + "\"," +
                        "\"access_token\":\"" + apitoken + "\"" +
                        "}" +
                "}";

        System.out.println(json.toString());
        try {

            URL url = new URL(urlString);
            con = (HttpURLConnection) url.openConnection(); //HttpURLConnectionを取得する
            con.setRequestMethod("DELETE");
            con.setInstanceFollowRedirects(false); // HTTP リダイレクト (応答コード 3xx の要求) を、この HttpURLConnection インスタンスで自動に従うかどうかを設定します。
            con.setRequestProperty("Accept-Language", "jp");
            con.setDoOutput(false); //この URLConnection の doOutput フィールドの値を、指定された値に設定します。→イマイチよく理解できない（URL 接続は、入力または出力、あるいはその両方に対して使用できます。URL 接続を出力用として使用する予定である場合は doOutput フラグを true に設定し、そうでない場合は false に設定します。デフォルトは false です。）
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");


            // リスエストの送信
            OutputStream os = con.getOutputStream(); //この接続に書き込みを行う出力ストリームを返します
            con.connect();

            // con.getResponseCode();

            PrintStream ps = new PrintStream(os); //行の自動フラッシュは行わずに、指定のファイルで新しい出力ストリームを作成します。
            ps.print(json);// JsonをPOSTする
            ps.close();
            status = con.getResponseCode();
            Log.d("レスポンス",String.valueOf(status));


        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // mProgress.dismiss();

        if (status/100 == 2){
            result2 = "OK";
        } else {
            result2 = "NG";
        }
        return result2;

    }


    public String GetdestList(){

        String urlString = "https://rails5api-wkojiro1.c9users.io/destinations.json?email="+ apiemail +"&token="+ apitoken +"";

        try {

            URL url = new URL(urlString); //URLを生成
            con = (HttpURLConnection) url.openConnection(); //HttpURLConnectionを取得する
            con.setRequestMethod("GET");
            con.setInstanceFollowRedirects(false); // HTTP リダイレクト (応答コード 3xx の要求) を、この HttpURLConnection インスタンスで自動に従うかどうかを設定します。
            con.setRequestProperty("Accept-Language", "jp");
           // con.setDoInput(true); //この URLConnection の doOutput フィールドの値を、指定された値に設定します。→イマイチよく理解できない（URL 接続は、入力または出力、あるいはその両方に対して使用できます。URL 接続を出力用として使用する予定である場合は doOutput フラグを true に設定し、そうでない場合は false に設定します。デフォルトは false です。）
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            inputStream = con.getInputStream(); //GETだから
            con.connect();

            status = con.getResponseCode();
            Log.d("結果",String.valueOf(status));
            if(status/100 == 2){

                reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));//デフォルトサイズのバッファーでバッファリングされた、文字型入力ストリームを作成します。
                String line = reader.readLine();

                while (line != null) {
                    jsonData.append(line);
                    line = reader.readLine();
                }

                System.out.println(jsonData.toString());

                JSONArray jsonarray = new JSONArray(jsonData.toString());
                for (int i = 0; i < jsonarray.length(); i++) {
                    JSONObject jsonobject = jsonarray.getJSONObject(i);
                    res_destname = jsonobject.getString("name");
                    res_destemail = jsonobject.getString("email");
                    res_destaddress = jsonobject.getString("address");
                    res_desturl = jsonobject.getString("url");
                    Log.d("res_destname",String.valueOf(res_destname));
                }

                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                realm.createOrUpdateAllFromJson(Dest.class,jsonarray);
                realm.commitTransaction();

                realm.close();

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



        if (status/100 == 2){
            result2 = "OK";
        } else {
            result2 = "NG";
        }
        return result2;
    }


    /* onCreate */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //memo: Login時に保存したユーザーデータを取得
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sp.registerOnSharedPreferenceChangeListener(this);

        apiusername = sp.getString(Const.UnameKEY, "");
        apiemail = sp.getString(Const.EmailKEY, "");
        apitoken = sp.getString(Const.TokenKey, "");

        //memo: 目的地一覧を取得
        new getDestinations().execute();

        setTitle("設定画面");
        mUserNameText = (TextView) findViewById(R.id.userNameText);
        mUserNameText.setText(apiusername);
        mEmailText = (TextView) findViewById(R.id.EmailText);
        mEmailText.setText(apiemail);
        mDestCountText = (TextView) findViewById(R.id.DestsText);


        mProfileButton = (Button) findViewById(R.id.ProfileButton);
        mProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // キーボードが出てたら閉じる
                InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);


                Log.d("プロフィールボタン","プロフィールボタン");


            }
        });

        mLogoutButton = (Button) findViewById(R.id.LogoutButton);
        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // キーボードが出てたら閉じる
                InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                Log.d("ログアウトボタン","ログアウト");

                new logout().execute();


            }
        });


        //memo: Realmの設定
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
                // ①入力・編集する画面に遷移させる ②トチェックを表示して、この目的地を選択し、選択されたものはPreferenceに保存される。
                Log.d("リストビュー","タップ");
                Dest dest = (Dest) parent.getAdapter().getItem(position);

                Intent intent = new Intent(SettingActivity.this, DestActivity.class);
                intent.putExtra(EXTRA_DEST, dest.getId());
                startActivity(intent);





            }
        });

        //memo: ListViewを長押ししたときの処理
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


        ArrayList<Dest> destArrayList = new ArrayList<>();

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
        Log.d("変更","SettingActivityに書かれているLogです。");
    }


    private class logout extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            Deletelogout();

            if(result2.equals("OK")){
                result = "OK";

            } else {
                result = "NG";
            }
            return result;

        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

            View v = findViewById(android.R.id.content);
            if(result.equals("OK")) {
                deleteUserdata();

                Snackbar.make(v, "ログアウトしました", Snackbar.LENGTH_LONG).show();
                finish();
            } else {
                Snackbar.make(v, "ログアウトに失敗しました。通信環境をご確認下さい。", Snackbar.LENGTH_LONG).show();
            }
        }

    }

    //memo: ログアウト時にPreferenceを削除する。（＊sp.edit().clear().commit() だと何故かListenerが反応しない。
    private void deleteUserdata() {
        // Preferenceを削除する

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().remove("username").remove("email").remove("access_token").commit();
        //sp.edit().clear().commit();
        Log.d("Delete","done");
        apiemail = null;
        apiusername = null;
        apitoken = null;

        mRealm.beginTransaction();
        mRealm.deleteAll();
        mRealm.commitTransaction();


    }


    //memo: 目的地一覧をGET
    private class getDestinations extends AsyncTask<String, Void, String> {
        @Override
        protected  String doInBackground(String... params) {



            GetdestList();
            if(result2.equals("OK")){
                result = "OK";

            } else {
                result = "NG";
            }


            return result;

        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("GetDestination","done");
            View v = findViewById(android.R.id.content);
            if(result.equals("OK")) {
                //saveUserdata();
                Snackbar.make(v, "目的地の一覧を取得しました", Snackbar.LENGTH_LONG).show();

            } else {
                Snackbar.make(v, "目的地の一覧取得に失敗しました。通信環境をご確認下さい。", Snackbar.LENGTH_LONG).show();
            }
        }
    }



    //memo: 右上のメニューボタン
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
}
