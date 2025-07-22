/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.devices;

import java.util.Arrays;

import com.avispl.symphony.dal.BaseDevice;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.PayloadType;
import static com.avispl.symphony.dal.communicator.lumen.vc.tr60a.LumenVCTR60AUtils.convertIntToByteArray;

/**
 * This class validates response packets from the device.
 *
 * @author Harry
 * @version 1.0.0
 * @since 1.0.0
 */
public class ResponseValidator extends BaseDevice {

	private final String host;
	private final int port;

	public ResponseValidator(String host, int port) {
		this.host = host;
		this.port = port;
	}

	/**
	 * Validate response payload type and sequence number.
	 *
	 * @param response     Response byte array from device
	 * @param sequenceNum  Expected sequence number
	 * @param expectedType Expected payload type
	 */
	public void validate(byte[] response, int sequenceNum, PayloadType expectedType) {
		if (response == null || response.length < 8) {
			throw new IllegalStateException("Invalid response packet from " + host + ":" + port);
		}

		if (response[1] != expectedType.getCode()) {
			throw new IllegalStateException("Unexpected reply from " + host + ":" + port);
		}

		byte[] responseSeqNum = Arrays.copyOfRange(response, 4, 8);
		byte[] expectedSeqNum = convertIntToByteArray(sequenceNum);

		if (!Arrays.equals(expectedSeqNum, responseSeqNum)) {
			this.logger.error("error: Unexpected sequence number: " + this.host + " port: " + this.port);
			throw new IllegalStateException("Unexpected sequence number from " + host + ":" + port);
		}
	}

}
