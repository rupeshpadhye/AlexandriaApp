package it.jaschke.alexandria.dto;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.Data;

/**
 * Created by RUPESH on 2/20/2016.
 */
@Data
public class IndustryIdentifiers implements Parcelable {
    private  String type;
    private  String  identifier;


    private IndustryIdentifiers(Parcel parcel) {
        super();
        type=parcel.readString();
        identifier=parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(type);
        out.writeString(identifier);
    }


    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public IndustryIdentifiers createFromParcel(Parcel in) {
            return new IndustryIdentifiers(in);
        }

        public IndustryIdentifiers[] newArray(int size) {
            return new IndustryIdentifiers[size];
        }
    };
}
