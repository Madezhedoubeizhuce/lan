package com.alpha.lan.utils;

public class Log {
	public static void d(String tag, String msg) {
		System.out.println(tag + ":debug -> " + msg);
	}

	public static void i(String tag, String msg) {
		System.out.println(tag + ":info -> " + msg);
	}

	public static void w(String tag, String msg) {
		System.out.println(tag + ":waring -> " + msg);
	}

	public static void e(String tag, String msg) {
		System.out.println(tag + ":error -> " + msg);
	}
}
