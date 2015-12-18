//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.spacecode.sdk.device.module;

import com.spacecode.sdk.SmartLogger;
import com.spacecode.sdk.device.Device;
import com.spacecode.sdk.device.DeviceCreationException;
import com.spacecode.sdk.device.Device.EventDispatcher;
import com.spacecode.sdk.device.module.Module;
import com.yoctopuce.YoctoAPI.YAPI;
import com.yoctopuce.YoctoAPI.YAPI_Exception;
import com.yoctopuce.YoctoAPI.YTemperature;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

public final class TemperatureProbe extends Module {
    public static final double ERROR_VALUE = 777.0D;
    public static final double MIN_MEASURE_DELTA = 0.1D;
    public static final int MIN_MEASURE_DELAY = 20;
    private YTemperature _sensor;
    private Timer _measurementTimer;
    private double _lastValue = 777.0D;
    private int _delay;
    private double _eventDelta;

    public TemperatureProbe(String serialNumber, Device listeningDevice, EventDispatcher eventDispatcher, int delay, double eventDelta) throws DeviceCreationException {
        super(serialNumber, listeningDevice, eventDispatcher);
        this._eventDelta = Math.max(eventDelta, 0.1D);
        this._delay = Math.max(delay, 20);

        try {
            YAPI.RegisterHub("127.0.0.1");
        } catch (YAPI_Exception var9) {
            String errorMsg = "Unable to connect to VirtualHub.";
            SmartLogger.getLogger().log(Level.SEVERE, errorMsg, var9);
            throw new DeviceCreationException(errorMsg);
        }

        this._sensor = YTemperature.FirstTemperature();
        if(this._sensor == null) {
            throw new DeviceCreationException("No USB module found.");
        } else {
            this.initializeTimer((double)this._delay);
        }
    }

    private void initializeTimer(double delay) {
        this._measurementTimer = new Timer();
        this._measurementTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                TemperatureProbe.this.readMeasureRaiseEvent();
            }
        }, 0L, (long)((int)(delay * 1000.0D)));
    }

    public double getTemperature() {
        if(!this._sensor.isOnline()) {
            return 777.0D;
        } else {
            try {
                double ye = this._sensor.get_currentValue();
                return ye == -1.79769313486231E308D?777.0D:ye;
            } catch (YAPI_Exception var3) {
                SmartLogger.getLogger().log(Level.SEVERE, "Exception occurred while trying to get last measure.", var3);
                return 777.0D;
            }
        }
    }

    public void release() {
        this._measurementTimer.cancel();
        YAPI.FreeAPI();
    }

    private void readMeasureRaiseEvent() {
        double temperature = this.getTemperature();
        if(temperature == 777.0D) {
            this._deviceEventDispatcher.eventTemperatureMeasure(777.0D);
        } else {
            double variationSinceLastValue = Math.abs(temperature - this._lastValue);
            if(variationSinceLastValue > this._eventDelta) {
                if(this._lastValue != 777.0D && variationSinceLastValue > 30.0D && this._delay <= 300) {
                    SmartLogger.getLogger().severe("Aberrant variation sent by Probe (+/- " + variationSinceLastValue + "), ignored.");
                    return;
                }

                this._deviceEventDispatcher.eventTemperatureMeasure(temperature);
                this._lastValue = temperature;
            }

        }
    }

    public static class Settings {
        private int _delay;
        private double _delta;
        private boolean _enabled;

        public Settings(int delay, double delta, boolean enabled) {
            this._delay = delay;
            this._delta = delta;
            this._enabled = enabled;
        }

        public int getDelay() {
            return this._delay;
        }

        public double getDelta() {
            return this._delta;
        }

        public boolean isEnabled() {
            return this._enabled;
        }
    }
}
