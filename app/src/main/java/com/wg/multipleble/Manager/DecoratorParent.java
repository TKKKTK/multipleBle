package com.wg.multipleble.Manager;

import android.os.Parcel;
import android.os.Parcelable;

public class DecoratorParent implements Parcelable {
    private DecoratorBleManager decoratorBleManager;

    public DecoratorParent(DecoratorBleManager decoratorBleManager){
        this.decoratorBleManager = decoratorBleManager;
    }

    protected DecoratorParent(Parcel in) {
        decoratorBleManager = in.readParcelable(DecoratorBleManager.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable((Parcelable) decoratorBleManager,flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DecoratorParent> CREATOR = new Creator<DecoratorParent>() {
        @Override
        public DecoratorParent createFromParcel(Parcel in) {
            return new DecoratorParent(in);
        }

        @Override
        public DecoratorParent[] newArray(int size) {
            return new DecoratorParent[size];
        }
    };

    public DecoratorBleManager getDecoratorBleManager() {
        return decoratorBleManager;
    }

    public void setDecoratorBleManager(DecoratorBleManager decoratorBleManager) {
        this.decoratorBleManager = decoratorBleManager;
    }
}
