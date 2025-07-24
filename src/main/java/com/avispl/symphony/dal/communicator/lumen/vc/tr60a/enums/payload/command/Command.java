/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.command;

import java.util.Arrays;

/**
 * This class is used to define command used to build payload
 *
 * @author Harry
 * @version 1.0.0
 * @since 1.0.0
 */
public enum Command {
	//Exposure Group
	EXPOSURE( "","Exposure", null),
	EXPOSURE_MODE( "Exposure","Mode", new byte[] { 0x39 }),
	ANTI_FLICK("Exposure","AntiFlicker", new byte[] { 0x3C }),
	WDR_OPTION("Exposure","WDR", new byte[] { 0x2D }),
	GAIN_LIMIT_DIRECT("Exposure","GainLimit(dB)", new byte[] { 0x2C }),
	SHUTTER_DIRECT("Exposure","ShutterSpeed", new byte[] { 0x4A }),
	SHUTTER_CONTROL("Exposure","ShutterControl", new byte[] { 0x4A, 0x00, 0x00 }),
	IRIS_DIRECT("Exposure","IrisLevel", new byte[] { 0x4B }),
	IRIS_CONTROL("Exposure","IrisLevelCurrent", new byte[] { 0x4B, 0x00, 0x00, 0x00 }),
	GAIN_DIRECT("Exposure","GainLevel", new byte[] { 0x4C, 0x00, 0x00 }),
	GAIN_CURRENT("Exposure","GainLevelCurrent", null),
	GAIN_LIMIT_CURRENT("Exposure","GainLimitCurrent", null),
	EXP_COMP_DIRECT("Exposure","ExposureCompLevel", new byte[] { 0x4E }),
	EXP_COMP_CONTROL("Exposure","ExposureCompLevel", new byte[] { 0x4E, 0x00, 0x00, 0x00 }),

	//Focus group
	FOCUS_GROUP( "", "Focus", null),
	FOCUS_MODE( "Focus", "Mode", new byte[] { 0x38 }),
	AF_FRAME( "Focus", "AFFrame", new byte[] { 0x5C }),
	AF_SENSITIVE( "Focus", "AFSensitivity", new byte[] { 0x58 }),
	FOCUS_FAR( "Focus", "Far", new byte[] { 0x08, 0x02 }),
	FOCUS_NEAR( "Focus", "Near", new byte[] { 0x08, 0x03 }),
	FOCUS_VARIABLE_SPEED("Dynamic", null, new byte[] { 0x08 }),

	//Mirror group
	MIRROR_GROUP( "", "Mirror", null),
	FLIP( "Mirror", "Flip", new byte[] { 0x66 }),
	MIRROR( "Mirror", "Mirror", new byte[] { 0x61 }),

	//Picture group
	IMAGE_MODE( "Picture", "ImageMode", new byte[] { 0x3F, 0x04 }),
	PICTURE( "Picture", "Picture", null),
	TWO_DNR( "Picture", "2DNR", new byte[] { 0x53 }),
	THREE_DNR( "Picture", "3DNR", new byte[] { 0x54 }),
	HUE( "Picture", "Hue", new byte[] { 0x4F }),
	HUE_CURRENT( "Picture", "HueCurrent", null),
	SATURATION( "Picture", "Saturation", new byte[] { 0x49 }),
	SATURATION_CURRENT( "Picture", "SaturationCurrent", null),
	BRIGHTNESS( "Picture", "Brightness", new byte[] { 0x4D }),
	BRIGHTNESS_CURRENT( "Picture", "BrightnessCurrent", null),
	SHARPNESS( "Picture", "Sharpness", new byte[] { 0x42 }),
	SHARPNESS_CURRENT( "Picture", "SharpnessCurrent", null),
	GAMMA( "Picture", "Gamma", new byte[] { 0x5B }),
	GAMMA_CURRENT( "Picture", "GammaCurrent", null),

	// PTZ group
	PTZ( "", "PanTiltZoom", null),
	D_ZOOM_LIMIT( "PanTiltZoom", "DZoomLimit", new byte[] { 0x26 }),
	INITIAL_POSITION( "PanTiltZoom", "InitialPosition", new byte[] { 0x75, 0x6A }),
	PRESET_SPEED( "PanTiltZoom", "PresetSpeed", new byte[] { 0x75, 0x32 }),
	PRESET_SPEED_CONTROL( "", "", new byte[] { 0x20 }),
	PTZ_SPEED_COMP( "PanTiltZoom", "PTZSpeedComp", new byte[] { 0x1F, 0x01 }),
	MOTIONLESS_PRESET( "PanTiltZoom", "MotionlessPreset", new byte[] { 0x01 }),

	// WB group
	WB_MODE( "WhiteBalance", "Mode", new byte[] { 0x35 }),
	WB_ONE_PUSH_TRIGGER( "WhiteBalance", "OnePushTrigger", new byte[] { 0x10, 0x05 }),
	RGAIN( "WhiteBalance", "ManualRed", new byte[] { 0x43 }),
	RGAIN_CURRENT( "WhiteBalance", "ManualRedCurrent", null),
	RGAIN_INQ( "WhiteBalance", "ManualRedCurrent", new byte[] { 0x43 }),
	BGAIN( "WhiteBalance", "ManualBlue", new byte[] { 0x44 }),
	BGAIN_CURRENT( "WhiteBalance", "ManualBlueCurrent", null),
	BGAIN_INQ( "WhiteBalance", "ManualBlueCurrent", new byte[] { 0x44 }),

	// System
	DHCP( "", "DHCP", new byte[] { 0x01 }),
	IPV4_ADDRESS( "", "IPv4Address", new byte[] { 0x02 }),
	IPV4_NETMASK( "", "IPv4Netmask", new byte[] { 0x03 }),
	GATEWAY( "", "Gateway", new byte[] { 0x04 }),
	DNS( "", "DNS", new byte[] { 0x05 }),
	TRACKING_LED_STATUS( "", "TrackingLedStatus", new byte[] { 0x00, 0x06 }),

	POWER( "", "Power", new byte[] { 0x00 }),
	POWER_STATUS( "", "PowerStatus", null),
	ZOOM( "", "ZoomControl", new byte[] { 0x07 }),

	FOCUS_ONE_PUSH( "", "OnePush", new byte[] { 0x18, 0x01 }),
	WHITE_BALANCE( "", "WhiteBalance", null),

	AUTO_SLOW_SHUTTER( "", "AutoSlowShutter", new byte[] { 0x5A }),

	BACKLIGHT( "", "Backlight", new byte[] { 0x33 }),
	PRESET( "", "PresetControl", new byte[] { 0x3F }),
	PAN_TILT_DRIVE( "", "PanTiltControl", new byte[] { 0x01 }),
	PAN_TILT_HOME( "", "Home", new byte[] { 0x04 }),
	FIRMWARE_VERSION( "", "FirmwareVersion", new byte[] { (byte) 0xCF }),
	SERIAL_NUMBER( "", "SerialNumber", new byte[] { 0x18 }),
	MAC_ADDRESS( "", "MACAddress", new byte[] { 0x78 }),
	CAMERA_ID( "", "CameraId", new byte[] { (byte) 0xCE }),
	HDMI_FORMAT( "", "VideoHDMIFormat", new byte[] { 0x01, 0x03 }),
	PRIVACY_MODE( "", "VideoPrivacyMode", new byte[] { 0x00, 0x02 }),
	PIP( "", "VideoPIP", new byte[] { 0x02, 0x02 }),
	BAUD_RATE( "", "BaudRate", new byte[] { 0x24, 0x00 }),
	SLOW_PAN_TILT( "", "PanTiltSlowMode", new byte[] { 0x044 });

	private final String group;
	private final String name;
	private final byte[] code;

	Command(String group, String name, byte[] code) {
		this.group = group;
		this.name = name;
		this.code = code;
	}

	public String getGroup() {
		return group;
	}

	/**
	 * Retrieves {@code {@link #name}}
	 *
	 * @return value of {@link #code}
	 */
	public String getName() {
		return name;
	}

	/**
	 * Retrieves {@code {@link #code}}
	 *
	 * @return value of {@link #code}
	 */
	public byte[] getCode() {
		return code;
	}

	public static Command getByGroupAndName(String group, String name) {
		return Arrays.stream(values())
				.filter(c -> c.group != null && c.name != null)
				.filter(c -> c.group.equalsIgnoreCase(group) && c.name.equalsIgnoreCase(name))
				.findFirst()
				.orElse(null);
	}

	/**
	 * This method is used to get Command by name
	 *
	 * @param name is the name of command that want to get
	 * @return Command is the command that want to get
	 */
	public static Command getByName(String name) {
		return Arrays.stream(values())
				.filter(c -> c.getName() != null)
				.filter(c -> c.getName().equals(name))
				.findFirst()
				.orElse(null);
	}
}
