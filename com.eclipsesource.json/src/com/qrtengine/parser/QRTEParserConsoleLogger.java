package com.qrtengine.parser;

public class QRTEParserConsoleLogger implements QRTEParserLoggerI {

	@Override
	public void log(String s) {
		this.log(s, 1);
		
	}
	
	public void log(String s, int level) {
		System.out.println("[" + System.currentTimeMillis() + "] " + s + "...");
	}

}
