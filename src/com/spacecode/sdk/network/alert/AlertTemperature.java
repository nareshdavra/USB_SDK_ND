package com.spacecode.sdk.network.alert;

import com.spacecode.sdk.SmartLogger;
import com.spacecode.sdk.network.alert.Alert;
import com.spacecode.sdk.network.alert.AlertType;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import javax.xml.bind.DatatypeConverter;

public class AlertTemperature extends Alert {

   static final String NODE_MIN = "min";
   static final String NODE_MAX = "max";
   private double _temperatureMin;
   private double _temperatureMax;


   AlertTemperature(Alert alert, double temperatureMin, double temperatureMax) {
      this(alert.getId(), alert.getToList(), alert.getCcList(), alert.getBccList(), alert.getEmailSubject(), alert.getEmailContent(), alert.isEnabled(), temperatureMin, temperatureMax);
   }

   public AlertTemperature(String to, String emailSubject, String emailContent, boolean enabled, double temperatureMin, double temperatureMax) {
      this(to, "", "", emailSubject, emailContent, enabled, temperatureMin, temperatureMax);
   }

   public AlertTemperature(String to, String cc, String bcc, String emailSubject, String emailContent, boolean enabled, double temperatureMin, double temperatureMax) {
      this(0, to, cc, bcc, emailSubject, emailContent, enabled, temperatureMin, temperatureMax);
   }

   @Deprecated
   public AlertTemperature(int id, String to, String cc, String bcc, String emailSubject, String emailContent, boolean enabled, double temperatureMin, double temperatureMax) {
      super(id, AlertType.TEMPERATURE, to, cc, bcc, emailSubject, emailContent, enabled);
      this._temperatureMin = temperatureMin;
      this._temperatureMax = temperatureMax;
   }

   public String serialize() {
      String serializedAlert = this.alertToXml();
      StringBuilder sb = new StringBuilder(serializedAlert.replace("</alert>", ""));
      sb.append("<temperature>");
      sb.append("<min>");
      sb.append(this._temperatureMin);
      sb.append("</min>");
      sb.append("<max>");
      sb.append(this._temperatureMax);
      sb.append("</max>");
      sb.append("</temperature>");
      sb.append("</alert>");

      try {
         return DatatypeConverter.printBase64Binary(sb.toString().getBytes("UTF-8"));
      } catch (UnsupportedEncodingException var4) {
         SmartLogger.getLogger().log(Level.SEVERE, "Unable to encode serialized user with UTF-8 charset.", var4);
         return "";
      }
   }

   public double getTemperatureMin() {
      return this._temperatureMin;
   }

   public double getTemperatureMax() {
      return this._temperatureMax;
   }

   public void setTemperatureMin(double temperatureMin) {
      this._temperatureMin = temperatureMin;
   }

   public void setTemperatureMax(double temperatureMax) {
      this._temperatureMax = temperatureMax;
   }
}
