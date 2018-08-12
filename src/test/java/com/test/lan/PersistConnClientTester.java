package com.test.lan;

import com.alpha.lan.client.Client;
import com.alpha.lan.client.OnResponseListener;
import com.alpha.lan.client.PersistentConnectionClient;
import com.alpha.lan.client.TextMsgDispatcher;
import com.alpha.lan.utils.Log;

public class PersistConnClientTester {
	private static final String TAG = "PersistConnClientTester->";

	public static void main(String[] args) {
		Client<String, String> client = new PersistentConnectionClient<String, String>(new TextMsgDispatcher());
		try {
			client.init("127.0.0.1", 6969);

			for (int i = 0; i < 3; i++) {
				client.send("heartbeat msg" + i, new OnResponseListener() {

					@Override
					public void onResponse(String result) {
						Log.d(TAG, "receive response form server: " + result);
					}
				});
				Thread.sleep(1000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
