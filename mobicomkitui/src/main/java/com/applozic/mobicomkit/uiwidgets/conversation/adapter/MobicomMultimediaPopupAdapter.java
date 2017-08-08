package com.applozic.mobicomkit.uiwidgets.conversation.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.applozic.mobicomkit.uiwidgets.AlCustomizationSettings;
import com.applozic.mobicomkit.uiwidgets.R;

import java.util.List;

/**
 * Created by reytum on 18/3/16.
 */
public class MobicomMultimediaPopupAdapter extends BaseAdapter {
    Context context;
    List<String> multimediaIcons;
    List<String> multimediaText;
    AlCustomizationSettings alCustomizationSettings;

    public MobicomMultimediaPopupAdapter(Context context, List<String> multimediaIcons, List<String> multimediaText) {
        this.context = context;
        this.multimediaIcons = multimediaIcons;
        this.multimediaText = multimediaText;
    }

    public void setAlCustomizationSettings(AlCustomizationSettings alCustomizationSettings) {
        this.alCustomizationSettings = alCustomizationSettings;
    }

    @Override
    public int getCount() {
        return multimediaText.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.mobicom_individual_multimedia_option_item, null);

        TextView icon = (TextView) convertView.findViewById(R.id.mobicom_multimedia_icon);
        Typeface iconTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/fontawesome-webfont.ttf");
        icon.setTypeface(iconTypeface);
        TextView text = (TextView) convertView.findViewById(R.id.mobicom_multimedia_text);
        icon.setTextColor(Color.parseColor(alCustomizationSettings.getAttachmentIconsBackgroundColor()));
        icon.setText(multimediaIcons.get(position));
        text.setText(multimediaText.get(position));
        return convertView;
    }

}
