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
public enum PictureDNROptions {
	OFF("Off", (byte) 0x00),
	ONE("1", (byte) 0x01),
	TWO("2", (byte) 0x02),
	THREE("3", (byte) 0x03),
	FOUR("4", (byte) 0x04),
	FIVE("5", (byte) 0x05),
	SIX("6", (byte) 0x06),
	SEVEN("7", (byte) 0x07);

	private final String name;
	private final byte code;

	PictureDNROptions(String name, byte code) {
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
	 * This method is used to get picture by name
	 *
	 * @param name is the name of PictureMode that want to get
	 * @return picture is the picture that want to get
	 */
	public static PictureDNROptions getByName(String name) {
		return Arrays.stream(values())
				.filter(c -> c.getName().equals(name))
				.findFirst()
				.orElse(null);
	}
}
