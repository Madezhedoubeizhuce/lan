package com.alpha.lan.manager;

import java.io.IOException;

import com.alpha.lan.client.Client;
import com.alpha.lan.client.DeviceAddress;
import com.alpha.lan.client.HeartbeatMsgSender;
import com.alpha.lan.client.OnResponseListener;
import com.alpha.lan.client.PersistentConnectionClient;
import com.alpha.lan.client.TextMsgSender;
import com.alpha.lan.utils.Log;
import com.alpha.lan.utils.TextUtils;

public class HeartbeatChecker {
	private static final String TAG = "HeartbeatChecker";

	private static HeartbeatChecker instance;

	private boolean isClientStarted = true;
	private boolean isReceiveResponse = false;

	private Client<String, String> client = new PersistentConnectionClient<String, String>(new HeartbeatMsgSender());
	private OnResponseListener callback = new OnResponseListener() {
		@Override
		public void onResponse(String result) {
			if (TextUtils.isEmpty(result)) {
				return;
			}
			Log.d(TAG, "onResponse heartbeat");
			isReceiveResponse = true;
		}
	};

	private HeartbeatChecker() {
	}

	public static HeartbeatChecker getInstance() {
		if (instance == null) {
			instance = new HeartbeatChecker();
		}

		return instance;
	}

	public void checkHeartbeat(final DeviceAddress stbAddress) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				Log.d(TAG, "checkHeartbeat: begin");
				try {
					isClientStarted = true;
					checking(stbAddress);
				} catch (Exception e) {
					e.printStackTrace();
					disconnect();
				}
			}
		}).start();
	}

	private void checking(DeviceAddress stbAddress) throws IOException, InterruptedException {
		client.init(stbAddress.getAddr(), stbAddress.getPort());
		Thread.sleep(5000);

		while (isClientStarted) {
			Log.d(TAG, "checking: send heartbeat msg");
			client.send("heartbeat msg", callback);

			Thread.sleep(5000);

			Log.d(TAG, "checking: read isReceiveResponse");
			if (!isReceiveResponse) {
				// 5秒后未收到消息则认为已断开连接，停止检测心跳
				break;
			} else {
				isReceiveResponse = false;
			}
		}

		Log.d(TAG, "heartbeat checking already stop");
		if (!isReceiveResponse && isClientStarted) {
			disconnect();
		}
	}

	void stopChecking() {
		try {
			Log.d(TAG, "stopChecking");
			isClientStarted = false;
			Thread.sleep(6000);

			stopClient();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void stopClient() throws IOException, InterruptedException {
		// 初始化空的STBAddress对象来关闭心跳检测客户端
		DeviceAddress address = new DeviceAddress();
		client.restart(address.getAddr(), address.getPort());
		Log.d(TAG, "stopClient: success");
	}

	private void disconnect() {
		try {
			isClientStarted = false;
			stopClient();
			LANDevicesManager.getInstance().disconnect();

			// 重启mDNS服务监听
			LANDevicesManager.getInstance().destroy();
			LANDevicesManager.getInstance().startDeviceFinder();

			Log.d(TAG, "disconnect: success");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
