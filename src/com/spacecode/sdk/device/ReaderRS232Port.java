//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.spacecode.sdk.device;

import com.spacecode.sdk.SmartLogger;
import com.spacecode.sdk.device.CRC;
import com.spacecode.sdk.device.RfidReaderBoard;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

final class ReaderRS232Port extends SerialPort {
    private static final char START_OF_FRAME = '\n';
    private static final char END_OF_FRAME = '\r';
    private boolean _isListening = false;
    private final RfidReaderBoard _rfidReaderBoard;

    public ReaderRS232Port(String portName, RfidReaderBoard readerBoard) {
        super(portName.trim());
        this._rfidReaderBoard = readerBoard;
    }

    public void startListening() throws SerialPortException {
        if(this.isOpened() && !this._isListening) {
            this.addEventListener(new ReaderRS232Port.RS232EventListener());
            this._isListening = true;
        }

    }

    public boolean sendMessage(String messageToBeSent) throws SerialPortException {
        String crcHex = CRC.computeCRC(messageToBeSent);
        String completeMessage = '\n' + messageToBeSent + crcHex + '\r';
        return this.writeString(completeMessage);
    }

    private void onMessageReceived(String message) {
        if(message != null && !message.trim().isEmpty()) {
            this._rfidReaderBoard.onSerialMessageReceived(message);
        }
    }

    private void onSerialConnectionAborted() {
        this._rfidReaderBoard.onSerialConnectionAborted();
    }

    final class RS232EventListener implements SerialPortEventListener {
        private String _incomingData = "";

        RS232EventListener() {
        }

        public void serialEvent(SerialPortEvent event) {
            if(event.isRXCHAR()) {
                if(event.getEventValue() == 0) {
                    return;
                }

                this.handleReception();
            } else if(event.isDSR() && event.getEventValue() != 1) {
                ReaderRS232Port.this.onSerialConnectionAborted();
            }

        }

        private void handleReception() {
            try {
                this._incomingData = this._incomingData + ReaderRS232Port.this.readString();
            } catch (SerialPortException var4) {
                SmartLogger.getLogger().log(Level.WARNING, "SerialPortException occurred while getting new message.", var4);
                return;
            }

            if(this._incomingData.charAt(0) != 10) {
                this._incomingData = "";
            } else {
                List fullMessages = this.getFullMessages();
                if(!fullMessages.isEmpty()) {
                    Iterator i$ = fullMessages.iterator();

                    while(i$.hasNext()) {
                        String message = (String)i$.next();
                        if(CRC.isCRCValid(message)) {
                            message = message.substring(0, message.length() - 4);
                            ReaderRS232Port.this.onMessageReceived(message.toUpperCase());
                        }
                    }

                }
            }
        }

        private List<String> getFullMessages() {
            ArrayList fullMessages = new ArrayList();

            for(int endIndex = this._incomingData.indexOf(13); endIndex != -1; endIndex = this._incomingData.indexOf(13)) {
                String newFullMessage = this._incomingData.substring(0, endIndex);
                if(newFullMessage.charAt(0) == 10) {
                    fullMessages.add(newFullMessage.substring(1));
                }

                this._incomingData = this._incomingData.substring(endIndex + 1);
            }

            return fullMessages;
        }
    }
}
