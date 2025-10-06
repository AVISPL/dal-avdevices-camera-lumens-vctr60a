/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.devices;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.avispl.symphony.dal.BaseDevice;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.LumenVCTR60AConstants;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.command.Command;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.AFFrame;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.AFSensitivity;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.AntiFlicker;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.BacklightStatus;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.ExposureCompLevel;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.ExposureMode;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.FocusMode;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.GainLevel;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.InitialPosition;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.IrisControl;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.PanTiltSpeedComp;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.PictureDNROptions;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.PictureMode;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.PowerStatus;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.PresetSpeed;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.ShutterControl;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.SlowPanTiltStatus;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.SlowShutterStatus;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.WBMode;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.WDROptions;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.ZoomPosition;

/**
 * This class parses the reply payloads from the device into meaningful objects.
 *
 * @author Harry
 * @version 1.0.0
 * @since 1.0.0
 */
public class ResponseParser extends BaseDevice {

	/**
	 * Parse reply according to the expected command.
	 *
	 * @param expectedCommand Command to parse
	 * @param reply           Reply payload
	 * @return Object         Parsed result
	 */
	public Object parse(Command expectedCommand, byte[] reply) {
		byte currentValue = reply[2];
		switch (expectedCommand) {
			case POWER:
				return findEnum(PowerStatus.values(), currentValue);
			case FOCUS_MODE:
				return findEnum(FocusMode.values(), currentValue);
			case EXPOSURE_MODE:
				return findEnum(ExposureMode.values(), currentValue);
			case ANTI_FLICK:
				return findEnum(AntiFlicker.values(), currentValue);
			case IMAGE_MODE:
				return findEnum(PictureMode.values(), currentValue);
			case INITIAL_POSITION:
				return findEnum(InitialPosition.values(), currentValue);
			case PRESET_SPEED:
				return findEnum(PresetSpeed.values(), currentValue);
			case THREE_DNR:
			case TWO_DNR:
				return findEnum(PictureDNROptions.values(), currentValue);
			case WDR_OPTION:
				return findEnum(WDROptions.values(), currentValue);
			case MOTIONLESS_PRESET:
			case AUTO_SLOW_SHUTTER:
				return findEnum(SlowShutterStatus.values(), currentValue);
			case IRIS_DIRECT:
				return parseIris(reply);
			case SHUTTER_DIRECT:
				return parseShutter(reply);
			case EXP_COMP_DIRECT:
				return parseExposureComp(reply);
			case RGAIN_INQ:
			case BGAIN_INQ:
				return reply[4] * 16 + reply[5];
			case GAIN_LIMIT_DIRECT:
				return 2 * (Byte.toUnsignedInt(reply[2]) - 4) + 8;
			case GAIN_LEVEL:
				return parseGainLevel(reply);
			case PRESET:
				return Byte.toUnsignedInt(reply[2]);
			case BACKLIGHT:
				return findEnum(BacklightStatus.values(), currentValue);
			case WB_MODE:
				return findEnum(WBMode.values(), currentValue);
			case PTZ_SPEED_COMP:
				return findEnum(PanTiltSpeedComp.values(), currentValue);
			case AF_SENSITIVE:
				return findEnum(AFSensitivity.values(), currentValue);
			case AF_FRAME:
				return findEnum(AFFrame.values(), currentValue);
			case TRACKING_LED_STATUS:
			case DHCP:
				return getEnumByName(SlowPanTiltStatus.values(), currentValue);
			case PRIVACY_MODE:
			case SLOW_PAN_TILT:
			case MIRROR:
			case FLIP:
				return findEnum(SlowPanTiltStatus.values(), currentValue);
			case D_ZOOM_LIMIT:
				return "x" + (Byte.toUnsignedInt(reply[2]) + 1);
			case GAMMA:
				return String.valueOf(reply[2] & 0xFF);
			case HUE:
			case SATURATION:
			case BRIGHTNESS:
			case SHARPNESS:
				return parsePictureCustomValue(reply);
			case MAC_ADDRESS:
				return parseMac(reply);
			case IPV4_ADDRESS:
			case IPV4_NETMASK:
			case GATEWAY:
			case DNS:
				return parseIp(reply);
			case FIRMWARE_VERSION:
			case SERIAL_NUMBER:
			case CAMERA_ID:
				return parseAsciiValue(reply);
			case ZOOM_POSITION:
				return getZoomLevelNameFromReply(reply);
			case FOCUS_POSITION:
				return getFocusPositionFromReply(reply);
			default:
				throw new IllegalStateException("Unexpected command: " + expectedCommand);
		}
	}

	/**
	 * Finds the first enum constant in the given array with a matching byte code.
	 *
	 * @param <T>    enum type that implements {@code DeviceEnum}
	 * @param values array of enum constants to search
	 * @param code   byte code to match
	 * @return matching enum constant, or {@code null} if no match is found
	 */
	private <T extends DeviceEnum> T findEnum(T[] values, byte code) {
		return Arrays.stream(values)
				.filter(mode -> mode.getCode() == code)
				.findFirst()
				.orElse(null);
	}

	/**
	 * Extracts and converts the focus position from a VISCA response.
	 *
	 * @param reply the complete VISCA response from the camera
	 * @return an integer representing the decoded focus position
	 */
	private int getFocusPositionFromReply(byte[] reply) {
		byte[] focusBytes = Arrays.copyOfRange(reply, 2, 6);
		return toFocusAndZoomPositionValue(focusBytes);
	}

	/**
	 * Convert 4-byte array to a single integer (Zoom position value)
	 *
	 * @param bytes byte array of size 4
	 * @return integer zoom position value
	 */
	public static int toFocusAndZoomPositionValue(byte[] bytes) {
		if (bytes == null || bytes.length != 4) {
			throw new IllegalArgumentException("Position must be 4 bytes.");
		}
		return ((bytes[0] & 0xFF) << 12)
				| ((bytes[1] & 0xFF) << 8)
				| ((bytes[2] & 0xFF) << 4)
				| (bytes[3] & 0x0F);
	}

	/**
	 * get zoom level name from the given reply.
	 * Uses bytes 2 to 6 to compute an index into {@link ShutterControl}.
	 *
	 * @param reply response byte array
	 * @return zoom level name
	 */
	private String getZoomLevelNameFromReply(byte[] reply) {
		byte[] zoomBytes = Arrays.copyOfRange(reply, 2, 6);
		String zoomLevel = ZoomPosition.getZoomLevelNameFromBytes(zoomBytes);
		for (ZoomPosition zp : ZoomPosition.values()) {
			if(zoomLevel.equals(zp.getName())){
				return zp.getName();
			}
		}
		return LumenVCTR60AConstants.NOT_AVAILABLE;
	}

	/**
	 * Finds the name of a {@link SlowPanTiltStatus} enum constant matching the given byte code.
	 *
	 * @param values     array of SlowPanTiltStatus constants to search
	 * @param code       byte code to match
	 * @return matching enum name, or {@code null} if not found
	 */
	private String getEnumByName(SlowPanTiltStatus[] values, byte code) {
		return Arrays.stream(values)
				.filter(mode -> mode.getCode() == code)
				.map(SlowPanTiltStatus::getName)
				.findFirst()
				.orElse(null);
	}

	/**
	 * Parses a gain level from the specified device reply.
	 *
	 * @param reply response byte array from the device
	 * @return gain level name from {@link GainLevel} or {@code NONE_VALUE} if index is out-of-range
	 */
	private String parseGainLevel(byte[] reply) {
		List<String> gainList = Arrays.stream(GainLevel.values())
				.map(GainLevel::getName)
				.collect(Collectors.toList());
		int index = Byte.toUnsignedInt(reply[5]);
		return index < gainList.size() ? gainList.get(index) : LumenVCTR60AConstants.NONE_VALUE;
	}

	/**
	 * Parses an iris level from the specified device reply.
	 *
	 * @param reply response byte array from the device
	 * @return iris level name from {@link IrisControl} or {@code NONE_VALUE} if index is out-of-range
	 */
	private String parseIris(byte[] reply) {
		List<String> irisList = Arrays.stream(IrisControl.values())
				.map(IrisControl::getName)
				.collect(Collectors.toList());
		int index = Byte.toUnsignedInt(reply[5]);
		return index < irisList.size() ? irisList.get(index) : LumenVCTR60AConstants.NONE_VALUE;
	}

	/**
	 * Parses shutter speed value from the given reply.
	 * Uses bytes 4 and 5 to compute an index into {@link ShutterControl}.
	 *
	 * @param reply response byte array
	 * @return shutter speed name
	 */
	private String parseShutter(byte[] reply) {
		List<String> shutterList = Arrays.stream(ShutterControl.values())
				.map(ShutterControl::getName)
				.collect(Collectors.toList());
		int raw = Byte.toUnsignedInt(reply[4]) * 16 + Byte.toUnsignedInt(reply[5]);
		return shutterList.get(Math.min(raw, shutterList.size() - 1));
	}

	/**
	 * Parses exposure compensation level from device reply.
	 *
	 * @param reply response byte array
	 * @return exposure compensation name from {@link ExposureCompLevel}, or NOT_AVAILABLE if index out of bounds
	 * @throws IllegalArgumentException if reply array is too short
	 */
	private String parseExposureComp(byte[] reply) {
			if (reply == null || reply.length < 7) {
				throw new IllegalArgumentException("Invalid response: too short.");
			}
			List<String> list = Arrays.stream(ExposureCompLevel.values())
					.map(ExposureCompLevel::getName)
					.collect(Collectors.toList());
			int index = Byte.toUnsignedInt(reply[5]);
			return index < list.size() ? list.get(index) : LumenVCTR60AConstants.NOT_AVAILABLE;
	}

	/**
	 * Parses a custom picture parameter value from the reply.
	 *
	 * Interprets a hex-coded numeric value spanning the last 4 nibbles.
	 *
	 * @param reply response byte array
	 * @return decimal string representation of the value
	 */
	private String parsePictureCustomValue(byte[] reply) {
		int pictureValue = 0;
		for (int i = reply.length - 5; i < reply.length - 1; i++) {
			pictureValue = (pictureValue << 4) | (reply[i] & 0x0F);
		}
		return String.valueOf(pictureValue);
	}

	/**
	 * Parses MAC address from device reply bytes.
	 *
	 * @param reply response byte array
	 * @return MAC address in lowercase format "xx:xx:xx:xx:xx:xx"
	 */
	private String parseMac(byte[] reply) {
		byte[] macBytes = Arrays.copyOfRange(reply, 2, 14);
		StringBuilder mac = new StringBuilder();
		for (int i = 0; i < 12; i += 2) {
			int high = macBytes[i] & 0x0F;
			int low = macBytes[i + 1] & 0x0F;
			mac.append(String.format("%02X", (high << 4) | low));
			if (i < 10) mac.append(":");
		}
		return mac.toString().toLowerCase();
	}

	/**
	 * Parses IPv4 address from the device response.
	 *
	 * @param reply response byte array
	 * @return IPv4 address string "x.x.x.x"
	 */
	private String parseIp(byte[] reply) {
		byte[] ipBytes = Arrays.copyOfRange(reply, 2, 10);
		int part1 = ((ipBytes[0] & 0x0F) << 4) | (ipBytes[1] & 0x0F);
		int part2 = ((ipBytes[2] & 0x0F) << 4) | (ipBytes[3] & 0x0F);
		int part3 = ((ipBytes[4] & 0x0F) << 4) | (ipBytes[5] & 0x0F);
		int part4 = ((ipBytes[6] & 0x0F) << 4) | (ipBytes[7] & 0x0F);
		return String.format("%d.%d.%d.%d", part1, part2, part3, part4);
	}

	/**
	 * Parses printable ASCII text from the reply array.
	 *
	 * Trims zero and 0xFF padding at the end, and returns
	 * NOT_AVAILABLE if result is empty or contains non-printable characters.
	 *
	 * @param reply response byte array
	 * @return parsed ASCII string or NOT_AVAILABLE
	 */
	private String parseAsciiValue(byte[] reply) {
		int start = 2, end = reply.length;
		while (end > start && (reply[end - 1] == 0x00 || reply[end - 1] == (byte) 0xFF)) end--;
		String value = new String(reply, start, end - start, StandardCharsets.US_ASCII);
		boolean isPrintable = value.codePoints().allMatch(c -> c >= 32 && c <= 126);
		return (isPrintable && !value.trim().isEmpty()) ? value : LumenVCTR60AConstants.NOT_AVAILABLE;
	}
}