/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param;

import java.util.Arrays;

import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.devices.DeviceEnum;

/**
 * This class is used to define WDROptions
 *
 * @author Harry
 * @version 1.0.0
 * @since 1.0.0
 */
public enum WDROptions implements DeviceEnum {
	OFF("Off", (byte) 0x00),
	WDR_OPTIONS_1("1", (byte) 0x01),
	WDR_OPTIONS_2("2", (byte) 0x02),
	WDR_OPTIONS_3("3", (byte) 0x03),
	WDR_OPTIONS_4("4", (byte) 0x04),
	WDR_OPTIONS_5("5", (byte) 0x05);

	private final String name;
	private final byte code;

	WDROptions(String name, byte code) {
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
	 * This method is used to get WDROptions by name
	 *
	 * @param name is the name of ae mode that want to get
	 * @return WDROptions is the wb mode that want to get
	 */
	public static WDROptions getByName(String name) {
		return Arrays.stream(values())
				.filter(c -> c.getName().equals(name))
				.findFirst()
				.orElse(null);
	}
}
