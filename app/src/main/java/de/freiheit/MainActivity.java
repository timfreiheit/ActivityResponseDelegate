package de.freiheit;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import de.freiheit.activityresponsedelegate.ActivityResponseDelegate;
import de.freiheit.activityresponsedelegate.ActivityResponseCallback;
import de.freiheit.basic.BaseActivity;
import de.freiheit.camera.CameraPreviewFragment;
import de.freiheit.contacts.ContactsFragment;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    TextView pickContactResultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pickContactResultTextView = (TextView) findViewById(R.id.pickContactResultTextView);
        findViewById(R.id.button_camera).setOnClickListener(this);
        findViewById(R.id.button_contacts).setOnClickListener(this);
        findViewById(R.id.button_pick_contact).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_camera:
                ShowCameraCallback.call(this);
                break;
            case R.id.button_contacts:
                ShowContactsCallback.call(this);
                break;
            case R.id.button_pick_contact:
                PickContactCallback.call(this, "Called From " + v.hashCode());
                break;
        }
    }

    public static class PickContactCallback extends ActivityResponseCallback<MainActivity> {

        private static final String EXTRA_SAMPLE = "extra_sample";

        public static void call(MainActivity owner, String sampleExtra){
            Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
            Bundle arguments = new Bundle();
            arguments.putString(EXTRA_SAMPLE, sampleExtra);
            ActivityResponseDelegate.from(owner).startActivityForResult(pickContactIntent, PickContactCallback.class, arguments);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            String sampleExtra = getArguments().getString(EXTRA_SAMPLE);
            if (resultCode == RESULT_OK) {
                Uri pickedPhoneNumber = data.getData();
                Toast.makeText(getOwner(), sampleExtra + "\nContact: " + pickedPhoneNumber.toString(), Toast.LENGTH_SHORT).show();
                getOwner().pickContactResultTextView.setText(pickedPhoneNumber.toString());
            } else {
                Toast.makeText(getOwner(), sampleExtra + "\nContact request canceled: ", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public static class ShowCameraCallback extends ActivityResponseCallback<MainActivity> {

        public static void call(MainActivity owner){
            ActivityResponseDelegate.from(owner).requestPermissions(
                    new String[]{Manifest.permission.CAMERA},
                    ShowCameraCallback.class);
        }

        @Override
        public void onAllGranted(int requestCode, @NonNull String[] permissions) {
            getOwner().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.sample_content_fragment, CameraPreviewFragment.newInstance())
                    .addToBackStack("camera")
                    .commitAllowingStateLoss();
        }

        @Override
        public void showRationale(int requestCode, @NonNull String[] permissions) {
            Toast.makeText(getOwner(), R.string.permission_camera_rationale, Toast.LENGTH_SHORT).show();
            super.showRationale(requestCode, permissions);
        }

        @Override
        public void onAnyDenied(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            Toast.makeText(getOwner(), R.string.permission_camera_denied, Toast.LENGTH_SHORT).show();
        }
    }

    public static class ShowContactsCallback extends ActivityResponseCallback<MainActivity> {

        public static void call(MainActivity owner){
            ActivityResponseDelegate.from(owner).requestPermissions(
                    new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS},
                    ShowContactsCallback.class);
        }

        @Override
        public void onAllGranted(int requestCode, @NonNull String[] permissions) {
            getOwner().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.sample_content_fragment, ContactsFragment.newInstance())
                    .addToBackStack("contacts")
                    .commitAllowingStateLoss();
        }

        @Override
        public void onAnyDenied(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            Toast.makeText(getOwner(), R.string.permission_contacts_denied, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void showRationale(int requestCode, @NonNull String[] permissions) {
            Toast.makeText(getOwner(), R.string.permission_contacts_rationale, Toast.LENGTH_SHORT).show();
            super.showRationale(requestCode, permissions);
        }
    }

    public void onBackClick(View view) {
        getSupportFragmentManager().popBackStack();
    }

}