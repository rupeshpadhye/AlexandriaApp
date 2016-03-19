//-----------------------------------------------------------------------------
package it.jaschke.alexandria.dto;
//-----------------------------------------------------------------------------
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
/**
 * Created by RUPESH on 2/14/2016.
 */
//-----------------------------------------------------------------------------
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
    private List<IndustryIdentifiers> industryIdentifiers;
    private ImageLinks imageLinks;
    private String subtitle;
    private String contentVersion;
    private List<String> categories;



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
        industryIdentifiers = new ArrayList<IndustryIdentifiers>();
        parcel.readTypedList(industryIdentifiers, IndustryIdentifiers.CREATOR);
        //imageLinks=parcel.readParcelable(VolumeInfo.class.getClassLoader());
        subtitle=parcel.readString();
        contentVersion=parcel.readString();
        if (categories == null) {
            categories = new ArrayList();
        }
        parcel.readStringList(categories);



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
        out.writeTypedList(industryIdentifiers);
        //out.writeParcelable(imageLinks, flags);
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
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------