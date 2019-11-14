package com.android.face.linphone.utils;

import android.text.TextUtils;

import com.android.face.linphone.LinphonePreferences;
import com.android.face.linphone.LinphonePreferences.AccountBuilder;
import com.android.face.linphone.manager.LinphoneManager;
import com.android.rgk.common.db.DataOperator;
import com.android.rgk.common.db.bean.SIPAccountInfo;

import org.linphone.core.LinphoneAccountCreator;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneAddress.TransportType;
import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneProxyConfig;

public class LinphoneLogInUtils {

    private static final String TAG = "LinphoneLogInUtils";

    private static boolean accountCreated = false;

    public static void genericLogIn(String username, String password, String prefix, String domain) {
        if (accountCreated) {
            retryLogin(username, password, prefix, domain, TransportType.LinphoneTransportUdp);
        } else {
            logIn(username, password, null, prefix, domain, TransportType.LinphoneTransportUdp, false);
        }
    }

    public static void retryLogin(String username, String password, String prefix, String domain,
                                  LinphoneAddress.TransportType transport) {
        accountCreated = false;
        saveCreatedAccount(username, password, null, prefix, domain, transport);
    }

    private static void logIn(String username, String password, String ha1, String prefix, String
            domain, TransportType transport, boolean sendEcCalibrationResult) {
        saveCreatedAccount(username, password, ha1, prefix, domain, transport);
    }

    public static void saveCreatedAccount(String username, String password, String prefix, String ha1,
                                          String domain, TransportType transport) {
        saveCreatedAccount(username, password, prefix, ha1, domain, transport, SIPAccountInfo.TYPE_MAIN);
    }

    public static void saveCreatedAccount(String username, String password, String prefix, String ha1,
                                          String domain, TransportType transport, String sip_type) {
        if (accountCreated) {
            return;
        }

        SIPAccountInfo accout = new SIPAccountInfo();
        accout.domain = domain;
        accout.password = password;
        accout.user = username;
        accout.type = sip_type;
        DataOperator dataOperator = DataOperator.getInstance();
        dataOperator.deleteSIPByType(sip_type);
        dataOperator.insertSIP(accout);

        LinphoneProxyConfig[] prxCfgs = LinphoneManager.getLc().getProxyConfigList();
        for (int i = 0; i < prxCfgs.length; i++) {
            if (prxCfgs[i] != null) {
                try {
                    LinphoneAddress addr =
                            LinphoneCoreFactory.instance().createLinphoneAddress(prxCfgs[i].getIdentity());
                    LinphoneAuthInfo authInfo =
                            LinphoneManager.getLc().findAuthInfo(addr.getUserName(), null, addr.getDomain());
                    if (authInfo != null) {
                        LinphoneManager.getLc().removeAuthInfo(authInfo);
                    }
                } catch (LinphoneCoreException e) {
                    Logger.e(TAG, "LinphoneCoreException:" + e);
                }
                LinphoneManager.getLc().removeProxyConfig(prxCfgs[i]);
            }
        }
        LinphoneManager.getLc().setDefaultProxyConfig(null);
        LinphoneManager.getLc().refreshRegisters();

        LinphonePreferences mPrefs = LinphonePreferences.instance();
        LinphoneAccountCreator accountCreator;
        accountCreator = LinphoneCoreFactory.instance().createAccountCreator(LinphoneManager.getLc(), LinphonePreferences.instance().getXmlrpcUrl());
        accountCreator.setDomain("sip.linphone.org");
        accountCreator.setListener(mLinphoneAccountCreatorListener);

        username = LinphoneUtils.getDisplayableUsernameFromAddress(username);
        domain = LinphoneUtils.getDisplayableUsernameFromAddress(domain);

        boolean isMainAccountLinphoneDotOrg = domain.equals("sip.linphone.org");
        AccountBuilder builder = new LinphonePreferences.AccountBuilder(LinphoneManager.getLc())
                .setUsername(username)
                .setDomain(domain)
                .setHa1(ha1)
                .setPassword(password);

        if (prefix != null) {
            builder.setPrefix(prefix);
        }

        if (isMainAccountLinphoneDotOrg) {
            builder.setProxy(domain)
                    .setTransport(TransportType.LinphoneTransportTls);

            builder.setExpires("604800")
                    .setAvpfEnabled(true)
                    .setAvpfRRInterval(3)
                    .setQualityReportingCollector("sip:voip-metrics@sip.linphone.org")
                    .setQualityReportingEnabled(true)
                    .setQualityReportingInterval(180)
                    .setRealm("sip.linphone.org")
                    .setNoDefault(true);

            mPrefs.enabledFriendlistSubscription(true);

            mPrefs.setStunServer("stun.linphone.org");
            mPrefs.setIceEnabled(true);

            accountCreator.setPassword(password);
            accountCreator.setHa1(ha1);
            accountCreator.setUsername(username);
        } else {
            String forcedProxy = "";
            if (!TextUtils.isEmpty(forcedProxy)) {
                builder.setProxy(forcedProxy)
                        .setOutboundProxyEnabled(true)
                        .setAvpfRRInterval(5);
            }

            if (transport != null) {
                builder.setTransport(transport);
            }
        }

        try {
            builder.saveNewAccount();
            accountCreated = true;
        } catch (LinphoneCoreException e) {
            Logger.e(TAG, "LinphoneCoreException" + e);
        }
    }

    private static LinphoneAccountCreator.LinphoneAccountCreatorListener mLinphoneAccountCreatorListener
            = new LinphoneAccountCreator.LinphoneAccountCreatorListener() {
        @Override
        public void onAccountCreatorIsAccountUsed(LinphoneAccountCreator linphoneAccountCreator, LinphoneAccountCreator.Status status) {
            LinphonePreferences.instance().firstLaunchSuccessful();
            Logger.i(TAG, "onAccountCreatorIsAccountUsed");
        }

        @Override
        public void onAccountCreatorAccountCreated(LinphoneAccountCreator linphoneAccountCreator, LinphoneAccountCreator.Status status) {
            Logger.i(TAG, "onAccountCreatorAccountCreated");
        }

        @Override
        public void onAccountCreatorAccountActivated(LinphoneAccountCreator linphoneAccountCreator, LinphoneAccountCreator.Status status) {
            Logger.i(TAG, "onAccountCreatorAccountActivated");
        }

        @Override
        public void onAccountCreatorAccountLinkedWithPhoneNumber(LinphoneAccountCreator linphoneAccountCreator, LinphoneAccountCreator.Status status) {
            Logger.i(TAG, "onAccountCreatorAccountLinkedWithPhoneNumber");
        }

        @Override
        public void onAccountCreatorPhoneNumberLinkActivated(LinphoneAccountCreator linphoneAccountCreator, LinphoneAccountCreator.Status status) {
            Logger.i(TAG, "onAccountCreatorPhoneNumberLinkActivated");
        }

        @Override
        public void onAccountCreatorIsAccountActivated(LinphoneAccountCreator linphoneAccountCreator, LinphoneAccountCreator.Status status) {
            Logger.i(TAG, "onAccountCreatorIsAccountActivated");
        }

        @Override
        public void onAccountCreatorPhoneAccountRecovered(LinphoneAccountCreator linphoneAccountCreator, LinphoneAccountCreator.Status status) {
            Logger.i(TAG, "onAccountCreatorPhoneAccountRecovered");
        }

        @Override
        public void onAccountCreatorIsAccountLinked(LinphoneAccountCreator linphoneAccountCreator, LinphoneAccountCreator.Status status) {
            Logger.i(TAG, "onAccountCreatorIsAccountLinked");
        }

        @Override
        public void onAccountCreatorIsPhoneNumberUsed(LinphoneAccountCreator linphoneAccountCreator, LinphoneAccountCreator.Status status) {
            Logger.i(TAG, "onAccountCreatorIsPhoneNumberUsed");
        }

        @Override
        public void onAccountCreatorPasswordUpdated(LinphoneAccountCreator linphoneAccountCreator, LinphoneAccountCreator.Status status) {
            Logger.i(TAG, "onAccountCreatorPasswordUpdated");
        }
    };
}
