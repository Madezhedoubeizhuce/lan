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

	public static RequestType valueOf(byte value) {
		switch (value) {
		case 0:
			return TEXT;
		case 1:
			return FILE;
		default:
			return null;
		}
	}
}
