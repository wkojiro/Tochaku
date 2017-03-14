package jp.techacademy.wakabayashi.kojiro.tochaku;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by wkojiro on 2017/03/13.
 */

public class Dest implements Serializable {

    private String mDestid;
    private String mDestname;
    private String mDestemail;
    private String mDestaddress;
    private float mDestlatitude;
    private float mDestlongitude;
    private ArrayList<Dest> mDestArrayList;

    public String getDestId(){
        return mDestid;
    }

    public String getDestName(){
        return  mDestname;
    }

    public void setDestName(String destname) {
        this.mDestname = destname;
    }


    public String getDestEmail(){
        return  mDestemail;
    }

    public void setDestEmail(String destemail) {
        this.mDestemail = destemail;
    }

    public String getDestAddress(){
        return mDestaddress;
    }
    public void setDestAddress(String destaddress) {
        this.mDestaddress = destaddress;
    }

    public Float getDestLatitude(){
        return mDestlatitude;
    }
    public Float getDestLongitude(){
        return mDestlongitude;
    }

    public ArrayList<Dest> getDests() {
        return mDestArrayList;
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
