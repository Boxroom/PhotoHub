package de.dhbw_mannheim.photohub;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class ItemHolder implements Parcelable {
    Bitmap bitmap;
    String path;
    String title;
    String location;
    String description;

    protected ItemHolder(Bitmap bitmap, String path, String title, String location, String description) {
        this.bitmap = bitmap;
        this.path = path;
        this.title = title;
        this.location = location;
        this.description = description;
    }

    protected ItemHolder(Parcel in) {
        bitmap = (Bitmap) in.readValue(Bitmap.class.getClassLoader());
        path = in.readString();
        title = in.readString();
        description = in.readString();
        location = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(bitmap);
        dest.writeString(path);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(location);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ItemHolder> CREATOR = new Parcelable.Creator<ItemHolder>() {
        @Override
        public ItemHolder createFromParcel(Parcel in) {
            return new ItemHolder(in);
        }

        @Override
        public ItemHolder[] newArray(int size) {
            return new ItemHolder[size];
        }
    };
}