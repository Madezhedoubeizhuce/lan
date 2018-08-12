package com.alpha.lan.client;

public enum RequestType {
	TEXT((byte) 0), FILE((byte) 1);

	private byte value;

	RequestType(byte value) {
		this.value = value;
	}

	public byte value() {
		return this.value;
	}
}
