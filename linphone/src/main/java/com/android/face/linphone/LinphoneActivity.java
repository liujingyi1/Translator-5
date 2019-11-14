package com.android.face.linphone;

/*
 LinphoneActivity.java
 Copyright (C) 2012  Belledonne Communications, Grenoble, France

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.face.R;
import com.android.face.linphone.call.CallActivity;
import com.android.face.linphone.call.CallIncomingActivity;
import com.android.face.linphone.call.CallOutgoingActivity;
import com.android.face.linphone.compatibility.Compatibility;
import com.android.face.linphone.fragments.AccountPreferencesFragment;
import com.android.face.linphone.fragments.DialerFragment;
import com.android.face.linphone.fragments.EmptyFragment;
import com.android.face.linphone.fragments.FragmentsAvailable;
import com.android.face.linphone.fragments.SettingsFragment;
import com.android.face.linphone.fragments.StatusFragment;
import com.android.face.linphone.manager.ContactsManager;
import com.android.face.linphone.manager.LinphoneManager;
import com.android.face.linphone.utils.LinphoneUtils;
import com.android.face.linphone.utils.Logger;
import com.android.face.linphone.xmlrpc.XmlRpcHelper;
import com.android.face.linphone.xmlrpc.XmlRpcListenerBase;

import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCall.State;
import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneChatRoom;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCore.RegistrationState;
import org.linphone.core.LinphoneCoreListenerBase;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.core.Reason;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Sylvain Berfini
 */
public class LinphoneActivity extends Activity implements OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "LinphoneActivity";

    private static final int SETTINGS_ACTIVITY = 123;
    private static final int CALL_ACTIVITY = 19;
    private static final int PERMISSIONS_REQUEST_OVERLAY = 206;
    private static final int PERMISSIONS_REQUEST_SYNC = 207;
    private static final int PERMISSIONS_REQUEST_CONTACTS = 208;
    private static final int PERMISSIONS_RECORD_AUDIO_ECHO_CANCELLER = 209;
    private static final int PERMISSIONS_READ_EXTERNAL_STORAGE_DEVICE_RINGTONE = 210;
    private static final int PERMISSIONS_RECORD_AUDIO_ECHO_TESTER = 211;

    private static LinphoneActivity instance;

    private StatusFragment statusFragment;
    private RelativeLayout mTopBar;
    private ImageView cancel;
    private FragmentsAvailable pendingFragmentTransaction, currentFragment;
    private Fragment fragment;
    private Fragment.SavedState dialerSavedState;
    private boolean newProxyConfig;
    private boolean isTrialAccount = false;
    private OrientationEventListener mOrientationHelper;
    private LinphoneCoreListenerBase mListener;

    private DrawerLayout sideMenu;
    private RelativeLayout sideMenuContent, quitLayout, defaultAccount;
    private ListView accountsList, sideMenuItemList;
    private ImageView menu;
    private boolean fetchedContactsOnce = false;
    private boolean doNotGoToCallActivity = false;
    private List<String> sideMenuItems;
    private boolean callTransfer = false;

    public static final boolean isInstanciated() {
        return instance != null;
    }

    public static final LinphoneActivity instance() {
        if (instance != null)
            return instance;
        throw new RuntimeException("LinphoneActivity not instantiated yet");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //This must be done before calling super.onCreate().
        super.onCreate(savedInstanceState);

        if (getResources().getBoolean(R.bool.orientation_portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        boolean useFirstLoginActivity = getResources().getBoolean(R.bool.display_account_assistant_at_first_start);

        Logger.d(TAG, "useFirstLoginActivity:" + useFirstLoginActivity);

        if (savedInstanceState == null && (useFirstLoginActivity && LinphonePreferences.instance().isFirstLaunch())) {
            Logger.d(TAG, "isFirstLaunch is true");

            // add by David for open video setting default
            LinphonePreferences.instance().setInitiateVideoCall(true);
            LinphonePreferences.instance().setAutomaticallyAcceptVideoRequests(true);
            LinphonePreferences.instance().setVideoPreset("custom");
            LinphonePreferences.instance().setPreferredVideoFps(30);
            // add end

            if (LinphonePreferences.instance().getAccountCount() > 0) {
                Logger.d(TAG, "getAccountCount > 0");
                LinphonePreferences.instance().firstLaunchSuccessful();
            }
        }

        if (getIntent() != null && getIntent().getExtras() != null) {
            newProxyConfig = getIntent().getExtras().getBoolean("isNewProxyConfig");
        }

        if (getResources().getBoolean(R.bool.use_linphone_tag)) {
            if (getPackageManager().checkPermission(Manifest.permission.WRITE_SYNC_SETTINGS, getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                checkSyncPermission();
            } else {
                ContactsManager.getInstance().initializeSyncAccount(getApplicationContext(), getContentResolver());
            }
        } else {
            ContactsManager.getInstance().initializeContactManager(getApplicationContext(), getContentResolver());
        }

        setContentView(R.layout.main);
        instance = this;
        pendingFragmentTransaction = FragmentsAvailable.UNKNOW;

        initButtons();
        initSideMenu();

        currentFragment = FragmentsAvailable.EMPTY;
        if (savedInstanceState == null) {
            changeCurrentFragment(FragmentsAvailable.DIALER, getIntent().getExtras());
        } else {
            currentFragment = (FragmentsAvailable) savedInstanceState.getSerializable("currentFragment");
        }

        mListener = new LinphoneCoreListenerBase() {
            @Override
            public void messageReceived(LinphoneCore lc, LinphoneChatRoom cr, LinphoneChatMessage message) {
            }

            @Override
            public void registrationState(LinphoneCore lc, LinphoneProxyConfig proxy, LinphoneCore.RegistrationState state, String smessage) {
                if (state.equals(RegistrationState.RegistrationCleared)) {
                    if (lc != null) {
                        LinphoneAuthInfo authInfo = lc.findAuthInfo(proxy.getIdentity(), proxy.getRealm(), proxy.getDomain());
                        if (authInfo != null)
                            lc.removeAuthInfo(authInfo);
                    }
                }

                refreshAccounts();

                if (getResources().getBoolean(R.bool.use_phone_number_validation)) {
                    if (state.equals(RegistrationState.RegistrationOk)) {
                        LinphoneManager.getInstance().isAccountWithAlias();
                    }
                }

                if (state.equals(RegistrationState.RegistrationFailed) && newProxyConfig) {
                    newProxyConfig = false;
                    if (proxy.getError() == Reason.BadCredentials) {
                        //displayCustomToast(getString(R.string.error_bad_credentials), Toast.LENGTH_LONG);
                    }
                    if (proxy.getError() == Reason.Unauthorized) {
                        displayCustomToast(getString(R.string.error_unauthorized), Toast.LENGTH_LONG);
                    }
                    if (proxy.getError() == Reason.IOError) {
                        displayCustomToast(getString(R.string.error_io_error), Toast.LENGTH_LONG);
                    }
                }
            }

            @Override
            public void callState(LinphoneCore lc, LinphoneCall call, LinphoneCall.State state, String message) {
                if (state == State.IncomingReceived) {
                    startActivity(new Intent(LinphoneActivity.instance(), CallIncomingActivity.class));
                } else if (state == State.OutgoingInit || state == State.OutgoingProgress) {
                    startActivity(new Intent(LinphoneActivity.instance(), CallOutgoingActivity.class));
                } else if (state == State.CallEnd || state == State.Error || state == State.CallReleased) {
                    resetClassicMenuLayoutAndGoBackToCallIfStillRunning();
                }
            }
        };

        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                rotation = 0;
                break;
            case Surface.ROTATION_90:
                rotation = 90;
                break;
            case Surface.ROTATION_180:
                rotation = 180;
                break;
            case Surface.ROTATION_270:
                rotation = 270;
                break;
        }

        LinphoneManager.getLc().setDeviceRotation(rotation);
        mAlwaysChangingPhoneAngle = rotation;
    }

    private void initButtons() {
        mTopBar = (RelativeLayout) findViewById(R.id.top_bar);

        cancel = (ImageView) findViewById(R.id.cancel);
        cancel.setOnClickListener(this);
    }

    private boolean isTablet() {
        return getResources().getBoolean(R.bool.isTablet);
    }

    public void showStatusBar() {
        if (isTablet()) {
            return;
        }

        if (statusFragment != null && !statusFragment.isVisible()) {
            statusFragment.getView().setVisibility(View.VISIBLE);
        }
        findViewById(R.id.status).setVisibility(View.VISIBLE);
    }

    public void isNewProxyConfig() {
        newProxyConfig = true;
    }

    private void changeCurrentFragment(FragmentsAvailable newFragmentType, Bundle extras) {
        changeCurrentFragment(newFragmentType, extras, false);
    }

    private void changeCurrentFragment(FragmentsAvailable newFragmentType, Bundle extras, boolean withoutAnimation) {
        if (currentFragment == FragmentsAvailable.DIALER) {
            try {
                DialerFragment dialerFragment = DialerFragment.instance();
                dialerSavedState = getFragmentManager().saveFragmentInstanceState(dialerFragment);
            } catch (Exception e) {
            }
        }

        fragment = null;

        switch (newFragmentType) {
            case DIALER:
                fragment = new DialerFragment();
                if (extras == null) {
                    fragment.setInitialSavedState(dialerSavedState);
                }
                break;
            case SETTINGS:
                fragment = new SettingsFragment();
                break;
            case ACCOUNT_SETTINGS:
                fragment = new AccountPreferencesFragment();
                break;
            case EMPTY:
                fragment = new EmptyFragment();
                break;
            default:
                break;
        }

        if (fragment != null) {
            fragment.setArguments(extras);
            changeFragment(fragment, newFragmentType, withoutAnimation);
        }
    }

    private void changeFragment(Fragment newFragment, FragmentsAvailable newFragmentType, boolean withoutAnimation) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        if (newFragmentType != FragmentsAvailable.DIALER) {
            transaction.addToBackStack(newFragmentType.toString());
        } else {
            while (fm.getBackStackEntryCount() > 0) {
                fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        }

        transaction.replace(R.id.fragmentContainer, newFragment, newFragmentType.toString());
        transaction.commitAllowingStateLoss();
        fm.executePendingTransactions();

        currentFragment = newFragmentType;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.cancel) {
            hideTopBar();
            displayDialer();
        }
    }

    public void hideTopBar() {
        mTopBar.setVisibility(View.GONE);
    }

    @SuppressWarnings("incomplete-switch")
    public void selectMenu(FragmentsAvailable menuToSelect) {
        currentFragment = menuToSelect;

        switch (menuToSelect) {
            case SETTINGS:
            case ACCOUNT_SETTINGS:
                mTopBar.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void updateDialerFragment(DialerFragment fragment) {
        // Hack to maintain soft input flags
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    public void goToDialerFragment() {
        changeCurrentFragment(FragmentsAvailable.DIALER, null);
    }

    public void updateStatusFragment(StatusFragment fragment) {
        statusFragment = fragment;
    }

    public void displaySettings() {
        changeCurrentFragment(FragmentsAvailable.SETTINGS, null);
    }

    public void displayDialer() {
        changeCurrentFragment(FragmentsAvailable.DIALER, null);
    }

    public void displayAccountSettings(int accountNumber) {
        Bundle bundle = new Bundle();
        bundle.putInt("Account", accountNumber);
        changeCurrentFragment(FragmentsAvailable.ACCOUNT_SETTINGS, bundle);
        //settings.setSelected(true);
    }

    public void displayCustomToast(final String message, final int duration) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast, (ViewGroup) findViewById(R.id.toastRoot));

        TextView toastText = (TextView) layout.findViewById(R.id.toastMessage);
        toastText.setText(message);

        final Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(duration);
        toast.setView(layout);
        toast.show();
    }

    public void startIncallActivity(LinphoneCall currentCall) {
        Intent intent = new Intent(this, CallActivity.class);
        startOrientationSensor();
        startActivityForResult(intent, CALL_ACTIVITY);
    }

    /**
     * Register a sensor to track phoneOrientation changes
     */
    private synchronized void startOrientationSensor() {
        if (mOrientationHelper == null) {
            mOrientationHelper = new LocalOrientationEventListener(this);
        }
        mOrientationHelper.enable();
    }

    private int mAlwaysChangingPhoneAngle = -1;

    private class LocalOrientationEventListener extends OrientationEventListener {
        public LocalOrientationEventListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(final int o) {
            if (o == OrientationEventListener.ORIENTATION_UNKNOWN) {
                return;
            }

            int degrees = 270;
            if (o < 45 || o > 315)
                degrees = 0;
            else if (o < 135)
                degrees = 90;
            else if (o < 225)
                degrees = 180;

            if (mAlwaysChangingPhoneAngle == degrees) {
                return;
            }
            mAlwaysChangingPhoneAngle = degrees;

            Logger.d(TAG, "Phone orientation changed to " + degrees);
            int rotation = (360 - degrees) % 360;
            LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
            if (lc != null) {
                lc.setDeviceRotation(rotation);
                LinphoneCall currentCall = lc.getCurrentCall();
                if (currentCall != null && currentCall.cameraEnabled() && currentCall.getCurrentParamsCopy().getVideoEnabled()) {
                    lc.updateCall(currentCall, null);
                }
            }
        }
    }

    public Boolean isCallTransfer() {
        return callTransfer;
    }

    private void initInCallMenuLayout(final boolean callTransfer) {
        selectMenu(FragmentsAvailable.DIALER);
        DialerFragment dialerFragment = DialerFragment.instance();
        if (dialerFragment != null) {
            ((DialerFragment) dialerFragment).resetLayout(callTransfer);
        }
    }

    public void resetClassicMenuLayoutAndGoBackToCallIfStillRunning() {
        DialerFragment dialerFragment = DialerFragment.instance();
        if (dialerFragment != null) {
            ((DialerFragment) dialerFragment).resetLayout(true);
        }

        if (LinphoneManager.isInstanciated() && LinphoneManager.getLc().getCallsNb() > 0) {
            LinphoneCall call = LinphoneManager.getLc().getCalls()[0];
            if (call.getState() == LinphoneCall.State.IncomingReceived) {
                startActivity(new Intent(LinphoneActivity.this, CallIncomingActivity.class));
            } else {
                startIncallActivity(call);
            }
        }
    }

    public void quit() {
        finish();
        stopService(new Intent(Intent.ACTION_MAIN).setClass(this, LinphoneService.class));
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (pendingFragmentTransaction != FragmentsAvailable.UNKNOW) {
            changeCurrentFragment(pendingFragmentTransaction, null, true);
            selectMenu(pendingFragmentTransaction);
            pendingFragmentTransaction = FragmentsAvailable.UNKNOW;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_FIRST_USER && requestCode == SETTINGS_ACTIVITY) {
            if (data.getExtras().getBoolean("Exit", false)) {
                quit();
            } else {
                pendingFragmentTransaction = (FragmentsAvailable) data.getExtras().getSerializable("FragmentToDisplay");
            }
        } else if (resultCode == Activity.RESULT_FIRST_USER && requestCode == CALL_ACTIVITY) {
            getIntent().putExtra("PreviousActivity", CALL_ACTIVITY);
            callTransfer = data == null ? false : data.getBooleanExtra("Transfer", false);

            if (LinphoneManager.getLc().getCallsNb() > 0) {
                initInCallMenuLayout(callTransfer);
            } else {
                resetClassicMenuLayoutAndGoBackToCallIfStillRunning();
            }
        } else if (requestCode == PERMISSIONS_REQUEST_OVERLAY) {
            if (Compatibility.canDrawOverlays(this)) {
                LinphonePreferences.instance().enableOverlay(true);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onPause() {
        getIntent().putExtra("PreviousActivity", 0);

        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null) {
            lc.removeListener(mListener);
        }
        callTransfer = false;

        super.onPause();
    }

    public boolean checkAndRequestOverlayPermission() {
        Logger.i(TAG, "[Permission] Draw overlays permission is " + (Compatibility.canDrawOverlays(this) ? "granted" : "denied"));
        if (!Compatibility.canDrawOverlays(this)) {
            Logger.i(TAG, "[Permission] Asking for overlay");
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, PERMISSIONS_REQUEST_OVERLAY);
            return false;
        }
        return true;
    }

    public void checkAndRequestRecordAudioPermissionForEchoCanceller() {
        checkAndRequestPermission(Manifest.permission.RECORD_AUDIO, PERMISSIONS_RECORD_AUDIO_ECHO_CANCELLER);
    }

    public void checkAndRequestRecordAudioPermissionsForEchoTester() {
        checkAndRequestPermission(Manifest.permission.RECORD_AUDIO, PERMISSIONS_RECORD_AUDIO_ECHO_TESTER);
    }

    public void checkAndRequestReadExternalStoragePermissionForDeviceRingtone() {
        checkAndRequestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSIONS_READ_EXTERNAL_STORAGE_DEVICE_RINGTONE);
    }

    private void checkSyncPermission() {
        checkAndRequestPermission(Manifest.permission.WRITE_SYNC_SETTINGS, PERMISSIONS_REQUEST_SYNC);
    }

    public void checkAndRequestPermission(String permission, int result) {
        int permissionGranted = getPackageManager().checkPermission(permission, getPackageName());
        Logger.i(TAG, "[Permission] " + permission + " is " + (permissionGranted == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));

        if (permissionGranted != PackageManager.PERMISSION_GRANTED) {
            if (LinphonePreferences.instance().firstTimeAskingForPermission(permission) || ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                Logger.i(TAG, "[Permission] Asking for " + permission);
                ActivityCompat.requestPermissions(this, new String[]{permission}, result);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (permissions.length <= 0)
            return;

        int readContactsI = -1;
        for (int i = 0; i < permissions.length; i++) {
            Logger.i(TAG, "[Permission] " + permissions[i] + " is " + (grantResults[i] == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));
            if (permissions[i] == Manifest.permission.READ_CONTACTS)
                readContactsI = i;
        }

        switch (requestCode) {
            case PERMISSIONS_REQUEST_SYNC:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ContactsManager.getInstance().initializeSyncAccount(getApplicationContext(), getContentResolver());
                } else {
                    ContactsManager.getInstance().initializeContactManager(getApplicationContext(), getContentResolver());
                }
                break;
            case PERMISSIONS_RECORD_AUDIO_ECHO_CANCELLER:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ((SettingsFragment) fragment).startEchoCancellerCalibration();
                } else {
                    ((SettingsFragment) fragment).echoCalibrationFail();
                }
                break;
            case PERMISSIONS_READ_EXTERNAL_STORAGE_DEVICE_RINGTONE:
                if (readContactsI >= 0 && grantResults[readContactsI] == PackageManager.PERMISSION_GRANTED) {
                    ContactsManager.getInstance().enableContactsAccess();
                }
                if (!fetchedContactsOnce) {
                    ContactsManager.getInstance().enableContactsAccess();
                    ContactsManager.getInstance().fetchContactsAsync();
                    fetchedContactsOnce = true;
                }
                if (permissions[0].compareTo(Manifest.permission.READ_EXTERNAL_STORAGE) != 0)
                    break;
                boolean enableRingtone = (grantResults[0] == PackageManager.PERMISSION_GRANTED);
                LinphonePreferences.instance().enableDeviceRingtone(enableRingtone);
                LinphoneManager.getInstance().enableDeviceRingtone(enableRingtone);
                break;
            case PERMISSIONS_RECORD_AUDIO_ECHO_TESTER:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    ((SettingsFragment) fragment).startEchoTester();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ArrayList<String> permissionsList = new ArrayList<String>();

        int contacts = getPackageManager().checkPermission(Manifest.permission.READ_CONTACTS, getPackageName());
        Logger.i(TAG, "[Permission] Contacts permission is " + (contacts == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));

        int readPhone = getPackageManager().checkPermission(Manifest.permission.READ_PHONE_STATE, getPackageName());
        Logger.i(TAG, "[Permission] Read phone state permission is " + (readPhone == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));

        int ringtone = getPackageManager().checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, getPackageName());
        Logger.i(TAG, "[Permission] Read external storage for ring tone permission is " + (ringtone == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));

        if (ringtone != PackageManager.PERMISSION_GRANTED) {
            if (LinphonePreferences.instance().firstTimeAskingForPermission(Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Logger.i(TAG, "[Permission] Asking for read external storage for ring tone");
                permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
        if (readPhone != PackageManager.PERMISSION_GRANTED) {
            if (LinphonePreferences.instance().firstTimeAskingForPermission(Manifest.permission.READ_PHONE_STATE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                Logger.i(TAG, "[Permission] Asking for read phone state");
                permissionsList.add(Manifest.permission.READ_PHONE_STATE);
            }
        }
        if (contacts != PackageManager.PERMISSION_GRANTED) {
            if (LinphonePreferences.instance().firstTimeAskingForPermission(Manifest.permission.READ_CONTACTS) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                Logger.i(TAG, "[Permission] Asking for contacts");
                permissionsList.add(Manifest.permission.READ_CONTACTS);
            }
        } else {
            if (!fetchedContactsOnce) {
                ContactsManager.getInstance().enableContactsAccess();
                ContactsManager.getInstance().fetchContactsAsync();
                fetchedContactsOnce = true;
            }
        }

        if (permissionsList.size() > 0) {
            String[] permissions = new String[permissionsList.size()];
            permissions = permissionsList.toArray(permissions);
            ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_READ_EXTERNAL_STORAGE_DEVICE_RINGTONE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("currentFragment", currentFragment);
        outState.putBoolean("fetchedContactsOnce", fetchedContactsOnce);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        fetchedContactsOnce = savedInstanceState.getBoolean("fetchedContactsOnce");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!LinphoneService.isReady()) {
            startService(new Intent(Intent.ACTION_MAIN).setClass(this, LinphoneService.class));
        }

        LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (lc != null) {
            lc.addListener(mListener);
        }

        if (isTablet()) {
            // Prevent fragmentContainer2 to be visible when rotating the device
            LinearLayout ll = (LinearLayout) findViewById(R.id.fragmentContainer2);
            if (currentFragment == FragmentsAvailable.DIALER
                    || currentFragment == FragmentsAvailable.SETTINGS
                    || currentFragment == FragmentsAvailable.ACCOUNT_SETTINGS) {
                ll.setVisibility(View.GONE);
            }
        }

        refreshAccounts();

        if (getResources().getBoolean(R.bool.enable_in_app_purchase)) {
            isTrialAccount();
        }

        if (LinphonePreferences.instance().isFriendlistsubscriptionEnabled() && LinphoneManager.getLc().getDefaultProxyConfig() != null) {
            LinphoneManager.getInstance().subscribeFriendList(true);
        } else {
            LinphoneManager.getInstance().subscribeFriendList(false);
        }

        LinphoneManager.getInstance().changeStatusToOnline();

        if (getIntent().getIntExtra("PreviousActivity", 0) != CALL_ACTIVITY && !doNotGoToCallActivity) {
            if (LinphoneManager.getLc().getCalls().length > 0) {
                LinphoneCall call = LinphoneManager.getLc().getCalls()[0];
                LinphoneCall.State callState = call.getState();

                if (callState == State.IncomingReceived) {
                    startActivity(new Intent(this, CallIncomingActivity.class));
                } else if (callState == State.OutgoingInit || callState == State.OutgoingProgress || callState == State.OutgoingRinging) {
                    startActivity(new Intent(this, CallOutgoingActivity.class));
                } else {
                    startIncallActivity(call);
                }
            }
        }
        doNotGoToCallActivity = false;
    }

    @Override
    protected void onDestroy() {
        if (mOrientationHelper != null) {
            mOrientationHelper.disable();
            mOrientationHelper = null;
        }

        instance = null;
        super.onDestroy();

        unbindDrawables(findViewById(R.id.topLayout));
        System.gc();
    }

    private void unbindDrawables(View view) {
        if (view != null && view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Bundle extras = intent.getExtras();
        if (extras != null && extras.getBoolean("Notification", false)) {
            if (LinphoneManager.getLc().getCallsNb() > 0) {
                LinphoneCall call = LinphoneManager.getLc().getCalls()[0];
                startIncallActivity(call);
            }
        } else {
            DialerFragment dialerFragment = DialerFragment.instance();
            if (dialerFragment != null) {
                if (extras != null && extras.containsKey("SipUriOrNumber")) {
                    if (getResources().getBoolean(R.bool.automatically_start_intercepted_outgoing_gsm_call)) {
                        ((DialerFragment) dialerFragment).newOutgoingCall(extras.getString("SipUriOrNumber"));
                    } else {
                        ((DialerFragment) dialerFragment).displayTextInAddressBar(extras.getString("SipUriOrNumber"));
                    }
                } else {
                    ((DialerFragment) dialerFragment).newOutgoingCall(intent);
                }
            }
            if (LinphoneManager.getLc().getCalls().length > 0) {
                // If a call is ringing, start incomingcallactivity
                Collection<State> incoming = new ArrayList<State>();
                incoming.add(LinphoneCall.State.IncomingReceived);
                if (LinphoneUtils.getCallsInState(LinphoneManager.getLc(), incoming).size() > 0) {
                    if (CallActivity.isInstanciated()) {
                        CallActivity.instance().startIncomingCallActivity();
                    } else {
                        startActivity(new Intent(this, CallIncomingActivity.class));
                    }
                }
            }
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (currentFragment == FragmentsAvailable.DIALER) {
                boolean isBackgroundModeActive = LinphonePreferences.instance().isBackgroundModeEnabled();
                if (!isBackgroundModeActive) {
                    stopService(new Intent(Intent.ACTION_MAIN).setClass(this, LinphoneService.class));
                    finish();
                } else if (LinphoneUtils.onKeyBackGoHome(this, keyCode, event)) {
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    //SIDE MENU
    public void openOrCloseSideMenu(boolean open) {
        if (open) {
            sideMenu.openDrawer(sideMenuContent);
        } else {
            sideMenu.closeDrawer(sideMenuContent);
        }
    }

    public void initSideMenu() {
        sideMenu = (DrawerLayout) findViewById(R.id.side_menu);
        sideMenuItems = new ArrayList<String>();
        sideMenuItems.add(getResources().getString(R.string.menu_settings));
        if (getResources().getBoolean(R.bool.enable_in_app_purchase)) {
            sideMenuItems.add(getResources().getString(R.string.inapp));
        }
        sideMenuContent = (RelativeLayout) findViewById(R.id.side_menu_content);
        sideMenuItemList = (ListView) findViewById(R.id.item_list);
        menu = (ImageView) findViewById(R.id.side_menu_button);

        sideMenuItemList.setAdapter(new ArrayAdapter<String>(this, R.layout.side_menu_item_cell, sideMenuItems));
        sideMenuItemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (sideMenuItemList.getAdapter().getItem(i).toString().equals(getString(R.string.menu_settings))) {
                    LinphoneActivity.instance().displaySettings();
                }
                openOrCloseSideMenu(false);
            }
        });

        initAccounts();

        menu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sideMenu.isDrawerVisible(Gravity.LEFT)) {
                    sideMenu.closeDrawer(sideMenuContent);
                } else {
                    sideMenu.openDrawer(sideMenuContent);
                }
            }
        });

        quitLayout = (RelativeLayout) findViewById(R.id.side_menu_quit);
        quitLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                LinphoneActivity.instance().quit();
            }
        });
    }

    private int getStatusIconResource(LinphoneCore.RegistrationState state) {
        try {
            if (state == RegistrationState.RegistrationOk) {
                return R.drawable.led_connected;
            } else if (state == RegistrationState.RegistrationProgress) {
                return R.drawable.led_inprogress;
            } else if (state == RegistrationState.RegistrationFailed) {
                return R.drawable.led_error;
            } else {
                return R.drawable.led_disconnected;
            }
        } catch (Exception e) {
            Logger.e(TAG, "Exception:" + e);
        }

        return R.drawable.led_disconnected;
    }

    private void displayMainAccount() {
        defaultAccount.setVisibility(View.VISIBLE);
        ImageView status = (ImageView) defaultAccount.findViewById(R.id.main_account_status);
        TextView address = (TextView) defaultAccount.findViewById(R.id.main_account_address);
        TextView displayName = (TextView) defaultAccount.findViewById(R.id.main_account_display_name);


        LinphoneProxyConfig proxy = LinphoneManager.getLc().getDefaultProxyConfig();
        if (proxy == null) {
            displayName.setText(getString(R.string.no_account));
            status.setVisibility(View.GONE);
            address.setText("");
            statusFragment.resetAccountStatus();
            LinphoneManager.getInstance().subscribeFriendList(false);

            defaultAccount.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    LinphoneActivity.instance().displayAccountSettings(0);
                    openOrCloseSideMenu(false);
                }
            });
        } else {
            address.setText(proxy.getAddress().asStringUriOnly());
            displayName.setText(LinphoneUtils.getAddressDisplayName(proxy.getAddress()));
            status.setImageResource(getStatusIconResource(proxy.getState()));
            status.setVisibility(View.VISIBLE);

            defaultAccount.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    LinphoneActivity.instance().displayAccountSettings(LinphonePreferences.instance().getDefaultAccountIndex());
                    openOrCloseSideMenu(false);
                }
            });
        }
    }

    public void refreshAccounts() {
        if (LinphoneManager.getLc().getProxyConfigList().length > 1) {
            accountsList.setVisibility(View.VISIBLE);
            accountsList.setAdapter(new AccountsListAdapter());
            accountsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (view != null) {
                        int position = Integer.parseInt(view.getTag().toString());
                        LinphoneActivity.instance().displayAccountSettings(position);
                    }
                    openOrCloseSideMenu(false);
                }
            });
        } else {
            accountsList.setVisibility(View.GONE);
        }
        displayMainAccount();
    }

    private void initAccounts() {
        accountsList = (ListView) findViewById(R.id.accounts_list);
        defaultAccount = (RelativeLayout) findViewById(R.id.default_account);
    }

    class AccountsListAdapter extends BaseAdapter {
        List<LinphoneProxyConfig> proxy_list;

        AccountsListAdapter() {
            proxy_list = new ArrayList<LinphoneProxyConfig>();
            refresh();
        }

        public void refresh() {
            proxy_list = new ArrayList<LinphoneProxyConfig>();
            for (LinphoneProxyConfig proxyConfig : LinphoneManager.getLc().getProxyConfigList()) {
                if (proxyConfig != LinphoneManager.getLc().getDefaultProxyConfig()) {
                    proxy_list.add(proxyConfig);
                }
            }
        }

        public int getCount() {
            if (proxy_list != null) {
                return proxy_list.size();
            } else {
                return 0;
            }
        }

        public Object getItem(int position) {
            return proxy_list.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = null;
            LinphoneProxyConfig lpc = (LinphoneProxyConfig) getItem(position);
            if (convertView != null) {
                view = convertView;
            } else {
                view = getLayoutInflater().inflate(R.layout.side_menu_account_cell, parent, false);
            }

            ImageView status = (ImageView) view.findViewById(R.id.account_status);
            TextView address = (TextView) view.findViewById(R.id.account_address);
            String sipAddress = lpc.getAddress().asStringUriOnly();

            address.setText(sipAddress);

            int nbAccounts = LinphonePreferences.instance().getAccountCount();
            int accountIndex = 0;

            for (int i = 0; i < nbAccounts; i++) {
                String username = LinphonePreferences.instance().getAccountUsername(i);
                String domain = LinphonePreferences.instance().getAccountDomain(i);
                String id = "sip:" + username + "@" + domain;
                if (id.equals(sipAddress) || sipAddress.startsWith(id)) {
                    accountIndex = i;
                    view.setTag(accountIndex);
                    break;
                }
            }
            status.setImageResource(getStatusIconResource(lpc.getState()));
            return view;
        }
    }

    //Inapp Purchase
    private void isTrialAccount() {
        if (LinphoneManager.getLc().getDefaultProxyConfig() != null && LinphonePreferences.instance().getInappPopupTime() != null) {
            XmlRpcHelper helper = new XmlRpcHelper();
            helper.isTrialAccountAsync(new XmlRpcListenerBase() {
                @Override
                public void onTrialAccountFetched(boolean isTrial) {
                    isTrialAccount = isTrial;
                    getExpirationAccount();
                }

                @Override
                public void onError(String error) {
                }
            }, LinphonePreferences.instance().getAccountUsername(LinphonePreferences.instance().getDefaultAccountIndex()), LinphonePreferences.instance().getAccountHa1(LinphonePreferences.instance().getDefaultAccountIndex()));
        }
    }

    private void getExpirationAccount() {
        if (LinphoneManager.getLc().getDefaultProxyConfig() != null && LinphonePreferences.instance().getInappPopupTime() != null) {
            XmlRpcHelper helper = new XmlRpcHelper();
            helper.getAccountExpireAsync(new XmlRpcListenerBase() {
                @Override
                public void onAccountExpireFetched(String result) {
                    if (result != null) {
                        long timestamp = Long.parseLong(result);

                        Calendar calresult = Calendar.getInstance();
                        calresult.setTimeInMillis(timestamp);

                        int diff = getDiffDays(calresult, Calendar.getInstance());
                        if (diff != -1 && diff <= getResources().getInteger(R.integer.days_notification_shown)) {
                            displayInappNotification(timestampToHumanDate(calresult));
                        }
                    }
                }

                @Override
                public void onError(String error) {
                }
            }, LinphonePreferences.instance().getAccountUsername(LinphonePreferences.instance().getDefaultAccountIndex()), LinphonePreferences.instance().getAccountHa1(LinphonePreferences.instance().getDefaultAccountIndex()));
        }
    }

    public void displayInappNotification(String date) {
        Timestamp now = new Timestamp(new Date().getTime());
        if (LinphonePreferences.instance().getInappPopupTime() != null && Long.parseLong(LinphonePreferences.instance().getInappPopupTime()) > now.getTime()) {
            return;
        } else {
            long newDate = now.getTime() + getResources().getInteger(R.integer.time_between_inapp_notification);
            LinphonePreferences.instance().setInappPopupTime(String.valueOf(newDate));
        }
        if (isTrialAccount) {
            LinphoneService.instance().displayInappNotification(String.format(getString(R.string.inapp_notification_trial_expire), date));
        } else {
            LinphoneService.instance().displayInappNotification(String.format(getString(R.string.inapp_notification_account_expire), date));
        }

    }

    private String timestampToHumanDate(Calendar cal) {
        SimpleDateFormat dateFormat;
        dateFormat = new SimpleDateFormat(getResources().getString(R.string.inapp_popup_date_format));
        return dateFormat.format(cal.getTime());
    }

    private int getDiffDays(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            return -1;
        }
        if (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)) {
            return cal1.get(Calendar.DAY_OF_YEAR) - cal2.get(Calendar.DAY_OF_YEAR);
        }
        return -1;
    }
}
