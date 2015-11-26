package de.freiheit.activityresponsedelegate;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * holds are important information about the callbacks
 * Created by timfreiheit on 26.11.15.
 */
class CallbackHolder<T> implements Parcelable{

    private Class<? extends ActivityResponseCallback<? super T>> clazz;
    private Bundle arguments;

    public CallbackHolder(Class<? extends ActivityResponseCallback<? super T>> clazz, Bundle arguments){
        this.clazz = clazz;
        this.arguments = arguments;
    }

    @SuppressWarnings("unchecked")
    private CallbackHolder(Parcel in) {
        clazz = (Class<? extends ActivityResponseCallback<? super T>>) in.readSerializable();
        arguments = in.readBundle();
    }

    @SuppressWarnings("unchecked")
    public ActivityResponseCallback<T> newCallback(T owner){
        if (clazz == null) {
            return null;
        }
        try {
            ActivityResponseCallback<T> callback = (ActivityResponseCallback < T >) clazz.newInstance();
            callback.setOwner(owner);
            callback.setArguments(arguments);
            return callback;
        } catch (Exception e) {
            // rethrow any exception
            throw new RuntimeException(e);
        }
    }

    public Class<? extends ActivityResponseCallback<? super T>> getClazz() {
        return clazz;
    }

    public void setClazz(Class<? extends ActivityResponseCallback<? super T>> clazz) {
        this.clazz = clazz;
    }

    public Bundle getArguments() {
        return arguments;
    }

    public void setArguments(Bundle arguments) {
        this.arguments = arguments;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(clazz);
        dest.writeBundle(arguments);
    }

    public static final Creator<CallbackHolder> CREATOR = new Creator<CallbackHolder>() {
        @Override
        public CallbackHolder createFromParcel(Parcel in) {
            return new CallbackHolder(in);
        }

        @Override
        public CallbackHolder[] newArray(int size) {
            return new CallbackHolder[size];
        }
    };
}
