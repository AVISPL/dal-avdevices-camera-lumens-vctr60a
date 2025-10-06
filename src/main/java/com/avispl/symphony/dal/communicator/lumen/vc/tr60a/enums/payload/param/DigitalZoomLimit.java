/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param;

import java.util.Arrays;

/**
 * This class is used to define DigitalZoomLimit
 *
 * @author Harry
 * @version 1.0.0
 * @since 1.0.0
 */
public enum DigitalZoomLimit {
	X1("x1", (byte) 0x00),
	X2("x2", (byte) 0x01),
	X3("x3", (byte) 0x02),
	X4("x4", (byte) 0x03),
	X5("x5", (byte) 0x04),
	X6("x6", (byte) 0x05),
	X7("x7", (byte) 0x06),
	X8("x8", (byte) 0x07),
	X9("x9", (byte) 0x08),
	X10("x10", (byte) 0x09),
	X11("x11", (byte) 0x0A),
	X12("x12", (byte) 0x0B),
	X13("x13", (byte) 0x0C),
	X14("x14", (byte) 0x0D),
	X15("x15", (byte) 0x0E),
	X16("x16", (byte) 0x0F);

	private final String name;
	private final byte code;

	DigitalZoomLimit(String name, byte code) {
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
	 * This method is used to get DigitalZoomLimit by name
	 *
	 * @param name is the name of af digital zoom limit that want to get
	 * @return DigitalZoomLimit is the af frame mode that want to get
	 */
	public static DigitalZoomLimit getByName(String name) {
		return Arrays.stream(values())
				.filter(c -> c.getName().equals(name))
				.findFirst()
				.orElse(null);
	}
}
