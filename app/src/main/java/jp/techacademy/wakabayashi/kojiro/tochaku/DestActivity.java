package jp.techacademy.wakabayashi.kojiro.tochaku;

/*
目的地の登録のためのActivity


 */

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

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

public class DestActivity extends AppCompatActivity {

    //パーツの定義
    EditText mDestNameText;
    EditText mDestEmailText;
    EditText mDestAddressText;
    Button mDestButton;

    //パーツから受け取るためのパラメータ
    Dest dest;
    String destname;
    String destemail;
    String destaddress;

    //Responseを受けるパラメータ
    String resdestid;
    String resdestname;
    String resdestemail;
    String resdestlatitude;
    String resdestlongitude;

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


    public static String Post(Dest dest){
        HttpURLConnection con = null;//httpURLConnectionのオブジェクトを初期化している。
        BufferedReader reader = null;
        StringBuilder jsonData = new StringBuilder();
        String urlString = "https://rails5api-wkojiro1.c9users.io/destinations.json?email="+ email +"&token="+ token +"";

        InputStream inputStream = null;
        String result = "";


        String name = dest.getDestName();
        String email = dest.getDestEmail();
        String address = dest.getDestAddress();
        // String password2 = user.getPassword();

        final String json =
                "{" +
                        "\"name\":\"" + name + "\"," +
                        "\"email\":\"" + email + "\"," +
                        "\"address\":\"" + address + "\"" +
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
            dest = gson.fromJson(jsonData.toString(),Dest.class);

            if(dest != null) {
                res_destid = dest.getDestId();
                res_destname = dest.getDestName();
                res_destemail = dest.getDestEmail();
                res_destaddress = dest.getDestAddress();
                res_destlatitude = dest.getDestLatitude();
                res_destlongitude = dest.getDestLongitude();

                System.out.println("destname = " + dest.getDestName());
                System.out.println("destaddress = " + dest.getDestAddress());

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
        setContentView(R.layout.activity_dest);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //this とはおそらくthis activityのこと ここはActivityの中だからthisでもいける。
        // getApplicationContext();　とも書ける。

        username = sp.getString(Const.UnameKEY, "");
        email = sp.getString(Const.EmailKEY, "");
        token = sp.getString(Const.TokenKey, "");
        Log.d("user name",String.valueOf(username));
        Log.d("Email",String.valueOf(username));
        Log.d("トークン",String.valueOf(token));




        mDestNameText = (EditText) findViewById(R.id.destNameText);
        mDestEmailText = (EditText) findViewById(R.id.destEmailText);
        mDestAddressText = (EditText) findViewById(R.id.destAddressText);


        Button createButton = (Button) findViewById(R.id.createButton);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                destname = mDestNameText.getText().toString();
                destemail = mDestEmailText.getText().toString();
                destaddress = mDestAddressText.getText().toString();


                Log.d("ユーザー登録","ddd");
                if (destname.length() != 0 && destemail.length() != 0 && destaddress.length() != 0) {

                    Log.d("目的地登録","ddd");

                    new DestActivity.createDestination().execute();

                } else {
                    Log.d("目的地登録エラー","ddd");
                    // エラーを表示する
                    Snackbar.make(v, "目的地の情報が正しく入力されていません", Snackbar.LENGTH_LONG).show();

                }

            }

        });
    }

    private class createDestination extends AsyncTask<String, Void, Void>{
        @Override
        protected Void doInBackground(String... params) {

            dest = new Dest();
            dest.setDestName(destname);
            dest.setDestEmail(destemail);
            dest.setDestAddress(destaddress);



            Post(dest);
            Log.d("dest",String.valueOf(dest));
            return null;

        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Void result) {
            Log.d("Post","done");

            //response();



            View v = findViewById(android.R.id.content);
            Snackbar.make(v, "目的地を追加しました。", Snackbar.LENGTH_LONG).show();

            finish();
        }


    }

}
