/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param;

import java.util.Arrays;

/**
 * This class is used to define AFFrame
 *
 * @author Harry
 * @version 1.0.0
 * @since 1.0.0
 */
public enum AFFrame {
	AUTO("Auto", (byte) 0x01),
	FULL_FRAME("FullFrame", (byte) 0x02),
	CENTER("Center", (byte) 0x03);

	private final String name;
	private final byte code;

	AFFrame(String name, byte code) {
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
	 * This method is used to get AFFrame by name
	 *
	 * @param name is the name of af frame mode that want to get
	 * @return AFFrame is the af frame mode that want to get
	 */
	public static AFFrame getByName(String name) {
		return Arrays.stream(values())
				.filter(c -> c.getName().equals(name))
				.findFirst()
				.orElse(null);
	}
}
