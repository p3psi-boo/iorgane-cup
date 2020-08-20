package xyz.p3psi.iorgane.cup;

import android.app.Application;

import com.polidea.rxandroidble2.RxBleClient;

public class MainApplication extends Application {

    public static RxBleClient rxBleClient;

    @Override
    public void onCreate() {
        super.onCreate();
        rxBleClient = RxBleClient.create(getApplicationContext());
    }

}
