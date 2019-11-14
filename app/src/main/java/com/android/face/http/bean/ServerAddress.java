package com.android.face.http.bean;

public class ServerAddress {
    public String httpIp;
    public String mqttIp;
    public String deviceId;
    public String center;
    public String sipIp;
    public String sipAccount;
    public String sipPassword;
    public String stunAddress;
    public String stunPort;

    public String getHttpIp() {
        return httpIp;
    }

    public void setHttpIp(String httpIp) {
        this.httpIp = httpIp;
    }

    public String getMqttIp() {
        return mqttIp;
    }

    public void setMqttIp(String mqttIp) {
        this.mqttIp = mqttIp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getCenter() {
        return center;
    }

    public void setCenter(String center) {
        this.center = center;
    }

    public String getSipIp() {
        return sipIp;
    }

    public void setSipIp(String sipIp) {
        this.sipIp = sipIp;
    }

    public String getSipAccount() {
        return sipAccount;
    }

    public void setSipAccount(String sipAccount) {
        this.sipAccount = sipAccount;
    }

    public String getSipPassword() {
        return sipPassword;
    }

    public void setSipPassword(String sipPassword) {
        this.sipPassword = sipPassword;
    }

    public String getStunAddress() {
        return stunAddress;
    }

    public void setStunAddress(String stunAddress) {
        this.stunAddress = stunAddress;
    }

    public String getStunPort() {
        return stunPort;
    }

    public void setStunPort(String stunPort) {
        this.stunPort = stunPort;
    }

    @Override
    public String toString() {
        return "ServerAddress{" +
                "httpIp='" + httpIp + '\'' +
                ", mqttIp='" + mqttIp + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", center='" + center + '\'' +
                ", sipIp='" + sipIp + '\'' +
                ", sipAccount='" + sipAccount + '\'' +
                ", sipPassword='" + sipPassword + '\'' +
                ", stunAddress='" + stunAddress + '\'' +
                ", stunPort='" + stunPort + '\'' +
                '}';
    }
}
