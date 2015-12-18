package com.spacecode.sdk.device.event;

import com.spacecode.sdk.device.event.DeviceEventHandler;
import com.spacecode.sdk.user.User;
import com.spacecode.sdk.user.data.AccessType;

public interface AccessControlEventHandler extends DeviceEventHandler {

   void authenticationSuccess(User var1, AccessType var2, boolean var3);

   void authenticationFailure(User var1, AccessType var2, boolean var3);
}
