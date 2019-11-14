/*
KeepAliveReceiver.java
Copyright (C) 2010  Belledonne Communications, Grenoble, France

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
package com.android.face.linphone.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.android.face.R;
import com.android.face.linphone.LinphonePreferences;
import com.android.face.linphone.LinphoneService;
import com.android.face.linphone.compatibility.Compatibility;
import com.android.face.linphone.manager.LinphoneManager;
import com.android.face.linphone.utils.Logger;

import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneProxyConfig;

/*
 * Purpose of this receiver is to disable keep alives when screen is off
 * */
public class KeepAliveReceiver extends BroadcastReceiver {

    private static final String TAG = "KeepAliveReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!LinphoneService.isReady()) {
            return;
        } else {
            boolean isDebugEnabled = LinphonePreferences.instance().isDebugEnabled();
            LinphoneCoreFactory.instance().enableLogCollection(isDebugEnabled);
            LinphoneCoreFactory.instance().setDebugMode(isDebugEnabled, context.getString(R.string.app_name));
            LinphoneCore lc = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
            if (lc == null) return;

            String action = intent.getAction();
            if (action == null) {
                Logger.i(TAG, "[KeepAlive] Refresh registers");
                lc.refreshRegisters();
                //make sure iterate will have enough time, device will not sleep until exit from this method
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Logger.e(TAG, "Cannot sleep for 2s " + e);
                } finally {
                    //make sure the application will at least wakes up every 10 mn
                    Intent newIntent = new Intent(context, KeepAliveReceiver.class);
                    PendingIntent keepAlivePendingIntent = PendingIntent.getBroadcast(context, 0, newIntent, PendingIntent.FLAG_ONE_SHOT);

                    AlarmManager alarmManager = ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE));
                    Compatibility.scheduleAlarm(alarmManager, AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 600000, keepAlivePendingIntent);
                }
            }

            Logger.d(TAG, "[KeepAlive] always enable!");
            lc.enableKeepAlive(true);

            Logger.d(TAG, "[KeepAlive] refresh register when is not registered!");
            LinphoneProxyConfig lpc = lc.getDefaultProxyConfig();
            if (lpc != null && !lpc.isRegistered()) {
                lc.refreshRegisters();
            }
        }
    }
}
