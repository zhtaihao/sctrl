package de.taihao.sctrl;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {

    public static List<String> STATUS_MSGS;

    private List<String> statusMSG = new ArrayList<>();

    public static int SERVER_STATUS;

    private int serverStatus;

    static Map<String,Switch> SWITCHES_BY_SERVER_ID;

    static Map<String,boolean[]> SWITCH_STATES_BY_SERVER_ID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Map that maps the serverType to its respective Switch

        final Map<String, Switch> switchesByServerID = new HashMap<>();

        Switch vanillaSw = findViewById(R.id.switchForServer1);
        Switch stoneblockSw = null;

        switchesByServerID.put("vanilla", vanillaSw);
        switchesByServerID.put("stoneblock", stoneblockSw);
        SWITCHES_BY_SERVER_ID = Collections.unmodifiableMap(switchesByServerID);

        // Map that maps serverID to the states of its respective switches

        final Map<String, boolean[]> switchStatesByServerID = new HashMap<>();

        switchStatesByServerID.put("vanilla", null);
        switchStatesByServerID.put("stoneblock", null );

        SWITCH_STATES_BY_SERVER_ID = Collections.unmodifiableMap(switchStatesByServerID);

        for (Map.Entry<String, Switch > entry : SWITCHES_BY_SERVER_ID.entrySet()) {
            new SetSwitchTask().execute(entry.getKey());
        }

    }

    /**
     *
     * @param serverID the name of the server being started
     * @return the respective switch for each serverType in SWITCHES_BY_SERVER_ID
     */
    public static Switch getSwitch(String serverID){
        return SWITCHES_BY_SERVER_ID.get(serverID);
    }


    private int getServerStatus(String serverID){
        new GetServerStatusTask().execute("vanilla");
        return SERVER_STATUS;
    }

    public void setSwitch(String serverID){
        new SetSwitchTask().execute(serverID);

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


