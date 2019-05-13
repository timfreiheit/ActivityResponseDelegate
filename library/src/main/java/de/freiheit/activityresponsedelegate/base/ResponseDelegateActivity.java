package de.freiheit.activityresponsedelegate.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import de.freiheit.activityresponsedelegate.ActivityResponseDelegate;

/**
 * useful base class when using {@link ActivityResponseDelegate}
 * <p>
 * Created by timfreiheit on 25.01.16.
 */
public class ResponseDelegateActivity extends Activity {

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // delegate the result handling
        ActivityResponseDelegate.from(this).onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // delegate the permission handling
        ActivityResponseDelegate.from(this).onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save the delegate state in case the activity will be destroyed
        ActivityResponseDelegate.from(this).onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // restore delegate state
        ActivityResponseDelegate.from(this).onRestoreInstanceState(savedInstanceState);
    }

}
