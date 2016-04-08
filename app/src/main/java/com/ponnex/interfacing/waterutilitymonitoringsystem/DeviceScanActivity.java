package com.ponnex.interfacing.waterutilitymonitoringsystem;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.skyfishjy.library.RippleBackground;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Ramos on 2/11/2016.
 */
public class DeviceScanActivity extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;

    private static final int REQUEST_ENABLE_BT = 1;

    private FloatingActionButton fab_scan_le;
    private boolean mScanning;

    private RippleBackground rippleBackground;

    private ArrayList<BluetoothDevice> mLeDevices;

    private Display display;
    private Point screenSize = new Point();

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private LocalBroadcastManager broadcastManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_le);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Intent broadcastintent = new Intent("com.ponnex.interfacing.waterbill.SettingsPreferenceFragment");
        broadcastintent.putExtra("isScanning", true);
        broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        broadcastManager.sendBroadcast(broadcastintent);

        display = getWindowManager().getDefaultDisplay();
        mLeDevices = new ArrayList<BluetoothDevice>();

        rippleBackground = (RippleBackground)findViewById(R.id.content_ripple);
        fab_scan_le = (FloatingActionButton) findViewById(R.id.scan_le);
        fab_scan_le.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mScanning) {
                    scanLeDevice(true);
                } else {
                    scanLeDevice(false);
                }
            }
        });

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        scanLeDevice(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            if(!rippleBackground.isRippleAnimationRunning()) {
                rippleBackground.startRippleAnimation();
            }
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            mScanning = true;
        } else {
            if(rippleBackground.isRippleAnimationRunning()) {
                rippleBackground.stopRippleAnimation();
            }
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addDevice(device);
                }
            });
        }
    };

    public void addDevice(final BluetoothDevice device) {
        if(!mLeDevices.contains(device)) {
            mLeDevices.add(device);

            View deviceLe = getLayoutInflater().inflate(R.layout.layout_device_le, rippleBackground);
            LinearLayout linearLayout = (LinearLayout)deviceLe.findViewById(R.id.content_device);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(getRandomX(), getRandomY(), 0, 0);
            linearLayout.setLayoutParams(layoutParams);

            TextView deviceName = (TextView)deviceLe.findViewById(R.id.device_name);
            deviceName.setText(device.getName());

            FloatingActionButton floatingActionButton = (FloatingActionButton)deviceLe.findViewById(R.id.device_le);
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    editor = sharedPreferences.edit();
                    editor.putString("default_ble", device.getAddress());
                    editor.apply();

                    Log.e("Default BLE", device.getAddress());

                    if (mScanning) {
                        if(rippleBackground.isRippleAnimationRunning()) {
                            rippleBackground.stopRippleAnimation();
                        }
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        mScanning = false;
                    }

                    finish();
                }
            });
        }
    }

    public int getRandomX() {
        display.getSize(screenSize);
        final int minLeft = (screenSize.x / 4);
        final int maxLeft = (screenSize.x / 2) - 56;
        int randomLeft = new Random().nextInt((maxLeft - minLeft) + 1) + minLeft;

        final int minRight = (screenSize.x / 2) + 56;
        final int maxRight = (screenSize.x / 4) * 3;
        int randomRight = new Random().nextInt((maxRight - minRight) + 1) + minRight;

        int[] rnd= {randomLeft, randomRight};

        return rnd[new Random().nextInt(rnd.length)];
    }

    public int getRandomY() {
        display.getSize(screenSize);
        final int minTop = (screenSize.y / 4);
        final int maxTop = (screenSize.y / 2) - 56;
        int randomTop = new Random().nextInt((maxTop - minTop) + 1) + minTop;

        final int minBottom = (screenSize.y / 2) + 56;
        final int maxBottom = (screenSize.y / 4) * 3;
        int randomBottom = new Random().nextInt((maxBottom - minBottom) + 1) + minBottom;

        int[] rnd= {randomTop, randomBottom};

        return rnd[new Random().nextInt(rnd.length)];
    }
}