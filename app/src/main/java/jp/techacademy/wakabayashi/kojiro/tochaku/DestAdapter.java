package jp.techacademy.wakabayashi.kojiro.tochaku;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by wkojiro on 2017/03/15.
 */

public class DestAdapter extends BaseAdapter{
    private LayoutInflater mLayoutInflater;
    private ArrayList<Dest> mDestArrayList;

    public DestAdapter(Context context) {
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setDestArrayList(ArrayList<Dest> destArrayList){
        mDestArrayList = destArrayList;
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
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            //convertView = mLayoutInflater.inflate(android.R.layout.simple_list_item_2, null);

            convertView = mLayoutInflater.inflate(R.layout.list_dests, parent ,false);
        }

        TextView textView1 = (TextView) convertView.findViewById(R.id.nameTextView);
        TextView textView2 = (TextView) convertView.findViewById(R.id.addressTextView);
        TextView textView3 = (TextView) convertView.findViewById(R.id.emailTextView);


        textView1.setText(mDestArrayList.get(position).getDestName());
        textView2.setText(mDestArrayList.get(position).getDestAddress());
        textView3.setText(mDestArrayList.get(position).getDestEmail());



        return convertView;
    }



}
