package jp.techacademy.wakabayashi.kojiro.tochaku;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by wkojiro on 2017/03/15.
 */

public class DestAdapter extends BaseAdapter{
    private LayoutInflater mLayoutInflater;
    private ArrayList<Dest> mDestArrayList;
    Integer selected_position = -1;


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
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            //convertView = mLayoutInflater.inflate(android.R.layout.simple_list_item_2, null);

            convertView = mLayoutInflater.inflate(R.layout.list_dests, parent ,false);
        }

        TextView textView1 = (TextView) convertView.findViewById(R.id.nameTextView);
        TextView textView2 = (TextView) convertView.findViewById(R.id.addressTextView);
        TextView textView3 = (TextView) convertView.findViewById(R.id.emailTextView);
        final CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);



        textView1.setText(mDestArrayList.get(position).getDestName());
        textView2.setText(mDestArrayList.get(position).getDestAddress());
        textView3.setText(mDestArrayList.get(position).getDestEmail());


       // Log.d("mDestArrayList", String.valueOf(mDestArrayList.get(position).getDestName()));
        Log.d("setChecked", String.valueOf(position));

        if (selected_position == position) {
            checkBox.setChecked(true);



        } else {
            checkBox.setChecked(false);

        }
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                if(cb.isChecked() == true)
                {
                    selected_position = position;

                }
                else
                {
                    selected_position = -1;
                }
                notifyDataSetChanged();
            }
        });




      //  checkBox.setChecked(position == selected_position);
        Log.d("selected_position最終", String.valueOf(selected_position));
        Log.d("position最終", String.valueOf(position));



        return convertView;
    }





}
