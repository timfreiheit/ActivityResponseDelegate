ActivityResponseDelegate
=====================

This library should help working responses received by an Activity or Fragment.   
For example onActivityResult and onRequestPermissionsResult.    
This methods will be delegated to static classes with a reference to the original receiver.   
There is also an advanced help to work with permission.   

# Why   
### Why work with static classes instead of anonymous classes as callbacks?    
When rotating the screen or other configs changed the Activity can be recreated.    
When using anonymous classes the old activity get leaked and the callbacks can not delivered to the new one.

# Usage

## delegate the methods
It is useful to create a BaseActivity or BaseFragment which will delegate all needed methods to the ActivityResponseDelegate.

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


## create a class which should handle the response

### onRequestPermissionsResult

```java

public static class ShowContactsCallback extends RequestPermissionCallback<MainActivity> {

    // optional
    @Override
    public void onAllGranted(MainActivity owner, int requestCode, @NonNull String[] permissions) {
        owner.getSupportFragmentManager().beginTransaction()
            .replace(R.id.sample_content_fragment, ContactsFragment.newInstance())
            .addToBackStack("contacts")
            .commitAllowingStateLoss();
    }

    // optional
    @Override
    public void onAnyDenied(MainActivity owner, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Toast.makeText(owner, R.string.permission_contacts_denied, Toast.LENGTH_SHORT).show();
    }

    // optional
    @Override
    public void showRationale(MainActivity owner, int requestCode, @NonNull String[] permissions) {
        Toast.makeText(owner, R.string.permission_contacts_rationale, Toast.LENGTH_SHORT).show();
        // ask for permission after user know why we need it
        requestPermissions(owner, permissions, requestCode);
    }
}

```

### onActivityResult

```java

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

```


## call the specific method

### requestPermission

```java
    
ActivityResponseDelegate.from(this).requestPermissions(
    new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS},
    ShowContactsCallback.class);

```

### startActivityForResult

```java
    
Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
ActivityResponseDelegate.from(this).startActivityForResult(pickContactIntent, PickContactCallback.class);

```