package de.freiheit.activityresponsedelegate.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import de.freiheit.activityresponsedelegate.ActivityResponseDelegate;

/**
 * useful base class when using {@link ActivityResponseDelegate}
 *
 * Created by timfreiheit on 25.01.16.
 */
public class SupportResponseDelegateFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityResponseDelegate.from(this).onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ActivityResponseDelegate.from(this).onSaveInstanceState(outState);
    }

}
