package jp.techacademy.wakabayashi.kojiro.tochaku;

import java.io.Serializable;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by wkojiro on 2017/03/13.
 */


/*
implements Serializableを付けることで生成したオブジェクトをシリアライズすることができるようになります。
シリアライズとはデータを丸ごとファイルに保存したり、TaskAppでいうと別のActivityに渡すことができるようにすることです。

JSONObjectはRealmObjectを真似てつけてみている。これでどれだけのメソッドを使えるようにextendsされるのか不明。

 */
public class Dest extends RealmObject implements Serializable {

    private int railsid;
    private String name;
    private String email;
    private String address;
    private float latitude;
    private float longitude;
    private String url;

    // id をプライマリーキーとして設定(Jsonで取得したデータをRealmで内部に保存する。）
    @PrimaryKey
    private int id;
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getRailsId(){
        return railsid; //Rails側のid
    }

    public void setRailsId(int id){
        this.railsid = railsid;
    }

    public String getDestName(){
        return name;
    }

    public void setDestName(String name) {
        this.name = name;
    }


    public String getDestEmail(){
        return  email;
    }

    public void setDestEmail(String email) {
        this.email = email;
    }

    public String getDestAddress(){
        return address;
    }
    public void setDestAddress(String address) {
        this.address = address;
    }

    public Float getDestLatitude(){
        return latitude;
    }
    public void setDestLatitude(Float latitude){
        this.latitude = latitude;
    }

    public Float getDestLongitude(){
        return longitude;
    }

    public void setDestLongitude(Float longitude){
        this.latitude = longitude;
    }

    public String getDestUrl(){
        return url;
    }
    public void setDestUrl(String url) {
        this.url = url;
    }

    /*
    public Dest(String destid, String destname, String destemail, String destaddress, float destlatitude, float destlongitude) {
        mDestid = destid;
        mDestname = destname;
        mDestemail = destemail;
        mDestaddress = destaddress;
        mDestlatitude = destlatitude;
        mDestlongitude = destlongitude;


    }*/


}
