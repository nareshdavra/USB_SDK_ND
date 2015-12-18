//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.spacecode.sdk.device;

import com.spacecode.sdk.SmartLogger;
import com.spacecode.sdk.device.ActionsPool;
import com.spacecode.sdk.device.DatatypeConversion;
import com.spacecode.sdk.device.FirmwareParser;
import com.spacecode.sdk.device.ReaderRS232Port;
import com.spacecode.sdk.device.TagUidHandler;
import com.spacecode.sdk.device.Device.EventDispatcher;
import com.spacecode.sdk.device.FirmwareParser.RowDetails;
import com.spacecode.sdk.device.data.RewriteUidResult;
import com.spacecode.sdk.device.data.ScanOption;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import jssc.SerialPortException;

final class RfidReaderBoard {
    private final RfidReaderBoard.RfidEventHandler _eventHandler;
    private final ReaderRS232Port _rs232Port;
    private String _serialNumber;
    private short _hardwareMajorVersion;
    private short _hardwareMinorVersion;
    private short _softwareMajorVersion;
    private short _softwareMinorVersion;
    private short _correlationThreshold;
    private final BlockingQueue<RfidReaderBoard.AsyncEventMessage> _asyncEventMessagesQueue = new LinkedBlockingQueue();
    static final String HELLO_WORLD_MESSAGE = String.format("%1$s%2$04X", new Object[]{"TUNNEL|", Integer.valueOf(18519)});
    private static final short HELLO_WORLD_TIMEOUT = 500;
    private static final short SYNC_REQUEST_TIMEOUT = 2000;
    private static final short BASIC_ORDER_COMMAND_TIMEOUT = 500;
    private Future<?> _asyncEventProcess;
    private Future<?> _ledLightingProcess;
    private volatile boolean _isLighting = false;
    private static final short LED_LIGHTING_TIME_INTERVAL = 1000;
    private boolean _waitingCompletion = false;
    private String _completionResponse = "";
    private final Object _completionSync = new Object();
    private boolean _waitingBasicOrderResponse = false;
    private boolean _basicOrderResponseState = false;
    private final Object _basicOrderSync = new Object();
    private boolean _waitingRewriteResponse = false;
    private byte _rewritingResponse = 0;
    private final Object _rewritingSync = new Object();
    private final Object _confirmationSync = new Object();
    private boolean _waitingConfirmationResponse = false;
    private boolean _confirmationResponse = false;
    private final Object _flashingSync = new Object();
    private boolean _waitingFlashingResponse = false;
    private String _flashingResponse = "";
    private short[] _presentCorrelationSamples;
    private short[] _missingCorrelationSamples;

    public RfidReaderBoard(EventDispatcher eventDispatcher, String comPort) throws SerialPortException, TimeoutException, InterruptedException {
        this._eventHandler = new RfidReaderBoard.RfidEventHandler(eventDispatcher);
        this._rs232Port = new ReaderRS232Port(comPort, this);

        try {
            this.initializeComPort(comPort);
            this.initializeHelloWorld();
        } catch (TimeoutException | InterruptedException | SerialPortException var4) {
            this.closeSerialPort();
            throw var4;
        }

        this._correlationThreshold = this.requestCorrelationThreshold();
        this.enableCorrelationEvent();
        this._asyncEventProcess = ActionsPool.getService().submit(new RfidReaderBoard.AsyncEventRunnable(this._asyncEventMessagesQueue));
    }

    String getSerialNumber() {
        return this._serialNumber;
    }

    short getHardwareMajorVersion() {
        return this._hardwareMajorVersion;
    }

    short getHardwareMinorVersion() {
        return this._hardwareMinorVersion;
    }

    short getSoftwareMajorVersion() {
        return this._softwareMajorVersion;
    }

    short getSoftwareMinorVersion() {
        return this._softwareMinorVersion;
    }

    void release() {
        this.stopContinuousLighting();
        this.closeSerialPort();
        SmartLogger.getLogger().setLevel(Level.OFF);
        this._asyncEventProcess.cancel(true);
        ActionsPool.getService().shutdown();
    }

    private void closeSerialPort() {
        try {
            this._rs232Port.closePort();
        } catch (SerialPortException var2) {
            SmartLogger.getLogger().log(Level.WARNING, "SerialPortException occurred while closing serial port.", var2);
        }

    }

    private void initializeComPort(String comPort) throws SerialPortException {
        if(!this._rs232Port.openPort()) {
            throw new SerialPortException(comPort, "Open port", "");
        } else if(!this._rs232Port.setParams(115200, 8, 1, 0)) {
            throw new SerialPortException(comPort, "Set parameters", "");
        } else if(!this._rs232Port.setEventsMask(17)) {
            throw new SerialPortException(comPort, "Set event mask", "");
        } else {
            this._rs232Port.startListening();
        }
    }

    boolean sendHelloWorld() throws SerialPortException {
        return this._rs232Port.sendMessage(HELLO_WORLD_MESSAGE);
    }

    void initializeHelloWorld() throws SerialPortException, TimeoutException, InterruptedException {
        if(!this.sendHelloWorld()) {
            throw new SerialPortException(this._rs232Port.getPortName(), "Hello World", "Communication Failed");
        } else {
            try {
                Object nfe = this._completionSync;
                synchronized(this._completionSync) {
                    this._waitingCompletion = true;
                    this._completionSync.wait(500L);
                }
            } catch (InterruptedException var5) {
                SmartLogger.getLogger().log(Level.SEVERE, "Device board interrupted while waiting for the initialization answer.", var5);
                throw var5;
            }

            if(this._waitingCompletion) {
                throw new TimeoutException();
            } else if(this._completionResponse.length() < 22) {
                throw new SerialPortException(this._rs232Port.getPortName(), "Hello World", "Invalid answer: " + this._completionResponse);
            } else {
                this._serialNumber = this._completionResponse.substring(8, 10) + this._completionResponse.substring(6, 8) + this._completionResponse.substring(4, 6) + this._completionResponse.substring(2, 4);

                try {
                    this._softwareMajorVersion = Short.parseShort(this._completionResponse.substring(14, 16), 16);
                    this._softwareMinorVersion = Short.parseShort(this._completionResponse.substring(16, 18), 16);
                    this._hardwareMajorVersion = Short.parseShort(this._completionResponse.substring(18, 20), 16);
                    this._hardwareMinorVersion = Short.parseShort(this._completionResponse.substring(20, 22), 16);
                } catch (NumberFormatException var3) {
                    SmartLogger.getLogger().log(Level.SEVERE, "Invalid packet received during HelloWorld init.", var3);
                    throw new SerialPortException(this._rs232Port.getPortName(), "Hello World", "Invalid numeric data");
                }
            }
        }
    }

    private void enableCorrelationEvent() {
        try {
            this.sendSynchronousRequest(this.formatBackdoorMessage((short)6, (byte)1, '\u0000', '\u0000'));
        } catch (SerialPortException var2) {
            SmartLogger.getLogger().log(Level.SEVERE, "Serial port communication error while trying to enable Correlation event.", var2);
        } catch (TimeoutException var3) {
            SmartLogger.getLogger().log(Level.SEVERE, "Timeout occurred while trying to enable the Correlation event.", var3);
        }

    }

    void onSerialMessageReceived(String message) {
        Object packets;
        if(message.toUpperCase().startsWith("TUNNEL|")) {
            if(this._waitingCompletion) {
                packets = this._completionSync;
                synchronized(this._completionSync) {
                    this._completionResponse = message.replace("TUNNEL|", "");
                    this._waitingCompletion = false;
                    this._completionSync.notify();
                }
            }
        } else if(message.toUpperCase().startsWith("ASYNCEVENT|")) {
            String packets1 = message.substring("ASYNCEVENT|".length());
            short[] messagePackets = DatatypeConversion.stringToShortArray(packets1);
            byte msgId = 0;

            try {
                this._asyncEventMessagesQueue.put(new RfidReaderBoard.AsyncEventMessage(msgId, messagePackets));
            } catch (InterruptedException var8) {
                SmartLogger.getLogger().log(Level.WARNING, "Interrupted while inserting a new AEM in AEM queue.", var8);
            }
        } else if((message.toUpperCase().startsWith("UPDATE|") || message.toUpperCase().startsWith("DOWNLOAD|") || message.toUpperCase().startsWith("FINALIZE|")) && this._waitingFlashingResponse) {
            packets = this._flashingSync;
            synchronized(this._flashingSync) {
                this._flashingResponse = message.substring(message.indexOf(124) + 1);
                this._waitingFlashingResponse = false;
                this._flashingSync.notify();
            }
        }

    }

    private String sendSynchronousRequest(String message) throws TimeoutException, SerialPortException {
        int timeOut = message.indexOf(16964) != -1?500:2000;

        try {
            Object ie = this._completionSync;
            synchronized(this._completionSync) {
                this._rs232Port.sendMessage(message);
                this._waitingCompletion = true;
                this._completionSync.wait((long)timeOut);
            }
        } catch (InterruptedException var6) {
            SmartLogger.getLogger().log(Level.WARNING, "Device board interrupted while waiting for a synchronous answer.", var6);
            return "";
        }

        if(this._waitingCompletion) {
            throw new TimeoutException();
        } else {
            return this._completionResponse;
        }
    }

    private String formatBackdoorMessage(short backdoorCommandType, byte option1, char option2, char option3) {
        return String.format("%1$s%2$04X%3$02X%4$02X%5$04X%6$04X", new Object[]{"TUNNEL|", Integer.valueOf(16964), Short.valueOf(backdoorCommandType), Byte.valueOf(option1), Integer.valueOf((char)(option2 >> 8 | option2 << 8)), Integer.valueOf((char)(option3 >> 8 | option3 << 8))});
    }

    private String formatBackdoorMessage(short backdoorCommandType, byte option1, long word1, long word2) {
        return String.format("%1$s%2$04X%3$02X%4$02X%5$016X%6$016X", new Object[]{"TUNNEL|", Integer.valueOf(16964), Short.valueOf(backdoorCommandType), Byte.valueOf(option1), Long.valueOf(word1), Long.valueOf(word2)});
    }

    boolean sendMessageAndCheckResponse(String message) throws TimeoutException, SerialPortException {
        return DatatypeConversion.stringToShortArray(this.sendSynchronousRequest(message))[0] == 0;
    }

    boolean stopScan() {
        try {
            return this._rs232Port.sendMessage(String.format("%s%04X%02X%02X", new Object[]{"TUNNEL|", Integer.valueOf(17231), Integer.valueOf(0), Integer.valueOf(1)}));
        } catch (SerialPortException var2) {
            SmartLogger.getLogger().log(Level.SEVERE, "Serial communication error while trying to stop scan.", var2);
            return false;
        }
    }

    void startScan(ScanOption... options) {
        short mode = RfidReaderBoard.TagScanModeFlag.CLEAR_TAG_LIST.getValue();
        short defaultMode = (short)(mode | RfidReaderBoard.TagScanModeFlag.USE_ALL_AXIS.getValue() | RfidReaderBoard.TagScanModeFlag.UNLOCK_ALL_TAG_ALL_AXIS.getValue() | RfidReaderBoard.TagScanModeFlag.USE_KR.getValue());
        if(options != null && options.length != 0) {
            List optionList = Arrays.asList(options);
            if(!optionList.contains(ScanOption.MONO_AXIS)) {
                mode |= RfidReaderBoard.TagScanModeFlag.USE_ALL_AXIS.getValue();
            }

            if(!optionList.contains(ScanOption.NO_UNLOCK)) {
                mode |= RfidReaderBoard.TagScanModeFlag.UNLOCK_ALL_TAG_ALL_AXIS.getValue();
            }

            if(!optionList.contains(ScanOption.NO_KR)) {
                mode |= RfidReaderBoard.TagScanModeFlag.USE_KR.getValue();
            }

            this.sendScanCommand(mode);
        } else {
            this.sendScanCommand(defaultMode);
        }
    }

    private void sendScanCommand(short mode) {
        String message1 = this.formatBackdoorMessage((short)16, (byte)0, '\u0000', '\u0000');
        String message2 = String.format("%1$s%2$04X%3$02X", new Object[]{"TUNNEL|", Integer.valueOf(21059), Short.valueOf(mode)});

        try {
            if(!this.sendMessageAndCheckResponse(message1)) {
                SmartLogger.getLogger().log(Level.WARNING, "Scan failed to start. Emptying inventory impossible.");
                this._eventHandler.eventScanFailed();
            }

            if(!this.sendMessageAndCheckResponse(message2)) {
                SmartLogger.getLogger().log(Level.WARNING, "Scan failed to start. Is device ready ?");
                this._eventHandler.eventScanFailed();
            }
        } catch (SerialPortException var5) {
            SmartLogger.getLogger().log(Level.WARNING, "Scan failed to start. RS232 problem.", var5);
            this._eventHandler.eventScanFailed();
        } catch (TimeoutException var6) {
            SmartLogger.getLogger().log(Level.WARNING, "Scan failed to start. Timeout problem.", var6);
            this._eventHandler.eventScanFailed();
        }

    }

    private short requestCorrelationThreshold() {
        String command = this.formatBackdoorMessage((short)11, (byte)0, '\u0000', '\u0000');

        try {
            return DatatypeConversion.stringToShortArray(this.sendSynchronousRequest(command))[1];
        } catch (SerialPortException | TimeoutException var3) {
            SmartLogger.getLogger().log(Level.SEVERE, "Unable to get correlation threshold from Device.", var3);
            return (short)0;
        }
    }

    byte askNumberOfAxis() {
        String command = this.formatBackdoorMessage((short)44, (byte)0, '\u0000', '\u0000');

        try {
            return (byte)DatatypeConversion.stringToShortArray(this.sendSynchronousRequest(command))[2];
        } catch (SerialPortException | TimeoutException var3) {
            SmartLogger.getLogger().log(Level.SEVERE, "Unable to get number of axis from Device.", var3);
            return (byte)0;
        }
    }

    boolean unlockAxis(byte axisNumber) {
        this.switchAxis(axisNumber);
        this.setAntennaState(true);
        this.sendSyncPulse();
        if(!this.sendBasicOrder((byte)11)) {
            SmartLogger.getLogger().log(Level.SEVERE, "Basic order KD was not executed by board (or board returned a result failure).");
            return false;
        } else {
            return true;
        }
    }

    boolean switchAxis(byte axisNumber) {
        this.stopField();

        try {
            if(!this.sendMessageAndCheckResponse(this.formatBackdoorMessage((short)40, (byte)1, (char)(48 + axisNumber), '\u0000'))) {
                SmartLogger.getLogger().severe("An error occurred while switching the current axis.");
                return false;
            }
        } catch (TimeoutException | SerialPortException var4) {
            SmartLogger.getLogger().log(Level.SEVERE, "A communication error occurred while trying to switch the current axis.", var4);
            return false;
        }

        try {
            Thread.sleep(50L);
        } catch (InterruptedException var3) {
            SmartLogger.getLogger().log(Level.SEVERE, "Interrupted while waiting after switching current axis.", var3);
        }

        return true;
    }

    private boolean setAntennaState(boolean state) {
        try {
            if(!this.sendMessageAndCheckResponse(this.formatBackdoorMessage((short)32, (byte)(state?1:0), '\u0000', '\u0000'))) {
                SmartLogger.getLogger().warning("Set antenna state error.");
                return false;
            } else {
                return true;
            }
        } catch (TimeoutException | SerialPortException var3) {
            SmartLogger.getLogger().log(Level.SEVERE, "Communication error while trying to set antenna state.", var3);
            return false;
        }
    }

    private void sendSyncPulse() {
        try {
            if(!this.sendMessageAndCheckResponse(this.formatBackdoorMessage((short)33, (byte)0, '\u0000', '\u0000'))) {
                SmartLogger.getLogger().warning("Sync pulse error.");
            }
        } catch (TimeoutException | SerialPortException var2) {
            SmartLogger.getLogger().log(Level.SEVERE, "Communication error while trying to synchronize axis.", var2);
        }

    }

    private boolean sendBasicOrder(byte basicOrder) {
        try {
            if(basicOrder != 12 && basicOrder != 11) {
                this._rs232Port.sendMessage(this.formatBackdoorMessage((short)34, basicOrder, '\u0000', '\u0000'));
                Object te = this._basicOrderSync;
                synchronized(this._basicOrderSync) {
                    this._waitingBasicOrderResponse = true;
                    this._basicOrderResponseState = false;
                    this._basicOrderSync.wait(500L);
                }

                if(this._waitingBasicOrderResponse) {
                    SmartLogger.getLogger().log(Level.SEVERE, "Basic order (" + basicOrder + ") request timed out.");
                    return false;
                } else {
                    return this._basicOrderResponseState;
                }
            } else if(!this.sendMessageAndCheckResponse(this.formatBackdoorMessage((short)34, basicOrder, '\u0000', '\u0000'))) {
                SmartLogger.getLogger().log(Level.SEVERE, "Basic-order (" + basicOrder + ") request failed.");
                return false;
            } else {
                return true;
            }
        } catch (SerialPortException var5) {
            SmartLogger.getLogger().log(Level.SEVERE, "RS232 error while sending basic order " + basicOrder + ".", var5);
            return false;
        } catch (InterruptedException var6) {
            SmartLogger.getLogger().log(Level.SEVERE, "Basic order request for " + basicOrder + " has been interrupted while waiting for response.", var6);
            return false;
        } catch (TimeoutException var7) {
            SmartLogger.getLogger().log(Level.SEVERE, "Timeout exception while waiting for basic-order (" + basicOrder + ") response.", var7);
            return false;
        }
    }

    void confirmAndLight(byte usedAxis, List<String> tags) {
        this.switchAxis(usedAxis);
        this.setAntennaState(true);
        this.sendSyncPulse();
        Iterator iterator = tags.iterator();

        while(iterator.hasNext()) {
            String tagUid = (String)iterator.next();
            if(this._softwareMajorVersion < 3) {
                tagUid = tagUid.startsWith("@")?TagUidHandler.getBlankE2PureOctalUid(tagUid):TagUidHandler.alphanumericE2UidToOctal(tagUid);
            }

            if(this.tryToLightTag(tagUid)) {
                iterator.remove();
            }
        }

    }

    private boolean tryToLightTag(String octalUid) {
        if(!this.sendBasicOrder((byte)13)) {
            SmartLogger.getLogger().log(Level.SEVERE, "Basic order KZ_SPCE2 was not executed by board (or board returned a result failure).");
            return false;
        } else if(!this.loadAndConfirmUid(octalUid)) {
            SmartLogger.getLogger().log(Level.SEVERE, "Unable to load or confirm tag Uid " + octalUid);
            return false;
        } else if(!this.sendBasicOrder((byte)8)) {
            SmartLogger.getLogger().log(Level.SEVERE, "Basic order KB was not executed by board (or board returned a result failure).");
            return false;
        } else if(!this.sendBasicOrder((byte)12)) {
            SmartLogger.getLogger().log(Level.SEVERE, "Basic order KL was not executed by board (or board returned a result failure).");
            return false;
        } else {
            return true;
        }
    }

    private boolean loadUidForConfirmation(String octalUid) {
        if(octalUid.isEmpty()) {
            return false;
        } else {
            char[] words = TagUidHandler.octalUidTo64BitsWord(TagUidHandler.simpleToCompleteOctalUid(octalUid, false));

            try {
                return !this.sendMessageAndCheckResponse(this.formatBackdoorMessage((short)36, (byte)1, words[3], words[2]))?false:this.sendMessageAndCheckResponse(this.formatBackdoorMessage((short)36, (byte)2, words[1], words[0]));
            } catch (TimeoutException var4) {
                SmartLogger.getLogger().log(Level.SEVERE, "Timeout exception while sending order to write tag UID to be loaded.", var4);
                return false;
            } catch (SerialPortException var5) {
                SmartLogger.getLogger().log(Level.SEVERE, "RS232 error while sending order to write tag UID to be loaded.", var5);
                return false;
            }
        }
    }

    private boolean confirmLoadedTagUid(byte nbDigits) {
        boolean confirmationResult;
        try {
            this._rs232Port.sendMessage(this.formatBackdoorMessage((short)50, (byte)(nbDigits - 1), '\u0000', '\u0000'));
            Object ie = this._confirmationSync;
            synchronized(this._confirmationSync) {
                this._waitingConfirmationResponse = true;
                this._confirmationSync.wait(500L);
            }

            if(this._waitingConfirmationResponse) {
                SmartLogger.getLogger().log(Level.WARNING, "Confirmation request timed out.");
                return false;
            }

            confirmationResult = this._confirmationResponse;
        } catch (SerialPortException var7) {
            SmartLogger.getLogger().log(Level.SEVERE, "Timeout exception while sending order to confirm loaded tag UID.", var7);
            return false;
        } catch (InterruptedException var8) {
            SmartLogger.getLogger().log(Level.WARNING, "Interrupted while waiting for confirmation response.", var8);
            return false;
        }

        try {
            Thread.sleep(100L);
            return confirmationResult;
        } catch (InterruptedException var5) {
            SmartLogger.getLogger().log(Level.SEVERE, "Interrupted while waiting for the device to complete Confirmation process.", var5);
            return false;
        }
    }

    private boolean loadFullUidForConfirmation(String octalFullUid, byte nbOfDigits) {
        long[] words = TagUidHandler.octalFullUidTo128BitsWord(octalFullUid);
        if(words.length == 0) {
            return false;
        } else {
            try {
                return this.sendMessageAndCheckResponse(this.formatBackdoorMessage((short)45, nbOfDigits, words[0], words[1]));
            } catch (TimeoutException var5) {
                SmartLogger.getLogger().log(Level.SEVERE, "Timeout exception while sending an order to load a full UID.", var5);
                return false;
            } catch (SerialPortException var6) {
                SmartLogger.getLogger().log(Level.SEVERE, "RS232 error while sending an order to load a full UID.", var6);
                return false;
            }
        }
    }

    void startContinuousLighting(List<Byte> axisList) {
        this._isLighting = true;
        this._ledLightingProcess = ActionsPool.getService().submit(new RfidReaderBoard.LedLightingRunnable(axisList));
    }

    boolean stopContinuousLighting() {
        this.stopField();
        if(this._ledLightingProcess == null) {
            return false;
        } else {
            this._isLighting = false;
            byte tries = 3;

            boolean cancellingResult;
            do {
                cancellingResult = this._ledLightingProcess.cancel(true);
                --tries;
            } while(!cancellingResult && tries > 0);

            this.stopField();
            return cancellingResult;
        }
    }

    void lightAllTagsOnCurrentAxis(byte axis, boolean changeChannel) throws InterruptedException {
        if(changeChannel) {
            this.switchAxis(axis);
        }

        this.setAntennaState(true);
        this.sendSyncPulse();
        if(!this.sendBasicOrder((byte)12)) {
            SmartLogger.getLogger().log(Level.WARNING, "Basic order KL failed in continuous lighting process.");
        }

        Thread.sleep(1000L);
        this.stopField();
        Thread.sleep(100L);
    }

    void stopField() {
        this.setAntennaState(false);
    }

    RewriteUidResult rewriteBlock(String octalOldUID, int[] newUIDBits, Byte axis) {
        if(!this.isSpce2TagConfirmedOnAxis(octalOldUID, axis.byteValue())) {
            SmartLogger.getLogger().log(Level.WARNING, "Tag to be rewritten not detected.");
            return RewriteUidResult.TAG_NOT_DETECTED;
        } else {
            int nbOfBlock = this._softwareMajorVersion < 3?3:4;

            for(byte block = 1; block < nbOfBlock + 1; ++block) {
                char high16bits = (char)(newUIDBits[block - 1] >> 16);
                char low16bits = (char)newUIDBits[block - 1];

                try {
                    this._rs232Port.sendMessage(this.formatBackdoorMessage((short)43, block, high16bits, low16bits));
                } catch (SerialPortException var14) {
                    SmartLogger.getLogger().log(Level.SEVERE, "SerialPort exception while waiting for rewriting operation response.", var14);
                    return RewriteUidResult.ERROR;
                }

                Object ie = this._rewritingSync;
                synchronized(this._rewritingSync) {
                    this._waitingRewriteResponse = true;

                    try {
                        this._rewritingSync.wait(500L);
                    } catch (InterruptedException var12) {
                        SmartLogger.getLogger().log(Level.WARNING, "Interrupted while waiting for rewriting operation response.", var12);
                        return RewriteUidResult.ERROR;
                    }
                }

                if(this._waitingRewriteResponse) {
                    SmartLogger.getLogger().log(Level.WARNING, "Rewriting request timed out.");
                    return RewriteUidResult.ERROR;
                }

                try {
                    Thread.sleep(100L);
                } catch (InterruptedException var11) {
                    SmartLogger.getLogger().log(Level.WARNING, "Interrupted while waiting interval during rewriting operation.", var11);
                }
            }

            this.stopField();
            switch(this._rewritingResponse) {
                case 0:
                    return RewriteUidResult.TAG_BLOCKED_OR_NOT_SUPPLIED;
                case 1:
                    return RewriteUidResult.TAG_NOT_SUPPLIED;
                case 2:
                    return RewriteUidResult.TAG_BLOCKED;
                case 3:
                    return RewriteUidResult.WRITING_SUCCESS;
                default:
                    return RewriteUidResult.ERROR;
            }
        }
    }

    private boolean loadAndConfirmUid(String octalUid) {
        if(octalUid != null && !"".equals(octalUid.trim())) {
            if(this._softwareMajorVersion < 3) {
                return this.loadUidForConfirmation(octalUid) && this.confirmLoadedTagUid((byte)12);
            } else {
                String octalFullUid = TagUidHandler.displayedToFullOctalUid(octalUid);
                if(octalFullUid == null) {
                    SmartLogger.getLogger().severe("Unable to confirm full UID because of an invalid conversion");
                    return false;
                } else {
                    byte nbOfDigits = (byte)(octalFullUid.startsWith("3")?12:(octalFullUid.startsWith("2")?42:18));
                    return this.loadFullUidForConfirmation(octalFullUid, nbOfDigits) && this.confirmLoadedTagUid(nbOfDigits);
                }
            }
        } else {
            return false;
        }
    }

    boolean isSpce2TagConfirmedOnAxis(String tagUid, byte axis) {
        if(!this.unlockAxis(axis)) {
            SmartLogger.getLogger().log(Level.WARNING, "Failed unlocking axis " + axis + " while checking presence of an E2 tag");
            return false;
        } else if(!this.sendBasicOrder((byte)13)) {
            SmartLogger.getLogger().log(Level.WARNING, "Basic order KZ_SPCE2 failed while checking presence of an E2 tag on axis " + axis);
            return false;
        } else {
            try {
                Thread.sleep(50L);
            } catch (InterruptedException var4) {
                var4.printStackTrace();
            }

            if(!this.loadAndConfirmUid(tagUid)) {
                SmartLogger.getLogger().log(Level.SEVERE, "Unable to confirm tag UID while checking presence of an E2 tag on axis " + axis);
                return false;
            } else {
                return true;
            }
        }
    }

    void onSerialConnectionAborted() {
        this._eventHandler.eventSerialConnectionAborted();
    }

    boolean setDoorState(byte doorType, boolean state) {
        try {
            int se = state?17493:17484;
            return this._rs232Port.sendMessage(String.format("%s%04X%02X", new Object[]{"TUNNEL|", Integer.valueOf(se), Byte.valueOf(doorType)}));
        } catch (SerialPortException var4) {
            SmartLogger.getLogger().log(Level.SEVERE, "Communication error while trying to use " + (doorType == 0?"Master":"Slave") + "door.", var4);
            return false;
        }
    }

    boolean isLightingProcessAlive() {
        return this._ledLightingProcess != null && !this._ledLightingProcess.isCancelled();
    }

    private boolean enableUpdateMode() throws SerialPortException, TimeoutException {
        this._rs232Port.sendMessage("UPDATE");

        try {
            Object ie = this._flashingSync;
            synchronized(this._flashingSync) {
                this._waitingFlashingResponse = true;
                this._flashingSync.wait(2000L);
            }
        } catch (InterruptedException var4) {
            SmartLogger.getLogger().log(Level.SEVERE, "Device board interrupted while waiting for an update request answer.", var4);
            return false;
        }

        if(this._waitingFlashingResponse) {
            throw new TimeoutException("UPDATE request");
        } else {
            return true;
        }
    }

    private boolean writeFirmwareRow(long rowIndex, String blocks) throws TimeoutException, SerialPortException {
        if(blocks != null && !blocks.trim().isEmpty()) {
            this._rs232Port.sendMessage(String.format("DOWNLOAD|%06X|", new Object[]{Long.valueOf(rowIndex)}) + blocks);

            try {
                Object ie = this._flashingSync;
                synchronized(this._flashingSync) {
                    this._waitingFlashingResponse = true;
                    this._flashingSync.wait(500L);
                }
            } catch (InterruptedException var7) {
                SmartLogger.getLogger().log(Level.SEVERE, "Device board interrupted while waiting for a DOWNLOAD request answer.", var7);
                return false;
            }

            if(this._waitingFlashingResponse) {
                throw new TimeoutException("DOWNLOAD request");
            } else {
                return this._flashingResponse != null && String.format("OK|%06X", new Object[]{Long.valueOf(rowIndex)}).equalsIgnoreCase(this._flashingResponse);
            }
        } else {
            return false;
        }
    }

    private boolean finalizeFirmwareUpdate(int firstAppRowIndex, int appRowCount, String crc) throws SerialPortException, TimeoutException {
        if(crc != null && !crc.trim().isEmpty()) {
            this._rs232Port.sendMessage(String.format("FINALIZE|%02X|%04X|%s", new Object[]{Integer.valueOf(firstAppRowIndex), Integer.valueOf(appRowCount), crc}));

            try {
                Object ie = this._flashingSync;
                synchronized(this._flashingSync) {
                    this._waitingFlashingResponse = true;
                    this._flashingSync.wait(500L);
                }
            } catch (InterruptedException var7) {
                SmartLogger.getLogger().log(Level.SEVERE, "Device board interrupted while waiting for a FINALIZE request answer.", var7);
                return false;
            }

            if(this._waitingFlashingResponse) {
                throw new TimeoutException("FINALIZE request");
            } else {
                return this._flashingResponse != null && "OK".equalsIgnoreCase(this._flashingResponse);
            }
        } else {
            SmartLogger.getLogger().severe("Flashing Firmware failed: invalid CRC");
            return false;
        }
    }

    public boolean flashFirmware(FirmwareParser firmwareParser) {
        if(firmwareParser == null) {
            return false;
        } else {
            try {
                if(!this.enableUpdateMode()) {
                    SmartLogger.getLogger().severe("Unable to enable Update Mode");
                    return false;
                }

                RowDetails te = new RowDetails();
                StringBuilder messageBuilder = new StringBuilder();
                boolean first = true;
                int rowNumber = 0;
                int rowCount = firmwareParser.getTotalRowCount();

                while(firmwareParser.enumerateRows(first, te)) {
                    ++rowNumber;
                    first = false;
                    messageBuilder.setLength(0);
                    byte[] arr$ = te.getRow();
                    int len$ = arr$.length;

                    for(int i$ = 0; i$ < len$; ++i$) {
                        byte rowByte = arr$[i$];
                        messageBuilder.append(String.format("%02X", new Object[]{Byte.valueOf(rowByte)}));
                    }

                    if(!this.writeFirmwareRow(te.getRowIndex(), messageBuilder.toString())) {
                        SmartLogger.getLogger().severe("Flashing row " + te.getRowIndex() + " failed.");
                        return false;
                    }

                    this._eventHandler.eventFlashingProgress(rowNumber, rowCount);
                }

                return this.finalizeFirmwareUpdate(72, firmwareParser.getApplicationRowCount(), firmwareParser.getCrc());
            } catch (SerialPortException var11) {
                SmartLogger.getLogger().log(Level.SEVERE, "SerialPort error when flashing the firmware...", var11);
            } catch (TimeoutException var12) {
                SmartLogger.getLogger().log(Level.SEVERE, "Timeout when flashing the firmware...", var12);
            }

            return false;
        }
    }

    void requestCorrelationSampleSeries(int numberOfSamples) {
        this._presentCorrelationSamples = new short[256];
        this._missingCorrelationSamples = new short[256];

        try {
            this._rs232Port.sendMessage(this.formatBackdoorMessage((short)3, (byte)1, (char)numberOfSamples, '\u0000'));
        } catch (SerialPortException var3) {
            SmartLogger.getLogger().log(Level.SEVERE, "Serial communication error while trying to start correlation sampling (series).", var3);
        }

    }

    public boolean requestCorrelationSampleSeries(int numberOfSamples, short[] presentCorrelationSamples, short[] missingCorrelationSamples) {
        this._presentCorrelationSamples = presentCorrelationSamples;
        this._missingCorrelationSamples = missingCorrelationSamples;

        try {
            return this._rs232Port.sendMessage(this.formatBackdoorMessage((short)3, (byte)1, (char)numberOfSamples, '\u0000'));
        } catch (SerialPortException var5) {
            SmartLogger.getLogger().log(Level.SEVERE, "Serial communication error while trying to start correlation sampling (series).", var5);
            return false;
        }
    }

    private short[] getCorrelationSeriesValues(boolean missingSamples, char groupsWithValues) {
        short[] values = missingSamples?this._presentCorrelationSamples:this._missingCorrelationSamples;
        byte requestMissing = (byte)(missingSamples?1:0);
        char groupBit = 1;

        for(char correlationOffset = 0; correlationOffset < values.length; correlationOffset = (char)(correlationOffset + 16)) {
            if((groupsWithValues & groupBit) != 0) {
                String response;
                try {
                    response = this.sendSynchronousRequest(this.formatBackdoorMessage((short)25, requestMissing, correlationOffset, '\u0000'));
                } catch (SerialPortException | TimeoutException var10) {
                    SmartLogger.getLogger().log(Level.SEVERE, "An unexpected error occurred while waiting for correlation counts", var10);
                    continue;
                }

                if(response.charAt(1) != 48) {
                    continue;
                }

                short[] messagePackets = DatatypeConversion.stringToShortArray(response.substring(4));
                if(messagePackets.length != 32) {
                    continue;
                }

                for(byte i = 0; i < 16; ++i) {
                    values[correlationOffset + i] += messagePackets[i * 2];
                }
            }

            groupBit = (char)(groupBit << 1);
        }

        return values;
    }

    short getCorrelationThreshold() {
        return this._correlationThreshold;
    }

    boolean setCorrelationThreshold(short value) {
        try {
            if(this._rs232Port.sendMessage(this.formatBackdoorMessage((short)9, (byte)value, '\u0000', '\u0000'))) {
                this._correlationThreshold = value;
                return true;
            }
        } catch (SerialPortException var3) {
            SmartLogger.getLogger().log(Level.SEVERE, "Serial communication error while setting the correlation threshold.", var3);
        }

        return false;
    }

    int[] getCarrierFrequency() {
        int[] result = new int[2];

        try {
            this.setAntennaState(true);
            String te = this.sendSynchronousRequest(this.formatBackdoorMessage((short)52, (byte)0, '\u0000', '\u0000'));
            short[] messagePackets = DatatypeConversion.stringToShortArray(te);
            result[0] = messagePackets[2] | messagePackets[3] << 8;
            result[1] = messagePackets[4];
        } catch (SerialPortException var4) {
            SmartLogger.getLogger().log(Level.SEVERE, "Serial communication error while trying to get carrier frequency.", var4);
        } catch (TimeoutException var5) {
            SmartLogger.getLogger().log(Level.SEVERE, "Timed out while trying to get carrier frequency.", var5);
        }

        this.setAntennaState(false);
        return result;
    }

    boolean increaseCarrierFrequency() {
        try {
            return this._rs232Port.sendMessage(this.formatBackdoorMessage((short)53, (byte)0, '\u0000', '\u0000'));
        } catch (SerialPortException var2) {
            SmartLogger.getLogger().log(Level.SEVERE, "Serial communication error while trying to increase carrier frequency.", var2);
            return false;
        }
    }

    boolean decreaseCarrierFrequency() {
        try {
            return this._rs232Port.sendMessage(this.formatBackdoorMessage((short)54, (byte)0, '\u0000', '\u0000'));
        } catch (SerialPortException var2) {
            SmartLogger.getLogger().log(Level.SEVERE, "Serial communication error while trying to decrease carrier frequency.", var2);
            return false;
        }
    }

    void setCalibrationModeState(boolean state) {
        try {
            this._rs232Port.sendMessage(this.formatBackdoorMessage((short)1, (byte)(state?1:0), '\u0000', '\u0000'));
        } catch (SerialPortException var3) {
            SmartLogger.getLogger().log(Level.SEVERE, "Serial communication error while trying to change calibration mode state.", var3);
        }

    }

    byte[] getCarrierSignal(short sampleIndex) {
        byte[] point32Values = new byte[32];

        try {
            String te = this.sendSynchronousRequest(this.formatBackdoorMessage((short)19, (byte)0, (char)sampleIndex, '\u0001'));
            point32Values = DatatypeConversion.stringToByteArray(te.substring(10));
        } catch (SerialPortException var4) {
            SmartLogger.getLogger().log(Level.SEVERE, "Serial communication error while trying to get carrier signal.", var4);
        } catch (TimeoutException var5) {
            SmartLogger.getLogger().log(Level.SEVERE, "Timed out while trying to get carrier signal.", var5);
        }

        return point32Values;
    }

    short[] getBridgeDutyCycle() {
        short[] packets = new short[3];

        try {
            String te = this.sendSynchronousRequest(this.formatBackdoorMessage((short)27, (byte)0, '\u0000', '\u0000'));
            short[] messagePackets = DatatypeConversion.stringToShortArray(te);
            packets[0] = messagePackets[1];
            packets[1] = (short)(messagePackets[3] << 8 | messagePackets[2]);
            packets[2] = (short)(messagePackets[5] << 8 | messagePackets[4]);
        } catch (SerialPortException var4) {
            SmartLogger.getLogger().log(Level.SEVERE, "Serial communication error while trying to get bridge state and duty cycle.", var4);
        } catch (TimeoutException var5) {
            SmartLogger.getLogger().log(Level.WARNING, "Timed out while waiting to get bridge state and duty cycle.", var5);
        }

        return packets;
    }

    boolean saveBridgeDutyCycle() {
        try {
            return this._rs232Port.sendMessage(this.formatBackdoorMessage((short)24, (byte)0, '\u0000', '\u0000'));
        } catch (SerialPortException var2) {
            SmartLogger.getLogger().log(Level.SEVERE, "Serial communication error while trying to save bridge state and duty cycles.", var2);
            return false;
        }
    }

    boolean setBridgeDutyCycle(short bridgeState, short dutyCycleFullBridge, short dutyCycleHalfBridge) {
        try {
            return this._rs232Port.sendMessage(this.formatBackdoorMessage((short)15, (byte)bridgeState, (char)dutyCycleFullBridge, (char)dutyCycleHalfBridge));
        } catch (SerialPortException var5) {
            SmartLogger.getLogger().log(Level.SEVERE, "Serial communication error while trying to set bridge state and duty cycles.", var5);
            return false;
        }
    }

    static final class BackdoorCommandType {
        static final short CALIBRATE_ANTENNA = 1;
        static final short TAG_PRESENCE_TEST = 2;
        static final short SAMPLE_CORRELATION = 3;
        static final short READ_DIGIT_TEST = 4;
        static final short NO_MODULATION_NOISE_TEST = 5;
        static final short ENABLE_CORRELATION_EVENT = 6;
        static final short RESET_RFID_STATISTICS = 7;
        static final short RESET_COMMUNICATION_STATISTICS = 8;
        static final short SET_CORRELATION_THRESHOLD = 9;
        static final short GET_CORRELATION_THRESHOLD = 11;
        static final short SAVE_CORRELATION_THRESHOLD_TO_ROM = 12;
        static final short GET_STATISTICS_PYXIBUS = 13;
        static final short GET_STATISTICS_RFID = 14;
        static final short SET_BRIDGE_STATE = 15;
        static final short CLEAR_TAG_LIST_BEFORE_TAG_SCAN = 16;
        static final short TAG_PHASE_TEST = 17;
        static final short GET_TAG_RESPONSE_SIGNAL = 18;
        static final short GET_CARRIER_SIGNAL = 19;
        static final short SET_DEVICE_SERIAL_NUMBER = 20;
        static final short SET_DEAD_BAND = 21;
        static final short GET_DEVICE_SERIAL_NUMBER = 22;
        static final short SAVE_BRIDGE_DUTY_CYCLES_TO_ROM = 24;
        static final short GET_CORRELATION_COUNTS = 25;
        static final short SET_ANTENNA_POLARITY_AND_POWER = 26;
        static final short GET_BRIDGE_STATE = 27;
        static final short START_TAG_CHARACTERIZATION = 28;
        static final short GET_TAG_CHARACTERIZATION_RESULTS = 29;
        static final short SET_ANTENNA_ON = 32;
        static final short SEND_SYNCHRONIZATION = 33;
        static final short SEND_BASIC_ORDER = 34;
        static final short SET_SYNCHRONISATION_TIME = 35;
        static final short LOAD_UID_DIGITS = 36;
        static final short SEND_UID_CONFIRMATION = 37;
        static final short SET_MODULATION_TIME = 38;
        static final short SET_ACK_TIME = 39;
        static final short SEND_SWITCH_COMMAND = 40;
        static final short GET_SUPPLY_12V = 41;
        static final short SAMPLE_CORRELATION_SIMPLE = 42;
        static final short WRITE_BLOCK = 43;
        static final short GET_NUMBER_MAX_AXIS = 44;
        static final short LOAD_FULL_MEMORY_UID = 45;
        static final short START_CONFIRMATION = 48;
        static final short END_CONFIRMATION = 49;
        static final short CONFIRM_UID = 50;
        static final short GET_CARRIER_FREQUENCY = 52;
        static final short INCREASE_CARRIER_FREQUENCY = 53;
        static final short DECREASE_CARRIER_FREQUENCY = 54;
        static final short FIND_GOOD_FREQUENCY = 55;
        static final short SET_ALARM_INFRA_ON = 56;

        private BackdoorCommandType() {
        }
    }

    static final class BasicOrder {
        static final byte K0 = 0;
        static final byte K1 = 1;
        static final byte K2 = 2;
        static final byte K3 = 3;
        static final byte K4 = 4;
        static final byte K5 = 5;
        static final byte K6 = 6;
        static final byte K7 = 7;
        static final byte KD = 11;
        static final byte KZ = 10;
        static final byte KZ_SPCE2 = 13;
        static final byte KR = 9;
        static final byte KB = 8;
        static final byte KL = 12;

        private BasicOrder() {
        }
    }

    private final class LedLightingRunnable implements Runnable {
        private final List<Byte> _axisList;

        public LedLightingRunnable(List axisList) {
            this._axisList = axisList;
        }

        public void run() {
            boolean changeChannel = this._axisList.size() > 1;

            while(RfidReaderBoard.this._isLighting) {
                Iterator i$ = this._axisList.iterator();

                while(i$.hasNext()) {
                    byte currentAxis = ((Byte)i$.next()).byteValue();

                    try {
                        RfidReaderBoard.this.lightAllTagsOnCurrentAxis(currentAxis, changeChannel);
                    } catch (InterruptedException var5) {
                        SmartLogger.getLogger().log(Level.WARNING, "Lighting process interrupted", var5);
                        RfidReaderBoard.this.stopField();
                        RfidReaderBoard.this._isLighting = false;
                        break;
                    }
                }
            }

        }
    }

    protected static final class CommandMessage {
        static final String PREFIX = "TUNNEL|";
        static final char HELLO_WORLD = '䡗';
        static final char BACKDOOR = '䉄';
        static final char REQUEST_SCAN = '剃';
        static final char CONTROL = '䍏';
        static final char DOOR_OPEN = '䑕';
        static final char DOOR_CLOSE = '䑌';

        private CommandMessage() {
        }
    }

    protected static final class AsyncEventMessage {
        private final short _length;
        private final short _id;
        private final short[] _messagePackets;
        private static final byte HEADER_LENGTH = 16;
        static final String PREFIX = "ASYNCEVENT|";
        static final byte TYPE_SIZE = 2;
        private final short _aemType;
        static final short DOOR_OPEN = 0;
        static final short DOOR_CLOSED = 1;
        static final short DEVICE_RESET_ABNORMALLY = 59;
        static final short SCAN_STATE_CHANGED = 60;
        static final short BACKDOOR_INFO = 189;
        static final short POWER_CUT = 255;
        static final short TAG_R8 = 62;
        static final short TAG_SPCE2_BLANK = 63;
        static final short TAG_SPCE2_WRITTEN = 61;
        static final short TAG_R8_FULL = 80;
        static final short TAG_SPCE2_BLANK_FULL = 81;
        static final short TAG_SPCE2_WRITTEN_FULL = 82;
        static final short TAG_SPCE2_DECIMAL_FULL = 83;

        public AsyncEventMessage(short id, short[] messagePackets) {
            this._length = (short)(messagePackets.length + 16);
            this._id = id;
            this._messagePackets = new short[messagePackets.length];
            System.arraycopy(messagePackets, 0, this._messagePackets, 0, messagePackets.length);
            this._aemType = messagePackets[0];
        }

        short getAEMType() {
            return this._aemType;
        }

        short getLength() {
            return this._length;
        }

        long getId() {
            return (long)this._id;
        }

        short[] getMessagePackets() {
            return Arrays.copyOf(this._messagePackets, this._messagePackets.length);
        }
    }

    private final class AsyncEventRunnable implements Runnable {
        private final BlockingQueue<RfidReaderBoard.AsyncEventMessage> _asyncEventMessageQueue;

        public AsyncEventRunnable(BlockingQueue asyncEventMessageQueue) {
            this._asyncEventMessageQueue = asyncEventMessageQueue;
        }

        public void run() {
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    RfidReaderBoard.AsyncEventMessage ie = (RfidReaderBoard.AsyncEventMessage)this._asyncEventMessageQueue.take();
                    this.handleAsyncEvent(ie);
                } catch (InterruptedException var2) {
                    Thread.currentThread().interrupt();
                }
            }

        }

        private void handleAsyncEvent(final RfidReaderBoard.AsyncEventMessage asyncEventMessage) {
            ActionsPool.getService().submit(new Runnable() {
                public void run() {
                    AsyncEventRunnable.this.dispatchAsyncEvent(asyncEventMessage);
                }
            });
        }

        private void dispatchAsyncEvent(RfidReaderBoard.AsyncEventMessage asyncEventMessage) {
            String messagePackets = DatatypeConversion.shortArrayToString(asyncEventMessage.getMessagePackets());
            switch(asyncEventMessage.getAEMType()) {
                case 0:
                    RfidReaderBoard.this._eventHandler.eventDoorOpened();
                    break;
                case 1:
                    RfidReaderBoard.this._eventHandler.eventDoorClosed();
                case 59:
                case 255:
                default:
                    break;
                case 60:
                    RfidReaderBoard.this._eventHandler.eventScanStateChanged(messagePackets);
                    break;
                case 61:
                    RfidReaderBoard.this._eventHandler.eventTagSPCE2(messagePackets, false);
                    break;
                case 62:
                    RfidReaderBoard.this._eventHandler.eventTagR8(messagePackets);
                    break;
                case 63:
                    RfidReaderBoard.this._eventHandler.eventTagSPCE2(messagePackets, true);
                    break;
                case 80:
                    RfidReaderBoard.this._eventHandler.eventTagR8Full(messagePackets);
                    break;
                case 81:
                    RfidReaderBoard.this._eventHandler.eventTagSPCE2Full(messagePackets, true);
                    break;
                case 82:
                    RfidReaderBoard.this._eventHandler.eventTagSPCE2Full(messagePackets, false);
                    break;
                case 83:
                    RfidReaderBoard.this._eventHandler.eventTagSPCE2Decimal(messagePackets);
                    break;
                case 189:
                    RfidReaderBoard.this._eventHandler.eventBackDoorInfo(messagePackets);
            }

        }
    }

    protected static enum TagScanModeFlag {
        CLEAR_TAG_LIST((short)1),
        USE_ALL_AXIS((short)2),
        UNLOCK_ALL_TAG_ALL_AXIS((short)4),
        USE_KR((short)8);

        private final short _value;

        private TagScanModeFlag(short value) {
            this._value = value;
        }

        short getValue() {
            return this._value;
        }
    }

    protected final class RfidEventHandler {
        private final EventDispatcher _deviceEventDispatcher;

        public RfidEventHandler(EventDispatcher deviceEventDispatcher) {
            this._deviceEventDispatcher = deviceEventDispatcher;
        }

        void eventSerialConnectionAborted() {
            this._deviceEventDispatcher.eventDeviceDisconnected(true);
        }

        void eventScanFailed() {
            this._deviceEventDispatcher.eventScanFailed();
        }

        void eventTagR8(String messagePackets) {
            String rawTagUID = messagePackets.substring(2);
            String validUid = TagUidHandler.getR8Uid(rawTagUID);
            if(!validUid.isEmpty()) {
                this._deviceEventDispatcher.eventTagAdded(validUid);
            }

        }

        void eventTagSPCE2(String messagePackets, boolean blank) {
            String rawTagUid = messagePackets.substring(2);
            this._deviceEventDispatcher.eventTagAdded(blank?TagUidHandler.getBlankE2Uid(rawTagUid):TagUidHandler.getE2Uid(rawTagUid));
        }

        void eventTagR8Full(String messagePackets) {
            String rawTagUid = messagePackets.substring(2);
            String fullOctalUid = TagUidHandler.rawUidDigitsToFullOctal(rawTagUid, 24);
            if(fullOctalUid != null) {
                this._deviceEventDispatcher.eventTagAdded(fullOctalUid);
            }
        }

        void eventTagSPCE2Full(String messagePackets, boolean blank) {
            String rawTagUid = messagePackets.substring(2);
            String fullOctalUid = TagUidHandler.rawUidDigitsToFullOctal(rawTagUid, blank?24:84);
            if(fullOctalUid != null) {
                if(blank) {
                    this._deviceEventDispatcher.eventTagAdded(fullOctalUid);
                } else {
                    if(!fullOctalUid.startsWith("2")) {
                        return;
                    }

                    String alphanumUid = TagUidHandler.fullOctalToAlphaNumericUid(fullOctalUid);
                    this._deviceEventDispatcher.eventTagAdded(alphanumUid);
                }

            }
        }

        void eventTagSPCE2Decimal(String messagePackets) {
            String rawTagUid = messagePackets.substring(2);
            String fullOctalUid = TagUidHandler.rawUidDigitsToFullOctal(rawTagUid, 36);
            if(fullOctalUid != null && fullOctalUid.startsWith("1")) {
                String decimalUid = TagUidHandler.fullOctalToDecimalUid(fullOctalUid);
                if(decimalUid != null) {
                    this._deviceEventDispatcher.eventTagAdded(decimalUid);
                }
            }
        }

        void eventBackDoorInfo(String messagePackets) {
            if(messagePackets.length() >= 12) {
                byte backdoorEventType = Byte.parseByte(messagePackets.substring(2, 4), 16);
                short[] value1bytes = DatatypeConversion.stringToShortArray(messagePackets.substring(4, 8));
                short[] value2bytes = DatatypeConversion.stringToShortArray(messagePackets.substring(8, 12));
                char value1 = (char)(value1bytes[0] | value1bytes[1] << 8);
                char value2 = (char)(value2bytes[0] | value2bytes[1] << 8);
                switch(backdoorEventType) {
                    case 1:
                        short phaseShift = (short)(value2 > 180?value2 - 360:value2);
                        this.eventCorrelationSample((short)value1, phaseShift);
                        break;
                    case 2:
                        short[] presentSamples = RfidReaderBoard.this.getCorrelationSeriesValues(false, value1);
                        short[] missingSamples = RfidReaderBoard.this.getCorrelationSeriesValues(true, value2);
                        this._deviceEventDispatcher.eventCorrelationSampleSeries(presentSamples, missingSamples);
                        break;
                    case 3:
                        this.eventTagCharacterization();
                        break;
                    case 4:
                        if(value1 == 10 && value2 == 1) {
                            return;
                        }

                        if(value1 > 16) {
                            return;
                        }

                        this._deviceEventDispatcher.eventAxisSwitched((byte)value1);
                        break;
                    case 5:
                        synchronized(RfidReaderBoard.this._basicOrderSync) {
                            RfidReaderBoard.this._waitingBasicOrderResponse = false;
                            RfidReaderBoard.this._basicOrderResponseState = value1 == 1;
                            RfidReaderBoard.this._basicOrderSync.notify();
                            break;
                        }
                    case 6:
                        synchronized(RfidReaderBoard.this._rewritingSync) {
                            RfidReaderBoard.this._waitingRewriteResponse = false;
                            RfidReaderBoard.this._rewritingResponse = (byte)value1;
                            RfidReaderBoard.this._rewritingSync.notify();
                        }
                }

            }
        }

        private void eventCorrelationSample(short correlationValue, short phaseShift) {
            this._deviceEventDispatcher.eventCorrelationSample(correlationValue, phaseShift);
        }

        private void eventTagCharacterization() {
        }

        void eventDoorOpened() {
            this._deviceEventDispatcher.eventDoorOpened();
        }

        void eventDoorClosed() {
            this._deviceEventDispatcher.eventDoorClosed();
        }

        void eventScanStateChanged(String messagePackets) {
            byte newState = Byte.parseByte(messagePackets.substring(2, 4));
            byte value1 = Byte.parseByte(messagePackets.substring(4, 6));
            switch(newState) {
                case 0:
                    if(value1 == 1) {
                        this._deviceEventDispatcher.eventScanStarted();
                    }
                    break;
                case 1:
                    this._deviceEventDispatcher.eventScanCompleted();
                    break;
                case 2:
                    this._deviceEventDispatcher.eventScanCancelledByHost();
                    break;
                case 3:
                    this._deviceEventDispatcher.eventScanCancelledByDoor();
                    break;
                case 4:
                    this._deviceEventDispatcher.eventScanFailed();
                case 5:
                default:
                    break;
                case 6:
                    synchronized(RfidReaderBoard.this._confirmationSync) {
                        RfidReaderBoard.this._waitingConfirmationResponse = false;
                        RfidReaderBoard.this._confirmationResponse = value1 == 1;
                        if(RfidReaderBoard.this._confirmationResponse) {
                            this._deviceEventDispatcher.eventScanTagConfirmation();
                        }

                        RfidReaderBoard.this._confirmationSync.notify();
                    }
            }

        }

        void eventFlashingProgress(int rowNumber, int rowCount) {
            this._deviceEventDispatcher.eventFlashingProgress(rowNumber, rowCount);
        }

        protected final class ScanStatusType {
            static final byte SS_STARTED = 0;
            static final byte SS_COMPLETED = 1;
            static final byte SS_CANCELLED_BY_USER = 2;
            static final byte SS_CANCELLED_BY_DOOR = 3;
            static final byte SS_FAILED_UNEXPECTED_ERROR = 4;
            static final byte SS_TAG_CONFIRMATION = 6;

            private ScanStatusType() {
            }
        }

        protected final class BackdoorEventType {
            static final byte CORRELATION_SAMPLE = 1;
            static final byte CORRELATION_SAMPLE_SERIES = 2;
            static final byte TAG_CHARACTERIZATION = 3;
            static final byte AXIS_SWITCHED = 4;
            static final byte BASIC_ORDER = 5;
            static final byte WRITE_BLOCK = 6;

            private BackdoorEventType() {
            }
        }
    }
}
