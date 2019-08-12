package com.example.bluetoothchat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
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

import java.io.IOException;
import java.net.Socket;

import static com.example.bluetoothchat.Messenger.conversation;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    final int DISCOVERABLE_REQ = 0;
    final int ENABLE_REQ = 1;
    final int BLUETOOTH_ADMIN_CODE = 101;
    final int BLUETOOTH_CODE = 102;
    final String tag = "Main_Activity";

    static IntentFilter bluFilter;
    public static BluetoothSocket ssocket = null;
    AcceptThread acceptThread;
    String curPage = "start"; //start or chat
    Bundle status;

    BluetoothReceiver receiver;
    BluetoothAdapter blu;
    ConnectedDevices deviceFragment;

    static {
        bluFilter = new IntentFilter();
      /*  bluFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); unused right now
        bluFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); */
        bluFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        bluFilter.addAction(BluetoothDevice.ACTION_FOUND);
        bluFilter.addAction("com.example.bluetoothchat.CHAT");
        bluFilter.addAction("com.example.bluetoothchat.LIST");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        status = savedInstanceState;
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
        navigationView.setNavigationItemSelectedListener(this);

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
        ssocket = null;
        acceptThread = null;
        acceptThread = new AcceptThread();
        acceptThread.start();

        if (blu != null && !blu.isEnabled()) {
            Intent bluIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE); //request to enable bluetooth & make device discoverable
            startActivityForResult(bluIntent, DISCOVERABLE_REQ);
        }

        if(status == null) {
            status = new Bundle();
            status.putString("Page", "list");
            status.putString("List", "paired");
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

        deviceFragment = (ConnectedDevices) getSupportFragmentManager().findFragmentById(R.id.listFragment);

        if(status != null) {
            if (status.getString("Page").equals("list")) {
                if (status.getString("List").equals("discovered")) {
                    Toast.makeText(this, "Checking for bluetooth devices", Toast.LENGTH_LONG).show();
                    if (blu != null && !blu.isDiscovering())
                        blu.startDiscovery();

                    onNewDevices(findViewById(R.id.foundBtn));
                } //default screen is already paired devices
            } else {
                deviceFragment.navToChat(status.getParcelable("device"), new Intent(this, Messenger.class));
            }
        }
        else {
            status = new Bundle();
            status.putString("Page", "list");
        }
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

    @Override
    public void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        status = state;
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        state = (Bundle)status.clone();
        super.onSaveInstanceState(state);
    }

    public void onNewDevices(View view) { //button click to view discovered devices
        Bundle bundle = new Bundle();
        bundle.putString("BLU_ACTION", android.bluetooth.BluetoothDevice.ACTION_FOUND);

        status.putString("List", "discovered");

        loadDeviceList(bundle);
    }

    public void onContacts(View view) { //button click to view already paired devices
        status.putString("List", "paired");

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
                case "com.example.bluetoothchat.CHAT":
                    status.putString("Page", "chat");
                    status.putParcelable("Device", intent.getParcelableExtra("device"));
                    break;
                case "com.example.bluetoothchat.LIST":
                    status.putString("Page", "list");
                    break;
            }

            CHANGED = true;
            loadDeviceList(arguments);
        }

        public boolean isChanged() {
            boolean ret = CHANGED;

            if(CHANGED)
                CHANGED = false;

            return ret;
        }
    }
    //for server side connections
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket

            try{
                tmp = blu.listenUsingInsecureRfcommWithServiceRecord(Constants.name, Constants.ID);
            }
            catch(IOException e){
                System.out.println("Error while attempting to create listining socket " + e);
            }


            mmServerSocket = tmp;
        }

        public void run() {

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (socket == null && blu != null) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }
            }
            ssocket = socket;
            final String name = socket.getRemoteDevice().getName();
            System.out.println("Connection Established with: " + name + " on server side");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Connected to: " + name, Toast.LENGTH_LONG).show();
                }
            });

            return;

        }

        public void cancel() {
            try{
                mmServerSocket.close();
            }
            catch (IOException e){
                System.out.println("Error closing socket");
            }
        }

        }


}
