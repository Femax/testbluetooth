package ru.advantum.testbluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
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
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket socket;
    BluetoothDevice myDevice;
    OutputStream outputStream;
    TextView textView1;
    private SensorManager sensorManager;
    private BluetoothSocket bluetoothSocket;
    private BluetoothAdapter mBluetoothAdapter;
    private ListView mListView;
    private DeviceAdapter mAdapter;
    private BluetoothSocket mmSocket;
    private OutputStream mmOutputStream;
    private InputStream mmInputStream;
    private Button textBT;
    private Handler handler;

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

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
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
        textBT = findViewById(R.id.text_button);
        textView1 = findViewById(R.id.textview);
        textBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myDevice == null) {
                    findBT("VEGA");
                } else try {
                    openBT();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    String str = (String) msg.obj;
                    textView1.setText(str);
                }
                super.handleMessage(msg);
            }
        };
        Button sendBt = findViewById(R.id.text_send);
        sendBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    sendData();
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
            mmInputStream = socket.getInputStream();
            new ConnectedThread(socket).start();


            textBT.setText("BT Connected");

        } else {
            textBT.setText("BT Error");

        }
    }

    void sendData() throws IOException {
        String[] size1 = new String[]{"0500", "0400", "0040", "0004"};
        String type = "05";
        String key = "31323334";
        byte[] crc2 = new byte[]{(byte) 0x04, (byte) 0x00, (byte) 0x05, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34
                , (byte) 0xA2, (byte) 0x4b, (byte) 0x64, (byte) 0x1b};


        String message = "\n";

        outputStream.write(crc2);


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
                if (device.getName().contains(myBTstring))//
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

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
            }
            mmInStream = tmpIn;
        }

        public void run() {
            byte[] buffer = new byte[5];
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    String str = new String(buffer);
                    handler.obtainMessage(1, bytes, -1, str).sendToTarget();

                } catch (Exception e) {
                    System.out.print("read error");
                    break;
                }

            }
        }
    }
}



