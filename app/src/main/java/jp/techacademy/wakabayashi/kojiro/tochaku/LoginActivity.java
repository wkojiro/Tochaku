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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import com.google.gson.Gson;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static java.sql.DriverManager.println;

public class LoginActivity extends AppCompatActivity {


    EditText mEmailEditText;
    EditText mPasswordEditText;
    EditText mUserNameEditText;

    User user;
    String username;
    String email;
    String password;

    String mPasswordConfirmationEditText;
    String mTokenText;

   // Person person;
   // private MyTask task;
    private Integer count = 0;

    private TochakuService mTochakuService;

    ProgressDialog mProgress;

    // アカウント作成時にフラグを立てる。今の所使い道は不明
    boolean mIsCreateAccount = false;


    //http://hmkcode.com/android-send-json-data-to-server/
    public static String Post(){

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

        final String json =
                "{\"user\":{" +
                        "\"username\":\"あいうえお5\"," +
                        "\"email\":\"test06@test.com\"," +
                        "\"password\":\"testtest\"," +
                        "\"password_confirmation\":\"testtest\"" +
                        "}" +
                "}";

/*
        String json = "";

        // 3. build jsonObject
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("username", user.getUserName());
        jsonObject.accumulate("email", user.getEmail());
        jsonObject.accumulate("password", user.getPassword());

        // 4. convert JSONObject to JSON to String
        json = jsonObject.toString();
*/
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

            PrintStream ps = new PrintStream(os); //行の自動フラッシュは行わずに、指定のファイルで新しい出力ストリームを作成します。
            ps.print(json);// JsonをPOSTする
            ps.close();

            //多分ここからResponseのための器をつくっている。（でもうまくいっていない。［］系の話の様子）
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
            User user = gson.fromJson(jsonData.toString(),
                    User.class);

            System.out.println("id = " + user.getUid());
            System.out.println("username = " + user.getUserName());
            System.out.println("token = " + user.getToken());


/*

            JSONObject jsonObject = new JSONObject(buffer);
            JSONArray jsonArray  = jsonObject.getJSONArray(buffer);
           // JSONArray jsonArray = new JSONArray(object);

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                Log.d("HTTP REQ", jsonObject.getString("email"));
            }
*/

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


        // return result
        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);






        // //////////////////////////////////////
        // Retrofit + Gson
        // //////////////////////////////////////

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TochakuService.END_POINT)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mTochakuService = retrofit.create(TochakuService.class);


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

                   // createAccount(email, password);
                } else {
                    // 非同期処理を開始する
                    new createAccount().execute();
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

                String email = mEmailEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();



                if (email.length() != 0 && password.length() >= 6) {
                    // フラグを落としておく
                    mIsCreateAccount = false;

                    Log.d("ログイン","aaa");

                    login(email, password);
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

            /*
            user = new User();
            user.setUsername(mUserNameEditText.getText().toString());
            user.setEmail(mEmailEditText.getText().toString());
            user.setPassword(mPasswordEditText.getText().toString());
*/
            //return POST();
            Post();
            return null;

        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Void result) {
            Log.d("Post","done");
        }
    }


    private void login(String email, String password) {
        // プログレスダイアログを表示する
        mProgress.show();

        // ログインする

        mProgress.dismiss();

    }

    private void saveUserdata(String email, String token) {
        // Preferenceに保存する
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Const.EmailKEY, email);
        editor.putString(Const.TokenKey, token);
        editor.commit();
    }


}
