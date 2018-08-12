/*package com.coship.stbspeech.lan.socket;

import android.os.Handler;
import android.os.HandlerThread;

import java.nio.channels.SocketChannel;

*//**
 * 项目：StbSpeech
 * 作者：909949
 * 时间：2018/7/27 15:06
 * 版本：1.0
 * 描述：描述内容
 *//*
public class ServerChecker {
    private static final String TAG = "ServerChecker";

    private final int delayTime = 30000;
    private boolean isAlive = false;
    private boolean isStart = false;
    private NioClient mClient;
    private NoResponseListener mListener;

    private Handler handler;
    private Runnable run = new Runnable() {
        @Override
        public void run() {
            try {
                handler.postDelayed(this, delayTime);

                if (isStart && !isAlive) {
                    if (mListener != null) {
                        mListener.noResponse();
                    }
                    return;
                }
                isStart = true;
                isAlive = false;
                mClient.sendMsg("127.0.0.1", 6969, "test", new NioClient.NioCallback() {
                    @Override
                    public void onResponse(SocketChannel channel, String result) {
                        try {
                            isAlive = true;
                            Log.d(TAG, "onResponse: server is alive");
                            channel.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private static class InstanceHolder {
        private static final ServerChecker INSTANCE = new ServerChecker();
    }

    private ServerChecker() {
        try {
            mClient = new NioClient();
            mClient.initClient();
            HandlerThread handlerThread = new HandlerThread("checkServerHeart");
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ServerChecker getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public void checkServerHeart(NoResponseListener listener) {
        mListener = listener;
        handler.postDelayed(run, delayTime);
    }

    public interface NoResponseListener {
        void noResponse();
    }
}
*/