/**
 * Fragment that will contain the list of all paired devices
 */

package com.example.bluetoothchat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.BundleCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.LayoutInflaterCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.TreeMap;

public class ConnectedDevices extends Fragment{
    public Set<BluetoothDevice> devices;
    Map<String, BluetoothDevice> foundDevices = new TreeMap<String, BluetoothDevice>();
    BluetoothAdapter adapter;
    ListView list;
    Bundle infoBundle = new Bundle();
    BluetoothSocket ssocket;

    @Override
    public void onCreate(Bundle instance) {
        super.onCreate(instance);
        infoBundle = instance;
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter = BluetoothAdapter.getDefaultAdapter();
        try {
            update(infoBundle); //chooses & updates list based on infoBundle
        }
        catch (Exception e){
            System.out.println(e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle instance) {
        return inflater.inflate(R.layout.fragment_connected_devices, root);
    }

    @Override
    public void onStop() {
        super.onStop();

        if(list != null)
            ((ViewGroup)list.getParent()).removeView(list); //must be taken off before updating

        foundDevices.clear();
    }

    public void update(Bundle infoB) {
        String infoS;

        if(infoB == null) //saved instance can be null
            infoS = null;
        else
            infoS = infoB.getString("BLU_ACTION");

        if(infoS == null || infoS.equals(BluetoothAdapter.ACTION_STATE_CHANGED) ) {
            try {
                showPaired();
            }
            catch (Exception e){
                System.out.println(e);
            }
        }
        else if(infoS.equals(BluetoothDevice.ACTION_FOUND)) {
            showFound((BluetoothDevice)infoB.getParcelable("DEVICE"));
        }
    }

    private void showPaired() { //puts the paired (bonded) devices into a ListView
        list = getView().findViewById(R.id.deviceList);

        if(adapter.isEnabled() && list != null) {
           devices = adapter.getBondedDevices();

           final Map<String, BluetoothDevice> deviceMap = new TreeMap<String, BluetoothDevice>();

           for(BluetoothDevice d : devices) { //need to find a way to device info into the view
               deviceMap.put(d.getName(), d);
           }

           final ArrayAdapter<BluetoothDevice> aAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_selectable_list_item, deviceMap.keySet().toArray());

           list.setAdapter(aAdapter);
           devices = null;

           list.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                public void onItemClick(AdapterView parent, View clicked, int loc, long id) {
                    Intent intent = new Intent(getActivity(), Messenger.class); //intent to open up chat
                    BluetoothDevice device = deviceMap.get(((android.widget.TextView)clicked).getText());
                    intent.putExtra("address", device.getAddress());
                    intent.putExtra("ID", getID(device.toString()));
                    getActivity().startActivity(intent);
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

    public int getID(String address){
        int id = 0;
        for(int i = 0; i < address.length(); i++){
            id += (int) address.charAt(i);
        }
        return id;
    }

    private void showFound(BluetoothDevice device) {
        list = getView().findViewById(R.id.deviceList);

        if(device != null) {
            devices.add(device);
            foundDevices.put(device.getName(), device);
        }
        if(foundDevices.isEmpty()) {
            foundDevices.put("Nothing here...", null);
        }

        final ArrayAdapter<BluetoothDevice> aAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, foundDevices.keySet().toArray());
        list.setAdapter(aAdapter);
    }
}
