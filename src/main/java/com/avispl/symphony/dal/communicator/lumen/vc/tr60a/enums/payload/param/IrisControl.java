/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param;

import java.util.Arrays;

/**
 * This class is used to define IrisControl
 *
 * @author Harry
 * @version 1.0.0
 * @since 1.0.0
 */
public enum IrisControl {
	CLOSE("Close", (byte) 0x00),
	F14("F14", (byte) 0x01),
	F11("F11", (byte) 0x02),
	F9_6("F9.6", (byte) 0x03),
	F8("F8", (byte) 0x04),
	F6_8("F6.8", (byte) 0x05),
	F5_6("F5.6", (byte) 0x06),
	F4_8("F4.8", (byte) 0x07),
	F4("F4", (byte) 0x08),
	F3_4("F3.4", (byte) 0x09),
	F2_8("F2.8", (byte) 0x0A),
	F2_4("F2.4", (byte) 0x0B),
	F2("F2", (byte) 0x0C),
	F1_6("F1.6", (byte) 0x0D);

	private final String name;
	private final byte code;

	IrisControl(String name, byte code) {
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
	 * This method is used to get Iris by name
	 *
	 * @param name is the name of ae mode that want to get
	 * @return IrisControl is the value that want to get
	 */
	public static IrisControl getByName(String name) {
		return Arrays.stream(values())
				.filter(c -> c.getName().equals(name))
				.findFirst()
				.orElse(null);
	}
}
