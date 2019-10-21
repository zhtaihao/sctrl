package de.taihao.sctrl;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import me.tomsdevsn.hetznercloud.HetznerCloudAPI;
import me.tomsdevsn.hetznercloud.objects.general.Server;



public class MainActivity extends Activity {

    private List<String> statusMSG = new ArrayList<>();

    private ServerControl serverControl = new ServerControl();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Switch sw = findViewById(R.id.switchServer1);

        setSwitch(sw); // NETWORK ON MAIN THREAD EXCEPTION


    }

    public void setSwitch(Switch sw){

        sw.setEnabled(false);
        switch (serverControl.getServerStatus()) {
            case 0: {
                // Server läuft gerade
                sw.setChecked(true);
                sw.setEnabled(true);
            }
            case 1: {
                // Server ist aus, kann gestartet werden
                sw.setChecked(false);
                sw.setEnabled(true);
            }
            case 2: {
                // Server wird gerade gestartet
                sw.setChecked(true);
                while (serverControl.getServerStatus() != 0){
                    showToast(true);
                }
                sw.setEnabled(true);
            }
            case 3: {
                // Server wird gerade gestoppt
                sw.setChecked(false);
                while (serverControl.getServerStatus() != 1){
                    showToast(false);
                }
                sw.setEnabled(true);
            }
        }
    }

    /*
     * shows Toast including three dots (...) indicating something is loading
     * @param starting false: stopping, true: starting
     */
    public void showToast(boolean starting) {
        String msg;
        if (starting){
            // set message accordingly
            msg = getString(R.string.server_starting);
        } else {
            msg = getString(R.string.server_stopping);
        }
        while (true) {
            Toast.makeText(this, msg + "", Toast.LENGTH_SHORT).show();
            try {
                wait(333);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, msg + ".", Toast.LENGTH_SHORT).show();
            try {
                wait(333);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, msg + "..", Toast.LENGTH_SHORT).show();
            try {
                wait(333);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, msg + "...", Toast.LENGTH_SHORT).show();
            try {
                wait(333);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void switchServer1(){
        final Switch sw = findViewById(R.id.switchServer1);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean isChecked) {
                if (isChecked) {
                    //action if it's now checked
                    serverControl.startServer(statusMSG);
                    while(serverControl.getServerStatus() != 0){
                        setSwitch(sw);
                    }
                } else {
                    //action if it's now unchecked
                    serverControl.createImage(statusMSG);
                    while(serverControl.getServerStatus() != 1){
                        setSwitch(sw);
                    }
                }
            }
        });

    }
}


