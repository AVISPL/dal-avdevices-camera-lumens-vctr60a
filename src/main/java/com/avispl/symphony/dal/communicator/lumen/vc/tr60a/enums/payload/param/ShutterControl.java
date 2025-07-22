/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param;

import java.util.Arrays;

/**
 * This class is used to define ShutterControl
 *
 * @author Harry
 * @version 1.0.0
 * @since 1.0.0
 */
public enum ShutterControl {
	S_10000("1/10000", new byte[] { 0x00, 0x00 }),
	S_5000("1/5000", new byte[] { 0x00, 0x01 }),
	S_3000("1/3000", new byte[] { 0x00, 0x02 }),
	S_2500("1/2500", new byte[] { 0x00, 0x03 }),
	S_2000("1/2000", new byte[] { 0x00, 0x04 }),
	S_1500("1/1500", new byte[] { 0x00, 0x05 }),
	S_1000("1/1000", new byte[] { 0x00, 0x06 }),
	S_725("1/725", new byte[] { 0x00, 0x07 }),
	S_500("1/500", new byte[] { 0x00, 0x08 }),
	S_350("1/350", new byte[] { 0x00, 0x09 }),
	S_250("1/250", new byte[] { 0x00, 0x0A }),
	S_180("1/180", new byte[] { 0x00, 0x0B }),
	S_120("1/120", new byte[] { 0x00, 0x0C }),
	S_100("1/100", new byte[] { 0x00, 0x0D }),
	S_90("1/90", new byte[] { 0x00, 0x0E }),
	S_60("1/60", new byte[] { 0x00, 0x0F }),
	S_30("1/30", new byte[] { 0x01, 0x00 });

	private final String name;
	private final byte[] code;

	ShutterControl(String name, byte[] code) {
		this.name = name;
		this.code = code;
	}

	/**
	 * Retrieves {@code {@link #name}}
	 *
	 * @return value of {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Retrieves {@code {@link #code}}
	 *
	 * @return value of {@link #code}
	 */
	public byte[] getCode() {
		return code;
	}

	/**
	 * This method is used to get Shutter by name
	 *
	 * @param name is the name of shutter that want to get
	 * @return ShutterControl is value that want to get
	 */
	public static ShutterControl getByName(String name) {
		return Arrays.stream(values())
				.filter(c -> c.getName().equals(name))
				.findFirst()
				.orElse(null);
	}
}
