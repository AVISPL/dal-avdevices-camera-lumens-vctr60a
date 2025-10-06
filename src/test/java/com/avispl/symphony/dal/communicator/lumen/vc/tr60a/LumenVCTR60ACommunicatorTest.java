/*
 * Copyright (c) 2025 AVI-SPL Inc. All Rights Reserved.
 */
package com.avispl.symphony.dal.communicator.lumen.vc.tr60a;

import static com.avispl.symphony.dal.communicator.lumen.vc.tr60a.LumenVCTR60AConstants.HASH;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Assertions;
import org.junit.rules.ExpectedException;

import com.avispl.symphony.api.dal.dto.control.AdvancedControllableProperty;
import com.avispl.symphony.api.dal.dto.monitor.ExtendedStatistics;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.Index;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.StatisticsProperty;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.command.Command;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.enums.payload.param.PresetControl;
import com.avispl.symphony.dal.communicator.lumen.vc.tr60a.interfaces.RealDeviceTest;

/**
 * Unit test for AverPTZ Communicator
 * Send success packet by override method read and doneReading
 *
 * @author Harry
 * @version 1.0
 * @since 1.0
 */

public class LumenVCTR60ACommunicatorTest {
	private LumenVCTR60ACommunicator averPTZCommunicator;
	private ExtendedStatistics extendedStatistic;
	private List<AdvancedControllableProperty> advancedControllableProperties;
	private Map<String, String> stats;

	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		averPTZCommunicator = new LumenVCTR60ACommunicator();
		averPTZCommunicator.setHost("");
		averPTZCommunicator.setPort(0);
		averPTZCommunicator.setLogin("");
		averPTZCommunicator.setPassword("");
		averPTZCommunicator.init();
		averPTZCommunicator.connect();
	}

	@After
	public void destroy() {
		averPTZCommunicator.disconnect();
	}

	@Test
	public void testGetMultipleProperties() throws Exception{
		ExtendedStatistics extendedStatistics = (ExtendedStatistics) averPTZCommunicator.getMultipleStatistics().get(0);
		Map<String, String> stats = extendedStatistics.getStatistics();
		List<AdvancedControllableProperty> control = extendedStatistics.getControllableProperties();
		System.out.println("stats: " + stats);
		System.out.println("control: " + control);
		Assertions.assertEquals("VRK201", stats.get("FirmwareVersion"));
	}

	/**
	 * Test AxisCommunicator#getMultipleStatistics success
	 * Expect device info get data success
	 */
	@Test
	@Category(RealDeviceTest.class)
	public void testAverPTZCommunicatorDeviceInfo() throws Exception {
		extendedStatistic = (ExtendedStatistics) averPTZCommunicator.getMultipleStatistics().get(0);
		stats = extendedStatistic.getStatistics();

		Assert.assertEquals("AVer Information Co.", stats.get(StatisticsProperty.DEVICE_INFORMATION.getName() + HASH + StatisticsProperty.DEVICE_MFG.getName()));
		Assert.assertEquals("PTZ330", stats.get(StatisticsProperty.DEVICE_INFORMATION.getName() + HASH + StatisticsProperty.DEVICE_MODEL.getName()));
		Assert.assertEquals("5310505800460", stats.get(StatisticsProperty.DEVICE_INFORMATION.getName() + HASH + StatisticsProperty.DEVICE_SERIAL_NUMBER.getName()));
		Assert.assertEquals("0.0.0003.72", stats.get(StatisticsProperty.DEVICE_INFORMATION.getName() + HASH + StatisticsProperty.DEVICE_FIRMWARE_VERSION.getName()));
		int preset = Integer.parseInt(stats.get(Command.PRESET.getName() + HASH + Index.ONE.getName() + PresetControl.LAST_PRESET_RECALLED.getName()));
		Assert.assertTrue(preset >= 0 && preset <= 255);
	}

	/**
	 * Test AxisCommunicator#getMultipleStatistics success
	 * Expect power status get data success
	 */
	@Test
	@Category(RealDeviceTest.class)
	public void testAverPTZCommunicatorPowerStatus() throws Exception {
		// Merge test power on/off into 1 test case because if we turn off -> on, it needs to wait about 1min to reboot -> cannot test other testcases.
		extendedStatistic = (ExtendedStatistics) averPTZCommunicator.getMultipleStatistics().get(0);
		advancedControllableProperties = extendedStatistic.getControllableProperties();

		for (AdvancedControllableProperty property : advancedControllableProperties) {
			String propertyName = property.getName();
			if (propertyName.equalsIgnoreCase(Command.POWER.getName())) {
				Assert.assertTrue((int) property.getValue() == 1 || (int) property.getValue() == 0);
				return;
			}
		}
	}
}
