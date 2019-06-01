/**
 * Fragment that will contain the list of all paired devices
 */

package com.example.bluetoothchat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.LayoutInflaterCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Set;
import java.util.ArrayList;

public class ConnectedDevices extends Fragment {
    Set<BluetoothDevice> devices;
    BluetoothAdapter adapter;
    ListView list;

    @Override
    public void onCreate(Bundle instance) {
        super.onCreate(instance);

        String action = getActivity().getIntent().getAction();

        if(action.equals(android.bluetooth.BluetoothAdapter.ACTION_STATE_CHANGED)) {
            refreshList();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle instance) {
        return inflater.inflate(R.layout.fragment_connected_devices, root);
    }

    @Override
    public void onViewCreated(View view, Bundle instance) {

    }

    public void refreshList() {
        if(adapter.isEnabled()) {
           devices = adapter.getBondedDevices();

           ArrayList<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();

           for(BluetoothDevice d : devices) {
               deviceList.add(d);
           }

            ArrayAdapter<BluetoothDevice> aAdapter = new ArrayAdapter(getActivity(), R.layout.fragment_connected_devices, deviceList);

           list = getView().findViewById(R.id.deviceList);
           list.setAdapter(aAdapter);
        }
    }
}
