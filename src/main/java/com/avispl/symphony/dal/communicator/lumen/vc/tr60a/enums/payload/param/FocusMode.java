/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param;

import java.util.Arrays;

import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.devices.DeviceEnum;

/**
 * This class is used to define focus status
 *
 * @author Harry
 * @version 1.0
 * @since 1.0
 */
public enum FocusMode implements DeviceEnum {
	AUTO("Auto", (byte) 0x02),
	MANUAL("Manual", (byte) 0x03);

	private final String name;
	private final byte code;

	FocusMode(String name, byte code) {
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
	 * This method is used to get Focus Mode by name
	 *
	 * @param name is the name of ae mode that want to get
	 * @return FocusMode is the focus mode that want to get
	 */
	public static FocusMode getByName(String name) {
		return Arrays.stream(values())
				.filter(c -> c.getName().equals(name))
				.findFirst()
				.orElse(null);
	}
}
