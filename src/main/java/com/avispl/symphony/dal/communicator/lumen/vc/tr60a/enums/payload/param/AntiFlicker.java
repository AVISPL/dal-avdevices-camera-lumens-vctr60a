/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param;

import java.util.Arrays;

import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.devices.DeviceEnum;

/**
 * This class is used to define AntiFlicker
 *
 * @author Harry
 * @version 1.0.0
 * @since 1.0.0
 */
public enum AntiFlicker implements DeviceEnum {
	OFF("Off", (byte) 0x00),
	ANTI_FLICKER_50HZ("50Hz", (byte) 0x01),
	ANTI_FLICKER_60HZ("60Hz", (byte) 0x02);

	private final String name;
	private final byte code;

	AntiFlicker(String name, byte code) {
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
	 * This method is used to get Anti flicker by name
	 *
	 * @param name is the name of ae mode that want to get
	 * @return AntiFlicker is the wb mode that want to get
	 */
	public static AntiFlicker getByName(String name) {
		return Arrays.stream(values())
				.filter(c -> c.getName().equals(name))
				.findFirst()
				.orElse(null);
	}
}
