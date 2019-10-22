package de.taihao.sctrl;

import android.os.AsyncTask;
import android.widget.Switch;

import net.jstorch.mc.cloudcontrol.ServerControl;

import java.util.ArrayList;
import java.util.List;

public class SetSwitchStateTask extends AsyncTask <String,Void,boolean[]> {

    private Switch sw;

    private String serverID;

    List<String> statusMSGs = new ArrayList<>();

    /**
     *  stores the given server ID in the serverID class variable then assigns a pair of booleans
     *  to the switch states dependent of the serverstatus
     * @param s the serverID of the server
     * @return the states of the switch as boolean array
     *          [0] -> setChecked
     *          [1] -> setEnabled
     */
    @Override
    protected boolean[] doInBackground(String... s) {

        serverID = s[0];

        ServerControl serverControl = new ServerControl(serverID, statusMSGs);
        int serverStatus = serverControl.getServerStatus();
        sw = MainActivity.getSwitch(serverID);

        // new boolean array [0] = setChecked [1] = setEnabled
        boolean[] switchStates = new boolean[2];

        switch (serverStatus) {
            case 0: {
                // Server läuft gerade
                switchStates[0] = true;
                switchStates[1] = true;
            }
            case 1: {
                // Server ist aus, kann gestartet werden

                switchStates[0] = false;
                switchStates[1] = true;
            }
            case 2: {
                // Server wird gerade gestartet
                switchStates[0] = true;
                while (serverStatus != 0){
                    //showToast(true);
                    serverStatus = serverControl.getServerStatus();
                }
                switchStates[1] = true;
            }
            case 3: {
                // Server wird gerade gestoppt
                switchStates[0] = false;
                while (serverStatus != 1){
                    //showToast(false);
                    serverStatus = serverControl.getServerStatus();
                }
                switchStates[1] = true;
            }
        }
        return switchStates;
    }

    /**
     * overrides the entry with the key serverID in the Switchstate Map in the Main Activity with the new Switchstates
     * @param switchStates
     */
    @Override
    protected void onPostExecute(boolean[] switchStates) {
        MainActivity.SWITCH_STATES_BY_SERVER_ID.put(serverID, switchStates);
    }
}

