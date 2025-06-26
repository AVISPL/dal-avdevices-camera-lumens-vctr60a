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
	EXPOSURE("Exposure", null),
	EXPOSURE_MODE("Mode", new byte[] { 0x39 }),
	ANTI_FLICK("AntiFlicker", new byte[] { 0x3C }),
	WDR_OPTION("WDR", new byte[] { 0x2D }),
	GAIN_LIMIT_DIRECT("GainLimit(dB)", new byte[] { 0x2C }),
	SHUTTER_DIRECT("ShutterSpeed", new byte[] { 0x4A }),
	SHUTTER_CURRENT("ShutterSpeedCurrent", null),
	IRIS_DIRECT("IrisLevel", new byte[] { 0x4B }),
	IRIS_CURRENT("IrisLevelCurrent", null),
	GAIN_DIRECT("GainLevel", new byte[] { 0x4C, 0x00, 0x00 }),
	GAIN_CURRENT("GainLevelCurrent", null),
	GAIN_LIMIT_CURRENT("GainLimitCurrent", null),
	EXP_COMP_DIRECT("ExposureCompLevel", new byte[] { 0x4E }),
	EXP_COMP_CURRENT("ExposureCompLevelCurrent", null),

	//Focus group
	FOCUS_MODE("Mode", new byte[] { 0x38 }),
	AF_FRAME("AFFrame", new byte[] { 0x5C }),
	AF_SENSITIVE("AFSensitivity", new byte[] { 0x58 }),

	//Mirror group
	MIRROR_GROUP("Mirror", null),
	FLIP("Flip", new byte[] { 0x66 }),
	MIRROR("Mirror", new byte[] { 0x61 }),

	//Picture group
	IMAGE_MODE("ImageMode", new byte[] { 0x3F, 0x04 }),
	PICTURE("Picture", null),
	TWO_DNR("2DNR", new byte[] { 0x53 }),
	THREE_DNR("3DNR", new byte[] { 0x54 }),
	HUE("Hue", new byte[] { 0x4F }),
	HUE_CURRENT("HueCurrent", null),
	SATURATION("Saturation", new byte[] { 0x49 }),
	SATURATION_CURRENT("SaturationCurrent", null),
	BRIGHTNESS("Brightness", new byte[] { 0x4D }),
	BRIGHTNESS_CURRENT("BrightnessCurrent", null),
	SHARPNESS("Sharpness", new byte[] { 0x42 }),
	SHARPNESS_CURRENT("SharpnessCurrent", null),
	GAMMA("Gamma", new byte[] { 0x5B }),
	GAMMA_CURRENT("GammaCurrent", null),

	// PTZ group
	PTZ("PanTiltZoom", null),
	D_ZOOM_LIMIT("DZoomLimit", new byte[] { 0x26 }),
	INITIAL_POSITION("InitialPosition", new byte[] { 0x75, 0x6A }),
	PRESET_SPEED("PresetSpeed", new byte[] { 0x75, 0x32 }),
	PTZ_SPEED_COMP("PTZSpeedComp", new byte[] { 0x1F, 0x01 }),
	MOTIONLESS_PRESET("MotionlessPreset", new byte[] { 0x01 }),

	// WB group
	WB_MODE("Mode", new byte[] { 0x35 }),
	WB_ONE_PUSH_TRIGGER("OnePushTrigger", new byte[] { 0x10, 0x05 }),
	RGAIN("ManualRed", new byte[] { 0x03 }),
	RGAIN_INQ("ManualRedCurrent", new byte[] { 0x43 }),
	BGAIN("ManualBlue", new byte[] { 0x04 }),
	BGAIN_INQ("ManualBlueCurrent", new byte[] { 0x44 }),

	// System
	DHCP("DHCP", new byte[] { 0x01 }),
	IPV4_NETMASK("IPv4Netmask", new byte[] { 0x02 }),
	GATEWAY("Gateway", new byte[] { 0x04 }),
	DNS("DNS", new byte[] { 0x05 }),
	TALLY_MODE("TallyMode", new byte[] { 0x01, 0x0A, 0x00 }),
	TRACKING_LED_STATUS("TrackingLedStatus", new byte[] { 0x00, 0x06 }),
	USB("USBStreamStatus", new byte[] { 0x02, 0x01 }),

	POWER("Power", new byte[] { 0x00 }),
	POWER_STATUS("PowerStatus", null),
	ZOOM("ZoomControl", new byte[] { 0x07 }),
	FOCUS("FocusControl", new byte[] { 0x08 }),

	FOCUS_ONE_PUSH("OnePush", new byte[] { 0x18, 0x01 }),
	WHITE_BALANCE("WhiteBalance", null),

	AUTO_SLOW_SHUTTER("AutoSlowShutter", new byte[] { 0x5A }),

	BACKLIGHT("Backlight", new byte[] { 0x33 }),
	PRESET("PresetControl", new byte[] { 0x3F }),
	PAN_TILT_DRIVE("PanTiltControl", new byte[] { 0x01 }),
	PAN_TILT_HOME("Home", new byte[] { 0x04 }),
	FIRMWARE_VERSION("FirmwareVersion", new byte[] { (byte) 0xCF }),
	SERIAL_NUMBER("SerialNumber", new byte[] { 0x18 }),
	MAC_ADDRESS("MACAddress", new byte[] { 0x78 }),
	CAMERA_ID("CameraId", new byte[] { (byte) 0xCE }),
	HDMI_FORMAT("VideoHDMIFormat", new byte[] { 0x01, 0x03 }),
	PRIVACY_MODE("VideoPrivacyMode", new byte[] { 0x00, 0x02 }),
	PIP("VideoPIP", new byte[] { 0x02, 0x02 }),
	BAUD_RATE("BaudRate", new byte[] { 0x24, 0x00 }),
	SLOW_PAN_TILT("PanTiltSlowMode", new byte[] { 0x044 });

	private final String name;
	private final byte[] code;

	Command(String name, byte[] code) {
		this.name = name;
		this.code = code;
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

	/**
	 * This method is used to get Command by name
	 *
	 * @param name is the name of command that want to get
	 * @return Command is the command that want to get
	 */
	public static Command getByName(String name) {
		return Arrays.stream(values())
				.filter(c -> c.getName().equals(name))
				.findFirst()
				.orElse(null);
	}
}
