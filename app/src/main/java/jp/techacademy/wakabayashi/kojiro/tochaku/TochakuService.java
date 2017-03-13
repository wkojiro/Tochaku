package jp.techacademy.wakabayashi.kojiro.tochaku;



import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by wkojiro on 2017/03/13.
 */

public interface TochakuService {

    String END_POINT = "https://rails5api-wkojiro1.c9users.io/";
 //   String TARGET_URL = "http://b.hatena.ne.jp/ctop/it";
 //   String CATEGORY_IT = "it.rss";
    /**
     * ユーザの情報を取得する
     * https://rails5api-wkojiro1.c9users.io/users/1.json
     */
    @GET("users/{uid}")
    Call<User> getUser(@Path("uid") String user);

    /**
     * ユーザの目的地一覧を取得する
     * https://rails5api-wkojiro1.c9users.io/destinations.json
     */
    @GET("destinations.json")
    Call<List<Dest>> listRepos(@Path("dest") String dest);


}
