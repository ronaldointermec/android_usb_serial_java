package com.example.cn80;

import android.Manifest;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.honeywell.osservice.sdk.SerialManager;
import com.honeywell.osservice.sdk.SerialPort;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


public class MainActivity extends AppCompatActivity {

    private SerialManager sm;
    private SerialPort sp;
    private String[] serial;
    private ByteBuffer buffer;
    private boolean isSerialConnected = false;
    private boolean shouldOpenNewThread = true;
    private TextView readText;
    private String readFromBuffer;
    private Button connectButton;
    private Button closeButton;
    private Button writeButton;
    private TextInputEditText inputText;
    public static final String TAG = "TESTE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setup();
    }

    public void connect(View view) {
        new Manifest.permission();
        try {
            sm = SerialManager.getInstance();
            Log.d(TAG, "getInstance");
            serial = sm.getSerialPorts();
            Log.d(TAG, "getSerialPorts");
            if (serial != null && serial.length > 0) {

                for (int i = 0; i < serial.length; i++) {
                    if (serial[i] != null) {
                        sp = sm.openSerialPort(serial[i]);
                        Log.d(TAG, "openSerialPort" + i);
                        sp.setParameters(57600, 8, 0, 1);
                        Log.d(TAG, "setParameters");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onConnect();
                            }
                        });
                    }
                }
            }
            isSerialConnected = true;
            if (shouldOpenNewThread) {
                MyThread thread = new MyThread();
                new Thread(thread).start();
                shouldOpenNewThread = false;
                Log.d(TAG, "Thread criada");
            }
        } catch (IOException | RemoteException e) {
            e.printStackTrace();
        }
    }

    public void write(View view) {
        if (isSerialConnected) {
            String valor = inputText.getText().toString();
            if (!valor.isEmpty() || !valor.equals("") || valor.length() > 0) {
                try {
                    String inputString = inputText.getText() + "\r\n";
                    Charset charset = StandardCharsets.UTF_8;
                    byte[] byteArray = inputString.getBytes(charset);
                    buffer = ByteBuffer.wrap(byteArray);
                    sp.write(buffer, buffer.array().length);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Digite algo primeiro", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Serial is not connected", Toast.LENGTH_SHORT).show();
        }
    }

    public void close(View view) {
        isSerialConnected = false;
        try {
            if (sp != null)
                sp.close();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onClose();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    class MyThread implements Runnable {
        private ByteBuffer readBuffter = ByteBuffer.allocate(1024);
        private String textConcatenatedBeforeEnter = "";

        @Override
        public void run() {
            while (isSerialConnected) {
                try {
                    int b = sp.read(readBuffter);
                    if (b > 0) {
                        readFromBuffer = new String(readBuffter.array(), StandardCharsets.UTF_8);
                        readFromBuffer = readFromBuffer.substring(0, b);
                        Log.d(TAG, "run: " + readFromBuffer);
                        int asciiValue = readFromBuffer.charAt(b - 1);
                        if (asciiValue != 13) {
                            textConcatenatedBeforeEnter = textConcatenatedBeforeEnter + readFromBuffer;
                        } else {
                            Log.d(TAG, "keyboard key: " + asciiValue);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    readText.setText(textConcatenatedBeforeEnter);
                                    textConcatenatedBeforeEnter = "";
                                    readFromBuffer = "";
                                }
                            });
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            Log.d(TAG, "thread closed");
            shouldOpenNewThread = true;
        }
    }

    private void setup() {
        readText = findViewById(R.id.read);
        connectButton = findViewById(R.id.connect);
        closeButton = findViewById(R.id.close);
        writeButton = findViewById(R.id.write);
        inputText = findViewById(R.id.input);
    }

    private void onConnect() {
        connectButton.setVisibility(View.GONE);
        closeButton.setVisibility(View.VISIBLE);
        writeButton.setVisibility(View.VISIBLE);
        readText.setVisibility(View.VISIBLE);
        inputText.setVisibility(View.VISIBLE);
    }

    private void onClose() {
        isSerialConnected = false;
        connectButton.setVisibility(View.VISIBLE);
        closeButton.setVisibility(View.GONE);
        writeButton.setVisibility(View.GONE);
        readText.setVisibility(View.GONE);
        inputText.setVisibility(View.GONE);
        readText.setText("");
        inputText.setText("");
    }
}
