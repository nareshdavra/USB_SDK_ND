package com.spacecode.sdk.user.data;


public enum FingerIndex {

   LEFT_PINKY("LEFT_PINKY", 0, 0),
   LEFT_RING("LEFT_RING", 1, 1),
   LEFT_MIDDLE("LEFT_MIDDLE", 2, 2),
   LEFT_INDEX("LEFT_INDEX", 3, 3),
   LEFT_THUMB("LEFT_THUMB", 4, 4),
   RIGHT_THUMB("RIGHT_THUMB", 5, 5),
   RIGHT_INDEX("RIGHT_INDEX", 6, 6),
   RIGHT_MIDDLE("RIGHT_MIDDLE", 7, 7),
   RIGHT_RING("RIGHT_RING", 8, 8),
   RIGHT_PINKY("RIGHT_PINKY", 9, 9);
   private final int _index;
   // $FF: synthetic field
   private static final FingerIndex[] $VALUES = new FingerIndex[]{LEFT_PINKY, LEFT_RING, LEFT_MIDDLE, LEFT_INDEX, LEFT_THUMB, RIGHT_THUMB, RIGHT_INDEX, RIGHT_MIDDLE, RIGHT_RING, RIGHT_PINKY};


   private FingerIndex(String var1, int var2, int index) {
      this._index = index;
   }

   public int getIndex() {
      return this._index;
   }

   public static FingerIndex getValueByIndex(int index) {
      FingerIndex[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         FingerIndex fingerIndex = arr$[i$];
         if(fingerIndex._index == index) {
            return fingerIndex;
         }
      }

      return null;
   }

}
