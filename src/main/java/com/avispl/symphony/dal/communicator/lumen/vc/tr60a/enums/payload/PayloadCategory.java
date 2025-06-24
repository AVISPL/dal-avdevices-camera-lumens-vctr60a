/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload;

/**
 * This class is used to define payload category
 *
 * @author Harry
 * @version 1.0.0
 * @since 1.0.0
 */
public enum PayloadCategory {
	INTERFACE((byte) 0x00),
	TRACKING((byte) 0x0B),
	FIRMWARE((byte) 0x7E),
	CAMERA_ID((byte) 0x7E),
	HDMI_FORMAT((byte) 0x7E),
	MOTIONLESS((byte) 0x7),
	PIP((byte) 0x7E),
	BAUD_RATE((byte) 0x04),
	SYSTEM((byte) 0x7C),
	SERIAL((byte) 0x02),
	CAMERA((byte) 0x04),
	PAN_TILTER((byte) 0x06);

	private final byte code;

	PayloadCategory(byte code) {
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
