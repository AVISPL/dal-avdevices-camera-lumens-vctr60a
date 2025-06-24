/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a;

import com.avispl.symphony.dal.communicator.RestCommunicator;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.dto.DeviceInfo;

/**
 * This class is used to getting monitoring properties:
 *
 * - Device Mfg
 * - Device Model
 * - Serial Number
 * - Firmware Version
 *
 * @author Harry
 * @version 1.0.0
 * @since 1.0.0
 */
public class LumenVCTR60ARestCommunicator extends RestCommunicator {
	/**
	 * {@inheritdoc}
	 * This is a method used to authenticate
	 */
	@Override
	protected void authenticate() {
		// Do nothing
	}

	/**
	 * This method used to get data from device
	 */
	public DeviceInfo getDeviceInfo() throws Exception {
		String response = doGet(LumenVCTR60AConstants.GET_DEVICE_INFO_URL);
		return digestResponse(response);
	}

	/**
	 * This method used to digest a response from device
	 *
	 * @param responseData This is a response data
	 */
	private DeviceInfo digestResponse(String responseData) {
		DeviceInfo deviceInfo = new DeviceInfo();
		String[] responses = responseData.split(LumenVCTR60AConstants.SEMICOLON);
		String[] values;

		for (String response : responses) {
			values = response.split(LumenVCTR60AConstants.EQUAL);
			if (response.startsWith(LumenVCTR60AConstants.MAC_LABEL)) {
				if (values[1].startsWith(LumenVCTR60AConstants.MAC_PREFIX)) {
					deviceInfo.setDeviceMfg(LumenVCTR60AConstants.DEVICE_MFG);
				}
			} else if (response.startsWith(LumenVCTR60AConstants.DEVICE_FIRMWARE_VERSION_LABEL)) {
				deviceInfo.setDeviceFirmwareVersion(values[1]);
			} else if (response.startsWith(LumenVCTR60AConstants.DEVICE_MODEL_LABEL)) {
				deviceInfo.setDeviceModel(values[1]);
			} else if (response.startsWith(LumenVCTR60AConstants.DEVICE_SERIAL_NUMBER_VERSION_LABEL)) {
				deviceInfo.setDeviceSerialNumber(values[1]);
			}
		}

		return deviceInfo;
	}
}
