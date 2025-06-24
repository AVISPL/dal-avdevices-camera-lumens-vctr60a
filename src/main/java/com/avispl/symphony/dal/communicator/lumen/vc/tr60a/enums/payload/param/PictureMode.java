/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param;

import java.util.Arrays;

/**
 * This class is used to define PictureMode
 *
 * @author Harry
 * @version 1.0.0
 * @since 1.0.0
 */
public enum PictureMode {
	CUSTOM("Custom", (byte) 0x01),
	DEFAULT("Default", (byte) 0x00);

	private final String name;
	private final byte code;

	PictureMode(String name, byte code) {
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
	 * This method is used to get PictureMode by name
	 *
	 * @param name is the name of PictureMode that want to get
	 * @return PictureMode is the picture mode that want to get
	 */
	public static PictureMode getByName(String name) {
		return Arrays.stream(values())
				.filter(c -> c.getName().equals(name))
				.findFirst()
				.orElse(null);
	}
}
