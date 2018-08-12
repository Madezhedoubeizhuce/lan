package com.alpha.lan.client;

public class Attachment<T> {
	private T data;
	private OnResponseListener listener;

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public OnResponseListener getListener() {
		return listener;
	}

	public void setListener(OnResponseListener listener) {
		this.listener = listener;
	}

}
