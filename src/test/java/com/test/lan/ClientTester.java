package com.test.lan;

import com.alpha.lan.client.OnResponseListener;
import com.alpha.lan.manager.LANDevicesManager;
import com.alpha.lan.utils.Log;

public class ClientTester {
	private static final String TAG = "ClientTester";

	public static void main(String[] args) {
		try {
			LANDevicesManager.getInstance().startDeviceFinder();
			Thread.sleep(5000);
			LANDevicesManager.getInstance().sendMsg("test", new OnResponseListener() {

				@Override
				public void onResponse(String result) {
					// TODO Auto-generated method stub
					Log.d(TAG, "reponse: " + result);
				}
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
