package com.spacecode.sdk.device.module.authentication;

import com.spacecode.sdk.SmartLogger;
import com.spacecode.sdk.device.Device;
import com.spacecode.sdk.device.data.DeviceStatus;
import com.spacecode.sdk.device.module.authentication.AuthenticationModule;
import com.spacecode.sdk.user.User;
import java.util.Iterator;
import java.util.logging.Level;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public final class BadgeReader extends AuthenticationModule implements SerialPortEventListener {

   private final SerialPort _serialPort;
   private String _incomingData;
   private static final char EOF = '\r';


   public BadgeReader(String serialNumber, String serialPortName, Device listeningDevice) throws SerialPortException {
      this(serialNumber, serialPortName, listeningDevice, (Device.EventDispatcher)null, true);
   }

   public BadgeReader(String serialNumber, String serialPortName, Device listeningDevice, Device.EventDispatcher eventDispatcher, boolean isMaster) throws SerialPortException {
      super(serialNumber, listeningDevice, eventDispatcher, isMaster);
      this._incomingData = "";
      this._serialPort = new SerialPort(serialPortName.trim());
      this.initializeSerialPort();
   }

   private void initializeSerialPort() throws SerialPortException {
      if(!this._serialPort.openPort()) {
         this._serialPort.closePort();
         throw new SerialPortException(this._serialPort.getPortName(), "Open port", "");
      } else if(!this._serialPort.setParams(9600, 8, 1, 0)) {
         this._serialPort.closePort();
         throw new SerialPortException(this._serialPort.getPortName(), "Set parameters", "");
      } else if(!this._serialPort.setEventsMask(17)) {
         this._serialPort.closePort();
         throw new SerialPortException(this._serialPort.getPortName(), "Set event mask", "");
      } else {
         this._serialPort.addEventListener(this);
         this._deviceEventDispatcher.eventBadgeReaderConnected(this._isMaster);
      }
   }

   public void release() {
      try {
         this._serialPort.closePort();
      } catch (SerialPortException var2) {
         SmartLogger.getLogger().log(Level.SEVERE, "SerialPortException occurred when trying to end Badge Reader communication.", var2);
      }

   }

   public String getSerialPortName() {
      return this._serialPort.getPortName();
   }

   private void newBadgeScanned(String badgeNumber) {
      this._deviceEventDispatcher.eventBadgeScanned(badgeNumber);
      if(this._listeningDevice.getStatus() != DeviceStatus.ENROLLING) {
         Iterator i$ = this._listeningDevice.getUsersService().getUsers().iterator();

         while(i$.hasNext()) {
            User user = (User)i$.next();
            if(badgeNumber.equals(user.getBadgeNumber())) {
               this.checkUserGrants(user);
               break;
            }
         }

      }
   }

   public void serialEvent(SerialPortEvent serialPortEvent) {
      if(serialPortEvent.isRXCHAR()) {
         if(serialPortEvent.getEventValue() == 0) {
            return;
         }

         this.handleIncomingData();
      } else if(serialPortEvent.isDSR() && serialPortEvent.getEventValue() != 1) {
         this._deviceEventDispatcher.eventBadgeReaderDisconnected(this._isMaster);
      }

   }

   private void handleIncomingData() {
      try {
         this._incomingData = this._incomingData + this._serialPort.readString();
         if(this._incomingData.indexOf(13) != -1) {
            String se = this._incomingData.substring(0, 10);
            this._incomingData = "";
            this.newBadgeScanned(se);
         }
      } catch (SerialPortException var2) {
         SmartLogger.getLogger().log(Level.SEVERE, "SerialPortException while reading data from BadgeReader.", var2);
      }

   }
}
