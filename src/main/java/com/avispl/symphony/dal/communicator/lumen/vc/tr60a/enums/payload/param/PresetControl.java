/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param;

/**
 * This class is used to define preset control
 *
 * @author Harry
 * @version 1.0.0
 * @since 1.0.0
 */
public enum PresetControl {
	/**
	 * Control name: SAVE, RECALL
	 * Extended property name: Save, Load
	 */
	PRESET_VALUE("PresetValue", (byte) 0x00),
	LAST_PRESET_RECALLED("LastPresetRecalled", (byte) 0x00),
	SET("Save", (byte) 0x01),
	RECALL("ReCall", (byte) 0x02);


	private final String name;
	private final byte code;

	PresetControl(String name, byte code) {
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
