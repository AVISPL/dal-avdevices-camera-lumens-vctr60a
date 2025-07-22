/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param;

import java.util.Arrays;

/**
 * This class is used to define ExposureCompLevel
 *
 * @author Harry
 * @version 1.0.0
 * @since 1.0.0
 */
public enum ExposureCompLevel {
	MINUS_7("-7", (byte) 0x00),
	MINUS_6("-6", (byte) 0x01),
	MINUS_5("-5", (byte) 0x02),
	MINUS_4("-4", (byte) 0x03),
	MINUS_3("-3", (byte) 0x04),
	MINUS_2("-2", (byte) 0x05),
	MINUS_1("-1", (byte) 0x06),
	ZERO("0",    (byte) 0x07),
	PLUS_1("1",  (byte) 0x08),
	PLUS_2("2",  (byte) 0x09),
	PLUS_3("3",  (byte) 0x0A),
	PLUS_4("4",  (byte) 0x0B),
	PLUS_5("5",  (byte) 0x0C),
	PLUS_6("6",  (byte) 0x0D),
	PLUS_7("7",  (byte) 0x0E);

	private final String name;
	private final byte code;

	ExposureCompLevel(String name, byte code) {
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
	 * This method is used to get Exposure comp level by name
	 *
	 * @param name is the name of ae mode that want to get
	 * @return ExposureCompLevel is the exposure comp level that want to get
	 */
	public static ExposureCompLevel getByName(String name) {
		return Arrays.stream(values())
				.filter(c -> c.getName().equals(name))
				.findFirst()
				.orElse(null);
	}
}
