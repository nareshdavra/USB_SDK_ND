package com.spacecode.sdk.device.data;

/**
 * Created by MY on 13/09/2015.
 */
public enum DoorInfo {
    DI_NO_DOOR("DI_NO_DOOR",0),
    DI_MASTER_DOOR("DI_MASTER_DOOR",1),
    DI_SLAVE_DOOR("DI_SLAVE_DOOR",2);

    private DoorInfo(String var1, int var2) {}

    private static final DoorInfo[] $values = new DoorInfo[]{DI_NO_DOOR,DI_MASTER_DOOR,DI_SLAVE_DOOR};

}

