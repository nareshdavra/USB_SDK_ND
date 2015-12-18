package com.digitalpersona.uareu;

import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Importer;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.dpfj.EngineImpl;
import com.digitalpersona.uareu.dpfj.ImporterImpl;
import com.digitalpersona.uareu.dpfpdd.ReaderCollectionImpl;

public final class UareUGlobal {

   private static ReaderCollectionImpl m_collection = null;
   private static EngineImpl m_engine = null;
   private static ImporterImpl m_importer = null;


   public static ReaderCollection GetReaderCollection() throws UareUException {
      if(null == m_collection) {
         try {
            m_collection = (ReaderCollectionImpl)Class.forName("com.digitalpersona.uareu.dpfpdd.ReaderCollectionImpl").newInstance();
            m_collection.Initialize();
         } catch (InstantiationException var1) {
            var1.printStackTrace();
         } catch (IllegalAccessException var2) {
            var2.printStackTrace();
         } catch (ClassNotFoundException var3) {
            var3.printStackTrace();
         } catch (UareUException var4) {
            m_collection = null;
            throw var4;
         }
      }

      return m_collection;
   }

   public static void DestroyReaderCollection() throws UareUException {
      if(null != m_collection) {
         m_collection.Release();
         m_collection = null;
      }

   }

   public static Engine GetEngine() {
      if(null == m_engine) {
         try {
            m_engine = (EngineImpl)Class.forName("com.digitalpersona.uareu.dpfj.EngineImpl").newInstance();
         } catch (InstantiationException var1) {
            var1.printStackTrace();
         } catch (IllegalAccessException var2) {
            var2.printStackTrace();
         } catch (ClassNotFoundException var3) {
            var3.printStackTrace();
         }
      }

      return m_engine;
   }

   public static Importer GetImporter() {
      if(null == m_importer) {
         try {
            m_importer = (ImporterImpl)Class.forName("com.digitalpersona.uareu.dpfj.ImporterImpl").newInstance();
         } catch (InstantiationException var1) {
            var1.printStackTrace();
         } catch (IllegalAccessException var2) {
            var2.printStackTrace();
         } catch (ClassNotFoundException var3) {
            var3.printStackTrace();
         }
      }

      return m_importer;
   }

}
