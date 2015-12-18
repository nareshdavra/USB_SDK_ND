//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.spacecode.sdk.device.module.authentication;

import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;
import com.digitalpersona.uareu.Engine.EnrollmentCallback;
import com.digitalpersona.uareu.Engine.PreEnrollmentFmd;
import com.digitalpersona.uareu.Fmd.Format;
import com.digitalpersona.uareu.Reader.CaptureQuality;
import com.digitalpersona.uareu.Reader.CaptureResult;
import com.digitalpersona.uareu.Reader.ImageProcessing;
import com.digitalpersona.uareu.Reader.Priority;
import com.digitalpersona.uareu.Reader.ReaderStatus;
import com.spacecode.sdk.SmartLogger;
import com.spacecode.sdk.device.Device;
import com.spacecode.sdk.device.Device.EventDispatcher;
import com.spacecode.sdk.device.data.DeviceStatus;
import com.spacecode.sdk.device.module.authentication.AuthenticationModule;
import com.spacecode.sdk.device.module.authentication.FingerprintReaderException;
import com.spacecode.sdk.user.User;
import com.spacecode.sdk.user.data.FingerIndex;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import javax.xml.bind.DatatypeConverter;

public final class FingerprintReader extends AuthenticationModule {
    private static ReaderCollection _readerCollection;
    private Reader _reader;
    private boolean _isEnrolling;
    private Future<?> _captureThreadFuture;
    private final Object _enrollmentSync;
    private Fmd _enrollmentFmd;
    private static int ENROLLING_TIMEOUT_DELAY = 300000;
    private final Object _captureSync;
    private CaptureResult _captureResult;
    private FingerIndex _lastVerifiedFingerIndex;
    private byte _currentSampleNumber;

    public FingerprintReader(String serialNumber, Device listeningDevice, EventDispatcher eventDispatcher, boolean isMaster) throws FingerprintReaderException {
        super(serialNumber, listeningDevice, eventDispatcher, isMaster);
        this._enrollmentSync = new Object();
        this._enrollmentFmd = null;
        this._captureSync = new Object();
        this._captureResult = null;
        if(serialNumber != null && !serialNumber.trim().isEmpty()) {
            if(_readerCollection.isEmpty()) {
                throw new FingerprintReaderException("No Fingerprint Reader available.");
            } else {
                boolean found = false;
                Iterator i$ = _readerCollection.iterator();

                while(i$.hasNext()) {
                    Reader reader = (Reader)i$.next();
                    if(serialNumber.equals(reader.GetDescription().serial_number)) {
                        found = true;
                        this._reader = reader;

                        try {
                            this._reader.Open(Priority.EXCLUSIVE);
                            break;
                        } catch (UareUException var9) {
                            SmartLogger.getLogger().log(Level.SEVERE, "Unexpected error when trying to connect the reader.", var9);
                            throw new FingerprintReaderException("Unable to load the reader.");
                        }
                    }
                }

                if(!found) {
                    throw new FingerprintReaderException("Reader " + serialNumber + " not found.");
                } else {
                    this._captureThreadFuture = Executors.newSingleThreadExecutor().submit(new FingerprintReader.CaptureRunnable());
                }
            }
        } else {
            throw new FingerprintReaderException("Invalid Serial Number provided.");
        }
    }

    public FingerprintReader(String serialNumber, Device listeningDevice, EventDispatcher eventDispatcher) throws FingerprintReaderException {
        this(serialNumber, listeningDevice, eventDispatcher, true);
    }

    public void release() {
        if(this._reader != null) {
            try {
                this._captureThreadFuture.cancel(true);
                this._reader.CancelCapture();
                this._reader.Close();
            } catch (UareUException var2) {
                SmartLogger.getLogger().log(Level.WARNING, "Exception while releasing fingerprint reader.", var2);
            }

        }
    }

    public static int getEnrollingTimeoutDelay() {
        return ENROLLING_TIMEOUT_DELAY;
    }

    private static ReaderCollection initializeReaderCollection() {
        try {
            return UareUGlobal.GetReaderCollection();
        } catch (UareUException var1) {
            SmartLogger.getLogger().log(Level.SEVERE, "Error while initializing Fingerprint Readers", var1);
            return null;
        }
    }

    public static int connectFingerprintReader() throws FingerprintReaderException {
        return connectFingerprintReaders(1);
    }

    public static int connectFingerprintReaders(int nbOfReadersExpected) throws FingerprintReaderException {
        if(nbOfReadersExpected == 0) {
            return 0;
        } else {
            _readerCollection = initializeReaderCollection();
            if(_readerCollection == null) {
                throw new FingerprintReaderException("Initialization failed. Unable to connect with the readers.");
            } else {
                int tries = 0;

                do {
                    try {
                        Thread.sleep(700L);
                        ++tries;
                        _readerCollection.GetReaders();
                    } catch (UareUException | InterruptedException var3) {
                        SmartLogger.getLogger().log(Level.SEVERE, "Unexpected error occurred while trying to connect with the readers.", var3);
                        break;
                    }
                } while(_readerCollection.size() < nbOfReadersExpected && tries < 10);

                return _readerCollection.size();
            }
        }
    }

    public String enrollFinger() throws TimeoutException {
        if(this._isEnrolling) {
            return null;
        } else {
            this._isEnrolling = true;
            this._listeningDevice.setStatus(DeviceStatus.ENROLLING);
            Executors.newSingleThreadExecutor().submit(new FingerprintReader.EnrollmentRunnable());
            Object var1 = this._enrollmentSync;
            synchronized(this._enrollmentSync) {
                label73: {
                    Object var3;
                    try {
                        this._enrollmentSync.wait((long)ENROLLING_TIMEOUT_DELAY);
                        break label73;
                    } catch (InterruptedException var9) {
                        SmartLogger.getLogger().log(Level.SEVERE, "Enrollment process has been interrupted while waiting for fingerprints.", var9);
                        this._isEnrolling = false;
                        var3 = null;
                    } finally {
                        this._listeningDevice.setStatus(DeviceStatus.READY);
                    }

                    return (String)var3;
                }
            }

            if(this._isEnrolling) {
                this._isEnrolling = false;
                throw new TimeoutException("Enrollment process has timed out.");
            } else {
                return this._enrollmentFmd == null?null:DatatypeConverter.printBase64Binary(this._enrollmentFmd.getData());
            }
        }
    }

    private void eventNewCaptureResult(CaptureResult captureResult) {
        if(this._isEnrolling) {
            Object user = this._captureSync;
            synchronized(this._captureSync) {
                this._captureResult = captureResult;
                this._captureSync.notify();
            }
        } else {
            User user1 = this.verifySample(captureResult);
            if(user1 == null) {
                return;
            }

            this.checkUserGrants(user1);
        }

    }

    private User verifySample(CaptureResult captureResult) {
        if(captureResult.quality == CaptureQuality.GOOD && captureResult.image != null) {
            try {
                Fmd uue = UareUGlobal.GetEngine().CreateFmd(captureResult.image, Format.DP_VER_FEATURES);
                List knownUsers = this._listeningDevice.getUsersService().getUsers();
                Iterator i$ = knownUsers.iterator();

                while(i$.hasNext()) {
                    User user = (User)i$.next();
                    Iterator i$1 = user.getEnrolledFingersIndexes().iterator();

                    while(i$1.hasNext()) {
                        FingerIndex fingerIndex = (FingerIndex)i$1.next();
                        Fmd fingerFmd = UareUGlobal.GetImporter().ImportFmd(DatatypeConverter.parseBase64Binary(user.getFingerprintTemplate(fingerIndex)), Format.DP_REG_FEATURES, Format.DP_REG_FEATURES);
                        if(UareUGlobal.GetEngine().Compare(uue, 0, fingerFmd, 0) < 21474) {
                            this._lastVerifiedFingerIndex = fingerIndex;
                            return user;
                        }
                    }
                }
            } catch (UareUException var9) {
                SmartLogger.getLogger().log(Level.SEVERE, "Unexpected error while verifying a new fingerprint template.", var9);
            }

            return null;
        } else {
            return null;
        }
    }

    public FingerIndex getLastVerifiedFingerIndex() {
        return this._lastVerifiedFingerIndex;
    }

    private class CaptureRunnable implements Runnable {
        private CaptureRunnable() {
        }

        public void run() {
            while(true) {
                if(!Thread.currentThread().isInterrupted()) {
                    try {
                        ReaderStatus e = FingerprintReader.this._reader.GetStatus().status;
                        if(e != ReaderStatus.READY && e != ReaderStatus.NEED_CALIBRATION) {
                            Thread.sleep(100L);
                            continue;
                        }

                        FingerprintReader.this._deviceEventDispatcher.eventFingerTouched(FingerprintReader.this._isMaster);
                        FingerprintReader.this._captureResult = FingerprintReader.this._reader.Capture(com.digitalpersona.uareu.Fid.Format.ANSI_381_2004, ImageProcessing.IMG_PROC_DEFAULT, 500, -1);
                        FingerprintReader.this.eventNewCaptureResult(FingerprintReader.this._captureResult);
                        continue;
                    } catch (InterruptedException | UareUException var2) {
                        SmartLogger.getLogger().log(Level.SEVERE, "Unexpected error occurred while waiting for FP Reader. Stopping capture...", var2);
                        FingerprintReader.this._captureResult = new CaptureResult();
                        FingerprintReader.this._captureResult.quality = CaptureQuality.CANCELED;
                        FingerprintReader.this.eventNewCaptureResult(FingerprintReader.this._captureResult);
                    }
                }

                return;
            }
        }
    }

    class EnrollmentRunnable implements Runnable, EnrollmentCallback {
        EnrollmentRunnable() {
        }

        public void run() {
            synchronized(FingerprintReader.this._enrollmentSync) {
                try {
                    FingerprintReader.this._currentSampleNumber = 0;
                    FingerprintReader.this._enrollmentFmd = UareUGlobal.GetEngine().CreateEnrollmentFmd(Format.DP_REG_FEATURES, this);
                } catch (UareUException var8) {
                    SmartLogger.getLogger().log(Level.SEVERE, "Unable to start Enrollment process.", var8);
                    FingerprintReader.this._enrollmentFmd = null;
                } finally {
                    FingerprintReader.this._isEnrolling = false;
                    FingerprintReader.this._enrollmentSync.notify();
                }

            }
        }

        public PreEnrollmentFmd GetFmd(Format format) {
            PreEnrollmentFmd preFmd = null;
            synchronized(FingerprintReader.this._captureSync) {
                while(preFmd == null) {
                    try {
                        FingerprintReader.this._captureSync.wait();
                    } catch (InterruptedException var7) {
                        SmartLogger.getLogger().log(Level.SEVERE, "Interrupted while waiting for a new fingerprint sample.", var7);
                        return null;
                    }

                    if(FingerprintReader.this._captureResult.quality == CaptureQuality.CANCELED) {
                        return null;
                    }

                    if(FingerprintReader.this._captureResult.image != null && FingerprintReader.this._captureResult.quality == CaptureQuality.GOOD) {
                        try {
                            preFmd = new PreEnrollmentFmd();
                            preFmd.fmd = UareUGlobal.GetEngine().CreateFmd(FingerprintReader.this._captureResult.image, Format.DP_PRE_REG_FEATURES);
                            FingerprintReader.this._deviceEventDispatcher.eventFingerprintEnrollmentSample(FingerprintReader.this._currentSampleNumber);
                            preFmd.view_index = 0;
                        } catch (UareUException var6) {
                            SmartLogger.getLogger().log(Level.SEVERE, "Error while extracting Fingerprint features.", var6);
                        }
                    }
                }

                return preFmd;
            }
        }
    }
}
