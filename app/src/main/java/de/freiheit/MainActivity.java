package de.freiheit;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Toast;

import de.freiheit.activityresponsedelegate.ActivityResponseDelegate;
import de.freiheit.activityresponsedelegate.ActivityResultCallback;
import de.freiheit.activityresponsedelegate.RequestPermissionCallback;
import de.freiheit.basic.BaseActivity;
import de.freiheit.camera.CameraPreviewFragment;
import de.freiheit.contacts.ContactsFragment;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button_camera).setOnClickListener(this);
        findViewById(R.id.button_contacts).setOnClickListener(this);
        findViewById(R.id.button_pick_contact).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_camera:
                ActivityResponseDelegate.from(this).requestPermissions(
                        new String[]{Manifest.permission.CAMERA},
                        ShowCameraCallback.class);
                break;
            case R.id.button_contacts:
                ActivityResponseDelegate.from(this).requestPermissions(
                        new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS},
                        ShowContactsCallback.class);
                break;
            case R.id.button_pick_contact:
                Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                ActivityResponseDelegate.from(this).startActivityForResult(pickContactIntent, PickContactCallback.class);
                break;
        }
    }

    public static class PickContactCallback extends ActivityResultCallback<Activity> {

        @Override
        public void onActivityResult(Activity owner, int requestCode, int resultCode, Intent data) {
            if (resultCode == RESULT_OK) {
                Uri pickedPhoneNumber = data.getData();
                Toast.makeText(owner, "Contact: " + pickedPhoneNumber.toString(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(owner, "Contact request canceled: ", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public static class ShowCameraCallback extends RequestPermissionCallback<MainActivity> {
        @Override
        public void onAllGranted(MainActivity owner, int requestCode, @NonNull String[] permissions) {
            owner.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.sample_content_fragment, CameraPreviewFragment.newInstance())
                    .addToBackStack("camera")
                    .commitAllowingStateLoss();
        }

        @Override
        public void showRationale(MainActivity owner, int requestCode, @NonNull String[] permissions) {
            Toast.makeText(owner, R.string.permission_camera_rationale, Toast.LENGTH_SHORT).show();
            requestPermissions(owner, permissions, requestCode);
        }

        @Override
        public void onAnyDenied(MainActivity owner, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            Toast.makeText(owner, R.string.permission_camera_denied, Toast.LENGTH_SHORT).show();
        }
    }

    public static class ShowContactsCallback extends RequestPermissionCallback<MainActivity> {
        @Override
        public void onAllGranted(MainActivity owner, int requestCode, @NonNull String[] permissions) {
            owner.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.sample_content_fragment, ContactsFragment.newInstance())
                    .addToBackStack("contacts")
                    .commitAllowingStateLoss();
        }

        @Override
        public void onAnyDenied(MainActivity owner, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            Toast.makeText(owner, R.string.permission_contacts_denied, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void showRationale(MainActivity owner, int requestCode, @NonNull String[] permissions) {
            Toast.makeText(owner, R.string.permission_contacts_rationale, Toast.LENGTH_SHORT).show();
            requestPermissions(owner, permissions, requestCode);
        }
    }

    public void onBackClick(View view) {
        getSupportFragmentManager().popBackStack();
    }

}