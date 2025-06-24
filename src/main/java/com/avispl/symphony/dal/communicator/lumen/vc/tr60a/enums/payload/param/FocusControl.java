/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param;

/**
 * This class is used to define focus control
 *
 * @author Harry
 * @version 1.0
 * @since 1.0
 */
public enum FocusControl {
	FAR("FocusFar", (byte) 0x20),
	NEAR("FocusNear", (byte) 0x30),
	STOP("Stop", (byte) 0x00);

	private final String name;
	private final byte code;

	FocusControl(String name, byte code) {
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
