package com.example.andrie.apprede;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    public final String TAG = "Main";

    private Button btAcelerar;
    private Button btFrear;
    private Button btAbastecer;
    private Switch swLigar;
    private SeekBar tanque;

    private TextView mensagem;
    private EditText edLitros;
    private EditText edKm;
    private EditText edKmh;
    private EditText edTemperatura;
    private Bluetooth bt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //mensagem = (TextView) findViewById(R.id.txMessagem);
        btAbastecer = (Button) findViewById(R.id.btabastecer);
        btAcelerar = (Button) findViewById(R.id.btacelerar);
        btFrear = (Button) findViewById(R.id.btfrear);
        edLitros = (EditText) findViewById(R.id.edLitros);
        edKm = (EditText) findViewById(R.id.edKm);
        edKmh = (EditText) findViewById(R.id.edKmh);
        edTemperatura = (EditText) findViewById(R.id.edTemperatura);
        swLigar  = (Switch) findViewById(R.id.swligar);
        tanque = (SeekBar) findViewById(R.id.seekBar);

      //  tanque.setEnabled(false);
        tanque.setMax(100);


        bt = new Bluetooth(this, mHandler);
        connectService();

        findViewById(R.id.restart).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                connectService();
            }
        });

        swLigar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               if(isChecked){
                   bt.sendMessage("l");
               }else{
                   bt.sendMessage("d");
               }
            }
        });
/*
        elevation = (SeekBar) findViewById(R.id.seekBar);
        elevation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("Seekbar","onStopTrackingTouch ");
                int progress = seekBar.getProgress();
                String p = "n";
                debug.setText(p);
                bt.sendMessage(p);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("Seekbar","onStartTrackingTouch ");
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Log.d("Seekbar", "onProgressChanged " + progress);
            }
        });*/



    }

    public void acelerar(View v) {
        bt.sendMessage("ac");
    }

    public void frear(View v) {
        bt.sendMessage("fr");
    }

    public void abastecer(View v) {
        String litros = edLitros.getText().toString();
        if(litros != null) {
            bt.sendMessage("ab" + Integer.parseInt(litros));
            Toast.makeText(this, "Adicionado " + litros +"L", Toast.LENGTH_SHORT).show();
        }
        edLitros.setText("");
    }

    public void connectService(){
        try {

            Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT).show();

            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter.isEnabled()) {
                bt.start();
                bt.connectDevice("HC-06");
                Log.d(TAG, "Btservice started - listening");
                Toast.makeText(this, bt.getState() + " conectado", Toast.LENGTH_SHORT).show();
            } else {
                Log.w(TAG, "Btservice started - bluetooth is not enabled");
                Toast.makeText(this, "Sem conexao", Toast.LENGTH_SHORT).show();
            }
        } catch(Exception e){
            Log.e(TAG, "Unable to start bt ",e);
            //status.setText("Unable to connect " +e);
        }
    }


    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Bluetooth.MESSAGE_STATE_CHANGE:
                    Log.d(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    break;
                case Bluetooth.MESSAGE_WRITE:
                    Log.d(TAG, "MESSAGE_WRITE ");
                    break;
                case Bluetooth.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;

                    // construct a string from the valid bytes in the buffer.
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.d(TAG, "MESSAGE_READ " + readMessage);
                    formatarMensagem(readMessage);
                    break;
                case Bluetooth.MESSAGE_DEVICE_NAME:
                    Log.d(TAG, "MESSAGE_DEVICE_NAME "+msg);
                    break;
                case Bluetooth.MESSAGE_TOAST:
                    Log.d(TAG, "MESSAGE_TOAST "+msg);
                    break;
            }
        }
    };

    private void formatarMensagem(String readMessage) {
        if(readMessage.startsWith("i") && readMessage.endsWith("f")){
            String mensagem = readMessage.replace("i;","").replace(";f", "");
            String array[] = new String[5];

            array = mensagem.split(";");


            edKm.setText(array[2]);
            edKmh.setText(array[3]);
            edTemperatura.setText(array[4]);

            int litros = Integer.parseInt(array[1]);

            tanque.setProgress(litros);

            if(litros == 0 && swLigar.isChecked()){
                swLigar.setChecked(false);
                Toast.makeText(this, "O tanque está vazio!", Toast.LENGTH_SHORT).show();
            }


        }
    }
}
