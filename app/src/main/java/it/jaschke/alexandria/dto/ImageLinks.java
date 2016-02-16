package it.jaschke.alexandria.dto;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.Data;

/**
 * Created by RUPESH on 2/14/2016.
 */
@Data
public class ImageLinks implements Parcelable {
    private String thumbnail;
    private String smallThumbnail;

    private ImageLinks(Parcel parcel) {
        super();
        thumbnail = parcel.readString();
        smallThumbnail = parcel.readString();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(thumbnail);
        out.writeString(smallThumbnail);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ImageLinks createFromParcel(Parcel in) {
            return new ImageLinks(in);
        }

        public ImageLinks[] newArray(int size) {
            return new ImageLinks[size];
        }
    };
}
