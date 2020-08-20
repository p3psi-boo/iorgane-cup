package xyz.p3psi.iorgane.cup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jakewharton.rx.ReplayingShare;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;


import org.w3c.dom.Text;


import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import xyz.p3psi.iorgane.cup.bean.WaterInfo;

import static xyz.p3psi.iorgane.cup.MainApplication.rxBleClient;
import static xyz.p3psi.iorgane.cup.BleUtils.parseCommand;

public class SettingActivity extends AppCompatActivity {

    final static String TAG = "SettingActivity";

    TextView linkTextView;
    TextView tempTextView;
    TextView batTextView;
    TextView volTextView;
    Button updateTimeButton;
    Button adjustCupButton;
    Button updateInfoButton;
    Button connectButton;

    RxBleDevice rxBleDevice;

    String macAddr;

    WaterInfo waterInfo;

    Handler handler;

    private Observable<RxBleConnection> connectionObservable;
    private PublishSubject<Boolean> disconnectTriggerSubject = PublishSubject.create();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Intent intent = getIntent();
        macAddr = intent.getStringExtra("addr");
        if (macAddr == null) {
            Log.d(TAG, "onCreate: ");
            this.finish();
        }

        rxBleDevice = rxBleClient.getBleDevice(macAddr);
        connectionObservable = prepareConnectionObservable();


        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.arg1 == 1) {
                    WaterInfo waterInfo = (WaterInfo) msg.obj;
                    batTextView.setText(waterInfo.batter);
                    tempTextView.setText(waterInfo.temp);
                    volTextView.setText(waterInfo.ml);
                }
            }
        };

        linkTextView = (TextView) findViewById(R.id.link_status);
        tempTextView = (TextView) findViewById(R.id.temp_status);
        batTextView = (TextView) findViewById(R.id.battery_status);
        volTextView = (TextView) findViewById(R.id.volume_status);


        updateInfoButton = (Button) findViewById(R.id.get_info_btn);
        updateInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnected()) {
                    final Disposable disposable = connectionObservable
                            .firstOrError()
                            .subscribe(
                                    rxBleConnection1 -> {
                                        rxBleConnection1.writeCharacteristic(Constant.UUID_KEY_DATA_NEW_W, "SJ10086".getBytes()).subscribe();
                                        rxBleConnection1.writeCharacteristic(Constant.UUID_KEY_DATA_NEW_W, "SE".getBytes()).subscribe();
                                        rxBleConnection1.setupNotification(Constant.UUID_KEY_DATA_NEW_R)
                                                .doOnNext(notificationObservable -> {
                                                })
                                                .flatMap(notificationObservable -> notificationObservable)
                                                .subscribe(
                                                        bytes -> {
                                                            waterInfo = parseCommand(bytes);
                                                            Message message = new Message();
                                                            message.arg1 = 1;
                                                            message.obj = waterInfo;
                                                            handler.sendMessage(message);
                                                        },
                                                        throwable -> Log.e(TAG, "onClick: ", throwable)
                                                );
                                    },
                                    throwable -> Log.e(TAG, "onClick: ", throwable)
                            );
                }
            }
        });
        adjustCupButton = (Button) findViewById(R.id.adjust_cup_btn);
        adjustCupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnected()) {
                    final Disposable disposable = connectionObservable
                            .firstOrError()
                            .flatMap(rxBleConnection -> rxBleConnection.writeCharacteristic(Constant.UUID_KEY_DATA_NEW_W, new byte[]{83, -90}))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    bytes -> Log.d(TAG, "onClick: Ajust Cup"),
                                    throwable -> Log.e(TAG, "onClick: ", throwable)
                            );
                    compositeDisposable.add(disposable);
                }
            }
        });
        updateTimeButton = (Button) findViewById(R.id.update_time_btn);
        updateTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnected()) {
                    byte[] data = new byte[6];
                    data[0] = 83;
                    data[1] = 84;
                    Utils.intToBytes((int) ((System.currentTimeMillis() / 1000) + 28800), data, 2);
                    final Disposable disposable = connectionObservable
                            .firstOrError()
                            .flatMap(rxBleConnection -> rxBleConnection.writeCharacteristic(Constant.UUID_KEY_DATA_NEW_W, data))
                            .subscribe(
                                    bytes -> Log.d(TAG, "onClick: Set time"),
                                    throwable -> Log.e(TAG, "onClick: ", throwable)
                            );
                    compositeDisposable.add(disposable);
                }
            }
        });
        connectButton = (Button) findViewById(R.id.connect_btn);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnected()) {
                    triggerDisconnect();
                } else {
                    final Disposable connectionDisposable = connectionObservable
                            .flatMapSingle(RxBleConnection::discoverServices)
                            .flatMapSingle(rxBleDeviceServices -> rxBleDeviceServices.getCharacteristic(Constant.UUID_KEY_DATA_NEW_R))
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnSubscribe(disposable -> connectButton.setText("连接中"))
                            .subscribe(
                                    characteristic -> {
                                        Log.d(TAG, "onClick: Hey, connection has been established!");
                                        connectButton.setText("已连接");
                                    },
                                    throwable -> Log.e(TAG, "onClick: ", throwable)
                            );
                }
            }
        });
    }

    private Observable<RxBleConnection> prepareConnectionObservable() {
        return rxBleDevice
                .establishConnection(false)
                .takeUntil(disconnectTriggerSubject)
                .compose(ReplayingShare.instance());
    }

    private boolean isConnected() {
        return rxBleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;
    }

    private void triggerDisconnect() {
        disconnectTriggerSubject.onNext(true);
    }
}