package xyz.p3psi.iorgane.cup.ui;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.polidea.rxandroidble2.NotificationSetupMode;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.RxBleDeviceServices;
import com.polidea.rxandroidble2.scan.ScanSettings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import xyz.p3psi.iorgane.cup.Constant;
import xyz.p3psi.iorgane.cup.R;
import xyz.p3psi.iorgane.cup.MainApplication;
import xyz.p3psi.iorgane.cup.SettingActivity;
import xyz.p3psi.iorgane.cup.Utils;
import xyz.p3psi.iorgane.cup.adapters.SearchViewAdapter;
import xyz.p3psi.iorgane.cup.bean.Cup;
import xyz.p3psi.iorgane.cup.BleUtils;

import static com.polidea.rxandroidble2.RxBleClient.State.BLUETOOTH_NOT_AVAILABLE;
import static com.polidea.rxandroidble2.RxBleClient.State.BLUETOOTH_NOT_ENABLED;
import static com.polidea.rxandroidble2.RxBleClient.State.LOCATION_PERMISSION_NOT_GRANTED;
import static com.polidea.rxandroidble2.RxBleClient.State.LOCATION_SERVICES_NOT_ENABLED;
import static com.polidea.rxandroidble2.RxBleClient.State.READY;
import static xyz.p3psi.iorgane.cup.BleUtils.parseCommand;
import static xyz.p3psi.iorgane.cup.Utils.parseBLERecord;

public class SearchCup extends AppCompatActivity {

    final static String TAG = "SearchCup";

    MainApplication mainApplication = (MainApplication)getApplication();

    private RecyclerView recyclerView;
    private SearchViewAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private RxBleClient rxBleClient;

    private List<RxBleDevice> cupList;
    private List<byte[]> cupRecordList;

    private Toolbar toolbar;

    private BluetoothGattCharacteristic bluetoothGattCharacteristic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_cup);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("点击右下角按钮开始扫描");

        this.rxBleClient =  MainApplication.rxBleClient;

        this.cupList = new ArrayList<RxBleDevice>();
        this.cupRecordList = new ArrayList<byte[]>();

        mAdapter = new SearchViewAdapter(cupList, cupRecordList);
        recyclerView = (RecyclerView) findViewById(R.id.scan_devices_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);


        mAdapter.setOnItemClickListener( new SearchViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final int position) {
                Intent intent = new Intent(SearchCup.this, SettingActivity.class);
                intent.putExtra("addr", cupList.get(position).getMacAddress());
                startActivity(intent);
            }
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startBLESearch();
            }
        });

    }

    private void startBLESearch() {
        cupList.clear();
        cupRecordList.clear();
        Log.d(TAG, "startBLESearch: start searching");
        Disposable scanSubscription = rxBleClient.scanBleDevices(
                new ScanSettings.Builder()
                        .build()
        ).subscribe(
                scanResult -> {
                    Log.d(TAG, "startBLESearch: search " + scanResult.getBleDevice().getMacAddress());
                    if (scanResult.getBleDevice().getName().equalsIgnoreCase("OrangeCup")) {
                        if (!cupList.contains(scanResult.getBleDevice())){
                            cupList.add(scanResult.getBleDevice());
                        }
                        if (!cupRecordList.contains(scanResult.getScanRecord().getBytes())) {
                            cupRecordList.add(scanResult.getScanRecord().getBytes());
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                },
                throwable -> {
                    Log.e(TAG, "startBLESearch: ", throwable);
                    Toast.makeText(this, "开启蓝牙后再进行操作", Toast.LENGTH_SHORT);
                }
        );
    }

}