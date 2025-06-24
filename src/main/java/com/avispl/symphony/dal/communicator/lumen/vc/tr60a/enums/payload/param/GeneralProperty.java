/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param;

import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.PayloadCategory;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.command.Command;

/**
 * This class is used to define general property
 *
 * @author Harry
 * @version 1.0.0
 * @since 1.0.0
 */
public enum GeneralProperty {
	FIRMWARE_VERSION("FirmwareVersion", PayloadCategory.FIRMWARE.getCode(), Command.FIRMWARE_VERSION),
	SERIAL_NUMBER("SerialNumber", PayloadCategory.SERIAL.getCode(), Command.SERIAL_NUMBER),
	MAC_ADDRESS("MACAddress", PayloadCategory.CAMERA.getCode(), Command.MAC_ADDRESS),
	CAMERA_ID("CameraId", PayloadCategory.CAMERA_ID.getCode(), Command.CAMERA_ID),
	VIDEO_HDMI_FORMAT("VideoHDMIFormat", PayloadCategory.HDMI_FORMAT.getCode(), Command.HDMI_FORMAT),
	VIDEO_PIP("VideoPIP", PayloadCategory.PIP.getCode(), Command.PIP),
	VIDEO_PRIVACY_MODE("VideoPrivacyMode", PayloadCategory.CAMERA.getCode(), Command.PRIVACY_MODE),
	SYSTEM_BAUD_RATE("System#BaudRate", PayloadCategory.BAUD_RATE.getCode(), Command.BAUD_RATE),
	SYSTEM_DHCP("System#DHCP", PayloadCategory.SYSTEM.getCode(), Command.DHCP),
	SYSTEM_DNS("System#DNS", PayloadCategory.SYSTEM.getCode(), Command.DNS),
	SYSTEM_GATEWAY("System#Gateway", PayloadCategory.SYSTEM.getCode(), Command.GATEWAY),
	SYSTEM_IPV4_NETMASK("System#IPv4Netmask", PayloadCategory.SYSTEM.getCode(), Command.IPV4_NETMASK),
	SYSTEM_TALLY_LAMP("System#TallyLamp", PayloadCategory.CAMERA_ID.getCode(), Command.TALLY_MODE),
	SYSTEM_TRACKING_LED("System#TrackingLedStatus", PayloadCategory.TRACKING.getCode(), Command.TRACKING_LED_STATUS),
	SYSTEM_USB_STREAM("System#USBStreamStatus", PayloadCategory.CAMERA_ID.getCode(), Command.USB);

	private final String key;
	private final byte categoryCode;
	private final Command command;

	/**
	 * Constructs a GeneralProperty enum instance.
	 *
	 * @param key           the key for stats map
	 * @param categoryCode  payload category code
	 * @param command       VISCA command to retrieve this property
	 */
	GeneralProperty(String key, byte categoryCode, Command command) {
		this.key = key;
		this.categoryCode = categoryCode;
		this.command = command;
	}

	/**
	 * Returns the stats map key for this property.
	 *
	 * @return the property key
	 */
	public String key() { return key; }

	/**
	 * Returns the payload category code used in the VISCA inquiry.
	 *
	 * @return the category code byte
	 */
	public byte categoryCode() { return categoryCode; }

	/**
	 * Returns the VISCA command to request this property.
	 *
	 * @return the Command enum
	 */
	public Command command() { return command; }
}
