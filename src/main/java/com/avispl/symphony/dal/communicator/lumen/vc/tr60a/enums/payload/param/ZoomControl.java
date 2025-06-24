/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param;

/**
 * This class is used to define zoom control
 *
 * @author Harry
 * @version 1.0.0
 * @since 1.0.0
 */
public enum ZoomControl {
	TELE("ZoomIn", (byte) 0x20),
	WIDE("ZoomOut", (byte) 0x30),
	STOP("Stop", (byte) 0x00);

	private final String name;
	private final byte code;

	ZoomControl(String name, byte code) {
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
}
