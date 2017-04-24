package com.sheshu.dragdroptest;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.sheshu.dragdroptest.FlickerFetcher.GetThumbnailsThread;

/**
 * Created by Sheshu on 4/23/17.
 */

class FlickerItem implements Parcelable {
    String id;
    int position;
    String thumbURL;
    Bitmap thumb;
    String largeURL;
    private Bitmap photo;
    private String owner;
    private String secret;
    private String server;
    private String farm;

    public FlickerItem(String id, String thumbURL, String largeURL, String owner, String secret, String server, String farm) {
        super();
        this.id = id;
        this.owner = owner;
        this.secret = secret;
        this.server = server;
        this.farm = farm;
    }

    public FlickerItem(String id, String owner, String secret, String server, String farm) {
        super();
        this.id = id;
        this.owner = owner;
        this.secret = secret;
        this.server = server;
        this.farm = farm;
        setThumbURL(createPhotoURL(FlickerFetcher.PHOTO_THUMB, this));
        setLargeURL(createPhotoURL(FlickerFetcher.PHOTO_LARGE, this));
    }

    public String getThumbURL() {
        return thumbURL;
    }

    public void setThumbURL(String thumbURL) {
        this.thumbURL = thumbURL;
        onSaveThumbURL(FlickerFetcher.uihandler, this);
    }

    public String getLargeURL() {
        return largeURL;
    }

    public void setLargeURL(String largeURL) {
        this.largeURL = largeURL;
    }

    @Override
    public String toString() {
        return "FlickerItem [id=" + id + ", thumbURL=" + thumbURL + ", largeURL=" + largeURL + ", owner=" + owner + ", secret=" + secret + ", server=" + server + ", farm="
                + farm + "]";
    }

    private String createPhotoURL(int photoType, FlickerItem imgCon) {
        String tmp = null;
        tmp = "http://farm" + imgCon.farm + ".staticflickr.com/" + imgCon.server + "/" + imgCon.id + "_" + imgCon.secret;// +".jpg";
        switch (photoType) {
            case FlickerFetcher.PHOTO_THUMB:
                tmp += "_t";
                break;
            case FlickerFetcher.PHOTO_LARGE:
                tmp += "_z";
                break;
        }
        tmp += ".jpg";
        return tmp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public Bitmap getThumb() {
        return thumb;
    }

    public void setThumb(Bitmap thumb) {
        this.thumb = thumb;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getFarm() {
        return farm;
    }

    public void setFarm(String farm) {
        this.farm = farm;
    }

    // @Override
    public void onSaveThumbURL(MainActivity.UIHandler uih, FlickerItem ic) {
        // TODO Auto-generated method stub
        new GetThumbnailsThread(uih, ic).start();
    }



    protected FlickerItem(Parcel in) {
        id = in.readString();
        position = in.readInt();
        thumbURL = in.readString();
        thumb = (Bitmap) in.readValue(Bitmap.class.getClassLoader());
        largeURL = in.readString();
        photo = (Bitmap) in.readValue(Bitmap.class.getClassLoader());
        owner = in.readString();
        secret = in.readString();
        server = in.readString();
        farm = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeInt(position);
        dest.writeString(thumbURL);
        dest.writeValue(thumb);
        dest.writeString(largeURL);
        dest.writeValue(photo);
        dest.writeString(owner);
        dest.writeString(secret);
        dest.writeString(server);
        dest.writeString(farm);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<FlickerItem> CREATOR = new Parcelable.Creator<FlickerItem>() {
        @Override
        public FlickerItem createFromParcel(Parcel in) {
            return new FlickerItem(in);
        }

        @Override
        public FlickerItem[] newArray(int size) {
            return new FlickerItem[size];
        }
    };
}