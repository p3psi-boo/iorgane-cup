package xyz.p3psi.iorgane.cup;

import java.sql.Date;
import java.text.SimpleDateFormat;

import xyz.p3psi.iorgane.cup.bean.BLEScanInfo;

public class Utils {

    public static String TimestampToStr(long time) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(1000 * time));
    }

    public static void intToBytes(int n, byte[] array, int offset) {
        array[offset] = (byte) (n & 255);
        array[offset + 1] = (byte) ((n >> 8) & 255);
        array[offset + 2] = (byte) ((n >> 16) & 255);
        array[offset + 3] = (byte) ((n >> 24) & 255);
    }

    public static int bytesToInt(byte[] b, int offset) {
        return (b[offset] & 255) | ((b[offset + 1] & 255) << 8) | ((b[offset + 2] & 255) << 16) | ((b[offset + 3] & 255) << 24);
    }

    public static void wordToBytes(int n, byte[] array, int offset) {
        array[offset] = (byte) (n & 255);
        array[offset + 1] = (byte) ((n >> 8) & 255);
    }

    public static int bytesToWord(byte[] b, int offset) {
        return (b[offset] & 255) | ((b[offset + 1] & 255) << 8);
    }

    public static int bytesToByte(byte[] b, int offset) {
        return b[offset] & 255;
    }

    public static byte[] cutOutByte(byte[] b, int start) {
        if (b.length == 0 || start >= b.length) {
            return null;
        }
        byte[] bjq = new byte[(b.length - start)];
        for (int i = 0; i < b.length - start; i++) {
            bjq[i] = b[start + i];
        }
        return bjq;
    }

    public static byte[] cutOutByte(byte[] b, int start, int length) {
        if (b.length == 0 || start >= b.length || length <= 0) {
            return null;
        }
        byte[] bjq = new byte[length];
        for (int i = 0; i < length; i++) {
            bjq[i] = b[start + i];
        }
        return bjq;
    }

    public static BLEScanInfo parseBLERecord(byte[] record) {
        BLEScanInfo bleScanInfo = new BLEScanInfo();
        int i = 0;
        while (i < record.length - 1) {
            byte length = record[i];
            byte type = record[i + 1];
            if (type == 9) {
                bleScanInfo.localName = new String(cutOutByte(record, i + 2, length - 1));
            } else if (type == -1) {
                bleScanInfo.specificData = new String(cutOutByte(record, i + 2, length - 1));
            }
            i += length + 1;
        }
        return bleScanInfo;
    }
}
