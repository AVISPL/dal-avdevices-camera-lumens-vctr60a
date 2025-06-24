/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param;

import java.util.Arrays;

/**
 * This class is used to define AFSensitivity
 *
 * @author Harry
 * @version 1.0.0
 * @since 1.0.0
 */
public enum AFSensitivity {
	HIGH("High", (byte) 0x01),
	MIDDLE("Middle", (byte) 0x02),
	LOW("Low", (byte) 0x03);

	private final String name;
	private final byte code;

	AFSensitivity(String name, byte code) {
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
	 * This method is used to get AFSensitivity by name
	 *
	 * @param name is the name of ae mode that want to get
	 * @return AFSensitivity is the wb mode that want to get
	 */
	public static AFSensitivity getByName(String name) {
		return Arrays.stream(values())
				.filter(c -> c.getName().equals(name))
				.findFirst()
				.orElse(null);
	}
}
