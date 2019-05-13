package de.freiheit.activityresponsedelegate;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

/**
 * callback which handles
 *  {@link android.app.Activity#onRequestPermissionsResult(int, String[], int[])}
 * and
 *  {@link android.app.Activity#onActivityResult(int, int, Intent)}
 *
 * Created by timfreiheit on 21.10.15.
 */
public abstract class ActivityResponseCallback<T> {

    private T mOwner;
    private Bundle mArguments;

    public void setOwner(@NonNull T owner){
        mOwner = owner;
    }

    public T getOwner(){
        return mOwner;
    }

    public Bundle getArguments() {
        return mArguments;
    }

    public void setArguments(Bundle arguments) {
        this.mArguments = arguments;
    }

    @SuppressWarnings("unchecked")
    public void requestPermissions(final @NonNull String[] permissions, final int requestCode) {
        ActivityResponseDelegate.fromRaw(getOwner()).requestPermissions(permissions, requestCode, (Class<? extends ActivityResponseCallback<T>>) getClass(), getArguments(), false);
    }

    /**
     * called when the app should show a rational to explain the need of the permission
     * per default it will just ask for the permission again without explaination
     */
    public void showRationale(int requestCode, @NonNull String[] permissions) {
        requestPermissions(permissions, requestCode);
    }

    /**
     * @see android.app.Activity#onRequestPermissionsResult(int, String[], int[])
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (PermissionUtils.verifyPermissions(grantResults)) {
            onAllGranted(requestCode, permissions);
        } else {
            onAnyDenied(requestCode, permissions, grantResults);
        }
    }

    /**
     * called when all requested permission was granted
     */
    public void onAllGranted(int requestCode, @NonNull String[] permissions) {

    }

    /**
     * called when of the requested permission was denied
     */
    public void onAnyDenied(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    }

    /**
     * @see android.app.Activity#onActivityResult(int, int, Intent)
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

}
