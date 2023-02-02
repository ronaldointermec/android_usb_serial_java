package com.example.cn80;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.cn80.databinding.ActivityMainBinding;
import com.honeywell.osservice.Manifest;
import com.honeywell.osservice.sdk.SerialManager;
import com.honeywell.osservice.sdk.SerialPort;

import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private SerialManager sm;
    private SerialPort sp;

    public static final String TAG = "TESTE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // new Manifest.permission();

                String[] serial;
                try {
                    sm = SerialManager.getInstance();
                    serial = sm.getSerialPorts();


                    if (serial != null && serial.length > 0) {
                        for (int i = 0; i < serial.length; i++) {

                            if (serial[i] != null) {
                                try {

                                    String inputString = "hello world!\r\n";
                                    Charset charset = StandardCharsets.UTF_8;
                                    byte[] byteArray = inputString.getBytes(charset);

                                    ByteBuffer buffer = ByteBuffer.wrap(byteArray);

                                    sp = sm.openSerialPort(serial[i]);
                                    sp.setParameters(57600, 8, 0, 1);
                                    sp.write(buffer, buffer.array().length);

                                } catch (IOException | RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.d(TAG, "onClick: " + e);
                }
            }
        });
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

}