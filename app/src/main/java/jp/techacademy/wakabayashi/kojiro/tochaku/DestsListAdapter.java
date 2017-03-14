package jp.techacademy.wakabayashi.kojiro.tochaku;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by wkojiro on 2017/03/14.
 */

public class DestsListAdapter extends BaseAdapter{
    private LayoutInflater mLayoutInflater = null; //要確認
    private ArrayList<Dest> mDestArrayList;

    public DestsListAdapter(Context context) {
        mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return mDestArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDestArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_dests, parent, false);
        }

        TextView nameText = (TextView) convertView.findViewById(R.id.nameTextView);
       nameText.setText(mDestArrayList.get(position).getDestName());

        TextView addressText = (TextView) convertView.findViewById(R.id.addressTextView);
        addressText.setText(mDestArrayList.get(position).getDestAddress());

        TextView emailText = (TextView) convertView.findViewById(R.id.emailTextView);
        emailText.setText(mDestArrayList.get(position).getDestEmail());



        return convertView;
    }

    public void setDestArrayList(ArrayList<Dest> destArrayList) {
        mDestArrayList = destArrayList;
    }

}
