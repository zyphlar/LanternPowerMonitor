package com.lanternsoftware.pigpio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class PiGpioFactory {
	private static final Logger LOG = LoggerFactory.getLogger(PIGPIO.class);
	private static final Map<Integer, Spi> spiHandles = new HashMap<>();
	private static boolean initialized = false;

	public static Spi getSpiChannel(int _channel, int _baud, boolean _auxiliary) {
		ensureInitialized();
		int channelId = (0xff & _channel);
		if (_auxiliary)
			channelId |= 0x100;
		Spi handle = spiHandles.get(channelId);
		if (handle == null) {
			int h = PIGPIO.spiOpen(_channel, _baud, _auxiliary ? 0x100 : 0);
			if (h >= 0) {
				handle = new Spi(h);
				spiHandles.put(channelId, handle);
			}
			else {
				LOG.error("Failed to get SPI handle");
			}
		}
		return handle;
	}

	private static void ensureInitialized() {
		if (initialized)
			return;
		int init = PIGPIO.gpioInitialise();
		LOG.info("GPIO init: {}", init);
		if (init < 0)
			LOG.error("Failed to initialize PiGpio");
		else
			initialized = true;
	}

	public static void shutdown() {
		for (Spi handle : spiHandles.values()) {
			PIGPIO.spiClose(handle.getHandle());
		}
		spiHandles.clear();
		PIGPIO.gpioTerminate();
	}
}
