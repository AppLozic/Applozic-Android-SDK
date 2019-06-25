package com.applozic.mobicomkit.uiwidgets.conversation.fragment;


import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.applozic.mobicomkit.uiwidgets.R;

import java.util.ArrayList;

/**
 *
 * @author nozha.gh
 *
 */
public class CustomGrid extends ArrayAdapter<Item> {
    Context context;
    int layoutResourceId;
    ArrayList<Item> data = new ArrayList<Item>();

    public CustomGrid(Context context, int layoutResourceId,
                                 ArrayList<Item> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RecordHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new RecordHolder();
            holder.txtTitle = (TextView) row.findViewById(R.id.grid_text);
            holder.imageItem = (VideoView) row.findViewById(R.id.item_video);
            row.setTag(holder);
        } else {
            holder = (RecordHolder) row.getTag();
        }

        Item item = data.get(position);
        holder.txtTitle.setText(item.getTitle());
       // holder.imageItem(item.getImage());
        holder.imageItem.setVideoPath(item.getImage());
        holder.imageItem.start();
        return row;

    }

    static class RecordHolder {
        TextView txtTitle;
        VideoView imageItem;

    }

}
