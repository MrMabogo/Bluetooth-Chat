/**
 * Fragment that will contain the list of all paired devices
 */

package com.example.bluetoothchat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.BundleCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.LayoutInflaterCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.TreeMap;

public class ConnectedDevices extends Fragment {
    Set<BluetoothDevice> devices;
    Map<String, BluetoothDevice> foundDevices = new TreeMap<String, BluetoothDevice>();
    BluetoothAdapter adapter;
    ListView list;

    @Override
    public void onCreate(Bundle instance) {
        super.onCreate(instance);

        String info = instance.getString("BLU_ACTION");

        if(info == null || info.equals(android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED) ) {
            showPaired();
        }

        if(info.equals(BluetoothDevice.ACTION_FOUND)) {
            showFound((BluetoothDevice)instance.getParcelable("DEVICE"));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle instance) {
        return inflater.inflate(R.layout.fragment_connected_devices, root);
    }

    @Override
    public void onStop() {
        super.onStop();

        foundDevices.clear();
    }

    public void showPaired() { //puts the paired (bonded) devices into a ListView
        list = getView().findViewById(R.id.deviceList);

        if(adapter.isEnabled()) {
           devices = adapter.getBondedDevices();

           final Map<String, BluetoothDevice> deviceMap = new TreeMap<String, BluetoothDevice>();

           for(BluetoothDevice d : devices) { //need to find a way to device info into the view
               deviceMap.put(d.getName(), d);
           }

           final ArrayAdapter<BluetoothDevice> aAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_selectable_list_item, deviceMap.keySet().toArray());

           list.setAdapter(aAdapter);
           devices.clear();

           list.setOnClickListener(new View.OnClickListener()
            {
            public void onClick(View clicked) {
                Intent intent = new Intent(); //intent to open up chat
                intent.setAction("com.example.bluetoothchat.CHAT"); //presumably
                intent.putExtra("address", deviceMap.get(((android.widget.TextView)clicked).getText()).getAddress());
                }
            });
        }
        else {
            ArrayList<String> empty = new ArrayList<String> ();

            empty.add("Nothing here...");

            final ArrayAdapter<String> eAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, empty);

            list.setAdapter(eAdapter);
        }
    }

    public void showFound(BluetoothDevice device) {
        list = getView().findViewById(R.id.deviceList);
        devices.add(device);

        foundDevices.put(device.getName(), device);

        final ArrayAdapter<BluetoothDevice> aAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, foundDevices.keySet().toArray());
        list.setAdapter(aAdapter);
    }
}
