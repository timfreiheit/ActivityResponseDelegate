package de.freiheit.activityresponsedelegate;

import android.content.Intent;

/**
 * callback which handles {@link android.app.Activity#onActivityResult(int, int, Intent)}
 *
 * Created by timfreiheit on 21.10.15.
 */
public abstract class ActivityResultCallback<T> {

    /**
     * @see android.app.Activity#onActivityResult(int, int, Intent)
     */
    public void onActivityResult(T owner, int requestCode, int resultCode, Intent data) {

    }
}
