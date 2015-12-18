//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.spacecode.sdk.device;

import com.spacecode.sdk.SmartLogger;
import com.spacecode.sdk.device.data.*;
import com.spacecode.sdk.device.event.AccessControlEventHandler;
import com.spacecode.sdk.device.event.AccessModuleEventHandler;
import com.spacecode.sdk.device.event.BasicEventHandler;
import com.spacecode.sdk.device.event.DeviceEventHandler;
import com.spacecode.sdk.device.event.DoorEventHandler;
import com.spacecode.sdk.device.event.LedEventHandler;
import com.spacecode.sdk.device.event.MaintenanceEventHandler;
import com.spacecode.sdk.device.event.ScanEventHandler;
import com.spacecode.sdk.device.event.TemperatureEventHandler;
import com.spacecode.sdk.device.module.Module;
import com.spacecode.sdk.device.module.TemperatureProbe;
import com.spacecode.sdk.device.module.authentication.AuthenticationModule;
import com.spacecode.sdk.device.module.authentication.BadgeReader;
import com.spacecode.sdk.device.module.authentication.FingerprintReader;
import com.spacecode.sdk.device.module.authentication.FingerprintReaderException;
import com.spacecode.sdk.user.User;
import com.spacecode.sdk.user.UsersService;
import com.spacecode.sdk.user.data.AccessType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class Device {
    private RfidReaderBoard _readerBoard;
    protected String _serialNumber;
    protected DeviceType _deviceType;
    protected final List<DeviceEventHandler> _eventHandlers;
    private final Device.EventDispatcher _eventDispatcher;
    protected DeviceStatus _status;
    private byte _numberOfAxis;
    private byte _currentAxis;
    protected final Map<String, Byte> _tagToAxis;
    private Inventory _currentInventory;
    private final Device.TagRewriter _tagRewriter;
    protected List<Module> _modules;
    private final UsersService _usersService;
    protected boolean _isLastScanManual;
    private User _lastScanInitiatingUser;
    private AccessType _lastAccessType;
    private int _delayMsBeforeOpenDoorAlert;
    private final Object _doorOpenSync;
    private boolean _isDoorOpen;
    private DoorInfo _doorUsed;

    public Device(String serialNumber) throws DeviceCreationException {
        this._deviceType = DeviceType.UNKNOWN;
        this._eventHandlers = new CopyOnWriteArrayList();
        this._eventDispatcher = new Device.EventDispatcher();
        this._status = DeviceStatus.NOT_READY;
        this._tagToAxis = new LinkedHashMap();
        this._currentInventory = new Inventory();
        this._tagRewriter = new Device.TagRewriter();
        this._modules = new ArrayList();
        this._usersService = new UsersService(this._modules);
        this._isLastScanManual = true;
        this._delayMsBeforeOpenDoorAlert = 30000;
        this._doorOpenSync = new Object();
        this.set_isDoorOpen(false);
        if(serialNumber != null && !serialNumber.trim().isEmpty()) {
            this._serialNumber = serialNumber;
            Map serialToPort = getPluggedDevices();
            if(!serialToPort.containsKey(serialNumber)) {
                throw new DeviceCreationException("Device " + serialNumber + " could not be found.");
            } else {
                this.initializeReaderBoard(((PluggedDevice)serialToPort.get(serialNumber)).getSerialPort());
                this.finalizeInitialization();
            }
        } else {
            throw new DeviceCreationException("Invalid parameter: serial number.");
        }
    }

    public Device(String serialNumber, String serialPortName) throws DeviceCreationException {
        this._deviceType = DeviceType.UNKNOWN;
        this._eventHandlers = new CopyOnWriteArrayList();
        this._eventDispatcher = new Device.EventDispatcher();
        this._status = DeviceStatus.NOT_READY;
        this._tagToAxis = new LinkedHashMap();
        this._currentInventory = new Inventory();
        this._tagRewriter = new Device.TagRewriter();
        this._modules = new ArrayList();
        this._usersService = new UsersService(this._modules);
        this._isLastScanManual = true;
        this._delayMsBeforeOpenDoorAlert = 30000;
        this._doorOpenSync = new Object();
        this.set_isDoorOpen(false);
        if(serialPortName != null && !serialPortName.trim().isEmpty()) {
            this.initializeReaderBoard(serialPortName);
            this.finalizeInitialization();
            this._serialNumber = this._readerBoard.getSerialNumber();
            if(serialNumber != null && !serialNumber.trim().isEmpty() && !this._serialNumber.equals(serialNumber)) {
                throw new DeviceCreationException("The device connected to " + serialPortName + " is: " + this._serialNumber);
            }
        } else {
            throw new DeviceCreationException("Invalid parameter: serialPortName.");
        }
    }

    protected Device() {
        this._deviceType = DeviceType.UNKNOWN;
        this._eventHandlers = new CopyOnWriteArrayList();
        this._eventDispatcher = new Device.EventDispatcher();
        this._status = DeviceStatus.NOT_READY;
        this._tagToAxis = new LinkedHashMap();
        this._currentInventory = new Inventory();
        this._tagRewriter = new Device.TagRewriter();
        this._modules = new ArrayList();
        this._usersService = new UsersService(this._modules);
        this._isLastScanManual = true;
        this._delayMsBeforeOpenDoorAlert = 30000;
        this._doorOpenSync = new Object();
        this.set_isDoorOpen(false);
    }

    private void finalizeInitialization() {
        this.initializeDeviceType();
        this._numberOfAxis = this.getNumberOfAxis();
        this.updateStatus(DeviceStatus.READY);
    }

    private void initializeReaderBoard(String comPort) throws DeviceCreationException {
        try {
            this._readerBoard = new RfidReaderBoard(this._eventDispatcher, comPort);
        } catch (InterruptedException | SerialPortException var3) {
            throw new DeviceCreationException("Unexpected error on RS232 communication.", var3);
        } catch (TimeoutException var4) {
            throw new DeviceCreationException("Device did not answer.", var4);
        }
    }

    private void initializeDeviceType() {
        switch(this._readerBoard.getHardwareMajorVersion()) {
            case 1:
            case 4:
                this._deviceType = DeviceType.SMARTBOX;
                break;
            case 2:
            case 3:
                this._deviceType = DeviceType.SMARTCABINET;
                break;
            case 5:
                this._deviceType = DeviceType.SMARTSTATION;
                break;
            case 6:
                this._deviceType = DeviceType.SMARTBOARD;
                break;
            case 7:
                this._deviceType = DeviceType.SAS;
                break;
            case 8:
            case 10:
                this._deviceType = DeviceType.SMARTFRIDGE;
                break;
            case 9:
                this._deviceType = DeviceType.SMARTDRAWER;
                break;
            case 11:
                this._deviceType = DeviceType.SMARTPAD;
                break;
            case 12:
                this._deviceType = DeviceType.SMARTRACK;
                break;
            default:
                this._deviceType = DeviceType.UNKNOWN;
        }

    }

    private byte getNumberOfAxis() {
        byte nbOfAxis = this._readerBoard.askNumberOfAxis();
        return nbOfAxis == 0?this.getNumberOfAxisFromType():nbOfAxis;
    }

    private byte getNumberOfAxisFromType() {
        switch(this._deviceType.ordinal()) {
            case 1:
                return (byte)4;
            case 2:
            case 3:
            case 4:
                return (byte)3;
            case 5:
            case 6:
                return (byte)1;
            default:
                return (byte)1;
        }
    }

    protected void updateStatus(DeviceStatus status) {
        DeviceStatus oldStatus = this._status;
        this._status = status;
        if(oldStatus != this._status) {
            this._eventDispatcher.eventStatusChanged(status);
        }

    }

    public static Map<String, PluggedDevice> getPluggedDevices() {
        List ports = Arrays.asList(SerialPortList.getPortNames());
        if(ports.isEmpty()) {
            return new HashMap();
        } else {
            HashMap pluggedDevicesInfo = new HashMap();
            Iterator i$ = ports.iterator();

            while(i$.hasNext()) {
                String portName = (String)i$.next();
                Device tempDevice = null;

                try {
                    tempDevice = new Device((String)null, portName);
                } catch (DeviceCreationException var9) {
                    SmartLogger.getLogger().log(Level.OFF, "", var9);
                    continue;
                } finally {
                    if(tempDevice != null) {
                        tempDevice.release();
                    }

                }

                PluggedDevice pluggedDevice = new PluggedDevice(tempDevice.getSerialNumber(), portName, tempDevice.getSoftwareVersion(), tempDevice.getHardwareVersion(), tempDevice.getDeviceType());
                pluggedDevicesInfo.put(tempDevice.getSerialNumber(), pluggedDevice);
            }

            return pluggedDevicesInfo;
        }
    }


    public String getSerialNumber() {
        return this._serialNumber;
    }

    public void addListener(DeviceEventHandler eventHandler) {
        if(eventHandler != null && !this._eventHandlers.contains(eventHandler)) {
            this._eventHandlers.add(eventHandler);
        }

    }

    public void removeListener(DeviceEventHandler eventHandler) {
        if(eventHandler != null && this._eventHandlers.contains(eventHandler)) {
            this._eventHandlers.remove(eventHandler);
        }

    }

    public void requestScan(ScanOption... options) {
        this._isLastScanManual = true;
        this.scan(options);
    }

    private void scan(ScanOption... options) {
        if(this._status == DeviceStatus.LED_ON || this._readerBoard.isLightingProcessAlive()) {
            this.stopLightingTagsLed();
        }

        this._readerBoard.startScan(options);
    }

    public boolean stopScan() {
        return this._status == DeviceStatus.SCANNING && this._readerBoard.stopScan();
    }

    public DeviceStatus getStatus() {
        return this._status;
    }

    public void setStatus(DeviceStatus status) {
        if(status != null) {
            this.updateStatus(status);
        }

    }

    public Map<String, Byte> getTagToAxis() {
        return new HashMap(this._tagToAxis);
    }

    public void release() {
        this._readerBoard.release();
        Iterator i$ = this._modules.iterator();

        while(i$.hasNext()) {
            Module module = (Module)i$.next();
            module.release();
        }

        this._modules.clear();
        this._eventDispatcher.eventDeviceDisconnected(false);
    }

    public boolean startLightingTagsLed(List<String> tagsUid) {
        if(tagsUid != null && !tagsUid.isEmpty() && this._status == DeviceStatus.READY && !this._readerBoard.isLightingProcessAlive()) {
            final ArrayList tagsLeft = new ArrayList(tagsUid);
            final ArrayList axisNotEmpty = new ArrayList();
            Iterator i = this._tagToAxis.entrySet().iterator();

            while(i.hasNext()) {
                Entry tagEntry = (Entry)i.next();
                if(tagsLeft.contains(tagEntry.getKey()) && !axisNotEmpty.contains(tagEntry.getValue())) {
                    axisNotEmpty.add(tagEntry.getValue());
                }
            }

            for(byte var6 = 1; var6 < this._numberOfAxis + 1; ++var6) {
                if(!this._readerBoard.unlockAxis(var6)) {
                    return false;
                }
            }

            ActionsPool.getService().submit(new Runnable() {
                public void run() {
                    Device.this.confirmTagsAndStartLighting(axisNotEmpty, tagsLeft);
                }
            });
            return true;
        } else {
            return false;
        }
    }

    private void confirmTagsAndStartLighting(List<Byte> axisNotEmpty, List<String> tagsLeft) {
        Iterator nbTagsLeft = axisNotEmpty.iterator();

        byte ie;
        while(nbTagsLeft.hasNext()) {
            ie = ((Byte)nbTagsLeft.next()).byteValue();
            this._readerBoard.confirmAndLight(ie, tagsLeft);
        }

        int var6 = tagsLeft.size();
        if(var6 > 0) {
            for(ie = 1; ie < this._numberOfAxis + 1; ++ie) {
                if(!axisNotEmpty.contains(Byte.valueOf(ie))) {
                    this._readerBoard.confirmAndLight(ie, tagsLeft);
                    if(var6 > tagsLeft.size()) {
                        axisNotEmpty.add(Byte.valueOf(ie));
                        var6 = tagsLeft.size();
                    }

                    if(tagsLeft.isEmpty()) {
                        break;
                    }
                }
            }
        }

        Collections.sort(axisNotEmpty);
        this._readerBoard.startContinuousLighting(axisNotEmpty);

        try {
            Thread.sleep(50L);
        } catch (InterruptedException var5) {
            SmartLogger.getLogger().log(Level.WARNING, "Interrupted while waiting for post-lighting delay", var5);
        }

        this._eventDispatcher.eventLightingStarted(tagsLeft);
    }

    public boolean stopLightingTagsLed() {
        if(this._readerBoard.stopContinuousLighting()) {
            this.updateStatus(DeviceStatus.READY);
            this._eventDispatcher.eventLightingStopped();
            return true;
        } else {
            return false;
        }
    }

    public RewriteUidResult rewriteUid(String oldUid, String newUid) {
        String octalOldUid;
        String octalNewUid;
        byte[] newUidOctalBytes;
        if(this._readerBoard.getSoftwareMajorVersion() < 3) {
            if(!TagUidHandler.isValidAlphanumericUid(newUid)) {
                return RewriteUidResult.NEW_UID_INVALID;
            }

            octalOldUid = oldUid.startsWith("@")?TagUidHandler.getBlankE2PureOctalUid(oldUid):TagUidHandler.alphanumericE2UidToOctal(oldUid);
            octalNewUid = TagUidHandler.alphanumericE2UidToOctal(newUid);
            newUidOctalBytes = TagUidHandler.expandFullUid(TagUidHandler.simpleToCompleteOctalUid(octalNewUid, true), 45);
        } else {
            if(!TagUidHandler.isValidAlphanumericUidFullMemory(newUid)) {
                return RewriteUidResult.NEW_UID_INVALID;
            }

            octalOldUid = oldUid;
            octalNewUid = TagUidHandler.displayedToFullOctalUid(newUid);
            if(octalNewUid == null) {
                return RewriteUidResult.NEW_UID_INVALID;
            }

            newUidOctalBytes = new byte[45];

            int newUid128bits;
            for(newUid128bits = 0; newUid128bits < octalNewUid.length(); ++newUid128bits) {
                newUidOctalBytes[newUid128bits] = (byte)(octalNewUid.charAt(newUid128bits) - 48);
            }

            for(newUid128bits = octalNewUid.length(); newUid128bits < newUidOctalBytes.length; ++newUid128bits) {
                newUidOctalBytes[newUid128bits] = 0;
            }

            octalNewUid = newUid;
        }

        int[] var8 = TagUidHandler.octalBytesUidTo128BitsWord(newUidOctalBytes);
        Byte axis = (Byte)this._tagToAxis.get(oldUid);
        return this.tryRewritingUid(octalOldUid, octalNewUid, var8, axis);
    }

    protected RewriteUidResult tryRewritingUid(String octalOldUID, String octalNewUID, int[] newUID128bits, Byte axis) {
        return this._tagRewriter.rewrite3D(this._readerBoard, this._numberOfAxis, octalOldUID, octalNewUID, newUID128bits, axis);
    }

    public Inventory getLastInventory() {
        return this._currentInventory;
    }

    public void setLastInventory(Inventory inventory) {
        this._currentInventory = inventory;
    }

    public String getSoftwareVersion() {
        return String.format("%d.%d", new Object[]{Short.valueOf(this._readerBoard.getSoftwareMajorVersion()), Short.valueOf(this._readerBoard.getSoftwareMinorVersion())});
    }

    public String getHardwareVersion() {
        return String.format("%d.%d", new Object[]{Short.valueOf(this._readerBoard.getHardwareMajorVersion()), Short.valueOf(this._readerBoard.getHardwareMinorVersion())});
    }

    public DeviceType getDeviceType() {
        return this._deviceType;
    }

    public UsersService getUsersService() {
        return this._usersService;
    }

    public boolean addFingerprintReader(String serialNumber, boolean isMaster) {
        if(serialNumber != null && !serialNumber.trim().isEmpty()) {
            Iterator fre = this._modules.iterator();

            Module module;
            do {
                if(!fre.hasNext()) {
                    try {
                        this._modules.add(new FingerprintReader(serialNumber, this, this._eventDispatcher, isMaster));
                        return true;
                    } catch (FingerprintReaderException var5) {
                        SmartLogger.getLogger().log(Level.SEVERE, "Unable to add a Fingerprint Reader to the device.", var5);
                        return false;
                    }
                }

                module = (Module)fre.next();
                if(serialNumber.equals(module.getSerialNumber())) {
                    return false;
                }
            } while(!isMaster || !(module instanceof FingerprintReader) || !((FingerprintReader)module).isMaster());

            return false;
        } else {
            return false;
        }
    }

    public boolean addBadgeReader(String serialPortName, boolean isMaster) {
        if(serialPortName != null && !serialPortName.trim().isEmpty()) {
            Iterator se = this._modules.iterator();

            BadgeReader currentBadgeReader;
            do {
                Module module;
                do {
                    if(!se.hasNext()) {
                        try {
                            this._modules.add(new BadgeReader("", serialPortName, this, this._eventDispatcher, isMaster));
                            return true;
                        } catch (SerialPortException var6) {
                            SmartLogger.getLogger().log(Level.WARNING, "Unable to create Badge Reader", var6);
                            return false;
                        }
                    }

                    module = (Module)se.next();
                } while(!(module instanceof BadgeReader));

                currentBadgeReader = (BadgeReader)module;
            } while(!serialPortName.equals(currentBadgeReader.getSerialPortName()) && (!isMaster || !currentBadgeReader.isMaster()));

            return false;
        } else {
            return false;
        }
    }

    public boolean addTemperatureProbe(String serialNumber, int delay, double eventDelta) {
        if(delay >= 20 && eventDelta >= 0.1D) {
            Iterator dce = this._modules.iterator();

            Module module;
            do {
                if(!dce.hasNext()) {
                    try {
                        this._modules.add(new TemperatureProbe(serialNumber, this, this._eventDispatcher, delay, eventDelta));
                        return true;
                    } catch (DeviceCreationException var7) {
                        SmartLogger.getLogger().log(Level.SEVERE, "Unable to create and add a temperature probe.", var7);
                        return false;
                    }
                }

                module = (Module)dce.next();
            } while(!module.getSerialNumber().equals(serialNumber));

            return false;
        } else {
            return false;
        }
    }

    public double getCurrentTemperature() {
        Iterator i$ = this._modules.iterator();

        Module module;
        do {
            if(!i$.hasNext()) {
                return 777.0D;
            }

            module = (Module)i$.next();
        } while(!(module instanceof TemperatureProbe));

        return ((TemperatureProbe)module).getTemperature();
    }

    public void disconnectTemperatureProbe() {
        Iterator it = this._modules.iterator();

        while(it.hasNext()) {
            Module module = (Module)it.next();
            if(module instanceof TemperatureProbe) {
                module.release();
                it.remove();
            }
        }

    }

    public boolean flashFirmware(List<String> lines) {
        this.updateStatus(DeviceStatus.FLASHING_FIRMWARE);
        FirmwareParser firmwareParser = FirmwareParser.loadHexLines(lines, false);
        if(firmwareParser == null) {
            SmartLogger.getLogger().severe("Parsing the firmware lines failed.");
            this.updateStatus(DeviceStatus.READY);
            return false;
        } else {
            boolean result = this._readerBoard.flashFirmware(firmwareParser);
            this.updateStatus(result?DeviceStatus.READY:DeviceStatus.ERROR);
            return result;
        }
    }

    public Object adminQuery(String query, Object... params) {
        byte var4 = -1;
        switch(query.hashCode()) {
            case -1545477013:
                if(query.equals("threshold")) {
                    var4 = 12;
                }
                break;
            case -854176536:
                if(query.equals("carrier_period")) {
                    var4 = 2;
                }
                break;
            case -634154081:
                if(query.equals("increase_frequency")) {
                    var4 = 5;
                }
                break;
            case -359055811:
                if(query.equals("duty_cycle")) {
                    var4 = 4;
                }
                break;
            case -256505650:
                if(query.equals("set_threshold")) {
                    var4 = 10;
                }
                break;
            case 214903044:
                if(query.equals("select_axis")) {
                    var4 = 9;
                }
                break;
            case 376676081:
                if(query.equals("axis_count")) {
                    var4 = 0;
                }
                break;
            case 395113023:
                if(query.equals("save_duty_cycle")) {
                    var4 = 7;
                }
                break;
            case 668394811:
                if(query.equals("threshold_sampling")) {
                    var4 = 11;
                }
                break;
            case 761355003:
                if(query.equals("decrease_frequency")) {
                    var4 = 3;
                }
                break;
            case 944350778:
                if(query.equals("set_duty_cycle")) {
                    var4 = 8;
                }
                break;
            case 1172093117:
                if(query.equals("set_door_state")) {
                    var4 = 6;
                }
                break;
            case 1421318634:
                if(query.equals("calibration")) {
                    var4 = 1;
                }
        }

        switch(var4) {
            case 0:
                return Byte.valueOf(this.getNumberOfAxis());
            case 1:
                short valuesCount = 256;
                byte[] fullSignalValues = new byte[valuesCount];
                short sampleIndex = 0;
                this._readerBoard.setCalibrationModeState(true);

                while(sampleIndex < valuesCount) {
                    byte[] new32Points = this._readerBoard.getCarrierSignal(sampleIndex);
                    int var21 = valuesCount - sampleIndex;
                    var21 = var21 > new32Points.length?new32Points.length:var21;
                    System.arraycopy(new32Points, 0, fullSignalValues, sampleIndex, var21);
                    sampleIndex = (short)(sampleIndex + var21);

                    try {
                        Thread.sleep(50L);
                    } catch (InterruptedException var20) {
                        this._readerBoard.setCalibrationModeState(false);
                        return null;
                    }
                }

                this._readerBoard.setCalibrationModeState(false);
                return fullSignalValues;
            case 2:
                return this._readerBoard.getCarrierFrequency();
            case 3:
                return Boolean.valueOf(this._readerBoard.decreaseCarrierFrequency());
            case 4:
                return this._readerBoard.getBridgeDutyCycle();
            case 5:
                return Boolean.valueOf(this._readerBoard.increaseCarrierFrequency());
            case 6:
                if(params != null && params.length >= 2) {
                    if(params[0] instanceof Boolean && params[1] instanceof Boolean) {
                        boolean isMaster = ((Boolean)params[0]).booleanValue();
                        boolean state = ((Boolean)params[1]).booleanValue();
                        int doorCode = isMaster?0:1;
                        return Boolean.valueOf(this._readerBoard.setDoorState((byte)doorCode, state));
                    }

                    return null;
                }

                return null;
            case 7:
                return Boolean.valueOf(this._readerBoard.saveBridgeDutyCycle());
            case 8:
                if(params != null && params.length >= 3) {
                    Object[] bridgeState = params;
                    int dcuFull = params.length;

                    for(int dcuHalf = 0; dcuHalf < dcuFull; ++dcuHalf) {
                        Object var25 = bridgeState[dcuHalf];
                        if(!(var25 instanceof Short)) {
                            return null;
                        }
                    }

                    short var22 = ((Short)params[0]).shortValue();
                    short var23 = ((Short)params[1]).shortValue();
                    short var24 = ((Short)params[2]).shortValue();
                    return Boolean.valueOf(this._readerBoard.setBridgeDutyCycle(var22, var23, var24));
                }

                return null;
            case 9:
                if(params != null && params.length >= 1) {
                    if(!(params[0] instanceof Byte)) {
                        return null;
                    }

                    byte axisNbr = ((Byte)params[0]).byteValue();
                    if(axisNbr < 1) {
                        return null;
                    }

                    return Boolean.valueOf(this._readerBoard.switchAxis(axisNbr));
                }

                return null;
            case 10:
                if(params != null && params.length >= 1) {
                    if(!(params[0] instanceof Integer) && !(params[0] instanceof Short)) {
                        return null;
                    }

                    short threshold = ((Short)params[0]).shortValue();
                    return Boolean.valueOf(this._readerBoard.setCorrelationThreshold(threshold));
                }

                return null;
            case 11:
                if(params != null && params.length >= 3) {
                    if(params[0] instanceof Integer && params[1] instanceof short[] && params[2] instanceof short[]) {
                        int numberOfSamples = ((Integer)params[0]).intValue();
                        short[] missingSamples = (short[])((short[])params[1]);
                        short[] presentSamples = (short[])((short[])params[2]);
                        return Boolean.valueOf(this._readerBoard.requestCorrelationSampleSeries(numberOfSamples, missingSamples, presentSamples));
                    }

                    return null;
                }

                return null;
            case 12:
                return Short.valueOf(this._readerBoard.getCorrelationThreshold());
            default:
                return null;
        }
    }

    public boolean is_isDoorOpen() {
        boolean ret = false ;
        synchronized (this) {
            ret = _isDoorOpen;
        }
        return ret;
    }

    public synchronized void set_isDoorOpen(boolean _isDoorOpen) {
        this._isDoorOpen = _isDoorOpen;
    }

    protected static final class TagRewriter {
        protected TagRewriter() {
        }

        RewriteUidResult rewrite3D(RfidReaderBoard readerBoard, byte numberOfAxis, String octalOldUid, String octalNewUid, int[] newUID128bits, Byte axis) {
            Byte startAxis = axis;
            if(axis == null) {
                startAxis = Byte.valueOf((byte)1);
            }

            byte nbAxisTried = 0;

            RewriteUidResult rewriteResult;
            do {
                if(startAxis.byteValue() > numberOfAxis) {
                    startAxis = Byte.valueOf((byte)1);
                }

                rewriteResult = readerBoard.rewriteBlock(octalOldUid, newUID128bits, startAxis);
                startAxis = Byte.valueOf((byte)(startAxis.byteValue() + 1));
                ++nbAxisTried;
            } while(rewriteResult != RewriteUidResult.WRITING_SUCCESS && nbAxisTried < numberOfAxis);

            try {
                Thread.sleep(100L);
            } catch (InterruptedException var11) {
                SmartLogger.getLogger().log(Level.WARNING, "Interrupted while waiting interval during rewriting operation.", var11);
            }

            return rewriteResult == RewriteUidResult.WRITING_SUCCESS?(readerBoard.isSpce2TagConfirmedOnAxis(octalNewUid, Byte.valueOf((byte)(startAxis.byteValue() - 1)).byteValue())?RewriteUidResult.WRITING_SUCCESS:RewriteUidResult.WRITING_CONFIRMATION_FAILED):rewriteResult;
        }
    }

    public class EventDispatcher {
        public EventDispatcher() {
        }

        protected void eventTagAdded(String tagUid) {
            if(!Device.this._tagToAxis.containsKey(tagUid)) {
                Device.this._tagToAxis.put(tagUid, Byte.valueOf(Device.this._currentAxis));
                Iterator i$ = Device.this._eventHandlers.iterator();

                while(i$.hasNext()) {
                    DeviceEventHandler eventHandler = (DeviceEventHandler)i$.next();
                    if(eventHandler instanceof ScanEventHandler) {
                        ((ScanEventHandler)eventHandler).tagAdded(tagUid);
                    }
                }

            }
        }

        protected void eventScanStarted() {
            synchronized (this) {
                Device.this.updateStatus(DeviceStatus.SCANNING);
            }
                Device.this._tagToAxis.clear();
            Iterator i$ = Device.this._eventHandlers.iterator();

            while(i$.hasNext()) {
                DeviceEventHandler eventHandler = (DeviceEventHandler)i$.next();
                if(eventHandler instanceof ScanEventHandler) {
                    ((ScanEventHandler)eventHandler).scanStarted();
                }
            }

        }

        protected void eventScanCompleted() {
            Device.this.updateStatus(DeviceStatus.READY);
            ArrayList allTags = new ArrayList(Device.this._tagToAxis.keySet());
            Inventory previousInventory = new Inventory(Device.this._currentInventory);
            Device.this._currentInventory = Inventory.generateLastInventory(previousInventory, allTags);
            if(!Device.this._isLastScanManual) {
                Device.this._currentInventory.setInitiatingUserName(Device.this._lastScanInitiatingUser.getUsername());
                Device.this._currentInventory.setAccessType(Device.this._lastAccessType);
                Device.this._currentInventory.setDoorIsMaster(Device.this._doorUsed);
            }

            Iterator i$ = Device.this._eventHandlers.iterator();

            while(i$.hasNext()) {
                DeviceEventHandler eventHandler = (DeviceEventHandler)i$.next();
                if(eventHandler instanceof ScanEventHandler) {
                    ((ScanEventHandler)eventHandler).scanCompleted();
                }
            }

        }

        protected void eventScanCancelledByHost() {
            Device.this.updateStatus(DeviceStatus.READY);
            Iterator i$ = Device.this._eventHandlers.iterator();

            while(i$.hasNext()) {
                DeviceEventHandler eventHandler = (DeviceEventHandler)i$.next();
                if(eventHandler instanceof ScanEventHandler) {
                    ((ScanEventHandler)eventHandler).scanCancelledByHost();
                }
            }

        }

        protected void eventScanTagConfirmation() {
        }

        protected void eventScanFailed() {
            Device.this.updateStatus(DeviceStatus.READY);
            Iterator i$ = Device.this._eventHandlers.iterator();

            while(i$.hasNext()) {
                DeviceEventHandler eventHandler = (DeviceEventHandler)i$.next();
                if(eventHandler instanceof ScanEventHandler) {
                    ((ScanEventHandler)eventHandler).scanFailed();
                }
            }

        }

        protected void eventAxisSwitched(byte newAxis) {
            Device.this._currentAxis = newAxis;
        }

        protected void eventScanCancelledByDoor() {
            Iterator i$ = Device.this._eventHandlers.iterator();

            while(i$.hasNext()) {
                DeviceEventHandler eventHandler = (DeviceEventHandler)i$.next();
                if(eventHandler instanceof DoorEventHandler) {
                    ((DoorEventHandler)eventHandler).scanCancelledByDoor();
                }
            }

        }

        protected void eventDoorOpened() {
            Device.this.updateStatus(DeviceStatus.DOOR_OPEN);

            Device.this.set_isDoorOpen(true);
            Iterator i$ = Device.this._eventHandlers.iterator();

            while(i$.hasNext()) {
                DeviceEventHandler eventHandler = (DeviceEventHandler)i$.next();
                if(eventHandler instanceof DoorEventHandler) {
                    ((DoorEventHandler)eventHandler).doorOpened();
                }
            }

            if(Device.this._delayMsBeforeOpenDoorAlert != 0) {
                Executors.newSingleThreadExecutor().submit(new Runnable() {
                    public void run() {
                        while(Device.this.is_isDoorOpen()) {
                            synchronized(Device.this._doorOpenSync) {
                                try {
                                    Device.this._doorOpenSync.wait((long)Device.this._delayMsBeforeOpenDoorAlert);
                                } catch (InterruptedException var4) {
                                    SmartLogger.getLogger().log(Level.WARNING, "Interrupted while waiting for door to be closed", var4);
                                }
                            }

                            if(Device.this.is_isDoorOpen()) {
                                EventDispatcher.this.eventDoorOpenDelay();
                            }
                        }

                    }
                });
            }
        }

        protected void eventDoorClosed() {
            if(Device.this._status == DeviceStatus.LED_ON) {
                Device.this.stopLightingTagsLed();
            }

            Device.this.updateStatus(DeviceStatus.DOOR_CLOSED);
            Device.this.set_isDoorOpen(false);
            Device.this._readerBoard.setDoorState((byte)0, false);
            Device.this._readerBoard.setDoorState((byte)1, false);
            Device.this._isLastScanManual = false;
            Device.this.scan(new ScanOption[0]);
            Iterator i$ = Device.this._eventHandlers.iterator();

            while(i$.hasNext()) {
                DeviceEventHandler eventHandler = (DeviceEventHandler)i$.next();
                if(eventHandler instanceof DoorEventHandler) {
                    ((DoorEventHandler)eventHandler).doorClosed();
                }
            }

            Device.this.updateStatus(DeviceStatus.READY);
            if(Device.this._delayMsBeforeOpenDoorAlert != 0) {
                synchronized(Device.this._doorOpenSync) {
                    Device.this._doorOpenSync.notify();
                }
            }
        }

        protected void eventDoorOpenDelay() {
            Iterator i$ = Device.this._eventHandlers.iterator();

            while(i$.hasNext()) {
                DeviceEventHandler eventHandler = (DeviceEventHandler)i$.next();
                if(eventHandler instanceof DoorEventHandler) {
                    ((DoorEventHandler)eventHandler).doorOpenDelay();
                }
            }

        }

        protected void eventDeviceDisconnected(boolean releaseDevice) {
            Device.this.updateStatus(DeviceStatus.NOT_READY);
            if(releaseDevice) {
                Device.this.release();
            } else {
                Iterator i$ = Device.this._eventHandlers.iterator();

                while(i$.hasNext()) {
                    DeviceEventHandler eventHandler = (DeviceEventHandler)i$.next();
                    if(eventHandler instanceof BasicEventHandler) {
                        ((BasicEventHandler)eventHandler).deviceDisconnected();
                    }
                }

            }
        }

        public void eventFingerTouched(boolean isMaster) {
            Iterator i$ = Device.this._eventHandlers.iterator();

            while(i$.hasNext()) {
                DeviceEventHandler eventHandler = (DeviceEventHandler)i$.next();
                if(eventHandler instanceof AccessModuleEventHandler) {
                    ((AccessModuleEventHandler)eventHandler).fingerTouched(isMaster);
                }
            }

        }

        public void eventFingerprintEnrollmentSample(byte sampleNumber) {
            Iterator i$ = Device.this._eventHandlers.iterator();

            while(i$.hasNext()) {
                DeviceEventHandler eventHandler = (DeviceEventHandler)i$.next();
                if(eventHandler instanceof AccessModuleEventHandler) {
                    ((AccessModuleEventHandler)eventHandler).fingerprintEnrollmentSample(sampleNumber);
                }
            }

        }

        public void eventBadgeReaderConnected(boolean isMaster) {
            Iterator i$ = Device.this._eventHandlers.iterator();

            while(i$.hasNext()) {
                DeviceEventHandler eventHandler = (DeviceEventHandler)i$.next();
                if(eventHandler instanceof AccessModuleEventHandler) {
                    ((AccessModuleEventHandler)eventHandler).badgeReaderConnected(isMaster);
                }
            }

        }

        public void eventBadgeReaderDisconnected(boolean isMaster) {
            Iterator i$ = Device.this._eventHandlers.iterator();

            while(i$.hasNext()) {
                DeviceEventHandler eventHandler = (DeviceEventHandler)i$.next();
                if(eventHandler instanceof AccessModuleEventHandler) {
                    ((AccessModuleEventHandler)eventHandler).badgeReaderDisconnected(isMaster);
                }
            }

        }

        public void eventBadgeScanned(String badgeNumber) {
            Iterator i$ = Device.this._eventHandlers.iterator();

            while(i$.hasNext()) {
                DeviceEventHandler eventHandler = (DeviceEventHandler)i$.next();
                if(eventHandler instanceof AccessModuleEventHandler) {
                    ((AccessModuleEventHandler)eventHandler).badgeScanned(badgeNumber);
                }
            }

        }

        public synchronized void eventAuthenticationSuccess(AuthenticationModule authenticationModule, User user) {
            Device.this._lastScanInitiatingUser = user;
            Device.this._isLastScanManual = false;


            if (Device.this._status != DeviceStatus.SCANNING && Device.this.is_isDoorOpen() == false) {
                synchronized (this){
                    Device.this._doorUsed = authenticationModule.isMaster() ? DoorInfo.DI_MASTER_DOOR : DoorInfo.DI_SLAVE_DOOR;
                    Device.this.set_isDoorOpen(Device.this._readerBoard.setDoorState((byte) (authenticationModule.isMaster() ? 0 : 1), true));
                }
            }
            Device.this._lastAccessType = authenticationModule instanceof FingerprintReader?AccessType.FINGERPRINT:(authenticationModule instanceof BadgeReader?AccessType.BADGE:AccessType.UNDEFINED);
            Iterator i$ = Device.this._eventHandlers.iterator();

            while(i$.hasNext()) {
                DeviceEventHandler eventHandler = (DeviceEventHandler)i$.next();
                if(eventHandler instanceof AccessControlEventHandler) {
                    ((AccessControlEventHandler)eventHandler).authenticationSuccess(user, Device.this._lastAccessType, authenticationModule.isMaster());
                }
            }

        }

        public void eventAuthenticationFailure(AuthenticationModule authenticationModule, User user) {
            Device.this._lastAccessType = authenticationModule instanceof FingerprintReader?AccessType.FINGERPRINT:(authenticationModule instanceof BadgeReader?AccessType.BADGE:AccessType.UNDEFINED);
            Iterator i$ = Device.this._eventHandlers.iterator();

            while(i$.hasNext()) {
                DeviceEventHandler eventHandler = (DeviceEventHandler)i$.next();
                if(eventHandler instanceof AccessControlEventHandler) {
                    ((AccessControlEventHandler)eventHandler).authenticationFailure(user, Device.this._lastAccessType, authenticationModule.isMaster());
                }
            }

        }

        public void eventTemperatureMeasure(double value) {
            Iterator i$ = Device.this._eventHandlers.iterator();

            while(i$.hasNext()) {
                DeviceEventHandler eventHandler = (DeviceEventHandler)i$.next();
                if(eventHandler instanceof TemperatureEventHandler) {
                    ((TemperatureEventHandler)eventHandler).temperatureMeasure(value);
                }
            }

        }

        protected void eventLightingStarted(List<String> tagsLeft) {
            Device.this.updateStatus(DeviceStatus.LED_ON);
            Iterator i$ = Device.this._eventHandlers.iterator();

            while(i$.hasNext()) {
                DeviceEventHandler eventHandler = (DeviceEventHandler)i$.next();
                if(eventHandler instanceof LedEventHandler) {
                    ((LedEventHandler)eventHandler).lightingStarted(tagsLeft);
                }
            }

        }

        protected void eventLightingStopped() {
            Device.this.updateStatus(DeviceStatus.READY);
            Iterator i$ = Device.this._eventHandlers.iterator();

            while(i$.hasNext()) {
                DeviceEventHandler eventHandler = (DeviceEventHandler)i$.next();
                if(eventHandler instanceof LedEventHandler) {
                    ((LedEventHandler)eventHandler).lightingStopped();
                }
            }

        }

        protected void eventStatusChanged(DeviceStatus status) {
            Iterator i$ = Device.this._eventHandlers.iterator();

            while(i$.hasNext()) {
                DeviceEventHandler eventHandler = (DeviceEventHandler)i$.next();
                if(eventHandler instanceof BasicEventHandler) {
                    ((BasicEventHandler)eventHandler).deviceStatusChanged(status);
                }
            }

        }

        protected void eventFlashingProgress(int rowNumber, int rowCount) {
            Iterator i$ = Device.this._eventHandlers.iterator();

            while(i$.hasNext()) {
                DeviceEventHandler eventHandler = (DeviceEventHandler)i$.next();
                if(eventHandler instanceof MaintenanceEventHandler) {
                    ((MaintenanceEventHandler)eventHandler).flashingProgress(rowNumber, rowCount);
                }
            }

        }

        protected void eventCorrelationSample(short correlation, short phaseShift) {
            Iterator i$ = Device.this._eventHandlers.iterator();

            while(i$.hasNext()) {
                DeviceEventHandler eventHandler = (DeviceEventHandler)i$.next();
                if(eventHandler instanceof MaintenanceEventHandler) {
                    ((MaintenanceEventHandler)eventHandler).correlationSample(correlation, phaseShift);
                }
            }

        }

        protected void eventCorrelationSampleSeries(short[] presentSamples, short[] missingSamples) {
            Iterator i$ = Device.this._eventHandlers.iterator();

            while(i$.hasNext()) {
                DeviceEventHandler eventHandler = (DeviceEventHandler)i$.next();
                if(eventHandler instanceof MaintenanceEventHandler) {
                    ((MaintenanceEventHandler)eventHandler).correlationSampleSeries(presentSamples, missingSamples);
                }
            }

        }

        private final class Door {
            public static final byte MASTER = 0;
            public static final byte SLAVE = 1;

            private Door() {
            }
        }
    }
}
