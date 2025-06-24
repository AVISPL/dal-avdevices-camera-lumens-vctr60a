/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param;

import java.util.Arrays;

/**
 * This class is used to define ExposureMode
 *
 * @author Harry
 * @version 1.0.0
 * @since 1.0.0
 */
public enum ExposureMode {
	FULL_AUTO("FullAuto", (byte) 0x00),
	MANUAL("Manual", (byte) 0x03),
	SHUTTER_PRIORITY("ShutterPriority", (byte) 0x0A),
	WHITE_BOARD("WhiteBoard", (byte) 0x5F),
	IRIS_PRIORITY("IrisPriority", (byte) 0x0B);

	private final String name;
	private final byte code;

	ExposureMode(String name, byte code) {
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
	 * This method is used to get AE Mode by name
	 *
	 * @param name is the name of ae mode that want to get
	 * @return AEMode is the wb mode that want to get
	 */
	public static ExposureMode getByName(String name) {
		return Arrays.stream(values())
				.filter(c -> c.getName().equals(name))
				.findFirst()
				.orElse(null);
	}
}
