package com.spacecode.sdk.network.alert;

import com.spacecode.sdk.SmartLogger;
import com.spacecode.sdk.XmlObject;
import com.spacecode.sdk.network.alert.AlertTemperature;
import com.spacecode.sdk.network.alert.AlertType;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import javax.xml.bind.DatatypeConverter;

public class Alert {

   static final String NODE_ID = "id";
   static final String NODE_TYPE = "type";
   static final String NODE_TO = "to";
   static final String NODE_CC = "cc";
   static final String NODE_BCC = "bcc";
   static final String NODE_SUBJECT = "subject";
   static final String NODE_CONTENT = "content";
   static final String NODE_ENABLED = "enabled";
   private int _id;
   private AlertType _type;
   private String _toList;
   private String _ccList;
   private String _bccList;
   private String _emailSubject;
   private String _emailContent;
   private boolean _enabled;


   public Alert(AlertType type, String to, String emailSubject, String emailContent, boolean enabled) {
      this(type, to, "", "", emailSubject, emailContent, enabled);
   }

   public Alert(AlertType type, String to, String cc, String bcc, String emailSubject, String emailContent, boolean enabled) {
      this(0, type, to, cc, bcc, emailSubject, emailContent, enabled);
   }

   @Deprecated
   public Alert(int id, AlertType type, String to, String cc, String bcc, String emailSubject, String emailContent, boolean enabled) {
      this._id = id;
      this._type = type;
      this._toList = to;
      this._ccList = cc;
      this._bccList = bcc;
      this._emailSubject = emailSubject;
      this._emailContent = emailContent;
      this._enabled = enabled;
   }

   private Alert() {}

   protected String alertToXml() {
      StringBuilder sb = new StringBuilder();
      sb.append("<alert>");
      sb.append("<").append("id").append(">");
      sb.append(this._id);
      sb.append("</").append("id").append(">");
      sb.append("<").append("type").append(">");
      sb.append(this._type.name());
      sb.append("</").append("type").append(">");
      sb.append("<").append("to").append(">");
      sb.append(this._toList);
      sb.append("</").append("to").append(">");
      sb.append("<").append("cc").append(">");
      sb.append(this._ccList);
      sb.append("</").append("cc").append(">");
      sb.append("<").append("bcc").append(">");
      sb.append(this._bccList);
      sb.append("</").append("bcc").append(">");
      sb.append("<").append("subject").append(">");
      sb.append(this._emailSubject);
      sb.append("</").append("subject").append(">");
      sb.append("<").append("content").append(">");
      sb.append(this._emailContent);
      sb.append("</").append("content").append(">");
      sb.append("<").append("enabled").append(">");
      sb.append(String.valueOf(this._enabled));
      sb.append("</").append("enabled").append(">");
      sb.append("</alert>");
      return sb.toString();
   }

   public String serialize() {
      try {
         return DatatypeConverter.printBase64Binary(this.alertToXml().getBytes("UTF-8"));
      } catch (UnsupportedEncodingException var2) {
         SmartLogger.getLogger().log(Level.SEVERE, "Unable to encode serialized user with UTF-8 charset.", var2);
         return "";
      }
   }

   public static Alert deserialize(String serializedAlert) {
      if(serializedAlert != null && !"".equals(serializedAlert.trim())) {
         String decodedSerializedInventory;
         try {
            decodedSerializedInventory = new String(DatatypeConverter.parseBase64Binary(serializedAlert), "UTF-8");
         } catch (UnsupportedEncodingException var30) {
            SmartLogger.getLogger().log(Level.SEVERE, "Unable to decode serialized inventory with UTF-8 charset.", var30);
            return null;
         }

         int idStart = decodedSerializedInventory.indexOf("<id>");
         int idEnd = decodedSerializedInventory.indexOf("</id>");
         int alertTypeStart = decodedSerializedInventory.indexOf("<type>");
         int alertTypeEnd = decodedSerializedInventory.indexOf("</type>");
         int toStart = decodedSerializedInventory.indexOf("<to>");
         int toEnd = decodedSerializedInventory.indexOf("</to>");
         int ccStart = decodedSerializedInventory.indexOf("<cc>");
         int ccEnd = decodedSerializedInventory.indexOf("</cc>");
         int bccStart = decodedSerializedInventory.indexOf("<bcc>");
         int bccEnd = decodedSerializedInventory.indexOf("</bcc>");
         int subjectStart = decodedSerializedInventory.indexOf("<subject>");
         int subjectEnd = decodedSerializedInventory.indexOf("</subject>");
         int contentStart = decodedSerializedInventory.indexOf("<content>");
         int contentEnd = decodedSerializedInventory.indexOf("</content>");
         int enabledStart = decodedSerializedInventory.indexOf("<enabled>");
         int enabledEnd = decodedSerializedInventory.indexOf("</enabled>");
         if(!XmlObject.notNegativeIndex(new int[]{idStart, idEnd, alertTypeStart, alertTypeEnd, toStart, toEnd, ccStart, ccEnd, bccStart, bccEnd, subjectStart, subjectEnd, contentStart, contentEnd, enabledStart, enabledEnd})) {
            return null;
         } else {
            Alert newAlert = new Alert();

            try {
               newAlert._id = Integer.parseInt(decodedSerializedInventory.substring(idStart + "id".length() + 2, idEnd));
            } catch (NumberFormatException var29) {
               SmartLogger.getLogger().log(Level.SEVERE, "Invalid ID value for serialized Alert.", var29);
               return null;
            }

            try {
               newAlert._type = AlertType.valueOf(decodedSerializedInventory.substring(alertTypeStart + "type".length() + 2, alertTypeEnd));
            } catch (IllegalArgumentException var28) {
               SmartLogger.getLogger().log(Level.SEVERE, "Unable to define deserialized alert type.", var28);
               return null;
            }

            newAlert._toList = decodedSerializedInventory.substring(toStart + "to".length() + 2, toEnd);
            newAlert._ccList = decodedSerializedInventory.substring(ccStart + "cc".length() + 2, ccEnd);
            newAlert._bccList = decodedSerializedInventory.substring(bccStart + "bcc".length() + 2, bccEnd);
            newAlert._emailSubject = decodedSerializedInventory.substring(subjectStart + "subject".length() + 2, subjectEnd);
            newAlert._emailContent = decodedSerializedInventory.substring(contentStart + "content".length() + 2, contentEnd);
            newAlert._enabled = Boolean.parseBoolean(decodedSerializedInventory.substring(enabledStart + "enabled".length() + 2, enabledEnd));
            int minStart = decodedSerializedInventory.indexOf("<min>");
            int minEnd = decodedSerializedInventory.indexOf("</min>");
            int maxStart = decodedSerializedInventory.indexOf("<max>");
            int maxEnd = decodedSerializedInventory.indexOf("</max>");
            if(!XmlObject.notNegativeIndex(new int[]{minStart, minEnd, maxStart, maxEnd})) {
               return newAlert;
            } else {
               try {
                  double nfe = Double.parseDouble(decodedSerializedInventory.substring(minStart + "min".length() + 2, minEnd));
                  double tempMax = Double.parseDouble(decodedSerializedInventory.substring(maxStart + "max".length() + 2, maxEnd));
                  return new AlertTemperature(newAlert, nfe, tempMax);
               } catch (NumberFormatException var27) {
                  SmartLogger.getLogger().log(Level.SEVERE, "Invalid min/max for serialized AlertTemperature.", var27);
                  return null;
               }
            }
         }
      } else {
         return null;
      }
   }

   public int getId() {
      return this._id;
   }

   public AlertType getType() {
      return this._type;
   }

   public void setToList(String toList) {
      if(toList != null && !"".equals(toList)) {
         this._toList = toList;
      }
   }

   public String getToList() {
      return this._toList;
   }

   public void setCcList(String ccList) {
      if(ccList != null) {
         this._ccList = ccList;
      }
   }

   public String getCcList() {
      return this._ccList;
   }

   public void setBccList(String bccList) {
      if(bccList != null) {
         this._bccList = bccList;
      }
   }

   public String getBccList() {
      return this._bccList;
   }

   public void setEmailSubject(String emailSubject) {
      if(emailSubject != null && !"".equals(emailSubject.trim())) {
         this._emailSubject = emailSubject;
      }
   }

   public String getEmailSubject() {
      return this._emailSubject;
   }

   public void setEmailContent(String emailContent) {
      if(emailContent != null && !"".equals(emailContent.trim())) {
         this._emailSubject = emailContent;
      }
   }

   public String getEmailContent() {
      return this._emailContent;
   }

   public void setEnabled(boolean enabled) {
      this._enabled = enabled;
   }

   public boolean isEnabled() {
      return this._enabled;
   }
}
