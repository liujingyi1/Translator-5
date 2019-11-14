package com.android.face.http.bean;

public class Etherent {
    public int isDHCP;
    public String ip;
    public String mask;
    public String gateway;
    public String DNS;

    public int getIsDHCP() {
        return isDHCP;
    }

    public void setIsDHCP(int isDHCP) {
        this.isDHCP = isDHCP;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getDNS() {
        return DNS;
    }

    public void setDNS(String DNS) {
        this.DNS = DNS;
    }
}
