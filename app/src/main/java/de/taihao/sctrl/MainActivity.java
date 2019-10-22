package de.taihao.sctrl;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import net.jstorch.mc.cloudcontrol.ServerControl;

public class MainActivity extends Activity {

    public static List<String> STATUS_MSGS;

    private List<String> statusMSG = new ArrayList<>();

    public static int SERVER_STATUS;

    private int serverStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Switch sw = findViewById(R.id.switchServer1);

        setSwitch(sw); // NETWORK ON MAIN THREAD EXCEPTION
    }

    public void setSwitch(Switch sw){

        new GetServerStatusTask().execute("vanilla");
        serverStatus = SERVER_STATUS;

        sw.setEnabled(false);
        switch (serverStatus) {
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
                while (serverStatus != 0){
                    //showToast(true);
                    new GetServerStatusTask().execute("vanilla");
                }
                sw.setEnabled(true);
            }
            /*
            case 3: {
                // Server wird gerade gestoppt
                sw.setChecked(false);
                while (serverStatus != 1){
                    //showToast(false);
                    new GetServerStatusTask().execute("vanilla");


                }
                sw.setEnabled(true);


            }

             */
        }
    }

    /**
     * shows Toast including three dots (...) indicating something is loading
     * @param starting false: stopping, true: starting
     *
     */
/*
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

 */
/*
    public void switchServer1(){
        final Switch sw = findViewById(R.id.switchServer1);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean isChecked) {
                if (isChecked) {
                    //action if it's now checked
                    serverControlVanilla.startServer(statusMSG);
                    while(serverControlVanilla.getServerStatus() != 0){
                        setSwitch(sw);
                    }
                } else {
                    //action if it's now unchecked
                    serverControlVanilla.createImage(statusMSG);
                    while(serverControlVanilla.getServerStatus() != 1){
                        setSwitch(sw);
                    }
                }
            }
        });

    }

 */
}


