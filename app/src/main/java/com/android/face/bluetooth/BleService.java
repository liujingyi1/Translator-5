package com.android.face.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.android.rgk.common.lock.LockManager;

import java.util.UUID;

public class BleService extends Service {
    private static final String TAG = "BleService";

    public static final UUID UUID_SERVICE = UUID.fromString
            ("e89c82e7-aa9d-e7a7-91e6-8a80206d6a83");
    public static final UUID UUID_CHARACTERISTIC_READ = UUID.fromString
            ("e89c82e7-aa9d-e7a7-91e6-8a80206d6a73");
    public static final UUID UUID_CHARACTERISTIC_WRITE = UUID.fromString
            ("e89c82e7-aa9d-e7a7-91e6-8a80206d6a74");
    public static final UUID UUID_DESCRIPTOR = UUID.fromString
            ("e89c82e7-aa9d-e7a7-91e6-8a80206d6a72");

    private BluetoothAdapter mBlueToothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothManager mBluetoothManager;
    private BluetoothGattServer mGattServer;

    @Override
    public void onCreate() {
        super.onCreate();
        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        initGATTServer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initGATTServer() {

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .build();

        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(true)
                .build();

        //通过UUID_SERVICE构建
        AdvertiseData scanResponseData = new AdvertiseData.Builder()
                .addServiceUuid(new ParcelUuid(UUID_SERVICE))
                .setIncludeTxPowerLevel(true)
                .build();

        //广播创建成功之后的回调
        AdvertiseCallback callback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                Log.d(TAG, "BLE advertisement added successfully");
                initServices(BleService.this);
            }

            @Override
            public void onStartFailure(int errorCode) {
                Log.e(TAG, "Failed to add BLE advertisement, reason: " + errorCode);
            }
        };

        //部分设备不支持Ble中心
        BluetoothLeAdvertiser bluetoothLeAdvertiser = mBlueToothAdapter.getBluetoothLeAdvertiser();
        if (bluetoothLeAdvertiser == null) {
            Log.i(TAG, "BluetoothLeAdvertiser为null");
        }

        if (bluetoothLeAdvertiser != null) {
            bluetoothLeAdvertiser.startAdvertising(settings, advertiseData, scanResponseData, callback);
        }
    }

    /**
     * 初始化Gatt服务，主要是配置Gatt服务各种UUID
     *
     * @param context
     */
    private void initServices(Context context) {
        //创建GattServer服务器
        mGattServer = mBluetoothManager.openGattServer(context, bluetoothGattServerCallback);

        //这个指定的创建指定UUID的服务
        BluetoothGattService service = new BluetoothGattService(UUID_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY);

        //添加指定UUID的可读characteristic
        BluetoothGattCharacteristic characteristicRead = new BluetoothGattCharacteristic(
                UUID_CHARACTERISTIC_READ,
                BluetoothGattCharacteristic.PROPERTY_READ |
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ);
        //添加可读characteristic的descriptor
        BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(UUID_DESCRIPTOR, BluetoothGattCharacteristic.PERMISSION_WRITE);
        characteristicRead.addDescriptor(descriptor);
        service.addCharacteristic(characteristicRead);

        //添加指定UUID的可写characteristic
        BluetoothGattCharacteristic characteristicWrite = new BluetoothGattCharacteristic(UUID_CHARACTERISTIC_WRITE,
                BluetoothGattCharacteristic.PROPERTY_WRITE |
                        BluetoothGattCharacteristic.PROPERTY_READ |
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_WRITE);
        characteristicWrite.addDescriptor(descriptor);
        service.addCharacteristic(characteristicWrite);

        mGattServer.addService(service);
        Log.e(TAG, "2. initServices ok");
    }

    /**
     * 服务事件的回调
     */
    private BluetoothGattServerCallback bluetoothGattServerCallback = new BluetoothGattServerCallback() {

        /**
         * 1.连接状态发生变化时
         * @param device
         * @param status
         * @param newState
         */
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            Log.e(TAG, String.format("1.onConnectionStateChange：device name = %s, address = %s", device.getName(), device.getAddress()));
            Log.e(TAG, String.format("1.onConnectionStateChange：status = %s, newState =%s ", status, newState));
            super.onConnectionStateChange(device, status, newState);
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
            Log.e(TAG, String.format("onServiceAdded：status = %s", status));
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            Log.e(TAG, String.format("onCharacteristicReadRequest：device name = %s, address = %s", device.getName(), device.getAddress()));
            Log.e(TAG, String.format("onCharacteristicReadRequest：requestId = %s, offset = %s", requestId, offset));

            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
        }

        /**
         * 3. onCharacteristicWriteRequest,接收具体的字节
         * @param device
         * @param requestId
         * @param characteristic
         * @param preparedWrite
         * @param responseNeeded
         * @param offset
         * @param requestBytes
         */
        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] requestBytes) {
            Log.e(TAG, String.format("3.onCharacteristicWriteRequest：device name = %s, address = %s", device.getName(), device.getAddress()));
            Log.e(TAG, String.format("3.onCharacteristicWriteRequest：requestId = %s, " +
                            "preparedWrite=%s, responseNeeded=%s, offset=%s, value=%s", requestId,
                    preparedWrite, responseNeeded, offset, new String(requestBytes)));
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, requestBytes);
            onResponseToClient(requestBytes, device, requestId, characteristic);
        }

        /**
         * 2.描述被写入时，在这里执行 bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS...  收，触发 onCharacteristicWriteRequest
         * @param device
         * @param requestId
         * @param descriptor
         * @param preparedWrite
         * @param responseNeeded
         * @param offset
         * @param value
         */
        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            Log.e(TAG, String.format("2.onDescriptorWriteRequest：device name = %s, address = %s", device.getName(), device.getAddress()));
            Log.e(TAG, String.format("2.onDescriptorWriteRequest：requestId = %s, preparedWrite = %s, responseNeeded = %s, offset = %s, value = %s,", requestId, preparedWrite, responseNeeded, offset, value.toString()));

            // now tell the connected device that this was all successfull
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                    "test".getBytes());
        }

        /**
         * 5.特征被读取。当回复响应成功后，客户端会读取然后触发本方法
         * @param device
         * @param requestId
         * @param offset
         * @param descriptor
         */
        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            Log.e(TAG, String.format("onDescriptorReadRequest：device name = %s, address = %s", device.getName(), device.getAddress()));
            Log.e(TAG, String.format("onDescriptorReadRequest：requestId = %s", requestId));
            mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
            Log.e(TAG, String.format("5.onNotificationSent：device name = %s, address = %s", device.getName(), device.getAddress()));
            Log.e(TAG, String.format("5.onNotificationSent：status = %s", status));
        }

        @Override
        public void onMtuChanged(BluetoothDevice device, int mtu) {
            super.onMtuChanged(device, mtu);
            Log.e(TAG, String.format("onMtuChanged：mtu = %s", mtu));
        }

        @Override
        public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
            super.onExecuteWrite(device, requestId, execute);
            Log.e(TAG, String.format("onExecuteWrite：requestId = %s", requestId));
        }
    };

    /**
     * 4.处理响应内容
     *
     * @param reqeustBytes
     * @param device
     * @param requestId
     * @param characteristic
     */
    private void onResponseToClient(byte[] reqeustBytes, BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic) {
        Log.e(TAG, String.format("4.onResponseToClient：device name = %s, address = %s", device.getName(), device.getAddress()));
        Log.e(TAG, String.format("4.onResponseToClient：requestId = %s", requestId));

        String str = new String(reqeustBytes);

        checkPassword(str);

        BluetoothGattCharacteristic characteristicRead = mGattServer.getService(UUID_SERVICE)
                .getCharacteristic(UUID_CHARACTERISTIC_READ);
        characteristicRead.setValue(str.getBytes());
        mGattServer.notifyCharacteristicChanged(device, characteristicRead, false);

        BluetoothGattCharacteristic characteristicWrite = mGattServer.getService(UUID_SERVICE)
                .getCharacteristic(UUID_CHARACTERISTIC_WRITE);
        characteristicWrite.setValue(str.getBytes());
        mGattServer.notifyCharacteristicChanged(device, characteristicWrite, false);

        Log.i(TAG, "4.响应：" + str);

    }

    private void checkPassword(String pw) {
        if ("20190125".equals(pw)) {
            String detail = getJsonString("18612122112", "111111111aa");
            LockManager.getInstance().unlock(LockManager.UNLOCK_TYPE_BLUETOOTH, detail);
        }
    }

    private String getJsonString(String phoneNumber, String blueToothMac) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("phoneNo", phoneNumber);
        jsonObject.put("blueToothMac", blueToothMac);
        return jsonObject.toJSONString();
    }
}

