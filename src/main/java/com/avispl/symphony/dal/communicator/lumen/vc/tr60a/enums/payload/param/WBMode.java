/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param;

import java.util.Arrays;

/**
 * This class is used to define wb mode
 *
 * @author Harry
 * @version 1.0.0
 * @since 1.0.0
 */
public enum WBMode {
	AUTO("Auto", (byte) 0x00),
	INDOOR("Indoor", (byte) 0x01),
	OUTDOOR("Outdoor", (byte) 0x02),
	ONE_PUSH_WB("OnePushWB", (byte) 0x03),
	ATW("ATW", (byte) 0x04),
	MANUAL("Manual", (byte) 0x05),
	SODIUM_LAMP("SodiumLamp", (byte) 0x0C);

	private final String name;
	private final byte code;

	WBMode(String name, byte code) {
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

	/**
	 * This method is used to get WB Mode by name
	 *
	 * @param name is the name of wb mode that want to get
	 * @return WBMode is the wb mode that want to get
	 */
	public static WBMode getByName(String name) {
		return Arrays.stream(values())
				.filter(c -> c.getName().equals(name))
				.findFirst()
				.orElse(null);
	}
}
