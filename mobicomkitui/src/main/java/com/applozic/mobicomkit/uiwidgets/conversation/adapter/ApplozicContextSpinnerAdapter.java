package com.applozic.mobicomkit.uiwidgets.conversation.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.applozic.mobicomkit.uiwidgets.R;
import com.applozic.mobicomkit.api.attachment.FileClientService;
import com.applozic.mobicomkit.feed.TopicDetail;
import com.applozic.mobicommons.commons.image.ImageLoader;
import com.applozic.mobicommons.commons.image.ImageUtils;
import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.people.channel.Conversation;

import java.util.List;

/**
 * Created by sunil on 13/4/16.
 */
public class ApplozicContextSpinnerAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private List<Conversation> conversationList;
    private ImageLoader productImageLoader;
    private FileClientService fileClientService;
    private Context context;

    public ApplozicContextSpinnerAdapter(final Context context, List<Conversation> conversations) {
        if (context == null) {
            return;
        }
        mInflater = LayoutInflater.from(context);
        this.conversationList = conversations;
        this.fileClientService = new FileClientService(context);
        this.context = context;
        productImageLoader = new ImageLoader(context, ImageUtils.getLargestScreenDimension((Activity) context)) {
            @Override
            protected Bitmap processBitmap(Object data) {
                return fileClientService.loadMessageImage(context, (Conversation) data);
            }
        };
        productImageLoader.addImageCache(((FragmentActivity) context).getSupportFragmentManager(), 0.1f);
        productImageLoader.setImageFadeIn(false);
    }


    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }


    public View getCustomView(int position, View convertView, ViewGroup parent) {

        Conversation conversation = (Conversation) getItem(position);
        ApplozicProductViewHolder viewHolder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.applozic_context_based_layout, parent, false);
            viewHolder = new ApplozicProductViewHolder();
            viewHolder.productImage = (ImageView) convertView.findViewById(R.id.productImage);
            viewHolder.titleTextView = (TextView) convertView.findViewById(R.id.title);
            viewHolder.subTitleTextView = (TextView) convertView.findViewById(R.id.subTitle);
            viewHolder.key1TextView = (TextView) convertView.findViewById(R.id.qtyTitleTextView);
            viewHolder.value1TextView = (TextView) convertView.findViewById(R.id.qtyValueTextView);
            viewHolder.key2TextView = (TextView) convertView.findViewById(R.id.priceTitleTextView);
            viewHolder.value2TextView = (TextView) convertView.findViewById(R.id.priceValueTextview);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ApplozicProductViewHolder) convertView
                    .getTag();
        }

        try {
            if (conversation != null) {
                String topicId = conversation.getTopicId();
                String topicDetailJson = conversation.getTopicDetail();
                if (!TextUtils.isEmpty(topicDetailJson)) {
                    TopicDetail topicDetail = (TopicDetail) GsonUtils.getObjectFromJson(topicDetailJson, TopicDetail.class);
                    if (!TextUtils.isEmpty(topicDetail.getLink())) {
                        productImageLoader.loadImage(conversation, viewHolder.productImage);
                    }
                    if (!TextUtils.isEmpty(topicDetail.getTitle())) {
                        viewHolder.titleTextView.setText(topicDetail.getTitle());
                    }
                    if (!TextUtils.isEmpty(topicDetail.getSubtitle())) {
                        viewHolder.subTitleTextView.setText(topicDetail.getSubtitle());
                    }
                    if (!TextUtils.isEmpty(topicDetail.getKey1())) {
                        viewHolder.key1TextView.setText(topicDetail.getKey1());
                    }
                    if (!TextUtils.isEmpty(topicDetail.getValue1())) {
                        viewHolder.value1TextView.setText(":" + topicDetail.getValue1());
                    }
                    if (!TextUtils.isEmpty(topicDetail.getKey2())) {
                        viewHolder.key2TextView.setText(topicDetail.getKey2());
                    }
                    if (!TextUtils.isEmpty(topicDetail.getValue2())) {
                        viewHolder.value2TextView.setText(":" + topicDetail.getValue2());
                    }

                } else {
                    viewHolder.productImage.setVisibility(View.GONE);
                    viewHolder.titleTextView.setVisibility(View.GONE);
                    viewHolder.subTitleTextView.setVisibility(View.GONE);
                    viewHolder.key1TextView.setVisibility(View.GONE);
                    viewHolder.value1TextView.setVisibility(View.GONE);
                    viewHolder.key2TextView.setVisibility(View.GONE);
                    viewHolder.value2TextView.setVisibility(View.GONE);
                }
            }

        } catch (Exception e) {

        }
        return convertView;

    }


    @Override
    public int getCount() {
        if (context == null) {
            return 0;
        }
        return conversationList.size();
    }

    @Override
    public Object getItem(int position) {
        if (context == null) {
            return null;
        }
        return conversationList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }


    private static class ApplozicProductViewHolder {
        TextView titleTextView, subTitleTextView, key1TextView, value1TextView, key2TextView, value2TextView;
        ImageView productImage;

        ApplozicProductViewHolder() {

        }

    }
}
