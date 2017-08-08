package com.applozic.mobicomkit.feed;

import android.os.Parcel;

import com.applozic.mobicommons.json.GsonUtils;
import com.applozic.mobicommons.json.JsonParcelableMarker;

/**
 * Created by sunil on 19/2/16.
 */
public class TopicDetail extends JsonParcelableMarker {


    public static final Creator<TopicDetail> CREATOR = new Creator<TopicDetail>() {
        @Override
        public TopicDetail createFromParcel(Parcel in) {
            return new TopicDetail(in);
        }

        @Override
        public TopicDetail[] newArray(int size) {
            return new TopicDetail[size];
        }
    };

    private String title;
    private String subtitle;
    private String link;
    private String key1;
    private String value1;
    private String key2;
    private String value2;


    protected TopicDetail(Parcel in) {
        title = in.readString();
        subtitle = in.readString();
        link = in.readString();
        key1 = in.readString();
        value1 = in.readString();
        key2 = in.readString();
        value2 = in.readString();
    }

    public TopicDetail() {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getKey1() {
        return key1;
    }

    public void setKey1(String key1) {
        this.key1 = key1;
    }

    public String getValue1() {
        return value1;
    }

    public void setValue1(String value1) {
        this.value1 = value1;
    }

    public String getKey2() {
        return key2;
    }

    public void setKey2(String key2) {
        this.key2 = key2;
    }

    public String getValue2() {
        return value2;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(title);
        dest.writeString(subtitle);
        dest.writeString(link);
        dest.writeString(key1);
        dest.writeString(value1);
        dest.writeString(key2);
        dest.writeString(value2);
    }

    public String getJson() {
        return GsonUtils.getJsonFromObject(this, TopicDetail.class);
    }

}
