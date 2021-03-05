package com.example.kauppalista;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomAdapter<Nimike> extends ArrayAdapter<Nimike> {

    public CustomAdapter(Context context, int i, List nimike){
        super(context,i, nimike);

    }

    @Override
    public int getItemViewType(int position) {

        return position;
    }

    @Override

    public View getView(int position, View view, ViewGroup viewGroup) {

        Object object = getItem(position);
        com.example.kauppalista.Nimike nimike = (com.example.kauppalista.Nimike) object;

        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listview_checkboxilla, viewGroup, false);
            holder.text = (TextView) view;
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.text.setText(object.toString());
        if (nimike.getKeratty() == true) {
            holder.text.setPaintFlags(holder.text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.text.setPaintFlags(Paint.ANTI_ALIAS_FLAG);
        }
        return view;

    }

    private class ViewHolder {
        TextView text;
    }

}


