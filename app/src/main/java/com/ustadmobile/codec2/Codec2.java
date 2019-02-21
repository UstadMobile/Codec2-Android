package com.ustadmobile.codec2;

public class Codec2 {

    static {
        System.loadLibrary("codec2");
        System.loadLibrary("Codec2JNI");
    }

    public static final int CODEC2_MODE_3200 = 0;
    public static final int CODEC2_MODE_2400 = 1;
    public static final int CODEC2_MODE_1600 = 2;
    public static final int CODEC2_MODE_1400 = 3;
    public static final int CODEC2_MODE_1300 = 4;
    public static final int CODEC2_MODE_1200 = 5;
    public static final int CODEC2_MODE_700 = 6;
    public static final int CODEC2_MODE_700B = 7;
    public static final int CODEC2_MODE_700C = 8;
    public static final int CODEC2_MODE_WB = 9;

    public native static long create(int mode);

    public native static int getSamplesPerFrame(long con);

    public native static int getBitsSize(long con);

    public native static int destroy(long con);

    public native static long encode(long con, short[] buf, char[] bits);

    public native static long decode(long con, short[] buf, char[] bits);
}