package com.misterchan.iconeditor.util;

public class LightingToLevels {
    private static final int IS = 0, IH = 1, OS = 2, OH = 3;
    private static final int[] IS_IH_OS_OH = {IS, IH, OS, OH}, IS_OH_OS_IH = {IS, OH, OS, IH}, OS_IH_IS_OH = {OS, IH, IS, OH}, OS_OH_IS_IH = {OS, OH, IS, IH};
    private static final int[][] IND = {OS_OH_IS_IH, OS_IH_IS_OH, OS_IH_IS_OH, OS_IH_IS_OH, IS_OH_OS_IH, IS_OH_OS_IH, IS_OH_OS_IH, IS_IH_OS_OH, OS_OH_IS_IH, OS_IH_IS_OH, IS_OH_OS_IH, IS_IH_OS_OH};
    private static final float[] V_00_00 = {0x00, 0x00}, V_00_FF = {0x00, 0xFF}, V_FF_00 = {0xFF, 0x00}, V_FF_FF = {0xFF, 0xFF};
    private static final float[][] VAL = {V_00_FF, V_00_FF, V_00_00, V_FF_FF, V_00_FF, V_00_00, V_FF_FF, V_00_FF, V_FF_00, V_FF_00, V_FF_00, V_FF_00};

    private interface Func {
        float apply(float scale, float shift, float[] arr);
    }

    private static final Func[] F = {(scale, shift, arr) -> (shift - arr[OS]) / -scale, (scale, shift, arr) -> (arr[OH] - arr[OS]) / scale + arr[IS], (scale, shift, arr) -> shift + arr[IS] * scale, (scale, shift, arr) -> (arr[IH] - arr[IS]) * scale + arr[OS]};

    private static boolean check(float[] arr) {
        for (float f : arr) if (0x00 > f || f > 0xFF) return true;
        return false;
    }

    public static float[] lightingToLevels(float scale, float shift) {
        float[] arr = new float[4];
        for (int i = 0; i == 0 || check(arr); ++i) {
            arr[IND[i][0]] = VAL[i][0];
            arr[IND[i][1]] = VAL[i][1];
            arr[IND[i][2]] = F[IND[i][2]].apply(scale, shift, arr);
            arr[IND[i][3]] = F[IND[i][3]].apply(scale, shift, arr);
        }
        return arr;
    }
}
