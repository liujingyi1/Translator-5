package com.android.face.permission;

import android.Manifest.permission;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

/**
 * Activity that requests permissions needed for activities exported from Contacts.
 */
public class RequestPermissionsActivity extends RequestPermissionsActivityBase {

    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            permission.CALL_PHONE,
            permission.READ_PHONE_STATE,
            permission.READ_CONTACTS,
            permission.ACCESS_COARSE_LOCATION,
            permission.WRITE_EXTERNAL_STORAGE,
            permission.READ_EXTERNAL_STORAGE,
            permission.CAMERA,
            permission.RECORD_AUDIO
    };

    @Override
    protected String[] getRequiredPermissions() {
        return REQUIRED_PERMISSIONS;
    }

    @Override
    protected String[] getDesiredPermissions() {
        return REQUIRED_PERMISSIONS;
    }

    public static boolean startPermissionActivity(Activity activity) {
        return startPermissionActivity(activity, REQUIRED_PERMISSIONS,
                RequestPermissionsActivity.class);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String permissions[], int[] grantResults) {
        if (permissions != null && permissions.length > 0
                && isAllGranted(permissions, grantResults)) {
            mPreviousActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(mPreviousActivityIntent);
            finish();
            overridePendingTransition(0, 0);
        } else {
            //Toast.makeText(this, R.string.missing_required_permission, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * M: Add for check basic permissions state.
     */
    public static boolean hasBasicPermissions(Context context) {
        return hasPermissions(context, REQUIRED_PERMISSIONS);
    }
}
