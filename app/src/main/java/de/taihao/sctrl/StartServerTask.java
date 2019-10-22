package de.taihao.sctrl;

import android.os.AsyncTask;

import net.jstorch.mc.cloudcontrol.ServerControl;

import java.util.ArrayList;
import java.util.List;

public class StartServerTask extends AsyncTask<String, Void, ServerControl > {

    List<String> statusMSG = new ArrayList<>();

    @Override
    protected ServerControl doInBackground(String[] serverType) {
        ServerControl serverControl = new ServerControl(serverType[0], statusMSG);

        return  serverControl;
    }

    @Override
    protected void onPostExecute(ServerControl serverControl) {

        serverControl.startServer(statusMSG);

    }
}
