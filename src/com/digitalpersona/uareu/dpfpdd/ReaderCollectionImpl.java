package com.digitalpersona.uareu.dpfpdd;

import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.jni.Dpfpdd;
import java.util.AbstractList;
import java.util.Vector;

public class ReaderCollectionImpl extends AbstractList implements ReaderCollection {

   private final Dpfpdd m_dpfpdd = new Dpfpdd();
   private boolean m_isInitialized = false;
   Vector m_readers = new Vector(4, 2);


   public ReaderCollectionImpl() throws UareUException {}

   public int size() {
      return this.m_readers.size();
   }

   public Reader get(int n) {
      return (Reader)this.m_readers.get(n);
   }

   public void GetReaders() throws UareUException {
      synchronized(this) {
         Reader.Description[] descriptions = this.m_dpfpdd.query_devices();
         int didx = 0;

         while(didx < this.m_readers.size()) {
            ReaderCollectionImpl.ReaderImpl bFound = (ReaderCollectionImpl.ReaderImpl)this.m_readers.get(didx);
            boolean reader = false;
            int didx1 = 0;

            while(true) {
               if(didx1 < descriptions.length) {
                  if(!bFound.GetDescription().name.equals(descriptions[didx1].name)) {
                     ++didx1;
                     continue;
                  }

                  reader = true;
               }

               if(!reader) {
                  this.m_readers.remove(didx);
                  --didx;
               }

               ++didx;
               break;
            }
         }

         didx = 0;

         while(didx < descriptions.length) {
            boolean var9 = false;
            int var10 = 0;

            while(true) {
               if(var10 < this.m_readers.size()) {
                  if(!descriptions[didx].name.equals(((ReaderCollectionImpl.ReaderImpl)this.m_readers.get(var10)).GetDescription().name)) {
                     ++var10;
                     continue;
                  }

                  var9 = true;
               }

               if(!var9) {
                  ReaderCollectionImpl.ReaderImpl var11 = new ReaderCollectionImpl.ReaderImpl(descriptions[didx]);
                  this.m_readers.add(var11);
               }

               ++didx;
               break;
            }
         }

      }
   }

   public void Initialize() throws UareUException {
      synchronized(this) {
         if(!this.m_isInitialized) {
            this.m_dpfpdd.init();
            this.m_isInitialized = true;
         }

      }
   }

   public void Release() throws UareUException {
      synchronized(this) {
         if(this.m_isInitialized) {
            this.m_isInitialized = false;
            this.m_dpfpdd.exit();
         }

      }
   }

   private class ReaderImpl implements Reader {

      private Reader.Description m_descr;
      private Reader.Capabilities m_caps;
      private long m_hReader;
      private int m_nImageSize;


      public ReaderImpl(Reader.Description descr) {
         this.m_descr = descr;
         this.m_nImageSize = 0;
      }

      public void Open(Reader.Priority priority) throws UareUException {
         synchronized(this) {
            this.m_hReader = ReaderCollectionImpl.this.m_dpfpdd.open(this.m_descr.name, priority);
            this.m_caps = ReaderCollectionImpl.this.m_dpfpdd.get_capabilities(this.m_hReader);
         }
      }

      public void Close() throws UareUException {
         synchronized(this) {
            long hReader = this.m_hReader;
            this.m_hReader = 0L;
            this.m_caps = null;
            ReaderCollectionImpl.this.m_dpfpdd.close(hReader);
         }
      }

      public Reader.Status GetStatus() throws UareUException {
         return ReaderCollectionImpl.this.m_dpfpdd.get_status(this.m_hReader);
      }

      public Reader.Capabilities GetCapabilities() {
         return this.m_caps;
      }

      public Reader.Description GetDescription() {
         return this.m_descr;
      }

      public Reader.CaptureResult Capture(Fid.Format img_format, Reader.ImageProcessing img_proc, int resolution, int timeout) throws UareUException {
         Reader.CaptureResult result = ReaderCollectionImpl.this.m_dpfpdd.capture(this.m_hReader, this.m_nImageSize, img_format, img_proc, resolution, timeout);
         if(null != result.image) {
            this.m_nImageSize = result.image.getData().length;
         }

         return result;
      }

      public void CancelCapture() throws UareUException {
         ReaderCollectionImpl.this.m_dpfpdd.capture_cancel(this.m_hReader);
      }

      public void StartStreaming() throws UareUException {
         ReaderCollectionImpl.this.m_dpfpdd.start_stream(this.m_hReader);
      }

      public void StopStreaming() throws UareUException {
         ReaderCollectionImpl.this.m_dpfpdd.stop_stream(this.m_hReader);
      }

      public Reader.CaptureResult GetStreamImage(Fid.Format img_format, Reader.ImageProcessing img_proc, int resolution) throws UareUException {
         Reader.CaptureResult result = ReaderCollectionImpl.this.m_dpfpdd.get_stream_image(this.m_hReader, this.m_nImageSize, img_format, img_proc, resolution);
         if(null != result.image) {
            this.m_nImageSize = result.image.getData().length;
         }

         return result;
      }

      public void Calibrate() throws UareUException {
         ReaderCollectionImpl.this.m_dpfpdd.calibrate(this.m_hReader);
      }

      public void Reset() throws UareUException {
         ReaderCollectionImpl.this.m_dpfpdd.reset(this.m_hReader);
      }
   }
}
