/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a;

import static com.avispl.symphony.dal.communicator.lumen.vc.tr60a.LumenVCTR60AUtils.buildSendPacket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
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
import java.util.OptionalInt;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.util.CollectionUtils;

import com.avispl.symphony.api.common.error.ResourceConfigurationException;
import com.avispl.symphony.api.dal.control.Controller;
import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty;
import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty.Button;
import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty.DropDown;
import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty.Slider;
import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty.Switch;
import com.avispl.symphony.api.dal.dto.control.ControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.api.dal.dto.monitor.Statistics;
import com.avispl.symphony.api.dal.error.CommandFailureException;
import com.avispl.symphony.api.dal.error.ResourceNotReachableException;
import com.avispl.symphony.api.dal.monitor.Monitorable;
import com.avispl.symphony.dal.communicator.UDPCommunicator;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.Index;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.ReplyStatus;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.devices.ResponseParser;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.devices.ResponseValidator;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.PayloadCategory;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.PayloadType;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.Prefix;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.command.Command;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.command.CommandType;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.AFFrame;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.AFSensitivity;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.AntiFlicker;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.DigitalZoomLimit;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.ExposureCompLevel;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.ExposureMode;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.FocusMode;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.GainLevel;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.GeneralProperty;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.InitialPosition;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.IrisControl;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.PanTiltDrive;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.PanTiltSpeedComp;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.PictureDNROptions;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.PictureMode;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.PowerStatus;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.PresetControl;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.PresetSpeed;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.ShutterControl;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.SlowPanTiltStatus;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.SlowShutterStatus;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.WBMode;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.WDROptions;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.ZoomControl;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.ZoomPosition;
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
	private String zoomSpeed = "";
	private String focusSpeed = "";
	private Integer zoomSpeedInt = null;
	private Integer focusSpeedInt = null;
	private int cameraIDInt = 1;
	private int panSpeedInt = 1;
	private int tiltSpeedInt = 1;
	private int sequenceNumber = 1;
	private int currentPreset = -1;
	private long nextMonitoringCycleTimestamp = System.currentTimeMillis();
	private String powerStatusMessage = null;

	/** Adapter metadata properties - adapter version and build date */
	private Properties adapterProperties;

	/**
	 * Constructor set command error and success list to be used as well the default camera ID
	 */
	public LumenVCTR60ACommunicator() throws IOException {
		super();
		this.setCommandSuccessList(Collections.singletonList(getHexByteString(ReplyStatus.COMPLETION.getCode())));
		adapterProperties = new Properties();
		adapterProperties.load(getClass().getResourceAsStream("/version.properties"));
		this.setCommandErrorList(Arrays.asList(
				getHexByteString(ReplyStatus.SYNTAX_ERROR_CONTROL.getCode()),
				getHexByteString(ReplyStatus.SYNTAX_ERROR_INQUIRY.getCode()),
				getHexByteString(ReplyStatus.SYNTAX_ERROR_CODE.getCode()),
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
		if (StringUtils.isNotNullOrEmpty(VISCACameraIDAddress)) {
			this.VISCACameraIDAddress = VISCACameraIDAddress.trim();
		}
	}

	/**
	 * Retrieves {@code {@link #panSpeed }}
	 *
	 * @return value of {@link #panSpeed}
	 */
	public String getPanSpeed() {
		return panSpeed;
	}

	/**
	 * Sets {@code panSpeed}
	 *
	 * @param panSpeed the {@code java.lang.String} field
	 */
	public void setPanSpeed(String panSpeed) {
		if (StringUtils.isNotNullOrEmpty(panSpeed)) {
			this.panSpeed = panSpeed.trim();
		}
	}

	/**
	 * Retrieves {@code {@link #tiltSpeed }}
	 *
	 * @return value of {@link #tiltSpeed}
	 */
	public String getTiltSpeed() {
		return tiltSpeed;
	}

	/**
	 * Sets {@code tiltSpeed}
	 *
	 * @param tiltSpeed the {@code java.lang.String} field
	 */
	public void setTiltSpeed(String tiltSpeed) {
		if (StringUtils.isNotNullOrEmpty(tiltSpeed)) {
			this.tiltSpeed = tiltSpeed.trim();
		}
	}

	/**
	 * Retrieves {@code {@link #zoomSpeed}}
	 *
	 * @return value of {@link #zoomSpeed}
	 */
	public String getZoomSpeed() {
		return zoomSpeed;
	}

	/**
	 * Sets {@code zoomSpeed}
	 *
	 * @param zoomSpeed the {@code java.lang.String} field
	 */
	public void setZoomSpeed(String zoomSpeed) {
		this.zoomSpeed = zoomSpeed.trim();
	}

	/**
	 * Retrieves {@code {@link #focusSpeed}}
	 *
	 * @return value of {@link #focusSpeed}
	 */
	public String getFocusSpeed() {
		return focusSpeed;
	}

	/**
	 * Sets {@code focusSpeed}
	 *
	 * @param focusSpeed the {@code java.lang.String} field
	 */
	public void setFocusSpeed(String focusSpeed) {
		this.focusSpeed = focusSpeed.trim();
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
			throw new IllegalStateException("Cannot control while power is " + powerStatusMessage);
		}
		Map<String, String> stats = this.localExtendedStatistics.getStatistics();
		List<AdvancedControllableProperty> advancedControllableProperties = this.localExtendedStatistics.getControllableProperties();

		String property = controllableProperty.getProperty();
		String value = String.valueOf(controllableProperty.getValue());

		if (this.logger.isDebugEnabled()) {
			this.logger.debug("controlProperty property " + property);
			this.logger.debug("controlProperty value " + value);
		}

		String[] splitProperty = property.split(LumenVCTR60AConstants.HASH);
		Command command = Command.getByName(splitProperty[0]);
		Command commandField = null;
		if(property.contains(LumenVCTR60AConstants.HASH)){
			commandField = Command.getByGroupAndName(splitProperty[0], splitProperty[1]);
		}
		switch (command) {
			case POWER: {
				if (value.equals(LumenVCTR60AConstants.SWITCH_STATUS_ON)) {
					powerStatusMessage = LumenVCTR60AConstants.POWER_ON_STATUS;
					performControl(PayloadCategory.CAMERA, Command.POWER, PowerStatus.ON.getCode());
				} else if (value.equals(LumenVCTR60AConstants.SWITCH_STATUS_OFF)) {
					powerStatusMessage = LumenVCTR60AConstants.POWER_OFF_STATUS;
					performControl(PayloadCategory.CAMERA, Command.POWER, PowerStatus.OFF.getCode());
				}
				break;
			}
			case ZOOM:{
				String zoomPosition = getZoomPosition();
				String direction = splitProperty[1];

				if (zoomPosition == null && logger.isWarnEnabled()) {
					this.logger.warn("Zoom position not available, skip control.");
					break;
				}

				Optional<byte[]> newZoomCodeOpt = calculateNewZoomCode(zoomPosition, direction);
				if (!newZoomCodeOpt.isPresent()) {
					break;
				}

				byte[] newZoomCode = newZoomCodeOpt.get();

				if (zoomSpeedInt != null) {
					byte[] zoomParam = new byte[5];
					System.arraycopy(newZoomCode, 0, zoomParam, 0, 4);
					zoomParam[4] = zoomSpeedInt.byteValue();
					performControl(PayloadCategory.CAMERA, Command.ZOOM_POSITION, zoomParam);
				} else {
					performControl(PayloadCategory.CAMERA, Command.ZOOM_POSITION, newZoomCode);
				}
				break;
			}
			case FOCUS_GROUP:
				focusControl(value, commandField);
				updateLocalControlValue(stats, advancedControllableProperties, property, value);
				break;
			case EXPOSURE: {
				exposureControl(value, commandField);
				updateLocalControlValue(stats, advancedControllableProperties, property, value);
				break;
			}
			case MIRROR_GROUP:
				if (Objects.equals(value, LumenVCTR60AConstants.SWITCH_STATUS_ON)) {
						performControl(PayloadCategory.CAMERA, commandField, SlowPanTiltStatus.ON.getCode());
					} else if (Objects.equals(value, LumenVCTR60AConstants.SWITCH_STATUS_OFF)) {
						performControl(PayloadCategory.CAMERA, commandField, SlowPanTiltStatus.OFF.getCode());
					}
				break;
			case WHITE_BALANCE: {
				whiteBalanceControl(value, commandField, stats, advancedControllableProperties);
				updateLocalControlValue(stats, advancedControllableProperties, property, value);
				break;
			}
			case PAN_TILT_DRIVE: {
				String panTiltDriveControlName = splitProperty[1].split(LumenVCTR60AConstants.CLOSE_PARENTHESIS, 2)[1];

				if (Objects.equals(panTiltDriveControlName, Command.PAN_TILT_HOME.getName())) {
					performControl(PayloadCategory.PAN_TILTER, Command.PAN_TILT_HOME);
					break;
				}
				sendPanTiltDriveCommand(PanTiltDrive.getByName(panTiltDriveControlName), panSpeedInt, tiltSpeedInt);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					this.logger.warn("Sleep interrupted", e);
				}
				sendPanTiltDriveCommand(PanTiltDrive.STOP, panSpeedInt, tiltSpeedInt);
				break;
			}
			case PRESET: {
				String presetControlName = splitProperty[1];
				if (Objects.equals(presetControlName, PresetControl.RECALL.getName())) {
					currentPreset = Integer.parseInt(value);
					if(currentPreset > 127){
						byte pp = (byte) (currentPreset - 128);
						performControl(PayloadCategory.CAMERA, Command.PRESET, PresetControl.RECALL_LARGE.getCode(), pp);
					} else {
						performControl(PayloadCategory.CAMERA, Command.PRESET, PresetControl.RECALL.getCode(), (byte) currentPreset);
					}
				}
				currentPreset = -1;
				break;
			}
			case PICTURE:
				pictureControl(value, commandField, stats, advancedControllableProperties);
				updateLocalControlValue(stats, advancedControllableProperties, property, value);
				break;
			case PTZ:
				ptzControl(value, commandField);
				updateLocalControlValue(stats, advancedControllableProperties, property, value);
				break;
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
			List<AdvancedControllableProperty> advancedControllableProperties = new ArrayList<>();
			Map<String, String> dynamicStatistics = new HashMap<>();
			StringBuilder errorMessages = new StringBuilder();
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Perform getMultipleStatistics() at host: %s, port: %s", this.getHost(), this.getPort()));
			}
		tryParseIntAdapterProperties(errorMessages);
		checkOutOfRange(errorMessages);
		if (!errorMessages.toString().isEmpty()) {
			throw new ResourceConfigurationException(errorMessages.toString());
		}

			retrieveMetadata(stats);
			if (System.currentTimeMillis() < nextMonitoringCycleTimestamp) {
				// If in monitoring cycle -> do not render controllable properties
				stats.put(Command.POWER_STATUS.getName(), powerStatusMessage);
			} else {
				// Reset sequence number to 0 if it reaches the max value of integer
				// (need to check it before all command can be performed)
				if (sequenceNumber == Integer.MAX_VALUE - Command.values().length) {
					sequenceNumber = 0;
				}
				// Control capabilities
				populateGeneralProperties(stats);
				populateControlCapabilities(stats, advancedControllableProperties);
			}

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
		cameraIDInt = parseIntOrDefault(VISCACameraIDAddress, "Camera ID", errorMessages);
		panSpeedInt = parseIntOrDefault(panSpeed, "Pan speed", errorMessages);
		tiltSpeedInt = parseIntOrDefault(tiltSpeed, "Tilt speed", errorMessages);
		zoomSpeedInt = tryParseInteger(zoomSpeed, "Zoom speed", errorMessages);
		focusSpeedInt = tryParseInteger(focusSpeed, "Focus speed", errorMessages);
	}

	/**
	 * This method is used for check adapter properties are out of range of not
	 *
	 * @param errorMessages is the error messages of properties when out of range
	 */
	private void checkOutOfRange(StringBuilder errorMessages) {
		if (this.cameraIDInt < 1 || this.cameraIDInt > 7) {
			errorMessages.append("Camera ID with value ").append(this.VISCACameraIDAddress).append(" is out of range. Camera ID must be between 1 and 7. ");
		}

		if (this.panSpeedInt < 1 || this.panSpeedInt > 24) {
			errorMessages.append("Pan speed with value ").append(this.panSpeed).append(" is out of range. Pan speed must be between 1 and 24. ");
		}

		if (this.tiltSpeedInt < 1 || this.tiltSpeedInt > 20) {
			errorMessages.append("Tilt speed with value ").append(this.tiltSpeed).append(" is out of range. Tilt speed must be between 1 and 20. ");
		}

		if ( this.zoomSpeedInt != null && (this.zoomSpeedInt < 0 || zoomSpeedInt > 7)) {
			errorMessages.append("Zoom speed with value ").append(this.zoomSpeedInt).append(" is out of range. Zoom speed must be between 0 and 7. ");
		}

		if (this.focusSpeedInt != null && (focusSpeedInt < 0 || focusSpeedInt > 7)) {
			errorMessages.append("Focus speed with value ").append(this.focusSpeedInt).append(" is out of range. Focus speed must be between 0 and 7.");
		}
	}

	/**
	 * Populates the provided stats map with all configured general properties.
	 *
	 * @param stats the map to populate with retrieved properties
	 */
	private void populateGeneralProperties(Map<String, String> stats) {
		try{
			for (GeneralProperty gp : GeneralProperty.values()) {
				String value = retrieveDeviceInfo(gp.key(), gp.categoryCode(), gp.command());
				stats.put(gp.key(), value);
			}
		} catch (Exception e) {
			throw new ResourceNotReachableException(e.getMessage(), e);
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
		try {
			// Getting power status from device
			String powerStatus = getPowerStatus();

			if (Objects.equals(powerStatus, PowerStatus.OFF.getName())) {
				populateSwitchControl(stats, advancedControllableProperties, Command.POWER.getName(), powerStatus, PowerStatus.OFF.getName(), PowerStatus.ON.getName());
			} else if (Objects.equals(powerStatus, PowerStatus.ON.getName())) {
				populateSwitchControl(stats, advancedControllableProperties, Command.POWER.getName(), powerStatus, PowerStatus.OFF.getName(), PowerStatus.ON.getName());

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
		} catch (Exception e){
			throw new ResourceNotReachableException(e.getMessage(), e);
		}
	}

	//region Control device

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
			case EXPOSURE_MODE: {
				performControl(PayloadCategory.CAMERA, Command.EXPOSURE_MODE, ExposureMode.getByName(value).getCode());
				break;
			}
			case ANTI_FLICK:
				performControl(PayloadCategory.CAMERA, Command.ANTI_FLICK, AntiFlicker.getByName(value).getCode());
				break;
			case GAIN_LIMIT_DIRECT:
				float gainLimitLevel = Float.parseFloat(value);
				byte param = (byte) (int) ((gainLimitLevel - 8) / 2 + 0x04);
				performControl(PayloadCategory.CAMERA, exposureCommand, param);
				break;
			case EXP_COMP_DIRECT:
				performControl(PayloadCategory.CAMERA, Command.EXP_COMP_CONTROL, ExposureCompLevel.getByName(value).getCode());
				break;
			case IRIS_DIRECT:
				performControl(PayloadCategory.CAMERA, Command.IRIS_CONTROL, IrisControl.getByName(value).getCode());
				break;
			case SHUTTER_DIRECT:
				performControl(PayloadCategory.CAMERA, Command.SHUTTER_CONTROL, ShutterControl.getByName(value).getCode());
				break;
			case GAIN_LEVEL:
				performControl(PayloadCategory.CAMERA, Command.GAIN_LEVEL_CONTROL, GainLevel.getByName(value).getCode());
				break;
			case WDR_OPTION:
				performControl(PayloadCategory.CAMERA, exposureCommand, WDROptions.getByName(value).getCode());
				break;
			default: {
				throw new IllegalStateException("Unexpected value: " + exposureCommand);
			}
		}
	}
	/**
	 * This method is used to control focus:
	 * <li>Focus Mode</li>
	 * <li>AFFrame</li>
	 * <li>AFSensitive</li>
	 * <li>Focus Far</li>
	 * <li>Focus Near</li>
	 *
	 * @param value is the value of controllable property
	 * @param focusCommand is the command get from controllable property name
	 */
	private void focusControl(String value, Command focusCommand) {
		try {
			switch (focusCommand){
				case FOCUS_MODE:
					performControl(PayloadCategory.CAMERA, Command.FOCUS_MODE, FocusMode.getByName(value).getCode());
					break;
				case AF_FRAME:
					performControl(PayloadCategory.CAMERA, focusCommand, AFFrame.getByName(value).getCode());
					break;
				case AF_SENSITIVE:
					performControl(PayloadCategory.CAMERA, focusCommand, AFSensitivity.getByName(value).getCode());
					break;
				case FOCUS_FAR:
				case FOCUS_NEAR:
					int newValue;
					int currentValue = Integer.parseInt(getFocusPosition());
					boolean isFar = focusCommand.getCode()[1] == 0x02;
					if (isFar) {
						newValue = Math.max(LumenVCTR60AConstants.FOCUS_MIN, currentValue - LumenVCTR60AConstants.FOCUS_STEP);
					} else {
						newValue = Math.min(LumenVCTR60AConstants.FOCUS_MAX, currentValue + LumenVCTR60AConstants.FOCUS_STEP);
					}
					byte[] focusBytes = convertFocusValueToBytes(newValue);
					performControl(PayloadCategory.CAMERA, Command.FOCUS_POSITION, focusBytes);
					break;
				default: {
					throw new IllegalStateException("Unexpected value: " + focusCommand);
				}
			}
		} catch (Exception e){
			logger.error("error during command " + focusCommand.getName() + " send", e);
			throw new IllegalStateException("Error while sending command " + focusCommand.getName());
		}
	}

	/**
	 * This is commonly used for encoding focus control values in VISCA-over-IP protocols,
	 * where each nibble must be transmitted separately.
	 *
	 * @param value the integer value to encode; typically a 16‑bit value (0–65535).
	 * @return a byte array of length 4 containing the high‑to‑low 4‑bit segments.
	 */
	public static byte[] convertFocusValueToBytes(int value) {
		byte[] result = new byte[4];
		result[0] = (byte) ((value >> 12) & 0x0F);
		result[1] = (byte) ((value >> 8) & 0x0F);
		result[2] = (byte) ((value >> 4) & 0x0F);
		result[3] = (byte) (value & 0x0F);
		return result;
	}

	/**
	 * This method is used to control picture:
	 * <li>Focus Mode</li>
	 * <li>AFFrame</li>
	 * <li>AFSensitive</li>
	 * <li>Focus Far</li>
	 * <li>Focus Near</li>
	 *
	 * @param value is the value of controllable property
	 * @param commandField is the command get from controllable property name
	 * @param stats is the map that store all statistics
	 * @param advancedControllableProperties is the list that store all controllable properties
	 */
	private void pictureControl(String value, Command commandField, Map<String, String> stats, List<AdvancedControllableProperty>advancedControllableProperties) {
		try {
			switch (commandField){
				case IMAGE_MODE:
					performControl(PayloadCategory.CAMERA, commandField, PictureMode.getByName(value).getCode());
					break;
				case TWO_DNR:
				case THREE_DNR:
					performControl(PayloadCategory.CAMERA, commandField, PictureDNROptions.getByName(value).getCode());
					break;
				case BRIGHTNESS:
					float levelBright = Float.parseFloat(value);
					byte[] pq = convertToTwoBytePQ(levelBright, 14);
					performControl(PayloadCategory.CAMERA, commandField, (byte) 0x00, (byte) 0x00, pq[0], pq[1]);
					handlePictureSlider(commandField, Command.BRIGHTNESS_CURRENT, value, LumenVCTR60AConstants.ZERO, LumenVCTR60AConstants.LABEL_END_HUE_LEVEL, LumenVCTR60AConstants.RANGE_START_HUE_LEVEL, LumenVCTR60AConstants.RANGE_END_HUE_LEVEL, stats, advancedControllableProperties);
					break;
				case SATURATION:
					float levelSaturation = Float.parseFloat(value);
					byte[] saturation = convertToTwoBytePQ(levelSaturation, 14);
					performControl(PayloadCategory.CAMERA, commandField, (byte) 0x00, (byte) 0x00, saturation[0], saturation[1]);
					handlePictureSlider(commandField, Command.SATURATION_CURRENT, value, LumenVCTR60AConstants.ZERO, LumenVCTR60AConstants.LABEL_END_HUE_LEVEL, LumenVCTR60AConstants.RANGE_START_HUE_LEVEL, LumenVCTR60AConstants.RANGE_END_HUE_LEVEL, stats, advancedControllableProperties);
					break;
				case HUE:
					float levelHue = Float.parseFloat(value);
					byte[] hue = convertToTwoBytePQ(levelHue, 14);
					performControl(PayloadCategory.CAMERA, commandField, (byte) 0x00, (byte) 0x00, hue[0], hue[1]);
					handlePictureSlider(commandField, Command.HUE_CURRENT, value, LumenVCTR60AConstants.ZERO, LumenVCTR60AConstants.LABEL_END_HUE_LEVEL, LumenVCTR60AConstants.RANGE_START_HUE_LEVEL, LumenVCTR60AConstants.RANGE_END_HUE_LEVEL, stats, advancedControllableProperties);
					break;
				case GAMMA:
					float levelGamma = Float.parseFloat(value);
					performControl(PayloadCategory.CAMERA, commandField, (byte) levelGamma);
					handlePictureSlider(commandField, Command.BRIGHTNESS_CURRENT, value, LumenVCTR60AConstants.ZERO, "4", LumenVCTR60AConstants.RANGE_START_HUE_LEVEL, 4F, stats, advancedControllableProperties);
					break;
				case SHARPNESS:
					float levelSharpness = Float.parseFloat(value);
					byte[] sharpness = convertToTwoBytePQ(levelSharpness, 11);
					performControl(PayloadCategory.CAMERA, commandField, (byte) 0x00, (byte) 0x00, sharpness[0], sharpness[1]);
					handlePictureSlider(commandField, Command.BRIGHTNESS_CURRENT, value, LumenVCTR60AConstants.ZERO, "11", LumenVCTR60AConstants.RANGE_START_HUE_LEVEL, 11F, stats, advancedControllableProperties);
					break;
				default: {
					throw new IllegalStateException("Unexpected value: " + commandField);
				}
			}

		} catch (Exception e){
			throw new IllegalStateException("Error while sending command " + commandField.getName());
		}
	}

	/**
	 * This method is used to control ptz:
	 * <li>DZoomLimit</li>
	 * <li>InitialPosition</li>
	 * <li>MotionlessPreset</li>
	 * <li>Preset Speed</li>
	 * <li>PTZ Speed Comp</li>
	 *
	 * @param value is the value of controllable property
	 * @param commandField is the command get from controllable property name
	 */
	private void ptzControl(String value, Command commandField) {
		try {
			switch (commandField){
				case D_ZOOM_LIMIT:
					performControl(PayloadCategory.CAMERA, commandField, DigitalZoomLimit.getByName(value).getCode());
					break;
				case INITIAL_POSITION:
					performControl(PayloadCategory.CAMERA, commandField, InitialPosition.getByName(value).getCode());
					break;
				case MOTIONLESS_PRESET:
					if (Objects.equals(value, LumenVCTR60AConstants.SWITCH_STATUS_ON)) {
						performControl(PayloadCategory.MOTIONLESS, commandField, SlowPanTiltStatus.ON.getCode());
					} else if (Objects.equals(value, LumenVCTR60AConstants.SWITCH_STATUS_OFF)) {
						performControl(PayloadCategory.MOTIONLESS, commandField, SlowPanTiltStatus.OFF.getCode());
					}
					break;
				case PRESET_SPEED:
					performControl(PayloadCategory.PAN_TILTER, Command.PRESET_SPEED_CONTROL, PresetSpeed.getByName(value).getCode());
					break;
				case PTZ_SPEED_COMP:
					if (Objects.equals(value, LumenVCTR60AConstants.SWITCH_STATUS_ON)) {
						performControl(PayloadCategory.PAN_TILTER, commandField, PanTiltSpeedComp.ON.getCode());
					} else if (Objects.equals(value, LumenVCTR60AConstants.SWITCH_STATUS_OFF)) {
						performControl(PayloadCategory.PAN_TILTER, commandField, PanTiltSpeedComp.OFF.getCode());
					}
					break;
				default: {
					throw new IllegalStateException("Unexpected value: " + commandField);
				}
			}

		} catch (Exception e){
			throw new IllegalStateException("Error while sending command " + commandField.getName());
		}
	}

	/**
	 * This method is used to control white balance:
	 * <li>WB Mode</li>
	 * <li>RGain</li>
	 * <li>BGain</li>
	 * <li>WB OnePushTrigger</li>
	 *
	 * @param value is the value of controllable property
	 * @param commandField is the command get from controllable property name
	 * @param stats is the map that store all statistics
	 * @param advancedControllableProperties is the list that store all controllable properties
	 */
	private void whiteBalanceControl(String value, Command commandField, Map<String, String>stats, List<AdvancedControllableProperty>advancedControllableProperties) {
		try {
			switch (commandField){
				case WB_MODE:
					performControl(PayloadCategory.CAMERA, commandField, WBMode.getByName(value).getCode());
					break;
				case RGAIN:
					float levelRed = Float.parseFloat(value);
					byte[] red = convertToTwoBytePQ(levelRed, 127);
					performControl(PayloadCategory.CAMERA, commandField, (byte) 0x00, (byte) 0x00, red[0], red[1]);
					handlePictureSlider(commandField, Command.RGAIN_CURRENT, value, LumenVCTR60AConstants.ZERO, "127", LumenVCTR60AConstants.RANGE_START_HUE_LEVEL, 127F, stats, advancedControllableProperties);
					break;
				case BGAIN:
					float levelBlue = Float.parseFloat(value);
					byte[] blue = convertToTwoBytePQ(levelBlue, 127);
					performControl(PayloadCategory.CAMERA, commandField, (byte) 0x00, (byte) 0x00, blue[0], blue[1]);
					handlePictureSlider(commandField, Command.BGAIN_CURRENT, value, LumenVCTR60AConstants.ZERO, "127", LumenVCTR60AConstants.RANGE_START_HUE_LEVEL, 127F, stats, advancedControllableProperties);
					break;
				case WB_ONE_PUSH_TRIGGER: {
					performControl(PayloadCategory.CAMERA, Command.WB_ONE_PUSH_TRIGGER);
					break;
				}
				default: {
					throw new IllegalStateException("Unexpected value: " + commandField);
				}
			}
		} catch (Exception e){
			throw new IllegalStateException("Error while sending command " + commandField.getName());
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
			if(!Command.PRESET.getName().equals(command.getName())){
				throw new IllegalStateException("Error while sending command " + command.getName());
			}
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
			stats.put(Command.FOCUS_GROUP.getName() + LumenVCTR60AConstants.HASH + Command.FOCUS_MODE.getName(), LumenVCTR60AConstants.NOT_AVAILABLE);
			return;
		}
		String afSensitivityValue = this.getAFSensitivity();
		String afFrameValue = this.getAFFrame();

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

		addAdvancedControlProperties(
				advancedControllableProperties,
				stats,
				createDropdown(Command.FOCUS_MODE.getGroup() + LumenVCTR60AConstants.HASH + Command.FOCUS_MODE.getName(),
				focusOptions,
				focusMode.getName()), focusMode.getName());

		if (focusMode == FocusMode.AUTO) {
			addAdvancedControlProperties(advancedControllableProperties, stats, createDropdown(Command.FOCUS_GROUP.getName() + LumenVCTR60AConstants.HASH + Command.AF_FRAME.getName(), afFrame, afFrameValue), afFrameValue);
			addAdvancedControlProperties(advancedControllableProperties, stats, createDropdown(Command.FOCUS_GROUP.getName() + LumenVCTR60AConstants.HASH + Command.AF_SENSITIVE.getName(), afSensitivity, afSensitivityValue) , afSensitivityValue);
		} else if (focusMode == FocusMode.MANUAL) {
			populateButtonControl(stats, advancedControllableProperties, Command.FOCUS_GROUP.getName() + LumenVCTR60AConstants.HASH + Command.FOCUS_NEAR.getName(), LumenVCTR60AConstants.PLUS);
			populateButtonControl(stats, advancedControllableProperties, Command.FOCUS_GROUP.getName() + LumenVCTR60AConstants.HASH + Command.FOCUS_FAR.getName(), LumenVCTR60AConstants.MINUS);
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
		String pictureMode = this.getPictureMode();
		String number2DNROptions = this.getDNR(Command.TWO_DNR);
		String number3DNROptions = this.getDNR(Command.THREE_DNR);

		List<String> dnrOptions = Arrays.stream(PictureDNROptions.values())
				.map(PictureDNROptions::getName)
				.collect(Collectors.toList());

		List<String> pictureOptions = Arrays.asList(
				PictureMode.DEFAULT.getName(),
				PictureMode.CUSTOM.getName());

		addAdvancedControlProperties(advancedControllableProperties,stats, createDropdown(Command.PICTURE.getName() + LumenVCTR60AConstants.HASH + Command.IMAGE_MODE.getName(), pictureOptions, pictureMode), pictureMode);
		if (Objects.equals(pictureMode, PictureMode.CUSTOM.getName())) {
			String hueLevel = this.getPictureCustomValue(Command.HUE);
			String saturation = this.getPictureCustomValue(Command.SATURATION);
			String brightness = this.getPictureCustomValue(Command.BRIGHTNESS);
			String gamma = this.getPictureCustomValue(Command.GAMMA);
			String sharpness = this.getPictureCustomValue(Command.SHARPNESS);

			handlePictureSlider(Command.HUE, Command.HUE_CURRENT, hueLevel, LumenVCTR60AConstants.ZERO, LumenVCTR60AConstants.LABEL_END_HUE_LEVEL, LumenVCTR60AConstants.RANGE_START_HUE_LEVEL, LumenVCTR60AConstants.RANGE_END_HUE_LEVEL, stats, advancedControllableProperties);
			handlePictureSlider(Command.SATURATION, Command.SATURATION_CURRENT, saturation, LumenVCTR60AConstants.ZERO, LumenVCTR60AConstants.LABEL_END_HUE_LEVEL, LumenVCTR60AConstants.RANGE_START_HUE_LEVEL, LumenVCTR60AConstants.RANGE_END_HUE_LEVEL, stats, advancedControllableProperties);
			handlePictureSlider(Command.BRIGHTNESS, Command.BRIGHTNESS_CURRENT, brightness, LumenVCTR60AConstants.ZERO, LumenVCTR60AConstants.LABEL_END_HUE_LEVEL, LumenVCTR60AConstants.RANGE_START_HUE_LEVEL, LumenVCTR60AConstants.RANGE_END_HUE_LEVEL, stats, advancedControllableProperties);
			handlePictureSlider(Command.GAMMA, Command.GAMMA_CURRENT, gamma, "0", "4", 0F, 4F, stats, advancedControllableProperties);
			handlePictureSlider(Command.SHARPNESS, Command.SHARPNESS_CURRENT, sharpness, "0", "11", 0F, 11F, stats, advancedControllableProperties);
		}
		addAdvancedControlProperties(advancedControllableProperties, stats, createDropdown(Command.PICTURE.getName() + LumenVCTR60AConstants.HASH + Command.TWO_DNR.getName(), dnrOptions, number2DNROptions), number2DNROptions);
		addAdvancedControlProperties(advancedControllableProperties, stats, createDropdown(Command.PICTURE.getName() + LumenVCTR60AConstants.HASH + Command.THREE_DNR.getName(), dnrOptions, number3DNROptions), number3DNROptions);
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

		try {
			if (value == null || Objects.equals(value, LumenVCTR60AConstants.NONE_VALUE) || value.equalsIgnoreCase(LumenVCTR60AConstants.NOT_AVAILABLE)) {
				stats.put(key, LumenVCTR60AConstants.NOT_AVAILABLE);
			} else {
				populateSliderControl(stats, props, key, Command.PICTURE.getName() + LumenVCTR60AConstants.HASH + currentCommand.getName(),
						value, labelStart, labelEnd, rangeStart, rangeEnd, Float.parseFloat(value));
			}
		} catch (NumberFormatException e) {
			if (logger.isWarnEnabled()) {
				logger.warn("Invalid float value for " + key + ": " + value, e);
			}
			stats.put(key, LumenVCTR60AConstants.NOT_AVAILABLE);
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
		String wdrOptions = this.getWDROptions();

		List<String> irisNameList = Arrays.stream(IrisControl.values())
				.map(IrisControl::getName)
				.collect(Collectors.toList());

		List<String> shutterList = Arrays.stream(ShutterControl.values())
				.map(ShutterControl::getName)
				.collect(Collectors.toList());

		List<String> gainLevelList = Arrays.stream(GainLevel.values())
				.map(GainLevel::getName)
				.collect(Collectors.toList());

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

		List<String> expCompLevelOptions = Arrays.stream(ExposureCompLevel.values())
				.map(ExposureCompLevel::getName)
				.collect(Collectors.toList());

		List<String> gainLimitOptions = IntStream.rangeClosed(8, 30)
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
				addDropdownOrNA(Command.ANTI_FLICK, this.getAntiFlicker(), antiFlickList, stats, advancedControllableProperties);
				addDropdownOrNA(Command.GAIN_LIMIT_DIRECT, this.getGainLimitLevel(), gainLimitOptions, stats, advancedControllableProperties);
				addDropdownOrNA(Command.EXP_COMP_DIRECT, this.getExposureValue(), expCompLevelOptions, stats, advancedControllableProperties);
				break;
			}
			case SHUTTER_PRIORITY: {
				addDropdownOrNA(Command.EXP_COMP_DIRECT, this.getExposureValue(), expCompLevelOptions, stats, advancedControllableProperties);
				addDropdownOrNA(Command.GAIN_LIMIT_DIRECT, this.getGainLimitLevel(), gainLimitOptions, stats, advancedControllableProperties);
				addDropdownOrNA(Command.SHUTTER_DIRECT, this.getShutterSpeed(), shutterList, stats, advancedControllableProperties);
				break;
			}
			case IRIS_PRIORITY: {
				addDropdownOrNA(Command.EXP_COMP_DIRECT, this.getExposureValue(), expCompLevelOptions, stats, advancedControllableProperties);
				addDropdownOrNA(Command.GAIN_LIMIT_DIRECT, this.getGainLimitLevel(), gainLimitOptions, stats, advancedControllableProperties);
				addDropdownOrNA(Command.IRIS_DIRECT, this.getIrisLevel(), irisNameList, stats, advancedControllableProperties);
				break;
			}
			case MANUAL:
				addDropdownOrNA(Command.GAIN_LEVEL, this.getGainLevel(), gainLevelList, stats, advancedControllableProperties);
				addDropdownOrNA(Command.SHUTTER_DIRECT, this.getShutterSpeed(), shutterList, stats, advancedControllableProperties);
				addDropdownOrNA(Command.IRIS_DIRECT, this.getIrisLevel(), irisNameList, stats, advancedControllableProperties);
				break;
			case WHITE_BOARD:{
				addDropdownOrNA(Command.GAIN_LIMIT_DIRECT, this.getGainLimitLevel(), gainLimitOptions, stats, advancedControllableProperties);
				addDropdownOrNA(Command.EXP_COMP_DIRECT, this.getExposureValue(), expCompLevelOptions, stats, advancedControllableProperties);
				break;
			}
			default:
				throw new IllegalStateException("Unexpected exposure mode: " + exposureMode);
		}
		addDropdownControl(Command.EXPOSURE, Command.WDR_OPTION, WDRList, wdrOptions, stats, advancedControllableProperties);
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

		addAdvancedControlProperties(advancedControllableProperties, stats, createDropdown(Command.WHITE_BALANCE.getName() + LumenVCTR60AConstants.HASH + Command.WB_MODE.getName(), wbModeList, wbMode), wbMode);

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

		for (int i = 0; i <= 255; ++i) {
			presetList.add(String.valueOf(i));
		}
		String presetValue = currentPreset == -1 ? LumenVCTR60AConstants.DEFAULT_PRESET : String.valueOf(currentPreset);

		addAdvancedControlProperties(advancedControllableProperties, stats, createDropdown(Command.PRESET.getName() + LumenVCTR60AConstants.HASH + PresetControl.RECALL.getName(), presetList, presetValue), presetValue);
}

	/**
	 * Populates PTZ-related controls including digital zoom, pan/tilt limit, PTZ speed compensation,
	 * motionless preset, zoom limit dropdown, preset speed, and initial position.
	 *
	 * @param stats the statistics-to-value map to populate
	 * @param advancedControllableProperties the list to which new advanced control properties will be appended
	 */
	private void populatePanTiltZoomControl(Map<String, String> stats, List<AdvancedControllableProperty> advancedControllableProperties) {
		List<String> dZoomList = Arrays.stream(DigitalZoomLimit.values())
				.map(DigitalZoomLimit::getName)
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
		String initialPosition = this.getInitialPosition();
		String presetSpeedValue = this.getPresetSpeed();

		populateSwitchControl(stats, advancedControllableProperties, Command.PTZ.getName() + LumenVCTR60AConstants.HASH + Command.PTZ_SPEED_COMP.getName(), ptzSpeedCompStatus,
				SlowPanTiltStatus.OFF.getName(), SlowPanTiltStatus.ON.getName());

		populateSwitchControl(stats, advancedControllableProperties, Command.PTZ.getName() + LumenVCTR60AConstants.HASH + Command.MOTIONLESS_PRESET.getName(), motionlessPreset,
				SlowPanTiltStatus.OFF.getName(), SlowPanTiltStatus.ON.getName());

		addAdvancedControlProperties(advancedControllableProperties, stats, createDropdown(Command.PTZ.getName() + LumenVCTR60AConstants.HASH + Command.D_ZOOM_LIMIT.getName(), dZoomList, dZoomValue), dZoomValue);
		addAdvancedControlProperties(advancedControllableProperties, stats, createDropdown(Command.PTZ.getName() + LumenVCTR60AConstants.HASH + Command.PRESET_SPEED.getName(), presetSpeedList, presetSpeedValue), presetSpeedValue);
		addAdvancedControlProperties(advancedControllableProperties, stats, createDropdown(Command.PTZ.getName() + LumenVCTR60AConstants.HASH + Command.INITIAL_POSITION.getName(), initialPositionList, initialPosition), initialPosition);
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
		addAdvancedControlProperties(advancedControllableProperties, stats, createSlider(propertyName, labelStart, labelEnd, rangeStart, rangeEnd, initialValue), LumenVCTR60AConstants.NOT_AVAILABLE);
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
			stats.put(propertyName, LumenVCTR60AConstants.NOT_AVAILABLE);
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
		stats.put(propertyName, LumenVCTR60AConstants.NOT_AVAILABLE);
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
			throw new ResourceNotReachableException(e.getMessage(), e);
		}
	}

	private <T> String getValueByCommand(Command command, PayloadCategory category, Class<T> responseType, String logMessage, Function<T, String> mapper) {
		try {
			int currentSeqNum = ++sequenceNumber;
			byte[] response = send(
					buildSendPacket(cameraIDInt, currentSeqNum,
							PayloadType.INQUIRY.getCode(),
							CommandType.INQUIRY.getCode(),
							category.getCode(),
							command.getCode()));

			Object rawResult = digestResponse(response, currentSeqNum, CommandType.INQUIRY, command);
			T result = responseType.cast(rawResult);

			if (result == null) {
				return LumenVCTR60AConstants.NOT_AVAILABLE;
			}
			return mapper.apply(result);

		} catch (Exception e) {
			this.logger.error("error during get " + logMessage, e);
			return LumenVCTR60AConstants.NOT_AVAILABLE;
		}
	}

	/**
	 * This method is used to get the Zoom position
	 *
	 * @return returns the Zoom position
	 */
	private String getZoomPosition() {
		return getValueByCommand(Command.ZOOM_POSITION, PayloadCategory.CAMERA, Object.class,
				"ZoomPosition", String::valueOf);
	}

	/**
	 * This method is used to get the Focus position
	 *
	 * @return returns the Focus position
	 */
	private String getFocusPosition() {
		return getValueByCommand(Command.FOCUS_POSITION, PayloadCategory.CAMERA, Object.class,
				"FocusPosition", String::valueOf);
	}

	/**
	 * This method is used to get the AF sensitivity
	 *
	 * @return String This returns the AF sensitivity
	 */
	private String getAFSensitivity() {
		return getValueByCommand(Command.AF_SENSITIVE, PayloadCategory.CAMERA, AFSensitivity.class,
				"AFSensitivity", AFSensitivity::getName);
	}

	/**
	 * This method is used to get the AF Frame
	 *
	 * @return String This returns the AF Frame
	 */
	private String getAFFrame() {
		return getValueByCommand(Command.AF_FRAME, PayloadCategory.CAMERA, AFFrame.class,
				"AFFrame", AFFrame::getName);
	}

	/**
	 * This method is used to get the current display current gain limit level
	 *
	 * @return String This returns the gain limit level
	 */
	private String getGainLimitLevel() {
		return getValueByCommand(Command.GAIN_LIMIT_DIRECT, PayloadCategory.CAMERA, Integer.class,
				"gain limit level", val -> String.valueOf((int) val));
	}

	/**
	 * This method is used to get the current display current gain level
	 *
	 * @return String This returns the gain level
	 */
	private String getGainLevel() {
		return getValueByCommand(Command.GAIN_LEVEL, PayloadCategory.CAMERA, Object.class,
				"gain level", String::valueOf);
	}

	/**
	 * This method is used to get the current display picture mode
	 *
	 * @return String This returns the picture mode
	 */
	private String getPictureMode() {
		return getValueByCommand(Command.IMAGE_MODE, PayloadCategory.CAMERA, PictureMode.class,
				"picture mode", PictureMode::getName);
	}

	/**
	 * This method is used to get the current display picture custom property like:
	 * Hue, Saturation, Brightness, Sharpness, Gamma
	 *
	 * @param expectedCommand: command of Hue, Saturation, Brightness, Sharpness, Gamma
	 * @return String This returns the expectedCommand
	 */
	private String getPictureCustomValue(Command expectedCommand) {
		return getValueByCommand(expectedCommand, PayloadCategory.CAMERA, Object.class,
				expectedCommand.getName(), String::valueOf);
	}

	/**
	 * This method is used to get the DNR value
	 *
	 * @return String This returns the DNR value
	 */
	private String getDNR(Command expectedCommand) {
		return getValueByCommand(expectedCommand, PayloadCategory.CAMERA, PictureDNROptions.class,
				expectedCommand.getName(), PictureDNROptions::getName);
	}

	/**
	 * This method is used to get the digital zoom value
	 *
	 * @return String This returns the digital zoom value
	 */
	private String getDZoomValue(){
		return getValueByCommand(Command.D_ZOOM_LIMIT, PayloadCategory.CAMERA, Object.class,
				"digital zoom", String::valueOf);
	}

	/**
	 * This method is used to get the initial position value
	 *
	 * @return String This returns the initial position value
	 */
	private String getInitialPosition(){
		return getValueByCommand(Command.INITIAL_POSITION, PayloadCategory.CAMERA, InitialPosition.class,
				"initial position", InitialPosition::getName);
	}

	/**
	 * This method is used to get the preset speed value
	 *
	 * @return String This returns the preset speed value
	 */
	private String getPresetSpeed(){
		return getValueByCommand(Command.PRESET_SPEED, PayloadCategory.CAMERA, PresetSpeed.class, "preset speed", PresetSpeed::getName);
	}

	/**
	 * This method is used to get the current display current exposure value
	 *
	 * @return String This returns the exposure value
	 */
	private String getExposureValue() {
		return getValueByCommand(Command.EXP_COMP_DIRECT, PayloadCategory.CAMERA, Object.class, "exposure value", String::valueOf);
	}

	/**
	 * This method is used to get the current display current shutter speed
	 *
	 * @return Entry<Integer, String> This returns the entry for shutter speed
	 * key: int value of slider, value: string value of shutter speed
	 */
	private String getShutterSpeed() {
		return getValueByCommand(Command.SHUTTER_DIRECT, PayloadCategory.CAMERA, Object.class, "shutter speed", String::valueOf);
	}

	/**
	 * This method is used to get the current display current iris level
	 *
	 * @return Entry<Integer, String> This returns the entry for iris level
	 * key: int value of slider, value: string value of iris level
	 */
	private String getIrisLevel() {
		return getValueByCommand(Command.IRIS_DIRECT, PayloadCategory.CAMERA, Object.class, "iris level", String::valueOf);
	}

	/**
	 * This method is used to get the current display current WB mode
	 *
	 * @return String This returns the WB mode
	 */
	private String getWBMode() {
		return getValueByCommand(Command.WB_MODE, PayloadCategory.CAMERA, WBMode.class, "WB mode", WBMode::getName);
	}

	/**
	 * This method is used to get the current display current BGain value
	 *
	 * @return String This returns the BGain value
	 */
	private String getBGain() {
		return getValueByCommand(Command.BGAIN_INQ, PayloadCategory.CAMERA, Object.class, "BGain value", String::valueOf);
	}

	/**
	 * This method is used to get the current display current RGain value
	 *
	 * @return String This returns the RGain value
	 */
	private String getRGain() {
		return getValueByCommand(Command.RGAIN_INQ, PayloadCategory.CAMERA, Object.class, "RGain value", String::valueOf);
	}

	/**
	 * This method is used to get the current display current Mirror status
	 *
	 * @return String This returns the Mirror status
	 */
	private String getMirrorStatus() {
		return getValueByCommand(Command.MIRROR, PayloadCategory.CAMERA, SlowPanTiltStatus.class, "mirror status", SlowPanTiltStatus::getName);
	}

	/**
	 * This method is used to get the current display current flip status
	 *
	 * @return String This returns the flip status
	 */
	private String getFlipStatus() {
		return getValueByCommand(Command.FLIP, PayloadCategory.CAMERA, SlowPanTiltStatus.class, "flip status", SlowPanTiltStatus::getName);
	}

	/**
	 * This method is used to get the current display current slow pan tilt status
	 *
	 * @return String This returns the slow pan tilt status
	 */
	private String getPanTiltStatus() {
		return getValueByCommand(Command.PTZ_SPEED_COMP, PayloadCategory.PAN_TILTER, PanTiltSpeedComp.class, "pan tilt status", PanTiltSpeedComp::getName);
	}

	/**
	 * This method is used to get the current display current motionless preset
	 *
	 * @return String This returns the motionless preset
	 */
	private String getMotionlessPreset() {
		return getValueByCommand(Command.MOTIONLESS_PRESET, PayloadCategory.MOTIONLESS, SlowShutterStatus.class,"motionless preset", SlowShutterStatus::getName);
	}

	/**
	 * This method is used to get the current display power status
	 *
	 * @return String This returns the power status
	 */
	private String getPowerStatus() {
		return getValueByCommand(Command.POWER, PayloadCategory.CAMERA, PowerStatus.class,"power status", PowerStatus::getName);
	}

	/**
	 * This method is used to get the WDR
	 *
	 * @return String This returns the WDR
	 */
	private String getWDROptions() {
		return getValueByCommand(Command.WDR_OPTION, PayloadCategory.CAMERA, WDROptions.class, "WDR options", WDROptions::getName);
	}

	/**
	 * This method is used to get the anti flicker
	 *
	 * @return String This returns the anti flicker
	 */
	private String getAntiFlicker() {
		return getValueByCommand(Command.ANTI_FLICK, PayloadCategory.CAMERA, AntiFlicker.class, "anti flicker", AntiFlicker::getName);
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
	 * Sends a pan/tilt drive command to the device with the specified speed values.
	 * @param command   the {@link PanTiltDrive} command to execute (e.g. UP, DOWN, LEFT, RIGHT, STOP)
	 * @param panSpeed  the speed value for the pan axis
	 * @param tiltSpeed the speed value for the tilt axis
	 * @throws IOException if an I/O error occurs while writing to the output stream
	 */
	private void sendPanTiltDriveCommand(PanTiltDrive command, int panSpeed, int tiltSpeed) throws IOException {
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			outputStream.write(new byte[]{(byte) panSpeed, (byte) tiltSpeed});
			outputStream.write(command.getCode());
			performControl(PayloadCategory.PAN_TILTER, Command.PAN_TILT_DRIVE, outputStream.toByteArray());
		}
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
	private Object digestResponse(byte[] response, int sequenceNum, CommandType commandType, Command expectedCommand) {
		ResponseValidator validator = new ResponseValidator(this.host, this.port);
		validator.validate(response, sequenceNum, PayloadType.REPLY);

		int payloadLength = response[3];
		byte[] reply = Arrays.copyOfRange(response, 8, 8 + payloadLength);

		if (commandType == CommandType.COMMAND) {
			if (!Arrays.equals(ReplyStatus.COMPLETION.getCode(), reply)) {
				this.logger.error("error: Unexpected completion packet: " + this.host + " port: " + this.port);
				throw new IllegalStateException("Unexpected completion packet");
			}
			return null;
		}

		ResponseParser parser = new ResponseParser();
		return parser.parse(expectedCommand, reply);
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

	/**
	 * Updates devices control value, after the control command was executed with the specified value.
	 *
	 * @param stats the stats are list of Statistics
	 * @param advancedControllableProperties the advancedControllableProperty are AdvancedControllableProperty instance
	 * @param name of the control property
	 * @param value to set to the control property
	 */
	private void updateLocalControlValue(Map<String, String> stats, List<AdvancedControllableProperty> advancedControllableProperties, String name, String value) {
		stats.put(name, value);
		advancedControllableProperties.stream().filter(advancedControllableProperty ->
				name.equals(advancedControllableProperty.getName())).findFirst().ifPresent(advancedControllableProperty ->
				advancedControllableProperty.setValue(value));
	}

	/**
	 * Converts a float value into two-byte representation following the 0p 0q protocol format.
	 * @param value The float value to convert
	 * @param maxRange The maximum valid value for this control
	 * @return A byte array of length 2, representing {p, q}
	 */
	private static byte[] convertToTwoBytePQ(float value, int maxRange) {
		int intValue = Math.round(value);
		if (value < 0 || value > maxRange) {
			throw new IllegalArgumentException("Value must be in range 0–14");
		}
		byte p = (byte) ((intValue >> 4) & 0x0F);
		byte q = (byte) (intValue & 0x0F);
		return new byte[]{p, q};
	}

	/**
	 * Calculates the new zoom code based on the current zoom position and the given direction.
	 *
	 * @param zoomPosition the name of the current zoom position (case-insensitive match with {@link ZoomPosition} enum)
	 * @param direction the direction of zoom adjustment, expected to be either {@link ZoomControl#TELE} or {@link ZoomControl#WIDE}
	 * @return an {@link Optional} containing the new zoom code as a byte array if the current position is valid; otherwise, {@link Optional#empty()}
	 */
	private Optional<byte[]> calculateNewZoomCode(String zoomPosition, String direction) {
		OptionalInt currentIndexOpt = IntStream.range(0, ZoomPosition.values().length)
				.filter(i -> ZoomPosition.values()[i].getName().equalsIgnoreCase(zoomPosition))
				.findFirst();

		if (!currentIndexOpt.isPresent() && logger.isWarnEnabled()) {
			this.logger.warn("Zoom position not found in enum: " + zoomPosition);
			return Optional.empty();
		}

		int currentIndex = currentIndexOpt.getAsInt();
		int targetIndex = currentIndex;

		boolean isTele = Objects.equals(direction, ZoomControl.TELE.getName());
		boolean isWide = Objects.equals(direction, ZoomControl.WIDE.getName());

		if (isTele && currentIndex < ZoomPosition.values().length - 1) {
			targetIndex++;
		} else if (isWide && currentIndex > 0) {
			targetIndex--;
		}

		return Optional.of(ZoomPosition.values()[targetIndex].getCode());
	}

	/**
	 * Helper method to parse string to int with default is null.
	 */
	private Integer tryParseInteger(String value, String fieldName, StringBuilder errorMessages) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			errorMessages.append(fieldName)
					.append(" with value '").append(value)
					.append("' is not a valid number. ");
			return null;
		}
	}

	/**
	 * Helper method to parse string to int with default is 1.
	 */
	private int parseIntOrDefault(String value, String fieldName, StringBuilder errorMessages) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			errorMessages.append(fieldName)
					.append(" with value '")
					.append(value)
					.append("' is not a valid number. ");
			return 1;
		}
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
				value);
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
		Button button = new Button();
		button.setLabel(label);
		button.setLabelPressed("Running...");
		button.setGracePeriod(0L);

		return new AdvancedControllableProperty(name, new Date(), button, LumenVCTR60AConstants.NOT_AVAILABLE);
	}

	/**
	 * Create a switch controllable property
	 *
	 * @param name name of the switch
	 * @param status initial switch state (0|1)
	 * @return AdvancedControllableProperty button instance
	 */
	private AdvancedControllableProperty createSwitch(String name, int status, String labelOff, String labelOn) {
		Switch toggle = new Switch();
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
		Slider slider = new Slider();
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

			String propertyValue = StringUtils.isNotNullOrEmpty(value) ? value : LumenVCTR60AConstants.NOT_AVAILABLE;
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
		DropDown dropDown = new DropDown();
		dropDown.setOptions(values.toArray(new String[0]));
		dropDown.setLabels(values.toArray(new String[0]));

		return new AdvancedControllableProperty(name, new Date(), dropDown, initialValue);
	}
	//--------------------------------------------------------------------------------------------------------------------------------
	//endregion
}
