/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a;

import static com.avispl.symphony.dal.communicator.lumen.vc.tr60a.LumenVCTR60AUtils.buildSendPacket;
import static com.avispl.symphony.dal.communicator.lumen.vc.tr60a.LumenVCTR60AUtils.convertIntToByteArray;
import static com.avispl.symphony.dal.communicator.lumen.vc.tr60a.LumenVCTR60AUtils.convertOneByteNumberToTwoBytesArray;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.util.CollectionUtils;

import com.avispl.symphony.api.dal.control.Controller;
import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.avispl.symphony.api.dal.error.CommandFailureException;
import com.avispl.symphony.api.dal.monitor.Monitorable;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.Index;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.ReplyStatus;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.PayloadCategory;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.PayloadType;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.Prefix;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.command.Command;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.command.CommandType;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.AFFrame;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.AFSensitivity;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.AntiFlicker;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.ExposureMode;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.BGainControl;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.BacklightStatus;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.FocusControl;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.FocusMode;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.GeneralProperty;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.InitialPosition;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.PanTiltDrive;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.PanTiltSpeedComp;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.PictureDNROptions;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.PictureMode;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.PowerStatus;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.PresetControl;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.PresetSpeed;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.RGainControl;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.SlowPanTiltStatus;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.SlowShutterStatus;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.WBMode;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.WDROptions;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.ZoomControl;
import com.avispl.symphony.dal.util.StringUtils;

/**
 * Lumen VC TR60A Adapter
 *
 * Properties are divided into groups:
 * Exposure, Focus, Mirror, SystemNetwork,
 * PanTiltControl, Picture, Preset, PTZ, WhiteBalance, Zoom
 *
 * Supported features are:
 *
 * Monitoring:
 * - Preset recall and status feedback
 * - CameraId
 * - FirmwareVersion
 * - MACAddress
 * - SerialNumber
 * - VideoHDMIFormat
 * - VideoPIP
 * - VideoPrivacyMode
 * - VideoResolution
 *
 * Controlling:
 * Exposure Group
 * - AntiFlicker
 * - ExposureCompLevel
 * - GainLimit
 * - GainLevel
 * - Mode
 * - IrisLevel
 * - ShutterSpeed
 * - WDR
 * Focus
 * Mirror
 * PanTiltControl
 * Picture
 * Preset
 * PTZ
 * WhiteBalance
 * ZoomControl
 *
 * @author Harry
 * @version 1.0.0
 * @since 1.0.0
 */
public class LumenVCTR60ACommunicator extends UDPCommunicator implements Controller, Monitorable {
	private String VISCACameraIDAddress = "1";
	private String panSpeed = "1";
	private String tiltSpeed = "1";
	private String zoomSpeed = "1";
	private String focusSpeed = "1";
	private int cameraIDInt = 1;
	private int panSpeedInt = 1;
	private int tiltSpeedInt = 1;
	private int zoomSpeedInt = 1;
	private int focusSpeedInt = 1;
	private int sequenceNumber = 2;
	private int currentPreset = -1;
	private long nextMonitoringCycleTimestamp = System.currentTimeMillis();

	/** Adapter metadata properties - adapter version and build date */
	private Properties adapterProperties;

	/**
	 * Constructor set command error and success list to be used as well the default camera ID
	 */
	public LumenVCTR60ACommunicator() throws IOException {
		super();
		this.setPort(52381);
		this.setCommandSuccessList(Collections.singletonList(getHexByteString(ReplyStatus.COMPLETION.getCode())));
		adapterProperties = new Properties();
		adapterProperties.load(getClass().getResourceAsStream("/version.properties"));
		this.setCommandErrorList(Arrays.asList(
				getHexByteString(ReplyStatus.SYNTAX_ERROR_CONTROL.getCode()),
				getHexByteString(ReplyStatus.SYNTAX_ERROR_INQUIRY.getCode()),
				getHexByteString(ReplyStatus.COMMAND_BUFFER_FULL_CONTROL.getCode()),
				getHexByteString(ReplyStatus.COMMAND_BUFFER_FULL_INQUIRY.getCode()),
				getHexByteString(ReplyStatus.NO_SOCKET_CONTROL.getCode()),
				getHexByteString(ReplyStatus.NO_SOCKET_INQUIRY.getCode()),
				getHexByteString(ReplyStatus.COMMAND_NOT_EXECUTABLE_CONTROL.getCode()),
				getHexByteString(ReplyStatus.COMMAND_NOT_EXECUTABLE_INQUIRY.getCode())
		));
	}

	/**
	 * ReentrantLock to prevent telnet session is closed when adapter is retrieving statistics from the device.
	 */
	private final ReentrantLock reentrantLock = new ReentrantLock();

	/**
	 * Store previous/current ExtendedStatistics
	 */
	private ExtendedStatistics localExtendedStatistics;

	private static final AtomicInteger seqCounter = new AtomicInteger(0);

	/**
	 * Device adapter instantiation timestamp.
	 */
	private long adapterInitializationTimestamp;

	/**
	 * Retrieves {@code {@link #VISCACameraIDAddress }}
	 *
	 * @return value of {@link #VISCACameraIDAddress}
	 */
	public String getVISCACameraIDAddress() {
		return VISCACameraIDAddress;
	}

	/**
	 * Sets {@code VISCAAddress}
	 *
	 * @param VISCACameraIDAddress the {@code java.lang.String} field
	 */
	public void setVISCACameraIDAddress(String VISCACameraIDAddress) {
		this.VISCACameraIDAddress = VISCACameraIDAddress;
	}

	/**
	 * {@inheritdoc}
	 * This method is recalled by Symphony to control specific property
	 *
	 * @param controllableProperty This is the property to be controlled
	 */
	@Override
	public void controlProperty(ControllableProperty controllableProperty) throws IOException {
		if (System.currentTimeMillis() < nextMonitoringCycleTimestamp) {
			throw new IllegalStateException("Cannot control while power is null ");
		}

		String property = controllableProperty.getProperty();
		String value = String.valueOf(controllableProperty.getValue());

		if (this.logger.isDebugEnabled()) {
			this.logger.debug("controlProperty property " + property);
			this.logger.debug("controlProperty value " + value);
		}

		String[] splitProperty = property.split(String.valueOf(LumenVCTR60AConstants.HASH));
		Command command = Command.getByName(splitProperty[0]);

		switch (command) {
			case ZOOM: {
				if (Objects.equals(splitProperty[1], ZoomControl.TELE.getName())) {
					performControl(PayloadCategory.CAMERA, Command.ZOOM, (byte) (ZoomControl.TELE.getCode() + zoomSpeedInt));
				} else if (Objects.equals(splitProperty[1], ZoomControl.WIDE.getName())) {
					performControl(PayloadCategory.CAMERA, Command.ZOOM, (byte) (ZoomControl.WIDE.getCode() + zoomSpeedInt));
				}
				performControl(PayloadCategory.CAMERA, Command.ZOOM, ZoomControl.STOP.getCode());
				break;
			}
			case FOCUS: {
				// (1)Name -> Split string by ")" and get the second value of slit string
				String focusControlName = splitProperty[1].split(LumenVCTR60AConstants.CLOSE_PARENTHESIS, 2)[1];

				if (Objects.equals(focusControlName, Command.FOCUS_MODE.getName())) {
					if (Objects.equals(value, LumenVCTR60AConstants.SWITCH_STATUS_ON)) {
						performControl(PayloadCategory.CAMERA, Command.FOCUS_MODE, FocusMode.MANUAL.getCode());
					} else if (Objects.equals(value, LumenVCTR60AConstants.SWITCH_STATUS_OFF)) {
						performControl(PayloadCategory.CAMERA, Command.FOCUS_MODE, FocusMode.AUTO.getCode());
					}
					break;
				}

				if (Objects.equals(focusControlName, Command.FOCUS_ONE_PUSH.getName())) {
					performControl(PayloadCategory.CAMERA, Command.FOCUS_ONE_PUSH);
					break;
				}

				if (Objects.equals(focusControlName, FocusControl.FAR.getName())) {
					performControl(PayloadCategory.CAMERA, Command.FOCUS, (byte) (FocusControl.FAR.getCode() + focusSpeedInt));
				} else if (Objects.equals(focusControlName, FocusControl.NEAR.getName())) {
					performControl(PayloadCategory.CAMERA, Command.FOCUS, (byte) (FocusControl.NEAR.getCode() + focusSpeedInt));
				}
				performControl(PayloadCategory.CAMERA, Command.FOCUS, FocusControl.STOP.getCode());
				break;
			}
			case EXPOSURE: {
				Command exposureCommand = Command.getByName(splitProperty[1]);
				exposureControl(value, exposureCommand);
				break;
			}
			case WHITE_BALANCE: {
				imageProcessControl(value, splitProperty);
				break;
			}
			case PAN_TILT_DRIVE: {
				String panTiltDriveControlName = splitProperty[1].split(LumenVCTR60AConstants.CLOSE_PARENTHESIS, 2)[1];

				if (Objects.equals(panTiltDriveControlName, Command.PAN_TILT_HOME.getName())) {
					performControl(PayloadCategory.PAN_TILTER, Command.PAN_TILT_HOME);
					break;
				} else if (Objects.equals(panTiltDriveControlName, Command.SLOW_PAN_TILT.getName())) {
					if (Objects.equals(value, LumenVCTR60AConstants.SWITCH_STATUS_ON)) {
						performControl(PayloadCategory.PAN_TILTER, Command.SLOW_PAN_TILT, SlowPanTiltStatus.ON.getCode());
					} else if (Objects.equals(value, LumenVCTR60AConstants.SWITCH_STATUS_OFF)) {
						performControl(PayloadCategory.PAN_TILTER, Command.SLOW_PAN_TILT, SlowPanTiltStatus.OFF.getCode());
					}
					break;
				}

				PanTiltDrive pantTiltDrive = PanTiltDrive.getByName(panTiltDriveControlName);
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				outputStream.write(new byte[] { (byte) panSpeedInt, (byte) tiltSpeedInt });
				outputStream.write(pantTiltDrive.getCode());
				performControl(PayloadCategory.PAN_TILTER, Command.PAN_TILT_DRIVE, outputStream.toByteArray());

				outputStream = new ByteArrayOutputStream();
				outputStream.write(new byte[] { (byte) panSpeedInt, (byte) tiltSpeedInt });
				outputStream.write(PanTiltDrive.STOP.getCode());
				performControl(PayloadCategory.PAN_TILTER, Command.PAN_TILT_DRIVE, outputStream.toByteArray());
				break;
			}
			case PRESET: {
				String presetControlName = splitProperty[1].split(LumenVCTR60AConstants.CLOSE_PARENTHESIS, 2)[1];

				if (Objects.equals(presetControlName, PresetControl.PRESET_VALUE.getName())) {
					try {
						currentPreset = Integer.parseInt(value);
					} catch (NumberFormatException e) {
						// value = "Please select a preset to control"
						currentPreset = -1;
					}
					break;
				}

				if (currentPreset == -1) {
					throw new IllegalArgumentException(LumenVCTR60AConstants.DEFAULT_PRESET);
				}

				if (Objects.equals(presetControlName, PresetControl.SET.getName())) {
					performControl(PayloadCategory.CAMERA, Command.PRESET, PresetControl.SET.getCode(), (byte) currentPreset);
				} else if (Objects.equals(presetControlName, PresetControl.RECALL.getName())) {
					performControl(PayloadCategory.CAMERA, Command.PRESET, PresetControl.RECALL.getCode(), (byte) currentPreset);
				}

				// Reset to default preset value each time set/recall a preset
				currentPreset = -1;
				break;
			}
			default: {
				throw new IllegalStateException("Unexpected value: " + command);
			}
		}
	}

	/**
	 * {@inheritdoc}
	 * This method is recalled by Symphony to control a list of properties
	 *
	 * @param controllableProperties This is the list of properties to be controlled
	 */
	@Override
	public void controlProperties(List<ControllableProperty> controllableProperties) throws IOException {
		if (CollectionUtils.isEmpty(controllableProperties)) {
			throw new IllegalArgumentException("LumenCommunicator: Controllable properties cannot be null or empty");
		}

		for (ControllableProperty controllableProperty : controllableProperties) {
			controlProperty(controllableProperty);
		}
	}

	/**
	 * {@inheritdoc}
	 * This method is recalled by Symphony to get the list of statistics to be displayed
	 *
	 * @return List<Statistics> This return the list of statistics.
	 */
	@Override
	public List<Statistics> getMultipleStatistics() throws Exception {
		reentrantLock.lock();
		try {
			ExtendedStatistics extStats = new ExtendedStatistics();
			Map<String, String> stats = new HashMap<>();
			Map<String, String> dynamicStatistics = new HashMap<>();
			List<AdvancedControllableProperty> advancedControllableProperties = new ArrayList<>();
			StringBuilder errorMessages = new StringBuilder();
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Perform getMultipleStatistics() at host: %s, port: %s", this.getHost(), this.getPort()));
			}
		tryParseIntAdapterProperties(errorMessages);
		checkOutOfRange(errorMessages);
		if (!errorMessages.toString().isEmpty()) {
			throw new IllegalArgumentException(errorMessages.toString());
		}

			// Monitoring capabilities
			retrieveMetadata(stats);
			populateGeneralProperties(stats);
			populateControlCapabilities(stats, advancedControllableProperties);

			extStats.setStatistics(stats);
			extStats.setControllableProperties(advancedControllableProperties);
			extStats.setDynamicStatistics(dynamicStatistics);
			localExtendedStatistics = extStats;
		} finally {
			reentrantLock.unlock();
		}
		return Collections.singletonList(localExtendedStatistics);
	}

	/**
	 * Retrieves metadata information and updates the provided statistics and dynamic map.
	 *
	 * @param stats the map where statistics will be stored
	 */
	private void retrieveMetadata(Map<String, String> stats) {
		try {
			stats.put( LumenVCTR60AConstants.ADAPTER_METADATA + LumenVCTR60AConstants.HASH + LumenVCTR60AConstants.ADAPTER_VERSION,
					getDefaultValueForNullData(adapterProperties.getProperty("adapter.version")));
			stats.put(LumenVCTR60AConstants.ADAPTER_METADATA + LumenVCTR60AConstants.HASH + LumenVCTR60AConstants.ADAPTER_BUILD_DATE,
					getDefaultValueForNullData(adapterProperties.getProperty("adapter.build.date")));

			long adapterUptime = System.currentTimeMillis() - adapterInitializationTimestamp;
			stats.put(LumenVCTR60AConstants.ADAPTER_METADATA + LumenVCTR60AConstants.HASH + LumenVCTR60AConstants.ADAPTER_UPTIME, formatUpTime(String.valueOf(adapterUptime / 1000)));
			stats.put(LumenVCTR60AConstants.ADAPTER_METADATA + LumenVCTR60AConstants.HASH + LumenVCTR60AConstants.ADAPTER_UPTIME_MIN, String.valueOf(adapterUptime / (1000 * 60)));
		} catch (Exception e) {
			logger.error("Failed to populate metadata information", e);
		}
	}

	/**
	 * This method is used for parse adapter properties from String to int value
	 *
	 * @param errorMessages is the error messages of properties when parse fail
	 */
	private void tryParseIntAdapterProperties(StringBuilder errorMessages) {
		try {
			cameraIDInt = Integer.parseInt(VISCACameraIDAddress);
		} catch (NumberFormatException e) {
			errorMessages.append("Camera ID with value ").append(this.VISCACameraIDAddress).append(" is wrong format of number. ");
		}

		try {
			panSpeedInt = Integer.parseInt(panSpeed);
		} catch (NumberFormatException e) {
			errorMessages.append("Pan speed with value ").append(this.panSpeed).append(" is wrong format of number. ");
		}

		try {
			tiltSpeedInt = Integer.parseInt(tiltSpeed);
		} catch (NumberFormatException e) {
			errorMessages.append("Tilt speed with value ").append(this.tiltSpeed).append(" is wrong format of number. ");
		}

		try {
			focusSpeedInt = Integer.parseInt(focusSpeed);
		} catch (NumberFormatException e) {
			errorMessages.append("Focus speed with value ").append(this.focusSpeed).append(" is wrong format of number. ");
		}

		try {
			zoomSpeedInt = Integer.parseInt(zoomSpeed);
		} catch (NumberFormatException e) {
			errorMessages.append("Zoom speed with value ").append(this.zoomSpeed).append(" is wrong format of number. ");
		}
	}

	/**
	 * This method is used for check adapter properties are out of range of not
	 *
	 * @param errorMessages is the error messages of properties when out of range
	 */
	private void checkOutOfRange(StringBuilder errorMessages) {
		if (this.cameraIDInt < 1 || this.cameraIDInt > 7) {
			errorMessages.append("Camera ID with value ").append(this.VISCACameraIDAddress).append(" is out of range. Camera ID must between 1 and 7. ");
		}

		if (this.panSpeedInt < 1 || this.panSpeedInt > 24) {
			errorMessages.append("Pan speed with value ").append(this.panSpeed).append(" is out of range. Pan speed must between 1 and 24. ");
		}

		if (this.tiltSpeedInt < 1 || this.tiltSpeedInt > 24) {
			errorMessages.append("Tilt speed with value ").append(this.tiltSpeed).append(" is out of range. Tilt speed must between 1 and 24. ");
		}

		if (this.zoomSpeedInt < 0 || this.zoomSpeedInt > 7) {
			errorMessages.append("Zoom speed with value ").append(this.zoomSpeed).append(" is out of range. Zoom speed must between 0 and 7. ");
		}

		if (this.focusSpeedInt < 0 || this.focusSpeedInt > 7) {
			errorMessages.append("Focus speed with value ").append(this.focusSpeed).append(" is out of range. Focus speed must between 0 and 7.");
		}
	}

	/**
	 * Populates the provided stats map with all configured general properties.
	 *
	 * @param stats the map to populate with retrieved properties
	 */
	private void populateGeneralProperties(Map<String, String> stats) {
		for (GeneralProperty gp : GeneralProperty.values()) {
			String value = retrieveDeviceInfo(gp.key(), gp.categoryCode(), gp.command());
			stats.put(gp.key(), value);
		}
	}

	/**
	 * This method is used for populate all controlling properties:
	 * <li>Exposure</li>
	 * <li>Focus</li>
	 * <li>Mirror</li>
	 * <li>PanTilt</li>
	 * <li>Picture</li>
	 * <li>Preset</li>
	 * <li>PanTilt Zoom</li>
	 * <li>WhiteBalance</li>
	 * <li>Zoom</li>
	 *
	 * @param stats is the map that store all statistics
	 * @param advancedControllableProperties is the list that store all controllable properties
	 */
	private void populateControlCapabilities(Map<String, String> stats, List<AdvancedControllableProperty> advancedControllableProperties) {
		// Exposure control
		populateExposureControl(stats, advancedControllableProperties);

		// Focus control
		populateFocusControl(stats, advancedControllableProperties);

		//Mirror control
		populateMirrorControl(stats, advancedControllableProperties);

		// WB control
		populateWBControl(stats, advancedControllableProperties);

		// Pan tilt control
		populatePanTiltControl(stats, advancedControllableProperties);

		// Pan tilt zoom control
		populatePanTiltZoomControl(stats, advancedControllableProperties);

		// Picture
		populatePictureControl(stats, advancedControllableProperties);

		// Preset control
		populatePresetControl(stats, advancedControllableProperties);

		// Zoom control
		populateZoomControl(stats, advancedControllableProperties);
	}

	//region Control device

	/**
	 * This method is used to control image process:
	 * <li>RGain</li>
	 * <li>BGain</li>
	 * <li>WBMode</li>
	 * <li>WB One push trigger</li>
	 *
	 * @param value is the value of controllable property
	 * @param splitProperty is the split controllable property
	 */
	private void imageProcessControl(String value, String[] splitProperty) {
		String imageProcessControlName = splitProperty[1].split(LumenVCTR60AConstants.CLOSE_PARENTHESIS, 2)[1];

		// RGain
		if (Objects.equals(imageProcessControlName, Command.RGAIN.getName() + RGainControl.UP.getName())) {
			performControl(PayloadCategory.CAMERA, Command.RGAIN, RGainControl.UP.getCode());
			return;
		} else if (Objects.equals(imageProcessControlName, Command.RGAIN.getName() + RGainControl.DOWN.getName())) {
			performControl(PayloadCategory.CAMERA, Command.RGAIN, RGainControl.DOWN.getCode());
			return;
		}

		// BGain
		if (Objects.equals(imageProcessControlName, Command.BGAIN.getName() + BGainControl.UP.getName())) {
			performControl(PayloadCategory.CAMERA, Command.BGAIN, BGainControl.UP.getCode());
			return;
		} else if (Objects.equals(imageProcessControlName, Command.BGAIN.getName() + BGainControl.DOWN.getName())) {
			performControl(PayloadCategory.CAMERA, Command.BGAIN, BGainControl.DOWN.getCode());
			return;
		}

		Command imageProcessCommand = Command.getByName(imageProcessControlName);

		switch (imageProcessCommand) {
			case WB_MODE:
				performControl(PayloadCategory.CAMERA, Command.WB_MODE, WBMode.getByName(value).getCode());
				break;

			case WB_ONE_PUSH_TRIGGER: {
				performControl(PayloadCategory.CAMERA, Command.WB_ONE_PUSH_TRIGGER);
				break;
			}
			default: {
				throw new IllegalStateException("Unexpected value: " + Arrays.toString(splitProperty));
			}
		}
	}

	/**
	 * This method is used to control exposure:
	 * <li>AE Mode</li>
	 * <li>Exposure Direct</li>
	 * <li>Gain Direct</li>
	 * <li>Gain Limit Direct</li>
	 * <li>Shutter Direct</li>
	 * <li>Iris Direct</li>
	 *
	 * @param value is the value of controllable property
	 * @param exposureCommand is the command get from controllable property name
	 */
	private void exposureControl(String value, Command exposureCommand) {
		switch (exposureCommand) {
			case BACKLIGHT: {
				if (Objects.equals(value, LumenVCTR60AConstants.SWITCH_STATUS_ON)) {
					performControl(PayloadCategory.CAMERA, Command.BACKLIGHT, BacklightStatus.ON.getCode());
				} else if (Objects.equals(value, LumenVCTR60AConstants.SWITCH_STATUS_OFF)) {
					performControl(PayloadCategory.CAMERA, Command.BACKLIGHT, BacklightStatus.OFF.getCode());
				}
				break;
			}
			case EXPOSURE_MODE: {
				performControl(PayloadCategory.CAMERA, Command.EXPOSURE_MODE, ExposureMode.getByName(value).getCode());
				break;
			}
			// All of these DIRECT (Except Gain limit) case are share the same logic of SHUTTER_DIRECT
			case GAIN_LIMIT_DIRECT:
				float gainLimitLevel = Float.parseFloat(value);
				performControl(PayloadCategory.CAMERA, exposureCommand, (byte) gainLimitLevel);
				break;
			case EXP_COMP_DIRECT:
			case GAIN_DIRECT:
			case IRIS_DIRECT:
			case SHUTTER_DIRECT: {
				float directValue = Float.parseFloat(value);
				performControl(PayloadCategory.CAMERA, exposureCommand, convertOneByteNumberToTwoBytesArray((byte) directValue));
				break;
			}
			case AUTO_SLOW_SHUTTER: {
				if (Objects.equals(value, LumenVCTR60AConstants.SWITCH_STATUS_ON)) {
					performControl(PayloadCategory.CAMERA, Command.AUTO_SLOW_SHUTTER, SlowShutterStatus.ON.getCode());
				} else if (Objects.equals(value, LumenVCTR60AConstants.SWITCH_STATUS_OFF)) {
					performControl(PayloadCategory.CAMERA, Command.AUTO_SLOW_SHUTTER, SlowShutterStatus.OFF.getCode());
				}
				break;
			}
			default: {
				throw new IllegalStateException("Unexpected value: " + exposureCommand);
			}
		}
	}

	/**
	 * This method used to perform control of all properties by send, receive command from device
	 *
	 * @param payloadCategory is the category of payload of the command to be sent
	 * @param command is the command to be sent
	 * @param param is the param of command to be sent
	 */
	public void performControl(PayloadCategory payloadCategory, Command command, byte... param) {
		byte[] request;
		byte[] response;

		try {
			int currentSeqNum = ++sequenceNumber;
			request = buildSendPacket(cameraIDInt, currentSeqNum, PayloadType.COMMAND.getCode(), CommandType.COMMAND.getCode(), payloadCategory.getCode(),
					command.getCode(), param);
			response = send(request);

			digestResponse(response, currentSeqNum, CommandType.COMMAND, null);
		} catch (Exception e) {
			this.logger.error("error during command " + command.getName() + " send", e);
			throw new IllegalStateException("Error while sending command " + command.getName());
		}
	}
	//endregion

	//region Populate control properties
	//--------------------------------------------------------------------------------------------------------------------------------

	/**
	 * This method is used for populate all zoom control properties (Tele/Wide)
	 *
	 * @param stats is the map that store all statistics
	 * @param advancedControllableProperties is the list that store all controllable properties
	 */
	private void populateZoomControl(Map<String, String> stats, List<AdvancedControllableProperty> advancedControllableProperties) {
		// Populate zoom tele button
		populateButtonControl(stats, advancedControllableProperties, Command.ZOOM.getName() + LumenVCTR60AConstants.HASH + ZoomControl.TELE.getName(), LumenVCTR60AConstants.PLUS);

		// Populate zoom wide button
		populateButtonControl(stats, advancedControllableProperties, Command.ZOOM.getName() + LumenVCTR60AConstants.HASH + ZoomControl.WIDE.getName(), LumenVCTR60AConstants.MINUS);
	}

	/**
	 * This method is used for populate all focus control properties (Focus near/far, focus mode, focus on push)
	 *
	 * @param stats is the map that store all statistics
	 * @param advancedControllableProperties is the list that store all controllable properties
	 */
	private void populateFocusControl(Map<String, String> stats, List<AdvancedControllableProperty> advancedControllableProperties) {
		FocusMode focusMode = this.getFocusStatus();
		if (focusMode == null) {
			stats.put(Command.FOCUS.getName() + LumenVCTR60AConstants.HASH + Command.FOCUS_MODE.getName(), LumenVCTR60AConstants.NOT_AVAILABLE);
			return;
		}
		AFSensitivity afSensitivityValue = this.getAFSensitivity();
		AFFrame afFrameValue = this.getAFFrame();

		List<String> afFrame = Arrays.asList(
				AFFrame.CENTER.getName(),
				AFFrame.FULL_FRAME.getName(),
				AFFrame.AUTO.getName()
		);

		List<String> afSensitivity = Arrays.asList(
				AFSensitivity.LOW.getName(),
				AFSensitivity.MIDDLE.getName(),
				AFSensitivity.HIGH.getName()
		);

		List<String> focusOptions = Arrays.asList(
				FocusMode.AUTO.getName(),
				FocusMode.MANUAL.getName()
		);

		addAdvancedControlProperties(advancedControllableProperties, stats, createDropdown(Command.FOCUS.getName() + LumenVCTR60AConstants.HASH + Command.FOCUS_MODE.getName(), focusOptions, focusMode.getName()), LumenVCTR60AConstants.NOT_AVAILABLE);

		if (focusMode == FocusMode.AUTO) {
			addAdvancedControlProperties(advancedControllableProperties, stats, createDropdown(Command.FOCUS.getName() + LumenVCTR60AConstants.HASH + Command.AF_FRAME.getName(), afFrame, afFrameValue.getName()), LumenVCTR60AConstants.NOT_AVAILABLE);
			addAdvancedControlProperties(advancedControllableProperties, stats, createDropdown(Command.FOCUS.getName() + LumenVCTR60AConstants.HASH + Command.AF_SENSITIVE.getName(), afSensitivity, afSensitivityValue.getName()) , LumenVCTR60AConstants.NOT_AVAILABLE);
		} else if (focusMode == FocusMode.MANUAL) {
			populateButtonControl(stats, advancedControllableProperties, Command.FOCUS.getName() + LumenVCTR60AConstants.HASH + FocusControl.NEAR.getName(), LumenVCTR60AConstants.PLUS);
			populateButtonControl(stats, advancedControllableProperties, Command.FOCUS.getName() + LumenVCTR60AConstants.HASH + FocusControl.FAR.getName(), LumenVCTR60AConstants.MINUS);
		}
	}

	/**
	 * This method is used for populate mirror control properties
	 *
	 * @param stats is the map that store all statistics
	 * @param advancedControllableProperties is the list that store all controllable properties
	 */
	private void populateMirrorControl(Map<String, String> stats, List<AdvancedControllableProperty> advancedControllableProperties) {
		String flipStatus = getFlipStatus();
		String mirrorStatus = getMirrorStatus();
		populateSwitchControl(stats, advancedControllableProperties, Command.MIRROR_GROUP.getName() + LumenVCTR60AConstants.HASH + Command.FLIP.getName(), flipStatus,
				SlowPanTiltStatus.OFF.getName(), SlowPanTiltStatus.ON.getName());
		populateSwitchControl(stats, advancedControllableProperties, Command.MIRROR_GROUP.getName() + LumenVCTR60AConstants.HASH + Command.MIRROR.getName(), mirrorStatus,
				SlowPanTiltStatus.OFF.getName(), SlowPanTiltStatus.ON.getName());
	}

	/**
	 * Populates the picture settings controls.
	 * If the picture mode is custom, it enables individual sliders for hue, saturation,
	 * brightness, gamma, and sharpness. Always includes 2D and 3D DNR controls.
	 *
	 * @param stats map containing the current device status values
	 * @param advancedControllableProperties list of control elements to populate
	 */
	private void populatePictureControl(Map<String, String> stats, List<AdvancedControllableProperty> advancedControllableProperties){
		PictureMode pictureMode = this.getPictureMode();
		PictureDNROptions number2DNROptions = this.getDNR(Command.TWO_DNR);
		PictureDNROptions number3DNROptions = this.getDNR(Command.THREE_DNR);

		List<String> dnrOptions = Arrays.stream(PictureDNROptions.values())
				.map(PictureDNROptions::getName)
				.collect(Collectors.toList());

		List<String> pictureOptions = Arrays.asList(
				PictureMode.DEFAULT.getName(),
				PictureMode.CUSTOM.getName());

		addAdvancedControlProperties(advancedControllableProperties,stats, createDropdown(Command.PICTURE.getName() + LumenVCTR60AConstants.HASH + Command.IMAGE_MODE.getName(), pictureOptions, pictureMode.getName()), LumenVCTR60AConstants.NOT_AVAILABLE);
		if (pictureMode == PictureMode.CUSTOM) {
			String hueLevel = this.getPictureCustomValue(Command.HUE);
			String saturation = this.getPictureCustomValue(Command.SATURATION);
			String brightness = this.getPictureCustomValue(Command.BRIGHTNESS);
			String gamma = this.getPictureCustomValue(Command.GAMMA);
			String sharpness = this.getPictureCustomValue(Command.SHARPNESS);

			handlePictureSlider(Command.HUE, Command.HUE_CURRENT, hueLevel, LumenVCTR60AConstants.LABEL_START_HUE_LEVEL, LumenVCTR60AConstants.LABEL_END_HUE_LEVEL, LumenVCTR60AConstants.RANGE_START_HUE_LEVEL, LumenVCTR60AConstants.RANGE_END_HUE_LEVEL, stats, advancedControllableProperties);
			handlePictureSlider(Command.SATURATION, Command.SATURATION_CURRENT, saturation, LumenVCTR60AConstants.LABEL_START_HUE_LEVEL, LumenVCTR60AConstants.LABEL_END_HUE_LEVEL, LumenVCTR60AConstants.RANGE_START_HUE_LEVEL, LumenVCTR60AConstants.RANGE_END_HUE_LEVEL, stats, advancedControllableProperties);
			handlePictureSlider(Command.BRIGHTNESS, Command.BRIGHTNESS_CURRENT, brightness, LumenVCTR60AConstants.LABEL_START_HUE_LEVEL, LumenVCTR60AConstants.LABEL_END_HUE_LEVEL, LumenVCTR60AConstants.RANGE_START_HUE_LEVEL, LumenVCTR60AConstants.RANGE_END_HUE_LEVEL, stats, advancedControllableProperties);
			handlePictureSlider(Command.GAMMA, Command.GAMMA_CURRENT, gamma, "0", "4", 0F, 4F, stats, advancedControllableProperties);
			handlePictureSlider(Command.SHARPNESS, Command.SHARPNESS_CURRENT, sharpness, "0", "11", 0F, 11F, stats, advancedControllableProperties);
		}
		addAdvancedControlProperties(advancedControllableProperties, stats, createDropdown(Command.PICTURE.getName() + LumenVCTR60AConstants.HASH + Command.TWO_DNR.getName(), dnrOptions, number2DNROptions.getName()), LumenVCTR60AConstants.NOT_AVAILABLE);
		addAdvancedControlProperties(advancedControllableProperties, stats, createDropdown(Command.PICTURE.getName() + LumenVCTR60AConstants.HASH + Command.THREE_DNR.getName(), dnrOptions, number3DNROptions.getName()), LumenVCTR60AConstants.NOT_AVAILABLE);
	}

	/**
	 * Handles the creation of a slider control for a specific picture setting.
	 * If the provided value is not available, it sets the corresponding stat as NOT_AVAILABLE.
	 * Otherwise, it adds a populated slider control to the list.
	 *
	 * @param command the command representing the picture setting (e.g. HUE, SATURATION)
	 * @param currentCommand the command used to fetch the current value
	 * @param value the current setting value as a string
	 * @param labelStart label for the slider's minimum value
	 * @param labelEnd label for the slider's maximum value
	 * @param rangeStart the minimum range value (float)
	 * @param rangeEnd the maximum range value (float)
	 * @param stats the map containing current device status values
	 * @param props the list of advanced controllable properties to populate
	 */
	private void handlePictureSlider(Command command, Command currentCommand, String value, String labelStart, String labelEnd, float rangeStart, float rangeEnd,
			Map<String, String> stats, List<AdvancedControllableProperty> props) {
		String key = Command.PICTURE.getName() + LumenVCTR60AConstants.HASH + command.getName();
		if (Objects.equals(value, LumenVCTR60AConstants.NONE_VALUE)) {
			stats.put(key, LumenVCTR60AConstants.NOT_AVAILABLE);
		} else {
			populateSliderControl(stats, props, key, Command.PICTURE.getName() + LumenVCTR60AConstants.HASH + currentCommand.getName(),
					value, labelStart, labelEnd, rangeStart, rangeEnd, Float.parseFloat(value));
		}
	}

	/**
	 * This method is used for populate all exposure control properties:
	 * Exposure Full Auto mode:
	 * <li>AntiFlicker control</li>
	 * <li>ExposureCompLevel control</li>
	 * <li>GainLimit control</li>
	 * <li>WDR control</li>
	 * <li>Mode control</li>
	 *
	 * Exposure Shutter Priority mode:
	 * <li>ExposureCompLevel control</li>
	 * <li>GainLimit control</li>
	 * <li>ShutterSpeed control</li>
	 * <li>WDR control</li>
	 * <li>Mode control</li>
	 *
	 * Exposure Iris Priority mode:
	 * <li>ExposureCompLevel control</li>
	 * <li>GainLimit control</li>
	 * <li>WDR control</li>
	 * <li>IrisLevel control</li>
	 * <li>Mode control</li>
	 *
	 * Manual:
	 * <li>Shutter control</li>
	 * <li>GainLevel control</li>
	 * <li>IrisLevel control</li>
	 * <li>WDR control</li>
	 * <li>ShutterSpeed control</li>
	 * <li>Mode control</li>
	 *
	 * White Balance:
	 * <li>ExposureCompLevel control</li>
	 * <li>GainLimit control</li>
	 * <li>Mode control</li>
	 * <li>WDR control</li>
	 *
	 * @param stats is the map that store all statistics
	 * @param advancedControllableProperties is the list that store all controllable properties
	 */
	private void populateExposureControl(Map<String, String> stats, List<AdvancedControllableProperty> advancedControllableProperties) {
		ExposureMode exposureMode = this.getExposureMode();
		AntiFlicker antiFlicker = this.getAntiFlicker();
		WDROptions wdrOptions = this.getWDROptions();

		List<String> WDRList = Arrays.asList(
				WDROptions.OFF.getName(),
				WDROptions.WDR_OPTIONS_1.getName(),
				WDROptions.WDR_OPTIONS_2.getName(),
				WDROptions.WDR_OPTIONS_3.getName(),
				WDROptions.WDR_OPTIONS_4.getName(),
				WDROptions.WDR_OPTIONS_5.getName()
		);

		List<String> aeModeList = Arrays.asList(
				ExposureMode.FULL_AUTO.getName(),
				ExposureMode.SHUTTER_PRIORITY.getName(),
				ExposureMode.IRIS_PRIORITY.getName(),
				ExposureMode.MANUAL.getName(),
				ExposureMode.WHITE_BOARD.getName()
		);

		List<String> gainLimitOptions = IntStream.rangeClosed(8, 30)
				.filter(n -> n % 2 == 0)
				.mapToObj(Integer::toString)
				.collect(Collectors.toList());
		List<String> gainLevelOptions = IntStream.rangeClosed(0, 30)
				.filter(n -> n % 2 == 0)
				.mapToObj(Integer::toString)
				.collect(Collectors.toList());

		addDropdownControl(Command.EXPOSURE, Command.EXPOSURE_MODE, aeModeList, exposureMode.getName(), stats, advancedControllableProperties);

		switch (exposureMode) {
			case FULL_AUTO: {
				List<String> antiFlickList = Arrays.asList(
						AntiFlicker.OFF.getName(),
						AntiFlicker.ANTI_FLICKER_50HZ.getName(),
						AntiFlicker.ANTI_FLICKER_60HZ.getName()
				);
				addDropdownOrNA(Command.ANTI_FLICK, antiFlicker != null ? antiFlicker.getName() : null, antiFlickList, stats, advancedControllableProperties);
				addDropdownOrNA(Command.GAIN_LIMIT_DIRECT, this.getGainLimitLevel(), gainLimitOptions, stats, advancedControllableProperties);
				addSliderOrNA(Command.EXP_COMP_DIRECT, Command.EXP_COMP_CURRENT, this.getExposureValue(), stats, advancedControllableProperties);
				break;
			}
			case SHUTTER_PRIORITY: {
				addSliderOrNA(Command.EXP_COMP_DIRECT, Command.EXP_COMP_CURRENT, this.getExposureValue(), stats, advancedControllableProperties);
				addDropdownOrNA(Command.GAIN_LIMIT_DIRECT, this.getGainLimitLevel(), gainLimitOptions, stats, advancedControllableProperties);
				addDropdownOrNA(Command.SHUTTER_DIRECT, this.getShutterSpeed(), LumenVCTR60AConstants.SHUTTER_VALUES, stats, advancedControllableProperties);
				break;
			}
			case IRIS_PRIORITY: {
				addSliderOrNA(Command.EXP_COMP_DIRECT, Command.EXP_COMP_CURRENT, this.getExposureValue(), stats, advancedControllableProperties);
				addDropdownOrNA(Command.GAIN_LIMIT_DIRECT, this.getGainLimitLevel(), gainLimitOptions, stats, advancedControllableProperties);
				addDropdownOrNA(Command.IRIS_DIRECT, this.getIrisLevel(), LumenVCTR60AConstants.IRIS_LEVELS, stats, advancedControllableProperties);
				break;
			}
			case MANUAL:
				addDropdownOrNA(Command.SHUTTER_DIRECT, this.getShutterSpeed(), LumenVCTR60AConstants.SHUTTER_VALUES, stats, advancedControllableProperties);
				addDropdownOrNA(Command.GAIN_DIRECT, this.getGainLevel(), gainLevelOptions, stats, advancedControllableProperties);
				addDropdownOrNA(Command.IRIS_DIRECT, this.getIrisLevel(), LumenVCTR60AConstants.IRIS_LEVELS, stats, advancedControllableProperties);
				break;
			case WHITE_BOARD:{
				addDropdownOrNA(Command.GAIN_LIMIT_DIRECT, this.getGainLimitLevel(), gainLimitOptions, stats, advancedControllableProperties);
				addSliderOrNA(Command.EXP_COMP_DIRECT, Command.EXP_COMP_CURRENT, this.getExposureValue(), stats, advancedControllableProperties);
				break;
			}
			default:
				throw new IllegalStateException("Unexpected exposure mode: " + exposureMode);
		}
		addDropdownControl(Command.EXPOSURE, Command.WDR_OPTION, WDRList, wdrOptions.getName(), stats, advancedControllableProperties);
	}

	/**
	 * Adds a dropdown control to the stats and controllable properties.
	 *
	 * @param group command group
	 * @param command specific command key
	 * @param options list of options for dropdown
	 * @param value current selected value
	 * @param stats status map to update
	 * @param props control properties list to update
	 */
	private void addDropdownControl(Command group, Command command, List<String> options, String value,
			Map<String, String> stats, List<AdvancedControllableProperty> props) {
		addAdvancedControlProperties(props, stats,
				createDropdown(group.getName() + LumenVCTR60AConstants.HASH + command.getName(), options, value),
				LumenVCTR60AConstants.NOT_AVAILABLE);
	}

	/**
	 * Adds a dropdown control or marks it as not available if value is null or NONE.
	 *
	 * @param command specific command key
	 * @param value current selected value
	 * @param options list of dropdown options
	 * @param stats status map to update
	 * @param props control properties list to update
	 */
	private void addDropdownOrNA(Command command, String value, List<String> options,
			Map<String, String> stats, List<AdvancedControllableProperty> props) {
		String key = Command.EXPOSURE.getName() + LumenVCTR60AConstants.HASH + command.getName();
		if (value == null || LumenVCTR60AConstants.NONE_VALUE.equals(value)) {
			stats.put(key, LumenVCTR60AConstants.NOT_AVAILABLE);
		} else {
			addDropdownControl(Command.EXPOSURE, command, options, value, stats, props);
		}
	}

	/**
	 * Adds a slider control or marks it as not available if value is null or NONE.
	 *
	 * @param command control command key
	 * @param currentCommand command used to retrieve current value
	 * @param value current slider value as string
	 * @param stats status map to update
	 * @param props control properties list to update
	 */
	private void addSliderOrNA(Command command, Command currentCommand, String value,
			Map<String, String> stats, List<AdvancedControllableProperty> props) {
		String key = Command.EXPOSURE.getName() + LumenVCTR60AConstants.HASH + command.getName();
		if (value == null || LumenVCTR60AConstants.NONE_VALUE.equals(value)) {
			stats.put(key, LumenVCTR60AConstants.NOT_AVAILABLE);
		} else {
			populateSliderControl(stats, props,
					Command.EXPOSURE.getName() + LumenVCTR60AConstants.HASH + command.getName(),
					Command.EXPOSURE.getName() + LumenVCTR60AConstants.HASH + currentCommand.getName(),
					value, "-7", "7", -7F, 7F, Float.parseFloat(value));
		}
	}

	/**
	 * This method is used for populate all WB control properties:
	 * WB Mode (Auto, Indoor, Outdoor, One push wb, manual)
	 * Manual (RGain, BGain)
	 * One push wb (One push trigger)
	 *
	 * @param stats is the map that store all statistics
	 * @param advancedControllableProperties is the list that store all controllable properties
	 */
	private void populateWBControl(Map<String, String> stats, List<AdvancedControllableProperty> advancedControllableProperties) {
		List<String> wbModeList = Arrays.asList(
				WBMode.AUTO.getName(),
				WBMode.INDOOR.getName(),
				WBMode.OUTDOOR.getName(),
				WBMode.ONE_PUSH_WB.getName(),
				WBMode.ATW.getName(),
				WBMode.MANUAL.getName(),
				WBMode.SODIUM_LAMP.getName()
		);

		String wbMode = this.getWBMode();

		addAdvancedControlProperties(advancedControllableProperties, stats, createDropdown(Command.WHITE_BALANCE.getName() + LumenVCTR60AConstants.HASH + Command.WB_MODE.getName(), wbModeList, wbMode), LumenVCTR60AConstants.NOT_AVAILABLE);

		if (Objects.equals(WBMode.MANUAL.getName(), wbMode)) {
			String rGainValue = this.getRGain();
			String bGainValue = this.getBGain();

			if (Objects.equals(bGainValue, LumenVCTR60AConstants.NONE_VALUE)) {
				stats.put(Command.WHITE_BALANCE.getName() + LumenVCTR60AConstants.HASH + Command.BGAIN.getName(), LumenVCTR60AConstants.NOT_AVAILABLE);
			} else {
				populateSliderControl(stats, advancedControllableProperties, Command.WHITE_BALANCE.getName() + LumenVCTR60AConstants.HASH + Command.BGAIN.getName(),
						Command.WHITE_BALANCE.getName() + LumenVCTR60AConstants.HASH + Command.BGAIN_INQ.getName(), bGainValue, "0", "127", 0F,
						127F, Float.parseFloat(bGainValue));
			}

			if (Objects.equals(rGainValue, LumenVCTR60AConstants.NONE_VALUE)) {
				stats.put(Command.WHITE_BALANCE.getName() + LumenVCTR60AConstants.HASH + Command.RGAIN.getName(), LumenVCTR60AConstants.NOT_AVAILABLE);
			} else {
				populateSliderControl(stats, advancedControllableProperties, Command.WHITE_BALANCE.getName() + LumenVCTR60AConstants.HASH + Command.RGAIN.getName(),
						Command.WHITE_BALANCE.getName() + LumenVCTR60AConstants.HASH + Command.RGAIN_INQ.getName(), rGainValue, "0", "127", 0F,
						127F, Float.parseFloat(rGainValue));
			}

		} else if (Objects.equals(WBMode.ONE_PUSH_WB.getName(), wbMode)) {
			populateButtonControl(stats, advancedControllableProperties, Command.WHITE_BALANCE.getName() + LumenVCTR60AConstants.HASH + Command.WB_ONE_PUSH_TRIGGER.getName(),
					Command.WB_ONE_PUSH_TRIGGER.getName());
		}
	}

	/**
	 * This method is used for populate all pan tilt control properties:
	 * <li>Pan tilt drive (up/down/left/right/up left/up right/down left/down right</li>
	 * <li>Pan tilt home</li>
	 * <li>Slow pan tilt mode</li>
	 *
	 * @param stats is the map that store all statistics
	 * @param advancedControllableProperties is the list that store all controllable properties
	 */
	private void populatePanTiltControl(Map<String, String> stats, List<AdvancedControllableProperty> advancedControllableProperties) {
		// Populate pan tilt drive home button
		populateButtonControl(stats, advancedControllableProperties, Command.PAN_TILT_DRIVE.getName() + LumenVCTR60AConstants.HASH + Index.ONE.getName() + Command.PAN_TILT_HOME.getName(), Command.PAN_TILT_HOME.getName());
		// Populate pan tilt drive up button
		populateButtonControl(stats, advancedControllableProperties, Command.PAN_TILT_DRIVE.getName() + LumenVCTR60AConstants.HASH + Index.TWO.getName() + PanTiltDrive.UP.getName(), PanTiltDrive.UP.getName());
		// Populate pan tilt drive down button
		populateButtonControl(stats, advancedControllableProperties, Command.PAN_TILT_DRIVE.getName() + LumenVCTR60AConstants.HASH + Index.THREE.getName() + PanTiltDrive.DOWN.getName(), PanTiltDrive.DOWN.getName());
		// Populate pan tilt drive left button
		populateButtonControl(stats, advancedControllableProperties, Command.PAN_TILT_DRIVE.getName() + LumenVCTR60AConstants.HASH + Index.FOUR.getName() + PanTiltDrive.LEFT.getName(), PanTiltDrive.LEFT.getName());
		// Populate pan tilt drive right button
		populateButtonControl(stats, advancedControllableProperties, Command.PAN_TILT_DRIVE.getName() + LumenVCTR60AConstants.HASH + Index.FIVE.getName() + PanTiltDrive.RIGHT.getName(), PanTiltDrive.RIGHT.getName());
		// Populate pan tilt drive up left button
		populateButtonControl(stats, advancedControllableProperties, Command.PAN_TILT_DRIVE.getName() + LumenVCTR60AConstants.HASH + Index.SIX.getName() + PanTiltDrive.UP_LEFT.getName(), PanTiltDrive.UP_LEFT.getName());
		// Populate pan tilt drive up right button
		populateButtonControl(stats, advancedControllableProperties, Command.PAN_TILT_DRIVE.getName() + LumenVCTR60AConstants.HASH + Index.SEVEN.getName() + PanTiltDrive.UP_RIGHT.getName(), PanTiltDrive.UP_RIGHT.getName());
		// Populate pan tilt drive down left button
		populateButtonControl(stats, advancedControllableProperties, Command.PAN_TILT_DRIVE.getName() + LumenVCTR60AConstants.HASH + Index.EIGHT.getName() + PanTiltDrive.DOWN_LEFT.getName(), PanTiltDrive.DOWN_LEFT.getName());
		// Populate pan tilt drive down right button
		populateButtonControl(stats, advancedControllableProperties, Command.PAN_TILT_DRIVE.getName() + LumenVCTR60AConstants.HASH + Index.NINE.getName() + PanTiltDrive.DOWN_RIGHT.getName(), PanTiltDrive.DOWN_RIGHT.getName());
	}

	/**
	 * This method is used for populate all preset control properties (preset set and recall)
	 *
	 * @param stats is the map that store all statistics
	 * @param advancedControllableProperties is the list that store all controllable properties
	 */
	private void populatePresetControl(Map<String, String> stats, List<AdvancedControllableProperty> advancedControllableProperties) {
		List<String> presetList = new ArrayList<>();
		presetList.add(LumenVCTR60AConstants.DEFAULT_PRESET);

		for (int i = 0; i <= 9; ++i) {
			presetList.add(String.valueOf(i));
		}
		String presetValue = currentPreset == -1 ? LumenVCTR60AConstants.DEFAULT_PRESET : String.valueOf(currentPreset);

		addAdvancedControlProperties(advancedControllableProperties, stats, createDropdown(Command.PRESET.getName() + LumenVCTR60AConstants.HASH + PresetControl.PRESET_VALUE.getName(), presetList, presetValue), LumenVCTR60AConstants.NOT_AVAILABLE);
}

	/**
	 * Populates PTZ-related controls including digital zoom, pan/tilt limit, PTZ speed compensation,
	 * motionless preset, zoom limit dropdown, preset speed, and initial position.
	 *
	 * @param stats the statistics-to-value map to populate
	 * @param advancedControllableProperties the list to which new advanced control properties will be appended
	 */
	private void populatePanTiltZoomControl(Map<String, String> stats, List<AdvancedControllableProperty> advancedControllableProperties) {
		List<String> dZoomList = IntStream.rangeClosed(1, 16)
				.mapToObj(i -> "x" + i)
				.collect(Collectors.toList());

		List<String> presetSpeedList = Arrays.asList(
				PresetSpeed.ONE.getName(),
				PresetSpeed.TWO.getName(),
				PresetSpeed.THREE.getName(),
				PresetSpeed.FOUR.getName(),
				PresetSpeed.FIVE.getName()
		);

		List<String> initialPositionList = Arrays.asList(
				InitialPosition.LAST_MEM.getName(),
				InitialPosition.FIRST_PRESET.getName()
		);

		String dZoomValue = this.getDZoomValue();
		String ptzSpeedCompStatus = this.getPanTiltStatus();
		String motionlessPreset = this.getMotionlessPreset();
		InitialPosition initialPosition = this.getInitialPosition();
		PresetSpeed presetSpeedValue = this.getPresetSpeed();

		populateSwitchControl(stats, advancedControllableProperties, Command.PTZ.getName() + LumenVCTR60AConstants.HASH + Command.PTZ_SPEED_COMP.getName(), ptzSpeedCompStatus,
				SlowPanTiltStatus.OFF.getName(), SlowPanTiltStatus.ON.getName());

		populateSwitchControl(stats, advancedControllableProperties, Command.PTZ.getName() + LumenVCTR60AConstants.HASH + Command.MOTIONLESS_PRESET.getName(), motionlessPreset,
				SlowPanTiltStatus.OFF.getName(), SlowPanTiltStatus.ON.getName());

		addAdvancedControlProperties(advancedControllableProperties, stats, createDropdown(Command.PTZ.getName() + LumenVCTR60AConstants.HASH + Command.D_ZOOM_LIMIT.getName(), dZoomList, dZoomValue), LumenVCTR60AConstants.NOT_AVAILABLE);
		addAdvancedControlProperties(advancedControllableProperties, stats, createDropdown(Command.PTZ.getName() + LumenVCTR60AConstants.HASH + Command.PRESET_SPEED.getName(), presetSpeedList, presetSpeedValue.getName()), LumenVCTR60AConstants.NOT_AVAILABLE);
		addAdvancedControlProperties(advancedControllableProperties, stats, createDropdown(Command.PTZ.getName() + LumenVCTR60AConstants.HASH + Command.INITIAL_POSITION.getName(), initialPositionList, initialPosition.getName()), LumenVCTR60AConstants.NOT_AVAILABLE);
	}

	/**
	 * This method is used for populate slider control
	 *
	 * @param stats is the map that store all statistics
	 * @param advancedControllableProperties is the list that store all controllable properties
	 * @param propertyName is the property name of slider
	 * @param currentPropertyName is the label for current value of property
	 * @param propertyValue is the current value of property
	 * @param labelStart is the label start of slider
	 * @param labelEnd is the label end of slider
	 * @param rangeStart is the range start of slider
	 * @param rangeEnd is the range end of slider
	 * @param initialValue is the initial value of slider
	 */
	private void populateSliderControl(Map<String, String> stats, List<AdvancedControllableProperty> advancedControllableProperties, String propertyName,
			String currentPropertyName, String propertyValue, String labelStart, String labelEnd, float rangeStart, float rangeEnd, float initialValue) {

		if (Objects.equals(propertyValue, LumenVCTR60AConstants.NONE_VALUE)) {
			stats.put(propertyName, LumenVCTR60AConstants.NONE_VALUE);
			stats.put(currentPropertyName, LumenVCTR60AConstants.NONE_VALUE);
			return;
		}

		stats.put(propertyName, String.valueOf(initialValue));
		stats.put(currentPropertyName, propertyValue);
		advancedControllableProperties.add(createSlider(propertyName, labelStart, labelEnd, rangeStart, rangeEnd, initialValue));
	}

	/**
	 * This method is used for populate switch control
	 *
	 * @param stats is the map that store all statistics
	 * @param advancedControllableProperties is the list that store all controllable properties
	 * @param propertyName is the property name of switch control
	 * @param currentStatus is the current status of switch control
	 * @param labelOff is the label off of switch control
	 * @param labelOn is the label on of switch control
	 */
	private void populateSwitchControl(Map<String, String> stats, List<AdvancedControllableProperty> advancedControllableProperties, String propertyName, String currentStatus,
			String labelOff, String labelOn) {
		if (Objects.equals(currentStatus, LumenVCTR60AConstants.NONE_VALUE)) {
			stats.put(propertyName, LumenVCTR60AConstants.NONE_VALUE);
			return;
		}

		if (Objects.equals(currentStatus, labelOn)) {
			stats.put(propertyName, String.valueOf(1));
			advancedControllableProperties.add(createSwitch(propertyName, 1, labelOff, labelOn));
		} else if (Objects.equals(currentStatus, labelOff)) {
			stats.put(propertyName, String.valueOf(0));
			advancedControllableProperties.add(createSwitch(propertyName, 0, labelOff, labelOn));
		}
	}

	/**
	 * This method is used for populate button control
	 *
	 * @param stats is the map that store all statistics
	 * @param advancedControllableProperties is the list that store all controllable properties
	 * @param propertyName is the property name of button control
	 * @param buttonLabel is the label of button control
	 */
	private void populateButtonControl(Map<String, String> stats, List<AdvancedControllableProperty> advancedControllableProperties, String propertyName, String buttonLabel) {
		stats.put(propertyName, "");
		advancedControllableProperties.add(createButton(propertyName, buttonLabel));
	}
	//--------------------------------------------------------------------------------------------------------------------------------
	//endregion

	//region Get current value from device
	//--------------------------------------------------------------------------------------------------------------------------------

	/**
	 * This method is used to get the monitoring capabilities
	 *
	 * @return String This returns the monitoring capabilities of device
	 */
	private String retrieveDeviceInfo(String key, byte category, Command expectedCommand) {
		try {
			int seq = nextSequence();
			byte[] packet = buildSendPacket(cameraIDInt, seq, PayloadType.INQUIRY.getCode(), CommandType.INQUIRY.getCode(), category, expectedCommand.getCode());
			byte[] resp = send(packet);
			String result = (String) digestResponse(resp, seq, CommandType.INQUIRY, expectedCommand);
			return result != null ? result : LumenVCTR60AConstants.NONE_VALUE;
		} catch (Exception e) {
			logger.error("Error getting " + key, e);
			return LumenVCTR60AConstants.NONE_VALUE;
		}
	}

	/**
	 * This method is used to get the current display focus status
	 *
	 * @return String This returns the focus status
	 */
	private FocusMode getFocusStatus() {
		try {
			int currentSeqNum = ++sequenceNumber;
			byte[] response = send(
					buildSendPacket(cameraIDInt, currentSeqNum, PayloadType.INQUIRY.getCode(), CommandType.INQUIRY.getCode(), PayloadCategory.CAMERA.getCode(), Command.FOCUS_MODE.getCode()));

			FocusMode mode = (FocusMode) digestResponse(response, currentSeqNum, CommandType.INQUIRY, Command.FOCUS_MODE);

			if (mode == null) {
				return FocusMode.AUTO;
			} else {
				return mode;
			}
		} catch (Exception e) {
			this.logger.error("error during get focus mode", e);
		}
		return FocusMode.AUTO;
	}

	/**
	 * This method is used to get the AF sensitivity
	 *
	 * @return String This returns the AF sensitivity
	 */
	private AFSensitivity getAFSensitivity() {
		try {
			int currentSeqNum = ++sequenceNumber;
			byte[] response = send(
					buildSendPacket(cameraIDInt, currentSeqNum, PayloadType.INQUIRY.getCode(), CommandType.INQUIRY.getCode(), PayloadCategory.CAMERA.getCode(), Command.AF_SENSITIVE.getCode()));

			AFSensitivity mode = (AFSensitivity) digestResponse(response, currentSeqNum, CommandType.INQUIRY, Command.AF_SENSITIVE);

			if (mode == null) {
				return AFSensitivity.LOW;
			} else {
				return mode;
			}
		} catch (Exception e) {
			this.logger.error("error during get AFSensitivity", e);
		}
		return AFSensitivity.LOW;
	}

	/**
	 * This method is used to get the AF Frame
	 *
	 * @return String This returns the AF Frame
	 */
	private AFFrame getAFFrame() {
		try {
			int currentSeqNum = ++sequenceNumber;
			byte[] response = send(
					buildSendPacket(cameraIDInt, currentSeqNum, PayloadType.INQUIRY.getCode(), CommandType.INQUIRY.getCode(), PayloadCategory.CAMERA.getCode(), Command.AF_FRAME.getCode()));

			AFFrame mode = (AFFrame) digestResponse(response, currentSeqNum, CommandType.INQUIRY, Command.AF_FRAME);

			if (mode == null) {
				return AFFrame.AUTO;
			} else {
				return mode;
			}
		} catch (Exception e) {
			this.logger.error("error during get AFSensitivity", e);
		}
		return AFFrame.AUTO;
	}

	/**
	 * This method is used to get the current display picture mode
	 *
	 * @return String This returns the picture mode
	 */
	private PictureMode getPictureMode() {
		try {
			int currentSeqNum = ++sequenceNumber;
			byte[] response = send(
					buildSendPacket(cameraIDInt, currentSeqNum, PayloadType.INQUIRY.getCode(), CommandType.INQUIRY.getCode(), PayloadCategory.CAMERA.getCode(), Command.IMAGE_MODE.getCode()));
			PictureMode mode = (PictureMode) digestResponse(response, currentSeqNum, CommandType.INQUIRY, Command.IMAGE_MODE);
			if (mode == null) {
				return PictureMode.DEFAULT;
			} else {
				return mode;
			}
		} catch (Exception e) {
			this.logger.error("error during get picture mode", e);
		}
		return PictureMode.DEFAULT;
	}

	/**
	 * This method is used to get the current display picture custom property like:
	 * Hue, Saturation, Brightness, Sharpness, Gamma
	 *
	 * @param expectedCommand: command of Hue, Saturation, Brightness, Sharpness, Gamma
	 * @return String This returns the expectedCommand
	 */
	private String getPictureCustomValue(Command expectedCommand) {
		try {
			int currentSeqNum = ++sequenceNumber;
			byte[] response = send(
					buildSendPacket(cameraIDInt, currentSeqNum, PayloadType.INQUIRY.getCode(), CommandType.INQUIRY.getCode(), PayloadCategory.CAMERA.getCode(), expectedCommand.getCode()));

			return String.valueOf(digestResponse(response, currentSeqNum, CommandType.INQUIRY, expectedCommand));
		} catch (Exception e) {
			this.logger.error("error during get " + expectedCommand.getName() + " " + "custom", e);
			return LumenVCTR60AConstants.NONE_VALUE;
		}
	}

	/**
	 * This method is used to get the digital zoom value
	 *
	 * @return String This returns the digital zoom value
	 */
	private String getDZoomValue(){
		try {
			int currentSeqNum = ++sequenceNumber;
			byte[] response = send(
					buildSendPacket(cameraIDInt, currentSeqNum, PayloadType.INQUIRY.getCode(), CommandType.INQUIRY.getCode(), PayloadCategory.CAMERA.getCode(), Command.D_ZOOM_LIMIT.getCode()));

			return String.valueOf(digestResponse(response, currentSeqNum, CommandType.INQUIRY, Command.D_ZOOM_LIMIT));
		} catch (Exception e) {
			this.logger.error("error during get digital zoom limit", e);
		}
		return LumenVCTR60AConstants.NONE_VALUE;
	}

	/**
	 * This method is used to get the initial position value
	 *
	 * @return String This returns the initial position value
	 */
	private InitialPosition getInitialPosition(){
		try {
			int currentSeqNum = ++sequenceNumber;
			byte[] response = send(
					buildSendPacket(cameraIDInt, currentSeqNum, PayloadType.INQUIRY.getCode(), CommandType.INQUIRY.getCode(), PayloadCategory.CAMERA.getCode(), Command.INITIAL_POSITION.getCode()));
			InitialPosition mode = (InitialPosition) digestResponse(response, currentSeqNum, CommandType.INQUIRY, Command.INITIAL_POSITION);
			if (mode == null) {
				return InitialPosition.LAST_MEM;
			} else {
				return mode;
			}
		} catch (Exception e) {
			this.logger.error("error during initial position options", e);
		}
		return InitialPosition.LAST_MEM;
	}

	/**
	 * This method is used to get the preset speed value
	 *
	 * @return String This returns the preset speed value
	 */
	private PresetSpeed getPresetSpeed(){
		try {
			int currentSeqNum = ++sequenceNumber;
			byte[] response = send(
					buildSendPacket(cameraIDInt, currentSeqNum, PayloadType.INQUIRY.getCode(), CommandType.INQUIRY.getCode(), PayloadCategory.CAMERA.getCode(), Command.PRESET_SPEED.getCode()));
			PresetSpeed mode = (PresetSpeed) digestResponse(response, currentSeqNum, CommandType.INQUIRY, Command.PRESET_SPEED);
			if (mode == null) {
				return PresetSpeed.ONE;
			} else {
				return mode;
			}
		} catch (Exception e) {
			this.logger.error("error during preset speed options", e);
		}
		return PresetSpeed.ONE;
	}

	/**
	 * This method is used to get the DNR value
	 *
	 * @return String This returns the DNR value
	 */
	private PictureDNROptions getDNR(Command expectedCommand) {
		try {
			int currentSeqNum = ++sequenceNumber;
			byte[] response = send(
					buildSendPacket(cameraIDInt, currentSeqNum, PayloadType.INQUIRY.getCode(), CommandType.INQUIRY.getCode(), PayloadCategory.CAMERA.getCode(), expectedCommand.getCode()));
			PictureDNROptions mode = (PictureDNROptions) digestResponse(response, currentSeqNum, CommandType.INQUIRY, expectedCommand);
			if (mode == null) {
				return PictureDNROptions.OFF;
			} else {
				return mode;
			}
		} catch (Exception e) {
			this.logger.error("error during get dnr options", e);
			return PictureDNROptions.OFF;
		}
	}

	/**
	 * This method is used to get the current display current AE mode
	 *
	 * @return String This returns the AE mode
	 */
	private ExposureMode getExposureMode() {
		try {
			int currentSeqNum = ++sequenceNumber;
			byte[] response = send(
					buildSendPacket(cameraIDInt, currentSeqNum, PayloadType.INQUIRY.getCode(), CommandType.INQUIRY.getCode(), PayloadCategory.CAMERA.getCode(), Command.EXPOSURE_MODE.getCode()));

			ExposureMode mode = (ExposureMode) digestResponse(response, currentSeqNum, CommandType.INQUIRY, Command.EXPOSURE_MODE);

			if (mode == null) {
				return ExposureMode.FULL_AUTO;
			} else {
				return mode;
			}
		} catch (Exception e) {
			this.logger.error("error during get exposure mode", e);
		}
		return ExposureMode.FULL_AUTO;
	}

	/**
	 * This method is used to get the anti flicker
	 *
	 * @return String This returns the anti flicker
	 */
	private AntiFlicker getAntiFlicker() {
		try {
			int currentSeqNum = ++sequenceNumber;
			byte[] response = send(
					buildSendPacket(cameraIDInt, currentSeqNum, PayloadType.INQUIRY.getCode(), CommandType.INQUIRY.getCode(), PayloadCategory.CAMERA.getCode(), Command.ANTI_FLICK.getCode()));

			AntiFlicker mode = (AntiFlicker) digestResponse(response, currentSeqNum, CommandType.INQUIRY, Command.ANTI_FLICK);

			if (mode == null) {
				return AntiFlicker.OFF;
			} else {
				return mode;
			}
		} catch (Exception e) {
			this.logger.error("error during get anti flicker", e);
		}
		return AntiFlicker.OFF;
	}

	/**
	 * This method is used to get the WDR
	 *
	 * @return String This returns the WDR
	 */
	private WDROptions getWDROptions() {
		try {
			int currentSeqNum = ++sequenceNumber;
			byte[] response = send(
					buildSendPacket(cameraIDInt, currentSeqNum, PayloadType.INQUIRY.getCode(), CommandType.INQUIRY.getCode(), PayloadCategory.CAMERA.getCode(), Command.WDR_OPTION.getCode()));

			WDROptions mode = (WDROptions) digestResponse(response, currentSeqNum, CommandType.INQUIRY, Command.WDR_OPTION);
			if (mode == null) {
				return WDROptions.OFF;
			} else {
				return mode;
			}
		} catch (Exception e) {
			this.logger.error("error during get WDR options", e);
		}
		return WDROptions.OFF;
	}

	/**
	 * This method is used to get the current display current exposure value
	 *
	 * @return String This returns the exposure value
	 */
	private String getExposureValue() {
		try {
			int currentSeqNum = ++sequenceNumber;
			byte[] response = send(
					buildSendPacket(cameraIDInt, currentSeqNum, PayloadType.INQUIRY.getCode(), CommandType.INQUIRY.getCode(), PayloadCategory.CAMERA.getCode(), Command.EXP_COMP_DIRECT.getCode()));
			return String.valueOf((int) digestResponse(response, currentSeqNum, CommandType.INQUIRY, Command.EXP_COMP_DIRECT));
		} catch (Exception e) {
			this.logger.error("error during get exposure value", e);
		}
		return LumenVCTR60AConstants.NONE_VALUE;
	}

	/**
	 * This method is used to get the current display current shutter speed
	 *
	 * @return Entry<Integer, String> This returns the entry for shutter speed
	 * key: int value of slider, value: string value of shutter speed
	 */
	private String getShutterSpeed() {
		try {
			int currentSeqNum = ++sequenceNumber;
			byte[] response = send(
					buildSendPacket(cameraIDInt, currentSeqNum, PayloadType.INQUIRY.getCode(), CommandType.INQUIRY.getCode(), PayloadCategory.CAMERA.getCode(), Command.SHUTTER_DIRECT.getCode()));
			return (String) digestResponse(response, currentSeqNum, CommandType.INQUIRY, Command.SHUTTER_DIRECT);
		} catch (Exception e) {
			this.logger.error("error during get shutter speed", e);
		}
		return LumenVCTR60AConstants.NONE_VALUE;
	}

	/**
	 * This method is used to get the current display current iris level
	 *
	 * @return Entry<Integer, String> This returns the entry for iris level
	 * key: int value of slider, value: string value of iris level
	 */
	private String getIrisLevel() {
		try {
			int currentSeqNum = ++sequenceNumber;
			byte[] response = send(
					buildSendPacket(cameraIDInt, currentSeqNum, PayloadType.INQUIRY.getCode(), CommandType.INQUIRY.getCode(), PayloadCategory.CAMERA.getCode(), Command.IRIS_DIRECT.getCode()));

			Integer index = (Integer) digestResponse(response, currentSeqNum, CommandType.INQUIRY, Command.IRIS_DIRECT);

			String level = (index != null && index >= 0 && index < LumenVCTR60AConstants.IRIS_LEVELS.size())
					? LumenVCTR60AConstants.IRIS_LEVELS.get(index)
					: LumenVCTR60AConstants.NONE_VALUE;
			return level;
		} catch (Exception e) {
			this.logger.error("error during get iris level", e);
		}
		return LumenVCTR60AConstants.NONE_VALUE;
	}

	/**
	 * This method is used to get the current display current gain level
	 *
	 * @return String This returns the gain level
	 */
	private String getGainLevel() {
		try {
			int currentSeqNum = ++sequenceNumber;
			byte[] response = send(
					buildSendPacket(cameraIDInt, currentSeqNum, PayloadType.INQUIRY.getCode(), CommandType.INQUIRY.getCode(), PayloadCategory.CAMERA.getCode(), Command.GAIN_DIRECT.getCode()));

			return String.valueOf(digestResponse(response, currentSeqNum, CommandType.INQUIRY, Command.GAIN_DIRECT));
		} catch (Exception e) {
			this.logger.error("error during get gain level", e);
		}
		return LumenVCTR60AConstants.NONE_VALUE;
	}

	/**
	 * This method is used to get the current display current gain limit level
	 *
	 * @return String This returns the gain limit level
	 */
	private String getGainLimitLevel() {
		try {
			int currentSeqNum = ++sequenceNumber;
			byte[] response = send(
					buildSendPacket(cameraIDInt, currentSeqNum, PayloadType.INQUIRY.getCode(), CommandType.INQUIRY.getCode(), PayloadCategory.CAMERA.getCode(), Command.GAIN_LIMIT_DIRECT.getCode()));
			return String.valueOf((int) digestResponse(response, currentSeqNum, CommandType.INQUIRY, Command.GAIN_LIMIT_DIRECT) );
		} catch (Exception e) {
			this.logger.error("error during get gain limit level", e);
		}
		return LumenVCTR60AConstants.NONE_VALUE;
	}

	/**
	 * This method is used to get the current display current WB mode
	 *
	 * @return String This returns the WB mode
	 */
	private String getWBMode() {
		try {
			int currentSeqNum = ++sequenceNumber;
			byte[] response = send(
					buildSendPacket(cameraIDInt, currentSeqNum, PayloadType.INQUIRY.getCode(), CommandType.INQUIRY.getCode(), PayloadCategory.CAMERA.getCode(), Command.WB_MODE.getCode()));

			WBMode mode = (WBMode) digestResponse(response, currentSeqNum, CommandType.INQUIRY, Command.WB_MODE);

			if (mode == null) {
				return WBMode.MANUAL.getName();
			} else {
				return mode.getName();
			}
		} catch (Exception e) {
			this.logger.error("error during get WB mode", e);
			return WBMode.MANUAL.getName();
		}
	}

	/**
	 * This method is used to get the current display current RGain value
	 *
	 * @return String This returns the RGain value
	 */
	private String getRGain() {
		try {
			int currentSeqNum = ++sequenceNumber;
			byte[] response = send(
					buildSendPacket(cameraIDInt, currentSeqNum, PayloadType.INQUIRY.getCode(), CommandType.INQUIRY.getCode(), PayloadCategory.CAMERA.getCode(), Command.RGAIN_INQ.getCode()));

			return String.valueOf(digestResponse(response, currentSeqNum, CommandType.INQUIRY, Command.RGAIN_INQ));

		} catch (Exception e) {
			this.logger.error("error during get RGain value", e);
		}
		return LumenVCTR60AConstants.NONE_VALUE;
	}

	/**
	 * This method is used to get the current display current BGain value
	 *
	 * @return String This returns the BGain value
	 */
	private String getBGain() {
		try {
			int currentSeqNum = ++sequenceNumber;
			byte[] response = send(
					buildSendPacket(cameraIDInt, currentSeqNum, PayloadType.INQUIRY.getCode(), CommandType.INQUIRY.getCode(), PayloadCategory.CAMERA.getCode(), Command.BGAIN_INQ.getCode()));

			return String.valueOf(digestResponse(response, currentSeqNum, CommandType.INQUIRY, Command.BGAIN_INQ));

		} catch (Exception e) {
			this.logger.error("error during get BGain value", e);
		}
		return LumenVCTR60AConstants.NONE_VALUE;
	}

	/**
	 * This method is used to get the current display current flip status
	 *
	 * @return String This returns the flip status
	 */
	private String getFlipStatus() {
		try {
			int currentSeqNum = ++sequenceNumber;
			byte[] response = send(
					buildSendPacket(cameraIDInt, currentSeqNum, PayloadType.INQUIRY.getCode(), CommandType.INQUIRY.getCode(), PayloadCategory.CAMERA.getCode(), Command.FLIP.getCode()));

			SlowPanTiltStatus status = (SlowPanTiltStatus) digestResponse(response, currentSeqNum, CommandType.INQUIRY, Command.FLIP);

			if (status == null) {
				return SlowPanTiltStatus.OFF.getName();
			} else {
				return status.getName();
			}
		} catch (Exception e) {
			this.logger.error("error during get flip status", e);
		}
		return LumenVCTR60AConstants.NONE_VALUE;
	}

	/**
	 * This method is used to get the current display current Mirror status
	 *
	 * @return String This returns the Mirror status
	 */
	private String getMirrorStatus() {
		try {
			int currentSeqNum = ++sequenceNumber;
			byte[] response = send(
					buildSendPacket(cameraIDInt, currentSeqNum, PayloadType.INQUIRY.getCode(), CommandType.INQUIRY.getCode(), PayloadCategory.CAMERA.getCode(), Command.MIRROR.getCode()));

			SlowPanTiltStatus status = (SlowPanTiltStatus) digestResponse(response, currentSeqNum, CommandType.INQUIRY, Command.MIRROR);

			if (status == null) {
				return SlowPanTiltStatus.OFF.getName();
			} else {
				return status.getName();
			}
		} catch (Exception e) {
			this.logger.error("error during get mirror status", e);
		}
		return LumenVCTR60AConstants.NONE_VALUE;
	}

	/**
	 * This method is used to get the current display current slow pan tilt status
	 *
	 * @return String This returns the slow pan tilt status
	 */
	private String getPanTiltStatus() {
		try {
			int currentSeqNum = ++sequenceNumber;
			byte[] response = send(
					buildSendPacket(cameraIDInt, currentSeqNum, PayloadType.INQUIRY.getCode(), CommandType.INQUIRY.getCode(), PayloadCategory.PAN_TILTER.getCode(), Command.PTZ_SPEED_COMP.getCode()));

			PanTiltSpeedComp status = (PanTiltSpeedComp) digestResponse(response, currentSeqNum, CommandType.INQUIRY, Command.PTZ_SPEED_COMP);

			if (status == null) {
				return PanTiltSpeedComp.OFF.getName();
			} else {
				return status.getName();
			}
		} catch (Exception e) {
			this.logger.error("error during get pan tilt status", e);
		}
		return LumenVCTR60AConstants.NONE_VALUE;
	}

	/**
	 * This method is used to get the current display current motionless preset
	 *
	 * @return String This returns the motionless preset
	 */
	private String getMotionlessPreset() {
		try {
			int currentSeqNum = ++sequenceNumber;
			byte[] response = send(
					buildSendPacket(cameraIDInt, currentSeqNum, PayloadType.INQUIRY.getCode(), CommandType.INQUIRY.getCode(), PayloadCategory.MOTIONLESS.getCode(), Command.MOTIONLESS_PRESET.getCode()));

			SlowShutterStatus status = (SlowShutterStatus) digestResponse(response, currentSeqNum, CommandType.INQUIRY, Command.MOTIONLESS_PRESET);

			if (status == null) {
				return SlowShutterStatus.OFF.getName();
			} else {
				return status.getName();
			}
		} catch (Exception e) {
			this.logger.error("error during get slow auto slow shutter status", e);
		}
		return LumenVCTR60AConstants.NONE_VALUE;
	}

	//--------------------------------------------------------------------------------------------------------------------------------
	//endregion

	/**
	 * {@inheritdoc}
	 * This method is used to send command to device
	 *
	 * @param outputData This is a byte array of command to be sent
	 * @return byte[] This returns the response receive from device
	 */
	@Override
	protected byte[] internalSend(byte[] outputData) throws IOException {
		DatagramPacket request = new DatagramPacket(outputData, outputData.length, this.address, this.port);
		this.write(request);

		// If send command power off -> device return nothing -> no need wait to receive
		if (Objects.equals(outputData[11], Command.POWER.getCode()[0]) && Objects.equals(outputData[12], PowerStatus.OFF.getCode())) {
			System.arraycopy(outputData, 4, LumenVCTR60AConstants.FAKE_COMPLETION, 4, 4); // Copy sequence number
			return LumenVCTR60AConstants.FAKE_COMPLETION;
		}

		return this.read(outputData);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void internalInit() throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("Internal init is called.");
		}
		adapterInitializationTimestamp = System.currentTimeMillis();
		super.internalInit();
	}

	/**
	 * Formats uptime from a string representation "hh:mm:ss" into "X hour(s) Y minute(s)" format.
	 *
	 * @param time the uptime string to format
	 * @return formatted uptime string or "None" if input is invalid
	 */
	private String formatUpTime(String time) {
		int seconds = Integer.parseInt(time);
		if (seconds < 0) {
			return LumenVCTR60AConstants.NONE_VALUE;
		}

		int days = seconds / (24 * 3600);
		seconds %= 24 * 3600;
		int hours = seconds / 3600;
		seconds %= 3600;
		int minutes = seconds / 60;
		seconds %= 60;

		StringBuilder result = new StringBuilder();
		if (days > 0) {
			result.append(days).append(" day(s) ");
		}
		if (hours > 0) {
			result.append(hours).append(" hour(s) ");
		}
		if (minutes > 0) {
			result.append(minutes).append(" minute(s) ");
		}
		if (seconds > 0) {
			result.append(seconds).append(" second(s) ");
		}

		if (result.length() == 0) {
			return "0 second(s)";
		}
		return result.toString().trim();
	}

	private int nextSequence() {
		return seqCounter.updateAndGet(i -> i == Integer.MAX_VALUE ? 0 : i + 1);
	}

	/**
	 * {@inheritdoc}
	 * This method is used to read data from device
	 *
	 * @param command This is a byte array of command to check done reading or not
	 * @return byte[] This returns the response receive from device
	 */
	@Override
	protected byte[] read(byte[] command) throws IOException {
		String responseString;
		boolean hasACK = false;
		byte[] response;

		do {
			response = super.read(command);
			responseString = getHexByteString(response);

			// If this is ACK packet, need to save to the flag to check done reading
			if (responseString.endsWith(getHexByteString(ReplyStatus.ACK.getCode()))) {
				hasACK = true;
			}

		} while (!doneReading(command, responseString, hasACK));

		return response;
	}

	/**
	 * This method is used to check when done reading
	 *
	 * @param command This is a command parameter for method read executed before throw error (if it has ACK)
	 * @param responseString This is a string represented for response to be checked
	 * @param hasACK This is a boolean value to check if it has ACK packet or not (ACK -> ERROR -> COMPLETION, if it has error, need to read COMPLETION packet left)
	 * @return boolean This is a boolean value return if done reading or not
	 */
	public boolean doneReading(byte[] command, String responseString, boolean hasACK) throws CommandFailureException, IOException {
		String commandString = getHexByteString(command);
		Iterator<String> iterator = this.getCommandErrorList().iterator();

		String string;
		do {
			if (!iterator.hasNext()) {
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				outputStream.write(Prefix.PAYLOAD_TYPE.getPrefixCode());
				outputStream.write(PayloadType.INQUIRY.getCode());

				// If type is INQUIRY command, just need to check command error list
				if (commandString.startsWith(getHexByteString(outputStream.toByteArray()))) {
					this.logger.trace("Done reading, found inquiry packet reply from: " + this.getHost() + " port: " + this.getPort());
					return true;
				}

				iterator = this.getCommandSuccessList().iterator();

				do {
					if (!iterator.hasNext()) {
						return false;
					}

					string = iterator.next();
				} while (!responseString.endsWith(string));

				if (this.logger.isTraceEnabled()) {
					this.logger.trace("Done reading, found success string: " + string + " from: " + this.getHost() + " port: " + this.getPort());
				}

				return true;
			}

			string = iterator.next();
		} while (!responseString.endsWith(string));

		if (this.logger.isTraceEnabled()) {
			this.logger.trace("Done reading, found error string: " + string + " from: " + this.getHost() + " port: " + this.getPort());
		}

		// if it has ACK packet first -> Error packet, 1 Completion packet left to receive from Device
		if (hasACK) {
			super.read(command);
		}

		throw new CommandFailureException(this.getHost(), commandString, responseString);
	}

	/**
	 * This method is used to digest the response received from the device
	 *
	 * @param response This is the response to be digested
	 * @param sequenceNum This is the sequence number of send packet
	 * @param commandType This is the type of command to be digested
	 * @param expectedCommand This is the expected command to be digested
	 * @return Object This returns the result digested from the response.
	 */
	public Object digestResponse(byte[] response, int sequenceNum, CommandType commandType, Command expectedCommand) {
		if (response[1] == PayloadType.REPLY.getCode()) {
			byte[] responseSeqNum = Arrays.copyOfRange(response, 4, 8);
			byte[] expectedSeqNum = convertIntToByteArray(sequenceNum);

			if (Arrays.equals(expectedSeqNum, responseSeqNum)) {
				int payloadLength = response[3];
				byte[] reply = Arrays.copyOfRange(response, 8, 8 + payloadLength);
				byte currentValue = reply[2];

				if (commandType == CommandType.COMMAND) {
					if (!Arrays.equals(ReplyStatus.COMPLETION.getCode(), reply)) {
						this.logger.error("error: Unexpected completion packet: " + this.host + " port: " + this.port);
						throw new IllegalStateException("Unexpected completion packet");
					}
				} else if (commandType == CommandType.INQUIRY) {
					switch (expectedCommand) {
						case POWER: {
							Optional<PowerStatus> powerStatus = Arrays.stream(PowerStatus.values())
									.filter(status -> status.getCode() == currentValue)
									.findFirst();

							return powerStatus.orElse(null);
						}
						case FOCUS_MODE: {
							Optional<FocusMode> focusMode = Arrays.stream(FocusMode.values())
									.filter(mode -> mode.getCode() == currentValue)
									.findFirst();

							return focusMode.orElse(null);
						}
						case EXPOSURE_MODE: {
							Optional<ExposureMode> aeMode = Arrays.stream(ExposureMode.values())
									.filter(mode -> mode.getCode() == currentValue)
									.findFirst();
							return aeMode.orElse(null);
						}
						case ANTI_FLICK: {
							Optional<AntiFlicker> antiFlicker = Arrays.stream(AntiFlicker.values())
									.filter(mode -> mode.getCode() == currentValue)
									.findFirst();
							return antiFlicker.orElse(null);
						}
						case IMAGE_MODE: {
							Optional<PictureMode> pictureMode = Arrays.stream(PictureMode.values())
									.filter(mode -> mode.getCode() == currentValue)
									.findFirst();
							return pictureMode.orElse(null);
						}
						case INITIAL_POSITION: {
							Optional<InitialPosition> initialPosition = Arrays.stream(InitialPosition.values())
									.filter(mode -> mode.getCode() == currentValue)
									.findFirst();
							return initialPosition.orElse(null);
						}
						case PRESET_SPEED: {
							Optional<PresetSpeed> presetSpeed = Arrays.stream(PresetSpeed.values()).filter(mode -> mode.getCode() == currentValue)
									.findFirst();
							return presetSpeed.orElse(null);
						}
						case THREE_DNR:
						case TWO_DNR: {
							Optional<PictureDNROptions> pictureDNR = Arrays.stream(PictureDNROptions.values()).filter(mode -> mode.getCode() == currentValue)
									.findFirst();
							return pictureDNR.orElse(null);
						}
						case WDR_OPTION: {
							Optional<WDROptions> wdrValue = Arrays.stream(WDROptions.values())
									.filter(mode -> mode.getCode() == currentValue)
									.findFirst();
							return wdrValue.orElse(null);
						}
						case MOTIONLESS_PRESET:
						case AUTO_SLOW_SHUTTER: {
							Optional<SlowShutterStatus> slowShutterStatus = Arrays.stream(SlowShutterStatus.values())
									.filter(mode -> mode.getCode() == currentValue)
									.findFirst();

							return slowShutterStatus.orElse(null);
						}
						case IRIS_DIRECT: {
							byte irisValue = reply[5];
							int i = Byte.toUnsignedInt(irisValue);
							if (i >= LumenVCTR60AConstants.IRIS_LEVELS.size()) {
								logger.warn("Iris index out of bounds: " + i);
								return LumenVCTR60AConstants.NONE_VALUE;
							}
							return i;
						}
						case EXP_COMP_DIRECT:
							int rawQ = reply[5] & 0x0F;
							if (rawQ > 0x0E) {
								logger.warn("ExpComp q out of range: 0x" + Integer.toHexString(rawQ));
								return LumenVCTR60AConstants.NONE_VALUE;
							}
							return rawQ - 7;
						case SHUTTER_DIRECT:
							int raw = Byte.toUnsignedInt(reply[4]) * 16 + Byte.toUnsignedInt(reply[5]);
							int idx = Math.min(raw, LumenVCTR60AConstants.SHUTTER_VALUES.size() - 1);
							return LumenVCTR60AConstants.SHUTTER_VALUES.get(idx);
						case GAIN_DIRECT:
						case RGAIN_INQ:
						case BGAIN_INQ: {
							return reply[4] * 16 + reply[5];
						}
						case GAIN_LIMIT_DIRECT:
							int p = Byte.toUnsignedInt(reply[2]);
							return 2 * (p - 4) + 8;
						case PRESET: {
							return Byte.toUnsignedInt(reply[2]);
						}
						case BACKLIGHT: {
							Optional<BacklightStatus> backlightStatus = Arrays.stream(BacklightStatus.values())
									.filter(status -> status.getCode() == currentValue)
									.findFirst();

							return backlightStatus.orElse(null);
						}
						case WB_MODE: {
							Optional<WBMode> wbMode = Arrays.stream(WBMode.values())
									.filter(mode -> mode.getCode() == currentValue)
									.findFirst();

							return wbMode.orElse(null);
						}
						case PTZ_SPEED_COMP:
						{
							Optional<PanTiltSpeedComp> panTiltSpeedComp = Arrays.stream(PanTiltSpeedComp.values())
									.filter(status -> status.getCode() == currentValue)
									.findFirst();

							return panTiltSpeedComp.orElse(null);
						}
						case AF_SENSITIVE:
							Optional<AFSensitivity> afSensitivity = Arrays.stream(AFSensitivity.values())
									.filter(status -> status.getCode() == currentValue)
									.findFirst();
							return afSensitivity.orElse(null);
						case AF_FRAME:
							Optional<AFFrame> afFrame = Arrays.stream(AFFrame.values())
									.filter(status -> status.getCode() == currentValue)
									.findFirst();
							return afFrame.orElse(null);
						case TRACKING_LED_STATUS:
						case DHCP:
							Optional<SlowPanTiltStatus> trackingValue = Arrays.stream(SlowPanTiltStatus.values())
									.filter(status -> status.getCode() == currentValue)
									.findFirst();
							return trackingValue.map(SlowPanTiltStatus::getName).orElse(null);
						case PRIVACY_MODE:
						case USB:
						case TALLY_MODE:
							Optional<SlowPanTiltStatus> currentStatus = Arrays.stream(SlowPanTiltStatus.values())
									.filter(status -> status.getCode() == currentValue)
									.findFirst();
							return currentStatus
									.map(status1 -> status1 == SlowPanTiltStatus.ON ? "Enable" : "Disable")
									.orElse("Disable");
						case SLOW_PAN_TILT:
						case MIRROR:
						case FLIP:{
							Optional<SlowPanTiltStatus> slowPanTiltStatus = Arrays.stream(SlowPanTiltStatus.values())
									.filter(status -> status.getCode() == currentValue)
									.findFirst();
							return slowPanTiltStatus.orElse(null);
						}
						case D_ZOOM_LIMIT:
							byte dZoomValue = reply[2];
							int zoomIndex = Byte.toUnsignedInt(dZoomValue);
							return "x" + (zoomIndex + 1);
						case GAMMA:
							return String.valueOf(reply[2] & 0xFF);
						case HUE:
						case SATURATION:
						case BRIGHTNESS:
						case SHARPNESS:
							int length = reply.length;
							int pictureValue = 0;
							for (int i = length - 5; i < length - 1; i++) {
								pictureValue = (pictureValue << 4) | (reply[i] & 0x0F);
							}
							return String.valueOf(pictureValue);
						case FIRMWARE_VERSION:
						case SERIAL_NUMBER:
						case MAC_ADDRESS:
						case CAMERA_ID:
						case HDMI_FORMAT:
						case PIP:
						case DNS:
						case IPV4_NETMASK:
						case GATEWAY:
						case BAUD_RATE:
							int start = 2;
							int end = reply.length;
							while (end > start && (reply[end - 1] == (byte)0x00 || reply[end - 1] == (byte)0xFF)) {
								end--;
							}
							String value = new String(reply, start, end - start, StandardCharsets.US_ASCII);
							boolean isPrintable = value.codePoints().allMatch(c -> c >= 32 && c <= 126);
							boolean isBlank = value.matches("\\s*");
							if(!isPrintable || isBlank){
								return LumenVCTR60AConstants.NOT_AVAILABLE;
							}
							return value;
						default:
							throw new IllegalStateException("Unexpected command: " + expectedCommand);
					}
				}
			} else {
				this.logger.error("error: Unexpected sequence number: " + this.host + " port: " + this.port);
				throw new IllegalStateException("Unexpected sequence number");
			}
		} else {
			this.logger.error("error: Unexpected reply: " + this.host + " port: " + this.port);
			throw new IllegalStateException("Unexpected reply");
		}

		return null;
	}

	/**
	 * check value is null or empty
	 *
	 * @param value input value
	 * @return value after checking
	 */
	private String getDefaultValueForNullData(String value) {
		return StringUtils.isNotNullOrEmpty(value) && !LumenVCTR60AConstants.NULL.equalsIgnoreCase(value) ? uppercaseFirstCharacter(value) : LumenVCTR60AConstants.NONE_VALUE;
	}

	/**
	 * capitalize the first character of the string
	 *
	 * @param input input string
	 * @return string after fix
	 */
	private String uppercaseFirstCharacter(String input) {
		return Character.toUpperCase(input.charAt(0)) + input.substring(1);
	}

	//region Create controllable property
	//--------------------------------------------------------------------------------------------------------------------------------

	/**
	 * Instantiate Text controllable property
	 *
	 * @param name name of the property
	 * @param label default button label
	 * @return instance of AdvancedControllableProperty with AdvancedControllableProperty.Button as type
	 */
	private AdvancedControllableProperty createButton(String name, String label) {
		AdvancedControllableProperty.Button button = new AdvancedControllableProperty.Button();
		button.setLabel(label);
		button.setLabelPressed("Running...");
		button.setGracePeriod(100L);

		return new AdvancedControllableProperty(name, new Date(), button, "");
	}

	/**
	 * Create a switch controllable property
	 *
	 * @param name name of the switch
	 * @param status initial switch state (0|1)
	 * @return AdvancedControllableProperty button instance
	 */
	private AdvancedControllableProperty createSwitch(String name, int status, String labelOff, String labelOn) {
		AdvancedControllableProperty.Switch toggle = new AdvancedControllableProperty.Switch();
		toggle.setLabelOff(labelOff);
		toggle.setLabelOn(labelOn);

		return new AdvancedControllableProperty(name, new Date(), toggle, status);
	}

	/***
	 * Create AdvancedControllableProperty slider instance
	 *
	 * @param name name of the control
	 * @param initialValue initial value of the control
	 * @param rangeStart start value for the slider
	 * @param rangeEnd end value for the slider
	 *
	 * @return AdvancedControllableProperty slider instance
	 */
	private AdvancedControllableProperty createSlider(String name, String labelStart, String labelEnd, Float rangeStart, Float rangeEnd, Float initialValue) {
		AdvancedControllableProperty.Slider slider = new AdvancedControllableProperty.Slider();
		slider.setLabelStart(labelStart);
		slider.setLabelEnd(labelEnd);
		slider.setRangeStart(rangeStart);
		slider.setRangeEnd(rangeEnd);

		return new AdvancedControllableProperty(name, new Date(), slider, initialValue);
	}

	/**
	 * Add addAdvancedControlProperties if advancedControllableProperties different empty
	 *
	 * @param advancedControllableProperties advancedControllableProperties is the list that store all controllable properties
	 * @param stats store all statistics
	 * @param property the property is item advancedControllableProperties
	 * @throws IllegalStateException when exception occur
	 */
	private void addAdvancedControlProperties(List<AdvancedControllableProperty> advancedControllableProperties, Map<String, String> stats, AdvancedControllableProperty property, String value) {
		if (property != null) {
			advancedControllableProperties.removeIf(controllableProperty -> controllableProperty.getName().equals(property.getName()));

			String propertyValue = StringUtils.isNotNullOrEmpty(value) ? value : "N/A";
			stats.put(property.getName(), propertyValue);

			advancedControllableProperties.add(property);
		}
	}

	/***
	 * Create AdvancedControllableProperty preset instance
	 * @param name name of the control
	 * @param initialValue initial value of the control
	 * @return AdvancedControllableProperty preset instance
	 */
	private AdvancedControllableProperty createDropdown(String name, List<String> values, String initialValue) {
		AdvancedControllableProperty.DropDown dropDown = new AdvancedControllableProperty.DropDown();
		dropDown.setOptions(values.toArray(new String[0]));
		dropDown.setLabels(values.toArray(new String[0]));

		return new AdvancedControllableProperty(name, new Date(), dropDown, initialValue);
	}
	//--------------------------------------------------------------------------------------------------------------------------------
	//endregion
}
