package tk.seijas.bluetoothcontroller;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.UUID;

public class btController extends ActionBarActivity {

    Button b_red;
    Button b_yellow;
    Button b_green;
    Button b_on;
    Button b_off;
    Button b_disconnect;

    boolean red = false, yellow = false, green = false;

    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;

    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent newint = getIntent();
        address = newint.getStringExtra(MainActivity.EXTRA_ADDRESS);

        setContentView(R.layout.activity_bt_controller);

        b_red = (Button) findViewById(R.id.b_red);
        b_yellow = (Button) findViewById(R.id.b_yellow);
        b_green = (Button) findViewById(R.id.b_green);
        b_on = (Button) findViewById(R.id.b_on);
        b_off = (Button) findViewById(R.id.b_off);
        b_disconnect = (Button) findViewById(R.id.b_disconnect);

        new ConnectBT().execute();
        b_red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( !red ) {
                    btWrite("ro\n");
                } else {
                    btWrite("rf\n");
                }
                red = !red;
            }
        });

        b_yellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( !yellow ) {
                    btWrite("yo\n");
                } else {
                    btWrite("yf\n");
                }
                yellow = !yellow;
            }
        });

        b_green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( !green ) {
                    btWrite("go\n");
                } else {
                    btWrite("gf\n");
                }
                green = !green;
            }
        });

        b_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btWrite("on\n");
            }
        });

        b_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btWrite("off\n");
            }
        });

        b_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect();
            }
        });


    }

    private void Disconnect() {
        if ( btSocket != null ) {
            try {
                btSocket.close();
            } catch (IOException e) {
                msg("Error");
            }
        }
        finish(); //return to the first layout
    }

    private void btWrite(String m) {
        if (btSocket!=null) {
            try {
                btSocket.getOutputStream().write(m.toString().getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    // fast way to call Toast
    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }


    private class ConnectBT extends AsyncTask<Void, Void, Void> { // UI thread

        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(btController.this, "Connecting...", "Please wait!!");
        }

        @Override
        protected Void doInBackground(Void... devices) { //while the progress dialog is shown, the connection is done in background
            try {
                if ( btSocket == null || !isBtConnected ) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID); //create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) { //after the doInBackground, it checks if everything went fine
            super.onPostExecute(result);

            if ( !ConnectSuccess ) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            } else {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}
