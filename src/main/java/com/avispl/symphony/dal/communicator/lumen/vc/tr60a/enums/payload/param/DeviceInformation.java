/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param;

import java.util.Arrays;

/**
 * This class is used to define DeviceInformation
 *
 * @author Harry
 * @version 1.0.0
 * @since 1.0.0
 */
public enum DeviceInformation{
	FIRMWARE_VERSION("FirmwareVersion", (byte) 0xCF),
	MAC_ADDRESS("MACAddress", (byte) 0x00),
	CAMERA_ID("CameraId", (byte) 0x00),
	VIDEO_RESOLUTION("VideoResolution", (byte) 0x00),
	VIDEO_HDMI_FORMAT("VideoHDMIFormat", (byte) 0x00),
	VIDEO_PIP("VideoPIP", (byte) 0x00),
	VIDEO_PRIVACY_MODE("VideoPrivacyMode", (byte) 0x00),
	SERIAL_NUMBER("SerialNumber", (byte) 0x03);

	private final String name;
	private final byte code;

	DeviceInformation(String name, byte code) {
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
	 * This method is used to get Generic information of device
	 *
	 * @param name is the name of device information that want to get
	 * @return DeviceInformation is the wb mode that want to get
	 */
	public static DeviceInformation getByName(String name) {
		return Arrays.stream(values())
				.filter(c -> c.getName().equals(name))
				.findFirst()
				.orElse(null);
	}
}
