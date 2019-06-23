package com.example.bluetoothchat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Process;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;
import android.view.View;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    final int DISCOVERABLE_REQ = 0;
    final int ENABLE_REQ = 1;
    final int BLUETOOTH_ADMIN_CODE = 101;
    final int BLUETOOTH_CODE = 102;
    final String tag = "Main_Activity";

    static IntentFilter bluFilter;

    BluetoothReceiver receiver;
    BluetoothAdapter blu;
    ConnectedDevices deviceFragment;

    static {
        bluFilter = new IntentFilter();
      /*  bluFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); unused right now
        bluFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); */
        bluFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        bluFilter.addAction(BluetoothDevice.ACTION_FOUND);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {Manifest.permission.BLUETOOTH};

            android.support.v4.app.ActivityCompat.requestPermissions(this, permissions, BLUETOOTH_CODE);
        }

        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {Manifest.permission.BLUETOOTH_ADMIN};

            android.support.v4.app.ActivityCompat.requestPermissions(this, permissions, BLUETOOTH_ADMIN_CODE);
        }

        receiver = new BluetoothReceiver();
        registerReceiver(receiver, bluFilter);
    }

    @Override
    public void onStart() {
        super.onStart();

        blu = BluetoothAdapter.getDefaultAdapter(); //ask for discovery when the activity becomes visible

        if (blu != null && !blu.isEnabled()) {
            Intent bluIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE); //request to enable bluetooth & make device discoverable
            startActivityForResult(bluIntent, DISCOVERABLE_REQ);
        }
    }

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {
        if (reqCode == DISCOVERABLE_REQ) {
            if (resCode != RESULT_OK) {
                Toast.makeText(this, "Unable to add contacts", Toast.LENGTH_LONG).show();
                Intent bluIntent2 = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(bluIntent2, ENABLE_REQ);
            }
        } else if (reqCode == ENABLE_REQ) {
            if (resCode != RESULT_OK) {
                Toast.makeText(this, "Bluetooth is required", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Toast.makeText(this, "Checking for bluetooth devices", Toast.LENGTH_LONG).show();

        if (blu != null && !blu.isDiscovering())
            blu.startDiscovery();

        deviceFragment = (ConnectedDevices) getSupportFragmentManager().findFragmentById(R.id.listFragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_exit:
                moveTaskToBack(true);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
                break;
            case R.id.nav_locate:
                break;
            case R.id.nav_savedmessages:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new FragmentSavedMessages()).commit();
                break;
            case R.id.nav_settings:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new FragmentSettings()).commit();
                break;
        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

        if (blu != null && blu.isDiscovering())
            blu.cancelDiscovery();
    }

    public void settingButtonPressed(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setType("application/vnd.javadude.data");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (blu != null && blu.isDiscovering())
            blu.cancelDiscovery();

        unregisterReceiver(receiver);
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] permissions, int[] results) {
        if (results.length != 0) {
            if (results[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Required permissions not granted", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    public void onNewDevices(View view) { //button click to view discovered devices
        Bundle bundle = new Bundle();
        bundle.putString("BLU_ACTION", android.bluetooth.BluetoothDevice.ACTION_FOUND);

        loadDeviceList(bundle);
    }

    public void onContacts(View view) { //button click to view already paired devices
        loadDeviceList(new Bundle());
    }

    public void loadDeviceList(Bundle bundle) {
        deviceFragment.update(bundle);
    }

    //inner class only used by MainActivity allows UI to be changed directly after broadcast
    private class BluetoothReceiver extends BroadcastReceiver {
        private boolean CHANGED = false;

        public void onReceive(Context broadcastContext, Intent intent) {
            String action = intent.getAction();
            Bundle arguments = new Bundle(); //to be sent to the device list
            arguments.putString("BLU_ACTION", action);

            switch (action) {
                case BluetoothDevice.ACTION_FOUND: //store the found device as a Parcelable
                    BluetoothDevice foundDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    arguments.putParcelable("DEVICE", foundDevice);
                    break;
            }

            CHANGED = true;
            loadDeviceList(arguments);
        }

        public boolean isChanged() {
            boolean ret = CHANGED;

            if (CHANGED)
                CHANGED = false;

            return ret;
        }
    }

}
