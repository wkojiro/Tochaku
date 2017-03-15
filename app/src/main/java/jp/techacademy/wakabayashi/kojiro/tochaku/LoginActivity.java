package jp.techacademy.wakabayashi.kojiro.tochaku;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonWriter;

import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


import static java.sql.DriverManager.println;


/*

ネットワークに繋がらなかった時
エラー時の対応（エラーメッセージのAPI)

 */


public class LoginActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {


    EditText mEmailEditText;
    EditText mPasswordEditText;
    EditText mUserNameEditText;

    User user;
    String username;
    String email;
    String password;
    String access_token;

    static String res_token;
    static String res_id;
    static String res_username;
    static String res_email;

    String mPasswordConfirmationEditText;
    String mTokenText;

   // Person person;
   // private MyTask task;
    private Integer count = 0;


    ProgressDialog mProgress;

    // アカウント作成時にフラグを立てる。今の所使い道は不明
    boolean mIsCreateAccount = false;

    public static String Login(User user){
        HttpURLConnection con = null;//httpURLConnectionのオブジェクトを初期化している。
        BufferedReader reader = null;
        StringBuilder jsonData = new StringBuilder();
        String urlString = "https://rails5api-wkojiro1.c9users.io/users/sign_in.json";

        InputStream inputStream = null;
        String result = "";

        String email = user.getEmail();
        String password = user.getPassword();

        final String json =
                "{\"user\":{" +
                        "\"email\":\"" + email + "\"," +
                        "\"password\":\""+ password + "\"" +
                        "}" +
                 "}";

        try {
            URL url = new URL(urlString); //URLを生成
            con = (HttpURLConnection) url.openConnection(); //HttpURLConnectionを取得する
            con.setRequestMethod("POST");
            con.setInstanceFollowRedirects(false); // HTTP リダイレクト (応答コード 3xx の要求) を、この HttpURLConnection インスタンスで自動に従うかどうかを設定します。
            con.setRequestProperty("Accept-Language", "jp");
            con.setDoOutput(true); //この URLConnection の doOutput フィールドの値を、指定された値に設定します。→イマイチよく理解できない（URL 接続は、入力または出力、あるいはその両方に対して使用できます。URL 接続を出力用として使用する予定である場合は doOutput フラグを true に設定し、そうでない場合は false に設定します。デフォルトは false です。）
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            // //////////////////////////////////////
            // リスエストの送信
            // //////////////////////////////////////
            OutputStream os = con.getOutputStream(); //この接続に書き込みを行う出力ストリームを返します
            con.connect();
            // con.getResponseCode();

            PrintStream ps = new PrintStream(os); //行の自動フラッシュは行わずに、指定のファイルで新しい出力ストリームを作成します。
            ps.print(json);// JsonをPOSTする
            ps.close();

            //多分ここからResponseのための器をつくっている。
            //戻り値の指定をしないと動かないのかな？

            reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));//デフォルトサイズのバッファーでバッファリングされた、文字型入力ストリームを作成します。
            String line = reader.readLine();
            while (line != null) {
                jsonData.append(line);
                line = reader.readLine();
            }
            System.out.println(jsonData.toString());

            // JSON to Java
            Gson gson = new Gson();
            user = gson.fromJson(jsonData.toString(),User.class);

            if(user != null) {
                res_id = user.getUid();
                res_token = user.getToken();
                res_username = user.getUserName();
                res_email = user.getEmail();

                System.out.println("id = " + user.getUid());
                System.out.println("username = " + user.getUserName());
                System.out.println("access_token = " + user.getToken());

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

        Log.d("会員登録", "Postしてみました");
        // mProgress.dismiss();

        result = "OK";

        return result;
    }


    //http://hmkcode.com/android-send-json-data-to-server/
    public static String Post(User user){

       // mProgress.show();

        // アカウントを作成する"https://rails5api-wkojiro1.c9users.io/users.json"
/*
        Javaでは、各種データをストリームとして扱うことができます。 ストリームデータには、ファイル内のデータ、標準入力や標準出力、通信データ、文字列データなどがあります。
　ストリームには、バイト単位に扱う最も下位のクラスと、バッファリングしたり文字コード変換を行うクラスと、行単位で入出力を行うクラスがあり、組み合わせて使います。
http://ash.jp/java/stream.htm
*/
        HttpURLConnection con = null;//httpURLConnectionのオブジェクトを初期化している。
        BufferedReader reader = null;
        StringBuilder jsonData = new StringBuilder();
        String urlString = "https://rails5api-wkojiro1.c9users.io/users.json";

        InputStream inputStream = null;
        String result = "";


        String username = user.getUserName();
        String email = user.getEmail();
        String password = user.getPassword();
       // String password2 = user.getPassword();

        final String json =
                "{\"user\":{" +
                        "\"username\":\"" + username + "\"," +
                        "\"email\":\"" + email + "\"," +
                        "\"password\":\"" + password + "\"," +
                        "\"password_confirmation\":\""+ password + "\"" +
                        "}" +
                "}";

        try {

            URL url = new URL(urlString); //URLを生成
            con = (HttpURLConnection) url.openConnection(); //HttpURLConnectionを取得する
            con.setRequestMethod("POST");
            con.setInstanceFollowRedirects(false); // HTTP リダイレクト (応答コード 3xx の要求) を、この HttpURLConnection インスタンスで自動に従うかどうかを設定します。
            con.setRequestProperty("Accept-Language", "jp");
            con.setDoOutput(true); //この URLConnection の doOutput フィールドの値を、指定された値に設定します。→イマイチよく理解できない（URL 接続は、入力または出力、あるいはその両方に対して使用できます。URL 接続を出力用として使用する予定である場合は doOutput フラグを true に設定し、そうでない場合は false に設定します。デフォルトは false です。）
            con.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            // //////////////////////////////////////
            // リスエストの送信
            // //////////////////////////////////////
            OutputStream os = con.getOutputStream(); //この接続に書き込みを行う出力ストリームを返します
            con.connect();
           // con.getResponseCode();

            PrintStream ps = new PrintStream(os); //行の自動フラッシュは行わずに、指定のファイルで新しい出力ストリームを作成します。
            ps.print(json);// JsonをPOSTする
            ps.close();

            //多分ここからResponseのための器をつくっている。
            //戻り値の指定をしないと動かないのかな？

            reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));//デフォルトサイズのバッファーでバッファリングされた、文字型入力ストリームを作成します。
            String line = reader.readLine();
            while (line != null) {
                jsonData.append(line);
                line = reader.readLine();
            }
            System.out.println(jsonData.toString());

            // JSON to Java
            Gson gson = new Gson();
            user = gson.fromJson(jsonData.toString(),User.class);

            if(user != null) {
               res_id = user.getUid();
               res_token = user.getToken();
               res_username = user.getUserName();
               res_email = user.getEmail();

                System.out.println("id = " + user.getUid());
                System.out.println("username = " + user.getUserName());
                System.out.println("token = " + user.getToken());

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

        Log.d("会員登録", "Postしてみました");
       // mProgress.dismiss();

        result = "OK";

        return result;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // handle the preference change here
        Log.d("変更情報","ログインしました");


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // //////////////////////////////////////
        // Retrofit + Gson
        // //////////////////////////////////////
/*
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TochakuService.END_POINT)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mTochakuService = retrofit.create(TochakuService.class);
*/

        //UIの準備
        setTitle("ログイン");

        mUserNameEditText = (EditText) findViewById(R.id.usernameText);
        mEmailEditText = (EditText) findViewById(R.id.emailText);
        mPasswordEditText = (EditText) findViewById(R.id.passwordText);


        mProgress = new ProgressDialog(this);
        mProgress.setMessage("処理中...");



        Button createButton = (Button) findViewById(R.id.createButton);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // キーボードが出てたら閉じる
                InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                username = mUserNameEditText.getText().toString();
                email = mEmailEditText.getText().toString();
                password = mPasswordEditText.getText().toString();


                Log.d("ユーザー登録","ddd");
                if (email.length() != 0 && password.length() >= 6 ) {
                    // ログイン時に表示名を保存するようにフラグを立てる
                    mIsCreateAccount = true;
                    Log.d("ユーザー登録","ddd");

                    new createAccount().execute();
                   // createAccount(email, password);
                } else {
                    // 非同期処理を開始する
                    //new createAccount().execute();
                    //createAccount();
                    Log.d("ユーザー登録","ddd");
                    // エラーを表示する
                    Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show();

                }
            }
        });

        Button loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // キーボードが出てたら閉じる
                InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                email = mEmailEditText.getText().toString();
                password = mPasswordEditText.getText().toString();

                if (email.length() != 0 && password.length() >= 6) {
                    // フラグを落としておく
                    mIsCreateAccount = false;

                    Log.d("ログイン","aaa");

                    new loginAccount().execute();

                  //  login(email, password);
                } else {

                    Log.d("ログイン","aaa");
                    // エラーを表示する
                    Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

/*
 * AsyncTask<型1, 型2,型3>
 *
 *   型1 … Activityからスレッド処理へ渡したい変数の型
 *          ※ Activityから呼び出すexecute()の引数の型
 *          ※ doInBackground()の引数の型
 *
 *   型2 … 進捗度合を表示する時に利用したい型
 *          ※ onProgressUpdate()の引数の型
 *
 *   型3 … バックグラウンド処理完了時に受け取る型
 *          ※ doInBackground()の戻り値の型
 *          ※ onPostExecute()の引数の型
 *
 *   ※ それぞれ不要な場合は、Voidを設定すれば良い
 */



//    private void createAccount(String email, String password) {
    private class createAccount extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {

            user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(password);

            //return POST();
            Post(user);
            Log.d("user",String.valueOf(user));
            return null;

        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Void result) {
            Log.d("Post","done");

            //response();


            saveUserdata(user);
            View v = findViewById(android.R.id.content);
            Snackbar.make(v, "会員登録が完了しました。", Snackbar.LENGTH_LONG).show();

            finish();
        }
    }


    private class loginAccount extends AsyncTask<String, Void, Void>{
        @Override
        protected Void doInBackground(String... params){
            user = new User();
            user.setEmail(email);
            user.setPassword(password);

            Login(user);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.d("Post","done");

            //response();


            saveUserdata(user);
            View v = findViewById(android.R.id.content);
            Snackbar.make(v, "ログインが完了しました。", Snackbar.LENGTH_LONG).show();

            finish();

        }
    }


/*
    private void login(String email, String password) {
        // プログレスダイアログを表示する
        mProgress.show();

        // ログインする

        mProgress.dismiss();

    }
*/
    private void saveUserdata(User user) {

       // Integer loginkey = 1;
        // Preferenceに保存する
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Const.UidKEY , res_id);
        editor.putString(Const.UnameKEY, res_username);
        editor.putString(Const.EmailKEY, res_email);
        editor.putString(Const.TokenKey, res_token);
        //editor.putInt(Const.LoginKey, 1);

        editor.commit();
    }


}
