/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is used for Lumens to save all Constants
 *
 * @author Harry
 * @version 1.0.0
 * @since 1.0.0
 */
public class LumenVCTR60AConstants {
	public static final byte BYTE_SUFFIX = (byte) 0xFF;
	public static final String HASH = "#";
	public static final String LABEL_END_HUE_LEVEL = "14";
	public static final float RANGE_START_HUE_LEVEL = 0F;
	public static final float RANGE_END_HUE_LEVEL = 14F;
	public static final String SWITCH_STATUS_ON = "1";
	public static final String SWITCH_STATUS_OFF = "0";
	public static final long DELAY_PERIOD = 45000;
	public static final String PLUS = "+";
	public static final String MINUS = "-";
	public static final String CLOSE_PARENTHESIS = "\\)";
	public static final String DEFAULT_PRESET = "Please select a preset";
	public static final String POWER_ON_STATUS = "Turning on...";
	public static final String POWER_OFF_STATUS = "Turning off...";
	public static final int FOCUS_STEP = 10;
	public static final int FOCUS_MIN = 0x0000; // Far
	public static final int FOCUS_MAX = 0x131A; // Near

	public static final String NONE_VALUE = "None";
	public static final String NULL = "Null";
	public static final String NOT_AVAILABLE = "N/A";
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String ON = "On";
	public static final String OFF = "Off";
	public static final String ZERO = "0";

	public static final String ADAPTER_METADATA = "AdapterMetadata";
	public static final String ADAPTER_VERSION = "AdapterVersion";
	public static final String ADAPTER_BUILD_DATE = "AdapterBuildDate";
	public static final String ADAPTER_UPTIME_MIN = "AdapterUptime(min)";
	public static final String ADAPTER_UPTIME = "AdapterUptime";

	protected static final byte[] FAKE_COMPLETION = new byte[] { 0x01, 0x11, 0x00, 0x03, 0x00, 0x00, 0x00, 0x01, (byte) 0x90, 0x51, (byte) 0xFF };
	protected static final List<String> SHUTTER_VALUES = new ArrayList<>(
			Arrays.asList("1/10000", "1/5000", "1/3000", "1/2500", "1/2000", "1/1500", "1/1000", "1/725", "1/500", "1/350", "1/250", "1/180", "1/120", "1/100", "1/90", "1/60", "1/30"));
	protected static final List<String> IRIS_LEVELS = new ArrayList<>(
			Arrays.asList("Close", "F14", "F11", "F9.6", "F8", "F6.8", "F5.6", "F4.8", "F4", "F3.4", "F2.8", "F2.4", "F2", "F1.6"));
}
