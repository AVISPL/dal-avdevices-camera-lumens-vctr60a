/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload;

/**
 * This class is used to define payload type
 *
 * @author Harry
 * @version 1.0.0
 * @since 1.0.0
 */
public enum PayloadType {
	COMMAND((byte) 0x00),
	INQUIRY((byte) 0x10),
	REPLY((byte) 0x11);

	private final byte code;

	PayloadType(byte code) {
		this.code = code;
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
