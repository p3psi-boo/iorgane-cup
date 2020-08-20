package xyz.p3psi.iorgane.cup;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.util.Log;

import com.polidea.rxandroidble2.RxBleClient;
import xyz.p3psi.iorgane.cup.MainApplication;
import xyz.p3psi.iorgane.cup.bean.WaterInfo;

public class BleUtils {

    private static final String TAG = "BleUtils";

    public BluetoothGattCharacteristic characteristicWrite = null;
    public BluetoothGattCharacteristic characteristicRead = null;

    public static WaterInfo parseCommand(byte[] data) {
        WaterInfo wi = new WaterInfo();
        Log.e(TAG, "data=" + new String(data));
        if (data[0] != 83) {
            wi.returncode = "500";
        } else if (data[1] == 65) {
            wi.returncode = Constant.SET_BLOOETOOTH_RETURNCODE;
            long timel = ((long) Utils.bytesToInt(data, 2)) - 28800;
            Log.e(TAG, "time=" + timel);
            wi.time = Utils.TimestampToStr(timel);
            wi.ml = new StringBuilder().append(Utils.bytesToWord(data, 6)).toString();
            wi.temp = new StringBuilder().append(Utils.bytesToWord(data, 8)).toString();
            wi.batter = new StringBuilder().append(Utils.bytesToWord(data, 10)).toString();
            Log.e(TAG, "wi.time=" + wi.time + ",wi.ml=" + wi.ml + ",wi.temp=" + wi.temp + ",wi.batter=" + wi.batter);
        } else if (data[1] == 69) {
            wi.returncode = "203";
        } else if (data[1] != 72) {
            wi.returncode = "500";
        } else if (data[2] == 48) {
            wi.returncode = "201";
        } else {
            wi.returncode = "202";
        }
        Log.e(TAG, "read message" + new String(data));
        return wi;
    }
}
