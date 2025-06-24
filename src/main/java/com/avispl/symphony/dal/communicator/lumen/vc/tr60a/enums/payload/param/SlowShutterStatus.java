/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param;

/**
 * This class is used to define slow shutter status
 *
 * @author Harry
 * @version 1.0.0
 * @since 1.0.0
 */
public enum SlowShutterStatus {
	ON("On", (byte) 0x02),
	OFF("Off", (byte) 0x03);

	private final String name;
	private final byte code;

	SlowShutterStatus(String name, byte code) {
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
	public byte getCode() {
		return code;
	}
}
