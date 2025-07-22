/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param;

import java.util.Arrays;

import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.devices.DeviceEnum;

/**
 * This class is used to define slow pan tilt status
 *
 * @author Harry
 * @version 1.0.0
 * @since 1.0.0
 */
public enum SlowPanTiltStatus implements DeviceEnum {
	ON("On", (byte) 0x02),
	OFF("Off", (byte) 0x03);

	private final String name;
	private final byte code;

	SlowPanTiltStatus(String name, byte code) {
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
	 * This method is used to get slow pan tilt by name
	 *
	 * @param name is the name of pan tilt that want to get
	 * @return SlowPanTilt is the value that want to get
	 */
	public static SlowPanTiltStatus getByName(String name) {
		return Arrays.stream(values())
				.filter(c -> c.getName().equals(name))
				.findFirst()
				.orElse(null);
	}
}
