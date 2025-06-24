package com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param;

import java.util.Arrays;

/**
 * This class is used to preset speed
 *
 * @author Harry
 * @version 1.0.0
 * @since 1.0.0
 */
public enum PresetSpeed {
	ONE("5deg/sec", (byte) 0x00),
	TWO("25deg/sec", (byte) 0x01),
	THREE("50deg/sec", (byte) 0x02),
	FOUR("80deg/sec", (byte) 0x03),
	FIVE("120deg/sec", (byte) 0x04),
	;

	private final String name;
	private final byte code;

	PresetSpeed(String name, byte code) {
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
	 * This method is used to get preset speed by name
	 *
	 * @param name is the name of af preset speed that want to get
	 * @return PresetSpeed is the preset speed that want to get
	 */
	public static PresetSpeed getByName(String name) {
		return Arrays.stream(values())
				.filter(c -> c.getName().equals(name))
				.findFirst()
				.orElse(null);
	}
}
