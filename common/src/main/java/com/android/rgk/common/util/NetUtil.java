package com.android.rgk.common.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Pattern;

public class NetUtil {
    private final static String TAG = "NetUtil";

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) {
            return false;
        }

        boolean isConnected = info.isConnected();
        if (isConnected && info.getType() == ConnectivityManager.TYPE_WIFI) {
            NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
            isConnected = (nc == null || nc.hasCapability(nc.NET_CAPABILITY_VALIDATED));
        }
        return isConnected;
    }

    public static boolean hasConnect(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) {
            return false;
        }

        return info.isConnected();
    }

    /**
     * Reports the type of network to which the
     * info in this {@code NetworkInfo} pertains.
     *
     * @return one of {@link ConnectivityManager#TYPE_MOBILE}, {@link
     * ConnectivityManager#TYPE_WIFI}, {@link ConnectivityManager#TYPE_WIMAX}, {@link
     * ConnectivityManager#TYPE_ETHERNET},  {@link ConnectivityManager#TYPE_BLUETOOTH}, or other
     * types defined by {@link ConnectivityManager}
     */
    public static int getNetworkType(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info.getType();
    }


    public static boolean isWifi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        return cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;
    }

    public void requestNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.requestNetwork(new NetworkRequest.Builder().build(), new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
            }
        });
    }

    public static String getLocIpAddress(Context context) {
        String ipAddress = "";

        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface networkInterfaces = en.nextElement();
                Enumeration<InetAddress> addresses = networkInterfaces.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet6Address) {
                        LogUtil.w(TAG, "IPV6");
                        continue;
                    }
                    if (!address.isLoopbackAddress()) {
                        ipAddress = address.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            LogUtil.e(TAG, "Failed to get IP address");
            e.printStackTrace();
        }

        LogUtil.i(TAG, "IP address:" + ipAddress);
        return ipAddress;
    }

    public static String getWifiLocalAddress(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info.getType() == ConnectivityManager.TYPE_WIFI) {
            LogUtil.w(TAG, "getWifiLocalAddress-type:" + info.getType());
            return null;
        }


        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        int ipAddress = wifiInfo.getIpAddress();
        if (ipAddress == 0) {
            return null;
        }

        return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "." + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
    }

    /**
     * Ipv4 address check.
     */
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^(" + "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}" +
                    "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");

    /**
     * Check if valid IPV4 address.
     *
     * @param input the address string to check for validity.
     * @return True if the input parameter is a valid IPv4 address.
     */
    public static boolean isIPv4Address(String input) {
        return IPV4_PATTERN.matcher(input).matches();
    }

    /**
     * Get local Ip address.
     */
    public static InetAddress getLocalIPAddress() {
        Enumeration<NetworkInterface> enumeration = null;
        try {
            enumeration = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                NetworkInterface nif = enumeration.nextElement();
                Enumeration<InetAddress> inetAddresses = nif.getInetAddresses();
                if (inetAddresses != null) {
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        if (!inetAddress.isLoopbackAddress() && isIPv4Address(inetAddress.getHostAddress())) {
                            return inetAddress;
                        }
                    }
                }
            }
        }
        return null;
    }
}
