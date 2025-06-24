/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is used for Aver PTZ to save all Constants
 *
 * @author Harry
 * @version 1.0.0
 * @since 1.0.0
 */
public class LumenVCTR60AConstants {
	public static final byte BYTE_SUFFIX = (byte) 0xFF;
	public static final char HASH = '#';
	public static final String SEMICOLON = ";";
	public static final String EQUAL = "=";
	public static final String GET_DEVICE_INFO_URL = "//storks/?cmd=get_sys_stat";
	public static final String MAC_PREFIX = "00:18:1A";
	public static final String DEVICE_MFG = "AVer Information Co.";
	public static final String MAC_LABEL = "mac";
	public static final String DEVICE_FIRMWARE_VERSION_LABEL = "fw_ver";
	public static final String DEVICE_MODEL_LABEL = "model_name";
	public static final String DEVICE_SERIAL_NUMBER_VERSION_LABEL = "sn";
	public static final String LABEL_START_HUE_LEVEL = "0";
	public static final String LABEL_END_HUE_LEVEL = "14";
	public static final float RANGE_START_HUE_LEVEL = 0F;
	public static final float RANGE_END_HUE_LEVEL = 14F;
	public static final String LABEL_START_GAIN_LEVEL = "0";
	public static final String SWITCH_STATUS_ON = "1";
	public static final String SWITCH_STATUS_OFF = "0";
	public static final String PLUS = "+";
	public static final String MINUS = "-";
	public static final String CLOSE_PARENTHESIS = "\\)";
	public static final String DEFAULT_PRESET = "Please select a preset";
	public static final String SYSTEM = "System";

	public static final String NONE_VALUE = "None";
	public static final String NULL = "Null";
	public static final String NOT_AVAILABLE = "N/A";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String ON = "On";
	public static final String OFF = "Off";

	public static final String ADAPTER_METADATA = "AdapterMetadata";
	public static final String ADAPTER_VERSION = "AdapterVersion";
	public static final String ADAPTER_BUILD_DATE = "AdapterBuildDate";
	public static final String ADAPTER_UPTIME_MIN = "AdapterUptime(min)";
	public static final String ADAPTER_UPTIME = "AdapterUptime";

	protected static final byte[] FAKE_COMPLETION = new byte[] { 0x01, 0x11, 0x00, 0x03, 0x00, 0x00, 0x00, 0x01, (byte) 0x90, 0x51, (byte) 0xFF };
	protected static final List<String> SHUTTER_VALUES = new ArrayList<>(
			Arrays.asList("1/10000", "1/5000", "1/3000", "1/2500", "1/2000", "1/1500", "1/1000", "1/725", "1/500", "1/350", "1/250", "1/180", "1/120", "1/100", "1/90", "1/60", "1/30"));
	protected static final List<String> IRIS_LEVELS = new ArrayList<>(
			Arrays.asList("Close", "F14", "F11", "F8.0", "F6.8", "F5.6", "F4.8", "F4.0", "F3.4", "F2.8", "F2.4", "F2.0", "F1.8", "F1.6"));
}
