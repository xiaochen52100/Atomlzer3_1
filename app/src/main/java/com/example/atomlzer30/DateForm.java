package com.example.atomlzer30;

public class DateForm {
    public static byte[] intToBytesArray(int n) {
        byte[] b = new byte[4];
        b[3] = (byte) (n & 0xff);
        b[2] = (byte) ((n&0xff00) >> 8);
        b[1] = (byte) ((n&0xff0000)>> 16);
        b[0] = (byte) ((n&0xff000000)>> 24);
        return b;

    }
    public static int byteArrayToInt(byte[] b) {
        int res = 0;
        for(int i=0;i<b.length;i++){
            res += (b[i] & 0xff) << ((3-i)*8);
        }
        return res;
    }
    public static double byteArrayToDouble(byte[] Array, int Pos) {
        long accum = 0;
        accum = Array[Pos + 0] & 0xFF;
        accum |= (long) (Array[Pos + 1] & 0xFF) << 8;
        accum |= (long) (Array[Pos + 2] & 0xFF) << 16;
        accum |= (long) (Array[Pos + 3] & 0xFF) << 24;
        accum |= (long) (Array[Pos + 4] & 0xFF) << 32;
        accum |= (long) (Array[Pos + 5] & 0xFF) << 40;
        accum |= (long) (Array[Pos + 6] & 0xFF) << 48;
        accum |= (long) (Array[Pos + 7] & 0xFF) << 56;
        return Double.longBitsToDouble(accum);
    }
    public static byte[] doubleToByteArray(double Value) {
        long accum = Double.doubleToRawLongBits(Value);
        byte[] byteRet = new byte[8];
        byteRet[0] = (byte) (accum & 0xFF);
        byteRet[1] = (byte) ((accum >> 8) & 0xFF);
        byteRet[2] = (byte) ((accum >> 16) & 0xFF);
        byteRet[3] = (byte) ((accum >> 24) & 0xFF);
        byteRet[4] = (byte) ((accum >> 32) & 0xFF);
        byteRet[5] = (byte) ((accum >> 40) & 0xFF);
        byteRet[6] = (byte) ((accum >> 48) & 0xFF);
        byteRet[7] = (byte) ((accum >> 56) & 0xFF);
        return byteRet;
    }


}
