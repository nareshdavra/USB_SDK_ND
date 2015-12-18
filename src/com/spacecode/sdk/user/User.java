package com.spacecode.sdk.user;

import com.spacecode.sdk.SmartLogger;
import com.spacecode.sdk.XmlObject;
import com.spacecode.sdk.user.data.FingerIndex;
import com.spacecode.sdk.user.data.GrantType;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.DatatypeConverter;

public class User {

   private final String _username;
   private final Map _fingerprintTemplates;
   private String _badgeNumber;
   private GrantType _grantType;
   static final String NODE_USERNAME = "username";
   static final String NODE_BADGE_NUMBER = "badgeNumber";
   static final String NODE_GRANT_TYPE = "grantType";
   static final String NODE_FINGERS = "fingers";
   static final String NODE_FINGER = "finger";
   static final String ATTR_FINGER_INDEX = "index";
   private static final Pattern FINGERPRINT_PATTERN = Pattern.compile("<finger index=\"([0-9])\">(.*?)</finger>");


   public User(String username, GrantType grantType) {
      this(username, grantType, "", new EnumMap(FingerIndex.class));
   }

   public User(String username, GrantType grantType, String badgeNumber) {
      this(username, grantType, badgeNumber, new EnumMap(FingerIndex.class));
   }

   public User(String username, GrantType grantType, String badgeNumber, Map fingerprintTemplates) {
      this._username = username;
      this._grantType = grantType;
      this._badgeNumber = badgeNumber != null && !"".equals(badgeNumber.trim())?badgeNumber:"";
      this._fingerprintTemplates = (Map)(fingerprintTemplates != null?fingerprintTemplates:new EnumMap(FingerIndex.class));
   }

   public final String getUsername() {
      return this._username;
   }

   public final String getFingerprintTemplate(FingerIndex fingerIndex) {
      return (String)this._fingerprintTemplates.get(fingerIndex);
   }

   final void setFingerprintTemplate(FingerIndex fingerIndex, String template) {
      if(template != null && !"".equals(template.trim())) {
         this._fingerprintTemplates.put(fingerIndex, template);
      } else {
         this._fingerprintTemplates.remove(fingerIndex);
      }

   }

   public final List getEnrolledFingersIndexes() {
      ArrayList result = new ArrayList();
      result.addAll(this._fingerprintTemplates.keySet());
      return result;
   }

   public final String getBadgeNumber() {
      return this._badgeNumber;
   }

   final void setBadgeNumber(String badgeNumber) {
      this._badgeNumber = badgeNumber;
   }

   public final GrantType getPermission() {
      return this._grantType;
   }

   final void setPermission(GrantType grantType) {
      this._grantType = grantType;
   }

   public final String serialize() {
      StringBuilder sb = new StringBuilder();
      sb.append("<user>");
      sb.append("<").append("username").append(">");
      sb.append(this._username);
      sb.append("</").append("username").append(">");
      sb.append("<").append("badgeNumber").append(">");
      sb.append(this._badgeNumber);
      sb.append("</").append("badgeNumber").append(">");
      sb.append("<").append("grantType").append(">");
      sb.append(this._grantType);
      sb.append("</").append("grantType").append(">");
      sb.append("<").append("fingers").append(">");
      Iterator uee = this._fingerprintTemplates.entrySet().iterator();

      while(uee.hasNext()) {
         Entry entry = (Entry)uee.next();
         sb.append(String.format("<%1$s %2$s=\"%3$d\">%4$s</%1$s>", new Object[]{"finger", "index", Integer.valueOf(((FingerIndex)entry.getKey()).getIndex()), entry.getValue()}));
      }

      sb.append("</").append("fingers").append(">");
      sb.append("</user>");

      try {
         return DatatypeConverter.printBase64Binary(sb.toString().getBytes("UTF-8"));
      } catch (UnsupportedEncodingException var4) {
         SmartLogger.getLogger().log(Level.SEVERE, "Unable to encode serialized user with UTF-8 charset.", var4);
         return "";
      }
   }

   public static User deserialize(String serializedUser) {
      if(serializedUser == null) {
         return null;
      } else {
         String decodedSerializedInventory;
         try {
            decodedSerializedInventory = new String(DatatypeConverter.parseBase64Binary(serializedUser), "UTF-8");
         } catch (UnsupportedEncodingException var20) {
            SmartLogger.getLogger().log(Level.SEVERE, "Unable to decode serialized inventory with UTF-8 charset.", var20);
            return null;
         }

         int usernameStart = decodedSerializedInventory.indexOf("<username>");
         int usernameEnd = decodedSerializedInventory.indexOf("</username>");
         int grantTypeStart = decodedSerializedInventory.indexOf("<grantType>");
         int grantTypeEnd = decodedSerializedInventory.indexOf("</grantType>");
         int badgeIdStart = decodedSerializedInventory.indexOf("<badgeNumber>");
         int badgeIdEnd = decodedSerializedInventory.indexOf("</badgeNumber>");
         int fingersStart = decodedSerializedInventory.indexOf("<fingers>");
         int fingersEnd = decodedSerializedInventory.indexOf("</fingers>");
         if(!XmlObject.notNegativeIndex(new int[]{usernameStart, usernameEnd, grantTypeStart, grantTypeEnd, badgeIdStart, badgeIdEnd, fingersStart, fingersEnd})) {
            return null;
         } else {
            String username = decodedSerializedInventory.substring(usernameStart + "username".length() + 2, usernameEnd);
            String badgeId = decodedSerializedInventory.substring(badgeIdStart + "badgeNumber".length() + 2, badgeIdEnd);

            GrantType grantType;
            try {
               grantType = GrantType.valueOf(decodedSerializedInventory.substring(grantTypeStart + "grantType".length() + 2, grantTypeEnd));
            } catch (IllegalArgumentException var19) {
               SmartLogger.getLogger().log(Level.SEVERE, "Unable to define deserialized user\'s grant type.", var19);
               grantType = GrantType.UNDEFINED;
            }

            String fingersList = decodedSerializedInventory.substring(fingersStart + "fingers".length() + 2, fingersEnd);
            Matcher matcher = FINGERPRINT_PATTERN.matcher(fingersList);
            HashMap fingers = new HashMap();

            while(matcher.find()) {
               if(matcher.groupCount() >= 2) {
                  try {
                     int iae = Integer.parseInt(matcher.group(1));
                     FingerIndex index = FingerIndex.getValueByIndex(iae);
                     if(index != null) {
                        fingers.put(index, matcher.group(2));
                     }
                  } catch (IllegalArgumentException var18) {
                     SmartLogger.getLogger().log(Level.SEVERE, "Invalid FingerIndex value in deserialized user.", var18);
                  }
               }
            }

            return new User(username, grantType, badgeId, fingers);
         }
      }
   }

}
