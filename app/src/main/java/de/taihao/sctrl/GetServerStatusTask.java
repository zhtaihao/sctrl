package de.taihao.sctrl;

import android.os.AsyncTask;

import net.jstorch.mc.cloudcontrol.ServerControl;

import java.util.ArrayList;
import java.util.List;

public class GetServerStatusTask extends AsyncTask<String, Void, Integer> {

    List<String> newStatusMSGs = new ArrayList<>();

    /**
     * creates a new instance of ServerControl for the Type specified in servertID and fetches the serverstatus
     * @param serverID the name of the modpack
     * @return the fetched serverstatus as specified in getServerStatus()
     */

    @Override
    protected Integer doInBackground(String[] serverID) {

        ServerControl serverControl = new ServerControl(serverID[0], newStatusMSGs);
        int serverStatus = serverControl.getServerStatus();
        MainActivity.STATUS_MSGS.addAll(newStatusMSGs);
        newStatusMSGs = new ArrayList<>();

        return  serverStatus;
    }

    /**
     * Sets the SERVER_STATUS Static in MainActivity to the current Serverstatus as specified in getServerStatus()
     * @param serverStatus has the serverstatus fetched in doInBackground
     */
    @Override
    protected void onPostExecute(Integer serverStatus) {
        MainActivity.SERVER_STATUS = serverStatus;
    }
}
