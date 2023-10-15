package com.misterchan.iconeditor.util;

import androidx.annotation.Size;

public class LightingToLevels {
    private static final int IS = 0, IH = 1, OS = 2, OH = 3;
    private static final int[] IS_IH_OS_OH = {IS, IH, OS, OH}, IS_OH_OS_IH = {IS, OH, OS, IH}, OS_IH_IS_OH = {OS, IH, IS, OH}, OS_OH_IS_IH = {OS, OH, IS, IH};
    private static final int[][] IND = {OS_OH_IS_IH, OS_IH_IS_OH, OS_IH_IS_OH, OS_IH_IS_OH, IS_OH_OS_IH, IS_OH_OS_IH, IS_OH_OS_IH, IS_IH_OS_OH, OS_OH_IS_IH, OS_IH_IS_OH, IS_OH_OS_IH, IS_IH_OS_OH};
    private static final float[] V_00_00 = {0x00, 0x00}, V_00_FF = {0x00, 0xFF}, V_FF_00 = {0xFF, 0x00}, V_FF_FF = {0xFF, 0xFF};
    private static final float[][] VAL = {V_00_FF, V_00_FF, V_00_00, V_FF_FF, V_00_FF, V_00_00, V_FF_FF, V_00_FF, V_FF_00, V_FF_00, V_FF_00, V_FF_00};

    private interface Func {
        float apply(float mul, float add, float[] arr);
    }

    private static final Func[] F = {(scale, shift, arr) -> (shift - arr[OS]) / -scale, (scale, shift, arr) -> (arr[OH] - arr[OS]) / scale + arr[IS], (scale, shift, arr) -> shift + arr[IS] * scale, (scale, shift, arr) -> (arr[IH] - arr[IS]) * scale + arr[OS]};

    private LightingToLevels() {
    }

    @Size(4)
    public static float[] lightingToLevels(float mul, float add) {
        if (Float.isInfinite(mul) || Float.isNaN(mul) || Float.isInfinite(add) || Float.isNaN(add))
            return new float[]{0x00, 0x00, 0x00, 0xFF};

        float[] arr = new float[4];
        for (int i = 0; ; ++i) {
            arr[IND[i][0]] = VAL[i][0];
            arr[IND[i][1]] = VAL[i][1];
            arr[IND[i][2]] = F[IND[i][2]].apply(mul, add, arr);
            arr[IND[i][3]] = F[IND[i][3]].apply(mul, add, arr);
            if (0x00 <= arr[IND[i][2]] && arr[IND[i][2]] <= 0xFF && 0x00 <= arr[IND[i][3]] && arr[IND[i][3]] <= 0xFF)
                return arr;
        }
    }
}
