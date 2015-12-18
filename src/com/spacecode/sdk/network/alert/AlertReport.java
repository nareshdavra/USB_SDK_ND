package com.spacecode.sdk.network.alert;

import com.spacecode.sdk.network.alert.Alert;
import java.util.Date;

public class AlertReport {

   private Alert _alert;
   private String _extraData;
   private Date _createdAt;


   public AlertReport(Alert alert, String extraData, Date createdAt) {
      this._alert = alert;
      this._extraData = extraData != null && !"".equals(extraData.trim())?extraData:"";
      this._createdAt = new Date(createdAt.getTime());
   }

   public Alert getAlert() {
      return this._alert;
   }

   public String getAdditionalData() {
      return this._extraData;
   }

   public Date getCreatedAt() {
      return new Date(this._createdAt.getTime());
   }
}
