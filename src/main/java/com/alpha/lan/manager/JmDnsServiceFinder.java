package com.alpha.lan.manager;


import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import com.alpha.lan.client.DeviceAddress;
import com.alpha.lan.utils.Log;

public class JmDnsServiceFinder {
    private static final String TAG = JmDnsServiceFinder.class.getSimpleName();
    private static final String SERVICE_TYPE = "_stb._tcp.local.";

    private static JmDnsServiceFinder instance;

    private ServiceListener serviceListener;
    private Map<String, DeviceAddress> stbAddressMap;
    private boolean isRegist = false;
    private JmDNSCallback callback;
    private String currentDevice;
    private JmDNS jmDNS;

    private JmDnsServiceFinder() {
        stbAddressMap = new HashMap<>();
    }

    public static JmDnsServiceFinder getInstance() {
        if (instance == null) {
            instance = new JmDnsServiceFinder();
        }
        return instance;
    }

    public Map<String, DeviceAddress> getStbAddresses() {
        return stbAddressMap;
    }

    public String getCurrentDevice() {
        return currentDevice;
    }

    public void setCallback(JmDNSCallback callback) {
        this.callback = callback;
    }

    public void init() throws IOException {
        InetAddress addr = InetAddress.getLocalHost();
        String hostname = addr.getHostName();
        Log.d(TAG, "init: Addr : " + addr + " Hostname: " + hostname);

        jmDNS = JmDNS.create(addr);

        serviceListener = new ServiceListener() {
            @Override
            public void serviceAdded(ServiceEvent event) {
                Log.i(TAG, "serviceAdded: regist service success! name: "
                        + event.getName() + " type: " + event.getType() + " It's ready to add device.");

                if (stbAddressMap.get(event.getName()) == null) {
                    saveDeviceAddress(event);

                    if (callback != null) {
                        callback.serviceAdded();
                    }
                }
            }

            @Override
            public void serviceRemoved(ServiceEvent event) {
                Log.i(TAG, "serviceRemove: " + event.getInfo().toString());

                // 删除当前设备
                stbAddressMap.remove(event.getName());
                setAnotherDevice();

                if (callback != null) {
                    callback.serviceRemove();
                }
            }

            @Override
            public void serviceResolved(ServiceEvent event) {
                Log.i(TAG, "serviceResolved: " + event.getInfo().toString());

                if (stbAddressMap.get(event.getName()) == null) {
                    saveDeviceAddress(event);

                    if (callback != null) {
                        callback.serviceAdded();
                    }
                }
            }
        };
        Log.d(TAG, "init: success");
    }

    public void deleteCurrentDevice() {
        stbAddressMap.remove(currentDevice);
        setAnotherDevice();
    }

    private void setAnotherDevice() {
        Iterator<String> iterator = stbAddressMap.keySet().iterator();
        if(iterator.hasNext()) {
            currentDevice = iterator.next();
        } else {
            currentDevice = null;
        }
    }

    private void saveDeviceAddress(ServiceEvent event) {
        if (event.getDNS() == null) {
            Log.d(TAG, "dns is null");
            return;
        }

        ServiceInfo info = event.getDNS().getServiceInfo(event.getType(), event.getName());
        if (info != null && info.getHostAddresses().length > 0) {
            String address = info.getHostAddresses()[0];
            int port = info.getPort();
            boolean isNeddChangeAddr = isNeedToChangeAddress(address);

            if (currentDevice == null) {
                currentDevice = event.getName();
            }

            DeviceAddress stbAddress = new DeviceAddress();
            stbAddress.setAddr(address);
            stbAddress.setPort(port);
            stbAddressMap.put(event.getName(), stbAddress);
            Log.i(TAG, "saveSTBAddress: add device: " + address + ":" + port);

            // ip地址相同时需要将失效的设备删除（设备断电重启时）
            if (isNeddChangeAddr && callback != null) {
                if (!event.getName().equals(currentDevice)) {
                    currentDevice = event.getName();
                }

                Log.d(TAG, "saveSTBAddress: new device is confilct with the connected device, " +
                        "because the old device was already disconnect, we try to connect it again!");
                callback.serviceRemove();
            }
        }
    }

    private boolean isNeedToChangeAddress(String address) {
        Iterator<Map.Entry<String, DeviceAddress>> entryIterator = stbAddressMap.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<String, DeviceAddress> entry = entryIterator.next();
            if (address != null && address.equals(entry.getValue().getAddr())) {
                entryIterator.remove();
                Log.d(TAG, "isNeedToChangeAddress: already connected ip: " +
                        address + " and remove it: " + entry.getValue().getAddr());
                return true;
            }
        }
        Log.d(TAG, "isNeedToChangeAddress: There is no such address: " + address + " in connected device");

        return false;
    }

    public void addServiceListener() {
        isRegist = true;
        jmDNS.addServiceListener(SERVICE_TYPE, serviceListener);
        Log.d(TAG, "addServiceListener:  success");
    }

    public void removeServiceListener() {
        if (isRegist) {
            jmDNS.removeServiceListener(SERVICE_TYPE, serviceListener);
            Log.d(TAG, "removeServiceListener:  success");
        }
    }

    public void closeJmDNS() {
        try {
            jmDNS.close();
            Log.d(TAG, "closeJmDNS: success");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
