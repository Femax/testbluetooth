package ru.advantum.testbluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {
    /**
     * Ausgabeformat für Dezimalzahlen
     */
    // TODO: private static final DecimalFormat df = (DecimalFormat)DecimalFormat.getNumberInstance(Locale.US);
    private static final DecimalFormat df = new DecimalFormat(",##0.00000");

    private static final String TAG = "BluetoothActivity";
    private static final int BUFFER_SIZE = 256;
    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket socket;
    BluetoothDevice myDevice;
    OutputStream outputStream;
    private SensorManager sensorManager;
    private BluetoothSocket bluetoothSocket;
    private BluetoothAdapter mBluetoothAdapter;
    private ListView mListView;
    private DeviceAdapter mAdapter;
    private BluetoothSocket mmSocket;
    private OutputStream mmOutputStream;
    private InputStream mmInputStream;

    public static byte[] SendByteData(String hexString) {
        byte[] sendingThisByteArray = new byte[hexString.length() / 2];
        int count = 0;

        for (int i = 0; i < hexString.length() - 1; i += 2) {
            //grab the hex in pairs
            String output = hexString.substring(i, (i + 2));

            //convert the 2 characters in the 'output' string to the hex number
            int decimal = (int) (Integer.parseInt(output, 16));

            //place into array for sending
            sendingThisByteArray[count] = (byte) (decimal);

            Log.d(TAG, "in byte array = " + sendingThisByteArray[count]);
            count++;
        }

        return sendingThisByteArray;
    }


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // UI-Elemente beziehen
        setContentView(R.layout.main);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }


        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            mAdapter = new DeviceAdapter(pairedDevices);
        }

        mListView = (ListView) findViewById(R.id.listview);
        final Handler myHandler = new Handler();
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
                try {
                    mmSocket = mAdapter.getItem(i).createRfcommSocketToServiceRecord(uuid);
                    mmSocket.connect();
                    mmInputStream = mmSocket.getInputStream();
                    new Thread(new Runnable() {
                        public void run() {
                            final byte[] buffer = new byte[BUFFER_SIZE];
                            try {
                                do {
                                    if (mmInputStream.read(buffer) == -1) {
                                        throw new Exception("Error reading data.");
                                    }
                                    myHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            makeToast(buffer.toString());
                                        }
                                    });
                                } while (true);


                            } catch (IOException e) {
                                e.printStackTrace();
                                makeToast(e.getMessage());
                            } catch (Exception e) {
                                makeToast(e.getMessage());

                            }
                        }
                    }).start();
                    mmOutputStream = mmSocket.getOutputStream();

                    mmOutputStream.write(new byte[]{(byte) 0x05, (byte) 0x00, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34
                            , (byte) 0xA2, (byte) 0x4b, (byte) 0x64, (byte) 0x1b});

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    void openBT() throws IOException {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // Standard
        // SerialPortService
        // ID
        if (myDevice != null) // only open a device if device set by string
        // input from user
        {// as in findBT() method
            socket = myDevice.createRfcommSocketToServiceRecord(uuid);
            socket.connect();
            outputStream = socket.getOutputStream();

            textBT.setText("BT Connected");

        } else {
            textBT.setText("BT Error");

        }
    }


    void sendData() throws IOException {
        try {
            relayID = buttonID + 1; // buttons are 0-7, relays are 1-8
            relayID = buttonID;

            String message = Integer.toString(relayID);

            message += "\n";
            outputStream.write(message.getBytes());
            textDebug1.setText("Data Sent: " + message);
        }

    void findBT(String myBTstring) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            textBT.setText("No BT available");
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(myBTstring))//
                {
                    myDevice = device;
                    textBT.setText("Looking for: " + myBTstring);

                    break;
                }
            }
        } else {
            textBT.setText("BT Error"); // probably no BT adapter or a paired device
        }

    }

    public void makeToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}



