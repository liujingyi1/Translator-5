/* * Copyright 2018 Yan Zhenjie. * * Licensed under the Apache License, Version 2.0 (the "License"); * you may not use this file except in compliance with the License. * You may obtain a copy of the License at * *      http://www.apache.org/licenses/LICENSE-2.0 * * Unless required by applicable law or agreed to in writing, software * distributed under the License is distributed on an "AS IS" BASIS, * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. * See the License for the specific language governing permissions and * limitations under the License. */package com.android.face.http.controller;import android.text.TextUtils;import android.util.Log;import com.alibaba.fastjson.JSON;import com.alibaba.fastjson.JSONObject;import com.android.face.FaceApplication;import com.android.face.http.bean.Etherent;import com.android.face.http.bean.RoomNumbers;import com.android.face.http.bean.ServerAddress;import com.android.face.http.bean.SysVersion;import com.android.face.http.util.APKVersionCodeUtils;import com.android.face.linphone.utils.LinphoneLogInUtils;import com.android.face.linphone.utils.LinphoneUtils;import com.android.face.mcu.McuManager;import com.android.rgk.common.db.DataOperator;import com.android.rgk.common.lock.LockManager;import com.android.rgk.common.net.MqttManager;import com.yanzhenjie.andserver.annotation.GetMapping;import com.yanzhenjie.andserver.annotation.PostMapping;import com.yanzhenjie.andserver.annotation.RequestBody;import com.yanzhenjie.andserver.annotation.RequestMapping;import com.yanzhenjie.andserver.annotation.RequestParam;import com.yanzhenjie.andserver.annotation.RestController;import com.yanzhenjie.andserver.util.MediaType;import java.io.IOException;import java.util.ArrayList;import java.util.List;/** * Created by YanZhenjie on 2018/6/9. */@RestController@RequestMapping(path = "/config")class ConfigController {    private static final String TAG = "ConfigController";    @GetMapping(path = "/get/etherent", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)    String getEtherent() {        String etherent = LockManager.getInstance().getEthernet();        Log.i(TAG, etherent);        return etherent;    }    @PostMapping(path = "/set/etherent")    String setEtherent(@RequestBody Etherent etherent) throws IOException {        Log.i(TAG, "isDHCP="+etherent.isDHCP+" ip="+etherent.ip+" mask="+etherent.mask+" gateway="+etherent.gateway+" DNS="+etherent.DNS);        McuManager.setEthernet(etherent.isDHCP, etherent.ip, etherent.mask, etherent.gateway, etherent.DNS);        return "success";    }    @GetMapping(path = "/get/device/location", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)    String getDeviceLocation() {        return "";    }    @GetMapping(path = "/get/device/type", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)    String getDeviceType() {        return "";    }    @GetMapping(path = "/get/device/config", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)    String getDeviceConfig() {        return "";    }    @GetMapping(path = "/get/serverAddress", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)    String getServerAddress() {        String jsStr = McuManager.get0X16String("");        Log.i(TAG, "jsStr="+jsStr);        JSONObject js = JSON.parseObject(jsStr);        ServerAddress serverAddress = new ServerAddress();        if (js != null) {            serverAddress.setHttpIp(js.getString("httpUrl"));            serverAddress.setMqttIp(js.getString("mqttUrl"));            serverAddress.setDeviceId(js.getString("deviceName"));            serverAddress.setCenter(js.getString("centerSip"));            serverAddress.setSipIp(js.getString("sipUrl"));            serverAddress.setSipAccount(js.getString("sipAccount"));            serverAddress.setSipPassword(js.getString("sipPwd"));            serverAddress.setStunAddress("");            serverAddress.setStunPort("");        }        return JSON.toJSONString(serverAddress);    }    @PostMapping(path = "/set/serverAddress")    String setServerAddress(@RequestBody ServerAddress serverAddress) throws IOException {        String jsStr = McuManager.get0X16String("");        JSONObject js = JSON.parseObject(jsStr);        js.put("httpUrl", serverAddress.getHttpIp());        js.put("mqttUrl", serverAddress.getMqttIp());        js.put("deviceName", serverAddress.getDeviceId());        js.put("centerSip", serverAddress.getCenter());        js.put("sipUrl", serverAddress.getSipIp());        js.put("sipAccount", serverAddress.getSipAccount());        js.put("sipPwd", serverAddress.getSipPassword());        McuManager.webUpdate0X16(js.toJSONString());        Log.i(TAG, serverAddress.toString());        return "success";    }    @GetMapping(path = "/get/sysinfo/sipState", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)    boolean getSysinfoSipState() {        return LinphoneUtils.isRegistered() ? true : false;    }    @GetMapping(path = "/get/sysinfo/mqttState", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)    boolean getSysinfoMqttState() {        return MqttManager.getInstance().isConnected();    }    @GetMapping(path = "/get/sysinfo/version", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)    String getSysinfoVersion() {        SysVersion sysVersion = new SysVersion();        sysVersion.setSwVersion(APKVersionCodeUtils.getVerName(FaceApplication.getInstance()));        sysVersion.setReadHeadVersion(McuManager.getVersion());        sysVersion.setCloudCallVersion("0.0");        return JSON.toJSONString(sysVersion);    }    @GetMapping(path = "/get/sysinfo/deviceMac", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)    String getDeviceMac() {        return McuManager.getInstance().getDevcieMac();    }    @GetMapping(path = "/roomPhone/get", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)    String getRoomNumbers() {        List<RoomNumbers> roomNumbersList = new ArrayList<>();        DataOperator dataOperator = DataOperator.getInstance();        ArrayList<String> allRooms = dataOperator.getAllRooms();        for (String room:allRooms) {            List<String> numbers = dataOperator.getNumberByRoom(room);            RoomNumbers roomNumbers = new RoomNumbers(room, numbers);            roomNumbersList.add(roomNumbers);        }        return JSON.toJSONString(roomNumbersList);    }    @PostMapping(path = "/roomPhone/add", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)    String addRoomNumbers(@RequestParam("room") String room, @RequestParam("number") String number) {        DataOperator dataOperator = DataOperator.getInstance();        List<String> numberList = dataOperator.getNumberByRoom(room);        if (numberList != null && numberList.size() > 0) {            dataOperator.insertNumber(room, number, numberList.size());        }        return "success";    }    @PostMapping(path = "/roomPhone/delete", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)    String deleteRoomNumbers(@RequestParam("room") String room, @RequestParam("number") String number) {        DataOperator dataOperator = DataOperator.getInstance();        dataOperator.deleteByRoomAndNumber(room, number);        return "success";    }}