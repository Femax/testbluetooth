package ru.advantum.testbluetooth;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by fedosov on 7/19/17.
 */

class DeviceAdapter extends BaseAdapter {
    List<BluetoothDevice> bluetoothDevices;

    DeviceAdapter(Set<BluetoothDevice> bluetoothDevices) {
        this.bluetoothDevices = new ArrayList<>();
        this.bluetoothDevices.addAll(bluetoothDevices);
    }

    @Override
    public int getCount() {
        return bluetoothDevices.size();
    }

    @Override
    public BluetoothDevice getItem(int i) {
        return bluetoothDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_device_list, viewGroup, false);
        }

        ((TextView) view.findViewById(R.id.device_name)).setText("" + bluetoothDevices.get(i).getName());

        return view;
    }

    @Override
    public CharSequence[] getAutofillOptions() {
        return new CharSequence[0];
    }
}
