package drupal.forumapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import drupal.forumapp.activities.ListBaseActivity;

/**
 * Created by serva on 12/10/2017.
 */

public class ShareParcelable implements Parcelable {
    public String value;

    public ShareParcelable(Parcel parcel) {
        this.value = parcel.readString();
    }

    public ShareParcelable(String  value) {
        this.value = value;
    }

    public static final Creator<ShareParcelable> CREATOR = new Creator<ShareParcelable>() {
        @Override
        public ShareParcelable createFromParcel(Parcel in) {
            ShareParcelable parcelable = new ShareParcelable(in);
            return  parcelable;
        }

        @Override
        public ShareParcelable[] newArray(int size) {
            return new ShareParcelable[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(value);
    }
}

