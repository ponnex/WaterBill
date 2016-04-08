package com.ponnex.interfacing.waterutilitymonitoringsystem;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Ramos on 3/28/2016.
 */
public class MonitoringActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final static String TAG = MonitoringActivity.class
            .getSimpleName();

    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private TextView totalCubicMeter;
    private TextView cubicMeterPerSec;
    private TextView percentText;
    private TextView billableText;
    private TextView budget;
    private Intent gattServiceIntent;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String savedCubicMeter;
    private WaveView waveView;
    private String[] datas;

    //Characteristics for HM-10 serial
    private BluetoothGattCharacteristic characteristicTX;
    private BluetoothGattCharacteristic characteristicRX;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        savedCubicMeter = (sharedPreferences.getString("accumulatedCubicMeter", "Can't Retrieve Data :'("));

        totalCubicMeter = (TextView)findViewById(R.id.total_cubic_meters);
        totalCubicMeter.setText(savedCubicMeter + getResources().getString(R.string.totalcubicmeter));

        cubicMeterPerSec = (TextView)findViewById(R.id.cubic_meters_sec);
        cubicMeterPerSec.setText("No Data");

        percentText = (TextView)findViewById(R.id.percent_remain);
        percentText.setText(sharedPreferences.getString("percent", "No Data"));

        billableText = (TextView)findViewById(R.id.billable_amount);
        budget = (TextView)findViewById(R.id.budget);

        waveView = (WaveView) findViewById(R.id.waveView);
        waveView.setProgress(sharedPreferences.getInt("percentWave", 0));

        ImageButton imagebutton = (ImageButton) findViewById(R.id.button_settings);
        imagebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MonitoringActivity.this, SettingsActivity.class);
                startActivity(intent);
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

        gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        Point size = new Point();
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getSize(size);

        imagebutton.setTranslationX(size.x / 3);
        imagebutton.getLayoutParams().width = size.x / 2;

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(scanningReceiver, new IntentFilter("com.ponnex.interfacing.waterbill.SettingsPreferenceFragment"));
    }

    private BroadcastReceiver scanningReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Boolean isScanning = intent.getBooleanExtra("isScanning", false);
            if (isScanning) {
                mBluetoothLeService.disconnect();
                mBluetoothLeService.close();
            }
        }
    };

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
                    .getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            Snackbar.make(findViewById(R.id.coordinator_layout), "Connecting...", Snackbar.LENGTH_INDEFINITE).show();
            mBluetoothLeService.connect(sharedPreferences.getString("default_ble", "00:00:00:00:00:00"));
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
                    .equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                cubicMeterPerSec.setText("Disconnected");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
                    .equals(action)) {
                setupSerial();
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

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

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(sharedPreferences.getString("default_ble", "00:00:00:00:00:00"));
            Log.d(TAG, "Connect request result=" + result);
        }

        updateUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(scanningReceiver);
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

    private void setupSerial() {

        String uuid;
        String unknownServiceString = getResources().getString(
                R.string.unknown_service);

        for (BluetoothGattService gattService : mBluetoothLeService
                .getSupportedGattServices()) {
            uuid = gattService.getUuid().toString();

            // If the service exists for HM 10 Serial, say so.
            if (SampleGattAttributes.lookup(uuid, unknownServiceString) == "HM 10 Serial") {

                // get characteristic when UUID matches RX/TX UUID
                characteristicTX = gattService
                        .getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
                characteristicRX = gattService
                        .getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);

                mBluetoothLeService.setCharacteristicNotification(
                        characteristicRX, true);
                break;
            }
        }
    }

    public static String truncate(String value, int length) {
        // Ensure String length is longer than requested size.
        if (value.length() > length) {
            return value.substring(0, length);
        } else {
            return value;
        }
    }

    private void displayData(String data) {

        if (data != null) {
            data = truncate(data, 20);
            datas = data.split(";");

            editor = sharedPreferences.edit();
            editor.putString("accumulatedCubicMeter", datas[0]);
            editor.apply();

            calculateAndUpdate();

            savedCubicMeter = (sharedPreferences.getString("accumulatedCubicMeter", "Can't Retrieve Data :'("));
            totalCubicMeter.setText("Accumulated: " + savedCubicMeter + getResources().getString(R.string.totalcubicmeter));

            cubicMeterPerSec.setText("Flow rate: " + datas[1] + getResources().getString(R.string.cubicmetersec));

            Log.e(TAG, "Accumulated: " + savedCubicMeter);
            Log.e(TAG, "CubicMeter/Sec: " + datas[1]);
        }
    }

    public void calculateAndUpdate() {
        float rating = Float.parseFloat(sharedPreferences.getString("rating", "23.61"));
        float budget = Float.parseFloat(sharedPreferences.getString("budget", "0.00"));
        double billable_amount = Double.parseDouble(sharedPreferences.getString("accumulatedCubicMeter", "0.00")) * rating;

        double percent_double = round((100 - (billable_amount / budget) * 100), 1);
        int percent_wave = (int) Math.round(percent_double);

        editor = sharedPreferences.edit();
        editor.putString("billable_amount", billable_amount + "");
        editor.apply();

        editor = sharedPreferences.edit();
        editor.putString("percent", percent_double + "");
        editor.apply();

        editor = sharedPreferences.edit();
        editor.putInt("percentWave", percent_wave);
        editor.apply();

        updateUI();
    }

    public void updateUI() {
        waveView.setProgress(sharedPreferences.getInt("percentWave", 0));
        percentText.setText(sharedPreferences.getString("percent", "0"));
        billableText.setText("₱" + round(Double.parseDouble(sharedPreferences.getString("billable_amount", "0.00")), 2));
        budget.setText("₱" + sharedPreferences.getString("budget", "0.00"));

        Log.e("Percent Wave: ", sharedPreferences.getInt("percentWave", 0) + "");
        Log.e("Percent Text: ", sharedPreferences.getString("percent", "0.00"));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals("rating") || key.equals("budget")) {
            calculateAndUpdate();
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mConnected) {
                    Snackbar.make(findViewById(R.id.coordinator_layout), resourceId, Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(findViewById(R.id.coordinator_layout), resourceId, Snackbar.LENGTH_INDEFINITE)
                            .setAction("RECONNECT", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Snackbar.make(findViewById(R.id.coordinator_layout), "Connecting...", Snackbar.LENGTH_INDEFINITE).show();
                                    bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                                    mBluetoothLeService.disconnect();
                                    mBluetoothLeService.close();
                                    mBluetoothLeService.connect(sharedPreferences.getString("default_ble", "00:00:00:00:00:00"));
                                }
                            }).show();
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter
                .addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
