package com.alpha.lan.client;

public class DeviceAddress {
	private static String addr;
	private static int port;
	public static String getAddr() {
		return addr;
	}
	public static void setAddr(String addr) {
		DeviceAddress.addr = addr;
	}
	public static int getPort() {
		return port;
	}
	public static void setPort(int port) {
		DeviceAddress.port = port;
	}
}
