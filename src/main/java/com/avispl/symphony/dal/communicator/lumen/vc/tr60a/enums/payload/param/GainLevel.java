/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param;

import java.util.Arrays;

/**
 * This class is used to define GainLevel
 *
 * @author Harry
 * @version 1.0.0
 * @since 1.0.0
 */
public enum GainLevel {
	DB_0("0", (byte) 0x00),
	DB_2("+2", (byte) 0x01),
	DB_4("+4", (byte) 0x02),
	DB_6("+6", (byte) 0x03),
	DB_8("+8", (byte) 0x04),
	DB_10("+10", (byte) 0x05),
	DB_12("+12", (byte) 0x06),
	DB_14("+14", (byte) 0x07),
	DB_16("+16", (byte) 0x08),
	DB_18("+18", (byte) 0x09),
	DB_20("+20", (byte) 0x0A),
	DB_22("+22", (byte) 0x0B),
	DB_24("+24", (byte) 0x0C),
	DB_26("+26", (byte) 0x0D),
	DB_28("+28", (byte) 0x0E),
	DB_30("+30", (byte) 0x0F);

	private final String name;
	private final byte code;

	GainLevel(String name, byte code) {
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
	 * This method is used to get Gain level by name
	 *
	 * @param name is the name of ae mode that want to get
	 * @return GainLevel is the value that want to get
	 */
	public static GainLevel getByName(String name) {
		return Arrays.stream(values())
				.filter(c -> c.getName().equals(name))
				.findFirst()
				.orElse(null);
	}
}
