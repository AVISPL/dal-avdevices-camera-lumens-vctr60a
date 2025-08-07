/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param;

import static com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.devices.ResponseParser.toFocusAndZoomPositionValue;

import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.LumenVCTR60AConstants;

/**
 * This class is used to define zoom position
 *
 * @author Harry
 * @version 1.0.0
 * @since 1.0.0
 */
public enum ZoomPosition {
	X1("x1", new byte[] { 0x00, 0x00, 0x00, 0x00 }),
	X2("x2", new byte[] { 0x01, 0x08, 0x06, 0x00 }),
	X3("x3", new byte[] { 0x02, 0x04, 0x0A, 0x04 }),
	X4("x4", new byte[] { 0x02, 0x0B, 0x0F, 0x0C }),
	X5("x5", new byte[] { 0x03, 0x01, 0x03, 0x08 }),
	X6("x6", new byte[] { 0x03, 0x04, 0x0F, 0x08 }),
	X7("x7", new byte[] { 0x03, 0x07, 0x0D, 0x0C }),
	X8("x8", new byte[] { 0x03, 0x0A, 0x03, 0x04 }),
	X9("x9", new byte[] { 0x03, 0x0C, 0x01, 0x04 }),
	X10("x10", new byte[] { 0x03, 0x0D, 0x0B, 0x08 }),
	X11("x11", new byte[] { 0x03, 0x0F, 0x00, 0x0C }),
	X12("x12", new byte[] { 0x04, 0x00, 0x00, 0x00 });

	private final String name;
	private final byte[] code;

	ZoomPosition(String name, byte[] code) {
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
	public byte[] getCode() {
		return code;
	}

	/**
	 * Determines the nearest standard zoom level name based on raw zoom bytes.
	 * This method converts four 4-bit nibbles (zoomBytes) into an integer zoom value,
	 * then finds the closest match among defined {@link ZoomPosition} constants.
	 * If a match is found, returns the corresponding name; otherwise returns NOT_AVAILABLE.
	 *
	 * @param zoomBytes array of four bytes representing VISCA zoom nibbles
	 * @return the name of the closest matching zoom level, or NOT_AVAILABLE if none matches
	 */
	public static String getZoomLevelNameFromBytes(byte[] zoomBytes) {
		int value = toFocusAndZoomPositionValue(zoomBytes);

		ZoomPosition closest = null;
		int minDiff = Integer.MAX_VALUE;
		for (ZoomPosition zp : ZoomPosition.values()) {
			int zpValue = toFocusAndZoomPositionValue(zp.getCode());
			int diff = Math.abs(value - zpValue);
			if (diff < minDiff) {
				minDiff = diff;
				closest = zp;
			}
		}
		return closest != null ? closest.getName() : LumenVCTR60AConstants.NOT_AVAILABLE;
	}
}
