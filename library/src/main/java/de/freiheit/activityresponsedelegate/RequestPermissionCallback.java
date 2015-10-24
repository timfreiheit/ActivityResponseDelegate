package de.freiheit.activityresponsedelegate;

import android.support.annotation.NonNull;

/**
 * callback which handles {@link android.app.Activity#onRequestPermissionsResult(int, String[], int[])}
 *
 * Created by timfreiheit on 21.10.15.
 */
public abstract class RequestPermissionCallback<T> {

    @SuppressWarnings("unchecked")
    public void requestPermissions(final @NonNull T owner,
                                   final @NonNull String[] permissions, final int requestCode) {
        ActivityResponseDelegate.fromRaw(owner).requestPermissions(permissions, requestCode, (Class<? extends RequestPermissionCallback<T>>) getClass(), false);
    }

    public void showRationale(T owner, int requestCode, @NonNull String[] permissions) {
        requestPermissions(owner, permissions, requestCode);
    }

    public void onRequestPermissionsResult(T owner, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (PermissionUtils.verifyPermissions(grantResults)) {
            onAllGranted(owner, requestCode, permissions);
        } else {
            onAnyDenied(owner, requestCode, permissions, grantResults);
        }
    }

    public void onAllGranted(T owner, int requestCode, @NonNull String[] permissions) {

    }

    public void onAnyDenied(T owner, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    }

}
