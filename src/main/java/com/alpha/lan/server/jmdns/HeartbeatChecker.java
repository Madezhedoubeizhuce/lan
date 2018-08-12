package com.alpha.lan.server.jmdns;

import java.io.IOException;

import com.alpha.lan.utils.Log;

public class HeartbeatChecker {
    private static final String TAG = "HeartbeatChecker";

    private static HeartbeatChecker instance;
    private boolean isChecking = false;
    private boolean isReceiveHeartbeatMsg = false;

    private HeartbeatChecker() {
    }

    public static HeartbeatChecker getInstance() {
        if (instance == null) {
            instance = new HeartbeatChecker();
        }

        return instance;
    }

    public void onReceive() {
        Log.d(TAG, "onReceive: receive heartbeat msg");
        isReceiveHeartbeatMsg = true;
    }

//    public void checkHeartbeat(StbMsgServer stbMsgServer) {
////        mStbMsgServer = stbMsgServer;
//
//        if (!isChecking) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    Log.d(TAG, "checkHeartbeat: start check heartbeat");
//                    isChecking = true;
//                    checking();
//                }
//            }).start();
//        }
//    }

    private void checking() {
        waitServerStarted();

        while (isChecking) {
            try {
                Thread.sleep(10000);

                // 10绉掑唴娌℃敹鍒板績璺冲寘
                if (!isReceiveHeartbeatMsg) {
                    Log.w(TAG, "checking: no heartbeat msg receive form client");

                    // 濡傛灉鎵句笉鍒板綋鍓嶈嚜宸辨敞鍐岀殑鏈嶅姟锛屽垯璁や负宸叉帀绾匡紝鍙栨秷娉ㄥ唽涔嬪墠鐨刴dns鏈嶅姟锛岄噸鏂版敞鍐�
                    if (!JmDnsServiceRegister.getInstance().isOwnServiceRegistered()) {
                        restartJmDNS();
                    }
                } else {
                    isReceiveHeartbeatMsg = false;
                    Log.d(TAG, "checking: receive heartbeat");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void restartJmDNS() throws IOException, InterruptedException {
        JmDnsServiceRegister.getInstance().unRegisterService();

        Thread.sleep(3000);

//        if (mStbMsgServer.isStarted()) {
//            JmDnsServiceRegister.getInstance().initJmDns();
//            JmDnsServiceRegister.getInstance().serviceRegist(NioServer.createPort());
//        } else {
//            mStbMsgServer.start();
//            waitServerStarted();
//        }
    }

    private void waitServerStarted() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
