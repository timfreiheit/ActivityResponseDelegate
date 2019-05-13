ActivityResponseDelegate
=====================

This library should help working responses received by an Activity or Fragment.   
For example onActivityResult and onRequestPermissionsResult.    
This methods will be delegated to static classes with a reference to the original receiver.   
There is also an advanced help to work with permission.   

# Why?
### Why work with static classes instead of anonymous classes as callbacks?    
When rotating the screen or other configs changed the Activity can be recreated.    
When using anonymous classes the old activity get leaked and the callbacks can not delivered to the new one.

# Usage

## create a class which should handle the response

### onRequestPermissionsResult

```java

public static class ShowCameraCallback extends ActivityResponseCallback<MainActivity> {

    // the call method is just to avoid writing the requestPermission code for every use of this callback
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

```

### onActivityResult

```java

public static class PickContactCallback extends ActivityResponseCallback<MainActivity> {

    private static final String EXTRA_SAMPLE = "extra_sample";

    // the call method is just to avoid writing the startActivityForResult code for every use of this callback
    public static void call(MainActivity owner, String sampleExtra){
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);

        // it is possible to pass some parameters to the callback
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

```

## delegate the methods
It is useful to create a BaseActivity and/or BaseFragment which will delegate all needed methods to the ActivityResponseDelegate.

```java

public class BaseActivity extends AppCompatActivity {

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

```


## Install

```groovy

repositories {    
    // ...    
    maven { url "https://jitpack.io" }   
}   

dependencies {    
    implementation 'com.github.timfreiheit:ActivityResponseDelegate:0.4'
}  

```