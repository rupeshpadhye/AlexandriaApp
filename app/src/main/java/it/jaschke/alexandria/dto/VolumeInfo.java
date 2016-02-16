package it.jaschke.alexandria.dto;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import lombok.Data;

/**
 * Created by RUPESH on 2/14/2016.
 */
@Data
public class VolumeInfo implements Parcelable {
    private String pageCount;
    private String infoLink;
    private String printType;
    private String allowAnonLogging;
    private String publisher;
    private ArrayList<String> authors;
    private String title;
    private String previewLink;
    private String description;
    private ImageLinks imageLinks;
    private String subtitle;
    private String contentVersion;
    private ArrayList<String> categories;
    /*private String language;
    private String publishedDate;
*/


    private VolumeInfo(Parcel parcel) {
        super();
        pageCount =  parcel.readString();
        infoLink=parcel.readString();
        printType=parcel.readString();
        allowAnonLogging=parcel.readString();
        publisher=parcel.readString();
        authors=parcel.readArrayList(null);
        title=parcel.readString();
        previewLink=parcel.readString();
        description=parcel.readString();
        imageLinks=parcel.readParcelable(ImageLinks.class.getClassLoader());
        subtitle=parcel.readString();
        contentVersion=parcel.readString();
        categories= parcel.readArrayList(null);
        /*language=parcel.readString();
        publishedDate=parcel.readString();
*/
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(pageCount);
        out.writeString(infoLink);
        out.writeString(printType);
        out.writeString(allowAnonLogging);
        out.writeString(publisher);
        out.writeList(authors);
        out.writeString(title);
        out.writeString(description);
        out.writeParcelable(imageLinks, flags);
        out.writeString(subtitle);
        out.writeString(contentVersion);
        out.writeList(categories);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public VolumeInfo createFromParcel(Parcel in) {
            return new VolumeInfo(in);
        }

        public VolumeInfo[] newArray(int size) {
            return new VolumeInfo[size];
        }
    };

}
