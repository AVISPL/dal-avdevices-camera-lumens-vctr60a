package com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param;

import java.util.Arrays;

import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.devices.DeviceEnum;

/**
 * This class is used to define initial position
 *
 * @author Harry
 * @version 1.0.0
 * @since 1.0.0
 */
public enum InitialPosition implements DeviceEnum {
	LAST_MEM("LastMEM", (byte) 0x00),
	FIRST_PRESET("1stPreset", (byte) 0x01);

	private final String name;
	private final byte code;

	InitialPosition(String name, byte code) {
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
	 * This method is used to get initial position by name
	 *
	 * @param name is the name of af initial position that want to get
	 * @return InitialPosition is the initial position that want to get
	 */
	public static InitialPosition getByName(String name) {
		return Arrays.stream(values())
				.filter(c -> c.getName().equals(name))
				.findFirst()
				.orElse(null);
	}
}
