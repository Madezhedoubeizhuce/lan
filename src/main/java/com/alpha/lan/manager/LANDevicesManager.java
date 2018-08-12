package com.alpha.lan.manager;

import java.io.IOException;
import java.util.Map;

import com.alpha.lan.client.Client;
import com.alpha.lan.client.DeviceAddress;
import com.alpha.lan.client.OnResponseListener;
import com.alpha.lan.client.ShortConnectionClient;
import com.alpha.lan.client.TextMsgDispatcher;
import com.alpha.lan.utils.Log;

public class LANDevicesManager {
	private static final String TAG = "LANDevicesManager";
	private static LANDevicesManager instance;

	private JmDnsServiceFinder jmDnsServiceFinder;
	private Client<String, String> client;
	private boolean isClientStarted = false;

	private LANDevicesManager() {
		client = new ShortConnectionClient<String, String>(new TextMsgDispatcher());
		initJmDnsServiceFinder();
	}

	private void initJmDnsServiceFinder() {
		jmDnsServiceFinder = JmDnsServiceFinder.getInstance();
		jmDnsServiceFinder.setCallback(new JmDNSCallback() {
			@Override
			public void serviceAdded() {
				try {
					Log.d(TAG, "serviceAdded: found a new device and we will try to start it");
					startClient();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void serviceRemove() {
				try {
					Log.d(TAG, "serviceRemove: current device is disconenct, we will try to find a new device.");
					HeartbeatChecker.getInstance().stopChecking();
					restartClient();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		Log.d(TAG, "initJmDnsServiceFinder: success");
	}

	public static LANDevicesManager getInstance() {
		if (instance == null) {
			instance = new LANDevicesManager();
		}

		return instance;
	}

	public void startDeviceFinder() {
		try {
			jmDnsServiceFinder.init();

			Log.d(TAG, "getting server address");
			jmDnsServiceFinder.addServiceListener();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void startClient() throws IOException {
		// 已启动客户端则不再重复启动
		if (isClientStarted) {
			Log.d(TAG, "startClient: there is already exit a connected client, no more client can be start!");
			return;
		}

		DeviceAddress stbAddress = getDeviceAddress();
		if (stbAddress == null) {
			throw new RuntimeException("stbAddress is null!");
		}

		client.init(stbAddress.getAddr(), stbAddress.getPort());
		isClientStarted = true;
		Log.d(TAG, "startClient: success");

		// 启动Client时开启心跳检测
		HeartbeatChecker.getInstance().checkHeartbeat(stbAddress);

		connectedToDevice();
	}

	public void destroy() {
		jmDnsServiceFinder.removeServiceListener();
		jmDnsServiceFinder.closeJmDNS();
		Log.d(TAG, "destroy: success");
	}

	public void sendMsg(String msg, OnResponseListener callback) throws IOException {
		if (client.isStarted()) {
			Log.d(TAG, "sendMsg: start");
			client.send(msg, callback);
		} else {
			notFoundDevice();
			Log.w(TAG, "sendMsg: Client is not connect to the STB!");
			throw new RuntimeException("Client is not connect to the STB!");
		}
	}

	void disconnect() throws IOException, InterruptedException {
		jmDnsServiceFinder.deleteCurrentDevice();
		restartClient();

		Log.w(TAG, "already disconnect with server!");
	}

	private void restartClient() throws IOException, InterruptedException {
		DeviceAddress devicesAddress = getDeviceAddress();

		if (devicesAddress == null) {
			devicesAddress = new DeviceAddress();
		}

		Log.i(TAG, "restartClient: change to: " + devicesAddress.getAddr() + ":" + devicesAddress.getPort());
		boolean result = client.restart(devicesAddress.getAddr(), devicesAddress.getPort());

		if (result) {
			connectedToDevice();

			// 重启心跳检测
			HeartbeatChecker.getInstance().stopChecking();
			HeartbeatChecker.getInstance().checkHeartbeat(devicesAddress);
		}
	}

	private DeviceAddress getDeviceAddress() {
		Map<String, DeviceAddress> addressesMap = jmDnsServiceFinder.getStbAddresses();
		DeviceAddress stbAddress = null;
		String currentDevice = jmDnsServiceFinder.getCurrentDevice();

		if (currentDevice == null) {
			Log.w(TAG, "getSTBAddress: no stb device found current");
			isClientStarted = false;
			notFoundDevice();

			return stbAddress;
		}

		if (addressesMap.size() > 0) {
			stbAddress = addressesMap.get(currentDevice);

			Log.d(TAG, "getSTBAddress: found a stb:" + stbAddress.getAddr() + ":" + stbAddress.getPort());
			foundDevice();
		} else {
			isClientStarted = false;

			Log.w(TAG, "getSTBAddress: no stb found");
			notFoundDevice();
		}
		return stbAddress;
	}

	private void notFoundDevice() {

	}

	private void foundDevice() {

	}

	private void connectedToDevice() {

	}
}
