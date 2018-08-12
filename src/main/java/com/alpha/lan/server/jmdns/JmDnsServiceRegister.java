package com.alpha.lan.server.jmdns;

import java.io.IOException;
import java.net.InetAddress;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

import com.alpha.lan.utils.Log;

public class JmDnsServiceRegister {
    private static final String TAG = JmDnsServiceRegister.class.getSimpleName();
    private static final String SERVICE_TYPE = "_stb._tcp.local.";

    private static JmDnsServiceRegister instance;
    private JmDNS jmDNS;
    private String hostname;
    private boolean isRegistered = false;

    private JmDnsServiceRegister() {
    }

    public static JmDnsServiceRegister getInstance() {
        if (instance == null) {
            instance = new JmDnsServiceRegister();
        }
        return instance;
    }

    public void initJmDns() throws IOException {
        if (isRegistered) {
            return;
        }

        InetAddress addr = InetAddress.getLocalHost();
        hostname = addr.getHostName();

        jmDNS = JmDNS.create(addr);
        Log.d(TAG, "initJmDns: success Addr : " + addr + " Hostname: " + hostname);
    }

    private String serviceName;

    public void serviceRegist(int port) throws IOException {
        if (isRegistered) {
            return;
        }
        serviceName = "JmDNS_msg_from: " + hostname + ":" + port;

        ServiceInfo serviceInfo = ServiceInfo.create(SERVICE_TYPE, serviceName, port, "stb");
        jmDNS.registerService(serviceInfo);
        isRegistered = true;

        Log.i(TAG, "regist success! Addr: " + hostname + " port: " + port);
    }

    private JmDNS checkJmdns;

    public boolean isOwnServiceRegistered() throws IOException {
        try {
            checkJmdns = JmDNS.create();

            Log.d(TAG, "isOwnServiceRegistered: current service name-> " + serviceName);
            ServiceInfo[] serviceInfos = checkJmdns.list(SERVICE_TYPE);

            for (ServiceInfo info : serviceInfos) {
                if (info != null && info.getHostAddresses().length > 0) {
                    Log.d(TAG, "isOwnServiceRegistered: service name-> " + info.getName());

                    // 鎵惧埌鑷繁娉ㄥ唽鐨勬湇鍔″悕鏃跺垯杩斿洖true
                    if (serviceName != null && serviceName.equals(info.getName())) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            checkJmdns.close();
        }

        return false;
    }

    public void unRegisterService() {
        if (!isRegistered) {
            return;
        }

        jmDNS.unregisterAllServices();
        Log.d(TAG, "unRegisterService success");

        isRegistered = false;

        try {
            jmDNS.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
