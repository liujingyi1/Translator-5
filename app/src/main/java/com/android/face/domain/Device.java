package com.android.face.domain;

import com.alibaba.fastjson.JSONObject;

public class Device {
    public Device(JSONObject js0x16, JSONObject js0x14, JSONObject js0x13) {
        //0x16
        network = new Network(js0x16);
        setNetworkWay = js0x16.getIntValue("isDHCP");

        //0x14
        deviceNo = js0x14.getString("deviceMac");
        bluetoothPassword = js0x14.getString("bluetoothPassword");
        doorOpenPassword = js0x14.getString("doorOpenPassword");
        doorOpenDelay = js0x14.getIntValue("doorOpenDelay");
        doorAlarmDelay = js0x14.getIntValue("doorAlarmDelay");

        //0x13
        deviceName = js0x13.getString("deviceName");
        streetID = js0x13.getString("streetID");
        committeeID = js0x13.getString("committeeID");
        villageID = js0x13.getString("villageID");
        buildingID = js0x13.getString("buildingID");
        longitude = js0x13.getDoubleValue("longitude");
        latitude = js0x13.getDoubleValue("latitude");
        productBrand = js0x13.getString("productBrand");
        productModel = js0x13.getString("productName");
        seriesType = js0x13.getIntValue("seriesType");
    }

    String deviceName;
    String deviceNo;
    String streetID;
    String committeeID;
    String villageID;
    String buildingID;
    String houseID;
    String bluetoothPassword;
    String doorOpenPassword;
    int doorOpenDelay;
    int doorAlarmDelay;
    Network network;
    int setNetworkWay;
    double longitude;
    double latitude;
    String type = "access";
    String productBrand;
    String productModel;
    int seriesType;

    class Network {
        Network(JSONObject js0x16) {
            deviceIP = js0x16.getString("ip");
            deviceMask = js0x16.getString("mask");
            deviceGateway = js0x16.getString("gateway");
            dnsServer = js0x16.getString("DNS");
            devicePort = js0x16.getString("port");
        }
        String deviceIP;
        String deviceMask;
        String deviceGateway;
        String dnsServer;
        String devicePort;
    }

    @Override
    public String toString() {



        return "deviceName:" + deviceName + ",deviceNo:" + deviceNo + ",streetID:" + streetID + ",committeeID:" + committeeID
                + ",villageID:" + villageID + ",buildingID:" + buildingID + ",houseID:" + houseID + ",bluetoothPassword:" + bluetoothPassword
                + ",doorOpenPassword:" + doorOpenPassword + ",doorOpenDelay:" + doorOpenDelay + ",doorAlarmDelay:" + doorAlarmDelay
                + ",setNetworkWay:" + setNetworkWay + ",longitude:" + longitude + ",latitude:" + latitude
                + ",type:" + type + ",productBrand:" + productBrand + ",productModel:" + productModel
                + ",seriesType:" + seriesType + ",deviceIP:" + network.deviceIP + ",deviceMask:" + network.deviceMask
                + ",deviceGateway:" + network.deviceGateway + ",dnsServer:" + network.dnsServer + ",devicePort:" + network.devicePort;
    }
}
