package de.freiheit.activityresponsedelegate;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.WeakHashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

/**
 * the core class to start activities or ask for permissions
 * make sure to send lifecycle events to this class
 * <p>
 * {@link #onRestoreInstanceState(Bundle)}
 * {@link #onSaveInstanceState(Bundle)}
 * {@link #onActivityResult(int, int, Intent)} (Bundle)}
 * {@link #onRequestPermissionsResult(int, String[], int[])} (Bundle)}s
 * <p>
 * Created by timfreiheit on 21.10.15.
 */
public final class ActivityResponseDelegate<T> implements Parcelable {

    private final static String TAG = ActivityResponseDelegate.class.getSimpleName();

    private final static String BUNDLE_KEY = ActivityResponseDelegate.class.getName() + "_BUNDLE_KEY";
    private final static WeakHashMap<Object, ActivityResponseDelegate> cache = new WeakHashMap<>();

    private static ActivityResponseConfig globalConfig = new ActivityResponseConfig.Builder().build();

    public static void setGlobalConfig(ActivityResponseConfig config) {
        if (config != null) {
            globalConfig = config;
        }
    }

    public static <T extends Activity> ActivityResponseDelegate<T> from(T owner) {
        return fromRaw(owner);
    }

    public static <T extends Fragment> ActivityResponseDelegate<T> from(T owner) {
        return fromRaw(owner);
    }

    @SuppressWarnings("unchecked")
    protected static <T> ActivityResponseDelegate<T> fromRaw(T owner) {
        ActivityResponseDelegate<T> ret;
        if (cache.containsKey(owner)) {
            ret = cache.get(owner);
        } else {
            synchronized (cache) {
                ActivityResponseDelegate<T> delegate = new ActivityResponseDelegate<>();
                cache.put(owner, delegate);
                ret = delegate;
            }
        }
        ret.mOwner = new WeakReference(owner);
        return ret;
    }

    /// end static members
    ///---------------------------------------------------------------------------------------------


    WeakReference<T> mOwner;

    // use 100 as start request code
    // try to avoid other request codes used in the app
    private int uniqueActivityResultRequestCode = 100;
    private int uniquePermissionRequestCode = 0;

    // SparseArray to store the CallbackHolder<T> of the used responses
    // declare the SparseArray with generic type Object to store it in Parcel
    private SparseArray<Object> mActivityResultCallbacks = new SparseArray<>();
    private SparseArray<Object> mPermissionCallbacks = new SparseArray<>();
    private ActivityResponseConfig config;

    public void setConfig(ActivityResponseConfig config) {
        if (config != null) {
            this.config = config;
        }
    }

    ActivityResponseConfig getConfig() {
        if (config == null) {
            return globalConfig;
        }
        return config;
    }

    /**
     * stores the delegate state to the bundle
     *
     * @param outState outState
     * @see Activity#onSaveInstanceState(Bundle)
     */
    public void onSaveInstanceState(Bundle outState) {
        if (outState != null) {
            outState.putParcelable(BUNDLE_KEY, this);
        }
    }

    /**
     * restores the delegate state in case the activity has been destroyed
     *
     * @param savedInstanceState savedInstanceState
     * @see Activity#onRestoreInstanceState(Bundle)
     */
    @SuppressWarnings("unchecked")
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            ActivityResponseDelegate delegate = savedInstanceState.getParcelable(BUNDLE_KEY);
            if (delegate != null) {
                uniqueActivityResultRequestCode = delegate.uniqueActivityResultRequestCode;
                uniquePermissionRequestCode = delegate.uniquePermissionRequestCode;
                mActivityResultCallbacks = delegate.mActivityResultCallbacks;
                mPermissionCallbacks = delegate.mPermissionCallbacks;
            }
        }
    }

    /**
     * @see ActivityResponseDelegate#startActivityForResult(Intent, int, Bundle, Class, Bundle)
     */
    public void startActivityForResult(
            Intent intent,
            final Class<? extends ActivityResponseCallback<? super T>> callback) {
        startActivityForResult(intent, nextActivityResultRequestCode(), callback);
    }

    /**
     * @see ActivityResponseDelegate#startActivityForResult(Intent, int, Bundle, Class, Bundle)
     */
    public void startActivityForResult(
            Intent intent,
            final Class<? extends ActivityResponseCallback<? super T>> callback, @Nullable Bundle callbackArguments) {
        startActivityForResult(intent, nextActivityResultRequestCode(), callback, callbackArguments);
    }

    /**
     * @see ActivityResponseDelegate#startActivityForResult(Intent, int, Bundle, Class, Bundle)
     */
    public void startActivityForResult(
            Intent intent,
            int requestCode,
            final Class<? extends ActivityResponseCallback<? super T>> callback) {
        startActivityForResult(intent, requestCode, null, callback, null);
    }

    /**
     * @see ActivityResponseDelegate#startActivityForResult(Intent, int, Bundle, Class, Bundle)
     */
    public void startActivityForResult(
            Intent intent,
            int requestCode,
            final Class<? extends ActivityResponseCallback<? super T>> callback, @Nullable Bundle callbackArguments) {
        startActivityForResult(intent, requestCode, null, callback, callbackArguments);
    }

    /**
     * @param callback          the class of the callback
     * @param callbackArguments the arguments passed to the callback
     * @see android.app.Activity#startActivityForResult(Intent, int, Bundle)
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void startActivityForResult(
            Intent intent,
            int requestCode,
            @Nullable Bundle options,
            final Class<? extends ActivityResponseCallback<? super T>> callback, @Nullable Bundle callbackArguments) {

        if (getConfig().isDebugLogsEnabled()) {
            Log.d(TAG, "startActivityForResult from " + mOwner + " with callback " + callback.getSimpleName());
        }

        T owner = mOwner.get();
        if (owner == null) {
            return;
        }

        mActivityResultCallbacks.put(requestCode, new CallbackHolder<>(callback, callbackArguments));

        if (owner instanceof Activity) {
            if (options != null) {
                ((Activity) owner).startActivityForResult(intent, requestCode, options);
            } else {
                ((Activity) owner).startActivityForResult(intent, requestCode);
            }
        } else if (owner instanceof Fragment) {
            if (options != null) {
                ((Fragment) owner).startActivityForResult(intent, requestCode, options);
            } else {
                ((Fragment) owner).startActivityForResult(intent, requestCode);
            }
        } else {
            throw new ClassCastException("owner must be an Activity or Fragment");
        }
    }

    /**
     * @see Activity#onActivityResult(int, int, Intent)
     */
    @SuppressWarnings("unchecked")
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        T owner = mOwner.get();
        if (owner == null) {
            return;
        }

        CallbackHolder<T> callbackHolder = (CallbackHolder<T>) mActivityResultCallbacks.get(requestCode);
        if (callbackHolder == null) {
            return;
        }
        ActivityResponseCallback<T> callback = callbackHolder.newCallback(owner);

        if (getConfig().isDebugLogsEnabled()) {
            Log.d(TAG, "onActivityResult from " + mOwner + " with callback " + callback);
        }

        if (callback != null) {
            callback.onActivityResult(requestCode, resultCode, data);
            mActivityResultCallbacks.remove(requestCode);
        }
    }

    /**
     * @see ActivityResponseDelegate#requestPermissions(String[], int, Class, Bundle, boolean)
     */
    public void requestPermissions(final @NonNull String[] permissions,
                                   final Class<? extends ActivityResponseCallback<? super T>> callback) {
        requestPermissions(permissions, nextPermissionRequestCode(), callback);
    }

    /**
     * @see ActivityResponseDelegate#requestPermissions(String[], int, Class, Bundle, boolean)
     */
    public void requestPermissions(final @NonNull String[] permissions,
                                   final Class<? extends ActivityResponseCallback<? super T>> callback,
                                   @Nullable Bundle callbackArguments) {
        requestPermissions(permissions, nextPermissionRequestCode(), callback, callbackArguments);
    }

    /**
     * @see ActivityResponseDelegate#requestPermissions(String[], int, Class, Bundle, boolean)
     */
    public void requestPermissions(final @NonNull String[] permissions, final int requestCode,
                                   final Class<? extends ActivityResponseCallback<? super T>> callback) {
        requestPermissions(permissions, requestCode, callback, null, true);
    }


    /**
     * @see ActivityResponseDelegate#requestPermissions(String[], int, Class, Bundle, boolean)
     */
    public void requestPermissions(final @NonNull String[] permissions, final int requestCode,
                                   final Class<? extends ActivityResponseCallback<? super T>> callback,
                                   @Nullable Bundle callbackArguments) {
        requestPermissions(permissions, requestCode, callback, callbackArguments, true);
    }

    /**
     * @param callback          the class of the callback
     * @param callbackArguments the arguments passed to the callback
     * @param showRational      if true the callback will ask to show a rationale
     *                          set this to false when calling this after showing the rational to avoid an infinite loop
     * @see android.app.Activity#requestPermissions(String[], int)
     */
    public void requestPermissions(final @NonNull String[] permissions, final int requestCode,
                                   final Class<? extends ActivityResponseCallback<? super T>> callback,
                                   @Nullable Bundle callbackArguments,
                                   boolean showRational) {


        if (getConfig().isDebugLogsEnabled()) {
            Log.d(TAG, "requestPermissions from " + mOwner + " with callback " + callback.getSimpleName() + ". Permissions: " + Arrays.asList(permissions).toString());
        }

        T owner = mOwner.get();
        if (owner == null) {
            return;
        }
        Activity context;
        if (owner instanceof Activity) {
            context = (Activity) owner;
        } else if (owner instanceof Fragment) {
            context = ((Fragment) owner).getActivity();
        } else {
            throw new ClassCastException("owner must be an Activity or Fragment");
        }

        mPermissionCallbacks.put(requestCode, new CallbackHolder<>(callback, callbackArguments));

        // check if we already have all permissions required
        if (PermissionUtils.hasSelfPermissions(context, permissions)) {
            int[] grantResults = new int[permissions.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        String[] showRationalPermissions;
        if (showRational && (showRationalPermissions = PermissionUtils.getShowRequestPermissionRationale(context, permissions)).length > 0) {

            @SuppressWarnings("unchecked")
            CallbackHolder<T> callbackHolder = (CallbackHolder<T>) mPermissionCallbacks.get(requestCode);
            if (callbackHolder == null) {
                return;
            }
            ActivityResponseCallback<T> callbackObject = callbackHolder.newCallback(owner);
            if (callbackObject != null) {
                callbackObject.setOwner(owner);
                callbackObject.showRationale(requestCode, showRationalPermissions);
                return;
            }
        }

        if (owner instanceof Activity) {
            ActivityCompat.requestPermissions((Activity) owner, permissions, requestCode);
        } else {
            ((Fragment) owner).requestPermissions(permissions, requestCode);
        }
    }

    /**
     * @see Activity#onRequestPermissionsResult(int, String[], int[])
     */
    @SuppressWarnings("unchecked")
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        T owner = mOwner.get();
        if (owner == null) {
            return;
        }

        CallbackHolder<T> callbackHolder = (CallbackHolder<T>) mPermissionCallbacks.get(requestCode);
        if (callbackHolder == null) {
            return;
        }
        ActivityResponseCallback<T> callback = callbackHolder.newCallback(owner);

        if (getConfig().isDebugLogsEnabled()) {
            Log.d(TAG, "onRequestPermissionsResult from " + mOwner + " with callback " + callback + ". Permissions: " + Arrays.asList(permissions).toString());
        }

        if (callback != null) {
            callback.onRequestPermissionsResult(requestCode, permissions, grantResults);
            mPermissionCallbacks.remove(requestCode);
        }
    }

    private int nextPermissionRequestCode() {
        return uniquePermissionRequestCode++;
    }

    private int nextActivityResultRequestCode() {
        return uniqueActivityResultRequestCode++;
    }


    // parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(uniqueActivityResultRequestCode);
        dest.writeInt(uniquePermissionRequestCode);
        dest.writeSparseArray(mActivityResultCallbacks);
        dest.writeSparseArray(mPermissionCallbacks);
    }

    @SuppressWarnings("unchecked")
    protected ActivityResponseDelegate<T> readFromParcel(Parcel in) {
        uniqueActivityResultRequestCode = in.readInt();
        uniquePermissionRequestCode = in.readInt();
        mActivityResultCallbacks = in.readSparseArray(CallbackHolder.class.getClassLoader());
        mPermissionCallbacks = in.readSparseArray(CallbackHolder.class.getClassLoader());
        return this;
    }

    public static final Creator<ActivityResponseDelegate> CREATOR = new Creator<ActivityResponseDelegate>() {
        @Override
        public ActivityResponseDelegate createFromParcel(Parcel in) {
            return new ActivityResponseDelegate().readFromParcel(in);
        }

        @Override
        public ActivityResponseDelegate[] newArray(int size) {
            return new ActivityResponseDelegate[size];
        }
    };
}
