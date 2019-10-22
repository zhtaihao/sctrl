package net.jstorch.mc.cloudcontrol;

import android.os.AsyncTask;

import de.taihao.sctrl.ApiToken;
import me.tomsdevsn.hetznercloud.HetznerCloudAPI;
import me.tomsdevsn.hetznercloud.objects.general.Image;
import me.tomsdevsn.hetznercloud.objects.general.SSHKey;
import me.tomsdevsn.hetznercloud.objects.general.Server;
import me.tomsdevsn.hetznercloud.objects.request.ChangeReverseDNSRequest;
import me.tomsdevsn.hetznercloud.objects.request.CreateImageRequest;
import me.tomsdevsn.hetznercloud.objects.request.ServerRequest;
import me.tomsdevsn.hetznercloud.objects.request.UpdateImageRequest;
import me.tomsdevsn.hetznercloud.objects.response.ImageType;

import java.io.IOException;
import java.net.URI;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Klasse zur Steuerung des Minecraft Servers
 * Ausgabe von Status Nachrichten über String Lists
 */
public class ServerControl /*extends AsyncTask<String, Void, List<String>> */{
    /**
     * API Token
     * Api Token to get from the ApiToken Class which contains the Token as a String and is not uploaded to git
     */
    private static String token;
    /**
     * Liste an SSH Keys. Wird im Konstruktor automatisch mit allen SSH Keys im Projekt gefüllt
     */
    private final List<Object> sshKeys;
    /**
     * Name den der Server beim Starten bekommt
     */
    private static String serverName;
    /**
     * Name den das aktuelle Image bekommt
     */
    private static String imageCurrent;
    /**
     * Name den das alte zu löschende Image bekommt
     */
    private static String imageOld;
    /**
     * IPv4 reverse DNS Eintrag
     */
    private static String reverseDNS4;
    /**
     * IPv6 reverse DNS Eintrag
     */
    private static String reverseDNS6;
    /**
     * Servertyp (siehe Hetznercloud Dokuemtation)
     */
    private static final String serverType = "cx21";
    /**
     * Serverstandort (siehe Hetznercloud Dokmentation)
     */
    private static final String location = "nbg1";
    /**
     * Cloud API Objekt
     */
    private HetznerCloudAPI cloud;

    public ServerControl(String serverID, List<String> statusMSG) {

        //change vanilla to minecraft serverID because joshuaStorch is too lazy to review his old code
        if(serverID.equals("vanilla"))
            serverID = "minecraft";

        try {
            token = ApiToken.getToken(serverID);

        } catch (IllegalArgumentException e){
            statusMSG.add( "Invalid serverID:" + serverID +"!");
        }

        //Set strings to match the given ServerID
        serverName = serverID + "-server.jstorch.net";
        imageCurrent = serverID + "-current";
        imageOld = serverID + "-old";
        reverseDNS4 = serverID + "4.jstorch.net";
        reverseDNS6 = serverID + "6.jstorch.net";

        cloud = new HetznerCloudAPI(token);
        List<Object> keys = new LinkedList<>();
        for (SSHKey key : cloud.getSSHKeys().getSshKeys()) {
            keys.add(key.getId());
        }
        sshKeys = keys;

    }

    /**
     *
     * @param action determines whether to start or stop the server
     * @return statusMSG will get populated by the status messages
     */
    /*
    @Override
    protected List<String> doInBackground(String[] action) {
        // method is required by abstract class android.os.AsyncTask

        ArrayList<String> statusMSG = new ArrayList<>();

        switch (action[0]) {
            case "start": {
                startServer(statusMSG);
            }
            case "stop": {
                createImage(statusMSG);
            }
        }

        return statusMSG;
    }
    */



    /**
     * Prüft den aktuellen Serverstatus
     * @return 0 der Server läuft gerade
     * @return 1 der Server ist aus und kann gestartet werden
     * @return 2 server wird gerade gestartet
     * @return 3 server wird gerade gestoppt
     * @return -1 Fehler
     */
    public int getServerStatus() {
        List<Server> serverList = cloud.getServerByName(serverName).getServers(); //liste von Servern abrufen
        if (serverList.size() == 1) {//1 Server existiert
            Server server = serverList.get(0);
            String status = server.getStatus();
            if (status.equals("running")) {//server läuft
                List<Image> imageListComplete = cloud.getImages(ImageType.SNAPSHOT).getImages();//Alle Images abrufen
                List<Image> imageList = new LinkedList<>();
                for (Image image : imageListComplete) {//Aktuelle Images finden
                    if (image.getDescription().equals(imageCurrent))
                        imageList.add(image);
                }

                if (imageList.size() == 1) {//ein image wurde erstellt und server wird gelöscht
                    return 3;
                }
                if (imageList.size() == 0){//kein aktuelles image existiert. der server läuft und kann gelöscht werden falls gewünscht
                    return 0;
                }
                return -1; //mehr als ein image existiert

            }
            if (status.equals("initializing") || server.getStatus().equals("starting")) {
                return 2;
            }
            return -1;//unbeabsichtigter Serverzustand
        }
        if (serverList.size() > 1) {//mehr als ein Server existiert ???
            return -1;
        }

        //kein Server existiert
        List<Image> imageListComplete = cloud.getImages(ImageType.SNAPSHOT).getImages();//Alle Images abrufen
        List<Image> imageList = new LinkedList<>();
        for (Image image : imageListComplete) {//Aktuelle Images finden
            if (image.getDescription().equals(imageCurrent))
                imageList.add(image);
        }

        if (imageList.size() == 1) {//ein image existiert server kann gestartet werden
            return 1;
        }

        //es existiert entweder kein image oder mehrere ... server kann nicht gestertet werden
        return -1;


    }

    /**
     * Startet den MC Server wenn dieser noch nicht existiert
     * Sonst wird nur die IP Adresse ausgegeben
     * @param statusMSG Stringlist in die Statusnachrichten gespeichert werden. Die ersten beiden Hinzugefügten Elemente sind die IP Adressen
     * @return 1 Server läuft nicht (statusMSG enthält genau eine Statusnachricht und KEINE IP Adressen!)
     * @return 0 Server wurde erstellt
     * @return 2 Server existiert Bereits (statusMSG enthält eine Statusmeldung zum Zustand an letzter Stelle)
     */
    public int startServer(List<String> statusMSG) {
        List<Server> serverList = cloud.getServerByName(serverName).getServers();
        if (serverList.size() == 1) {
            getIP(statusMSG);
            Server server = serverList.get(0);
            String status = server.getStatus();
            if (status.equals("initializing") || server.getStatus().equals("starting")) {
                statusMSG.add("Der Server wurde noch nicht fertig erstellt!");
                return 2;
            }
            if (status.equals("off")) {
                cloud.powerOnServer(server.getId());
                statusMSG.add("Ein bereits existierender Server wurde gestartet!");
                return 2;
            }
            statusMSG.add("Ein Server existiert bereits mit Serverstatus: " + status);
            return 2;
        }
        if (serverList.size() > 1) {
            statusMSG.add("Es existiert mehr als 1 Server. Es handelt sich um einen unbekannten Zustand!");
            return 1;
        }
        List<Image> imageListComplete = cloud.getImages(ImageType.SNAPSHOT).getImages();
        List<Image> imageList = new LinkedList<>();
        for (Image image : imageListComplete) {
            if (image.getDescription().equals(imageCurrent))
                imageList.add(image);
        }
        if (imageList.size() > 1) {
            statusMSG.add("Es existiert mehr als 1 Image. Es handelt sich um einen unbekannten Zustand!");
            return 1;
        }
        if (imageList.size() == 0) {
            statusMSG.add("Es existiert kein Image. Es kann kein Server erstellt werden!");
            return 1;
        }
        Image image = imageList.get(0);
        if (!image.getStatus().equals("available")) {
            statusMSG.add("Ein Image wird gerade erstellt, ist aber noch nicht fertig!");
            return 1;
        }
        Server server = cloud.createServer(ServerRequest.builder().startAfterCreate(true).serverType(serverType).location(location).image(String.valueOf(image.getId())).name(serverName).sshKeys(sshKeys).build()).getServer();
        cloud.updateImage(image.getId(), UpdateImageRequest.builder().description(imageOld).build());
        getIP(statusMSG);
        cloud.changeDNSPTR(server.getId(), ChangeReverseDNSRequest.builder().ip(server.getPublicNet().getIpv4().getIp()).dnsPTR(reverseDNS4).build());
        cloud.changeDNSPTR(server.getId(), ChangeReverseDNSRequest.builder().dnsPTR(reverseDNS6).ip(server.getPublicNet().getIpv6().getIp().replace("/64", "1")).build());
        return 0;
    }

    /**
     * Prüft ob imageCurrent existiert. Wenn ja wird der Server gelöscht. Falls imageOld existiert wird es gelöscht.
     * NICHT DURCH ENDBENUTZER AUFZURUFEN!
     * @param statusMSG Liste mit Statusnachrichten.
     * @return 1 es wurde kein Server gelöscht
     * @return 0 Server wurde gelöscht
     */
    public int stopServer(List<String> statusMSG) {
        List<Server> serverList = cloud.getServerByName(serverName).getServers();
        if (serverList.size() == 0) {
            statusMSG.add("Es existiert kein Server der gelöscht werden kann!");
            return 1;
        } else if (serverList.size() > 1) {
            statusMSG.add("Es existiert mehr als 1 Server. Es handelt sich um einen unbekannten Zustand!");
            return 1;
        }
        List<Image> imageListOld = new LinkedList<>();
        List<Image> imageListNew = new LinkedList<>();
        List<Image> imageListComplete = cloud.getImages(ImageType.SNAPSHOT).getImages();
        for (Image image : imageListComplete) {
            if (image.getDescription().equals(imageOld))
                imageListOld.add(image);
            if (image.getDescription().equals(imageCurrent))
                imageListNew.add(image);
        }
        if (imageListOld.size() < 1)
            statusMSG.add("Warnung: es existiert kein altes image zum löschen");
        else if (imageListOld.size() == 1) {
            cloud.deleteImage(imageListOld.get(0).getId());
        }

        if (imageListNew.size() < 1) {
            statusMSG.add("Es existiert kein neues Image. Der Server wird nicht gelöscht!");
            return 1;
        }
        if (imageListNew.get(0).getStatus().equals("available")) {
            Server server = serverList.get(0);
            cloud.deleteServer(server.getId());
            statusMSG.add("Server wurde gelöscht");
            return 0;
        } else
            statusMSG.add("Das Image ist noch nicht fertig!");


        return 1;
    }

    /**
     * imageCurrent wird aus aktuellem Server erstellt
     * @param statusMSG Liste mit Statusmeldungen
     * @return 0 ein Image wird erstellt (keine oder eine Statusnachricht in statusMSG, weniger relevant Image wird auf jeden Fall erstellt!)
     * @return 1 es wird kein Image erstellt (eine Statusnachricht in statusMSG)
     */
    public int createImage(List<String> statusMSG) {
        List<Server> serverList = cloud.getServerByName(serverName).getServers();
        if (serverList.size() == 0) {
            statusMSG.add("Es existiert kein Server für den ein Image erstellt werden kann!");
            return 1;
        } else if (serverList.size() > 1) {
            statusMSG.add("Es existiert mehr als 1 Server. Es handelt sich um einen unbekannten Zustand!");
            return 1;
        }
        List<Image> imageList = new LinkedList<>();
        List<Image> imageListComplete = cloud.getImages(ImageType.SNAPSHOT).getImages();
        for (Image image : imageListComplete) {
            if (image.getDescription().equals(imageCurrent))
                imageList.add(image);
        }
        if (imageList.size() == 1) {
            statusMSG.add("Ein schon existierendes Image wird überschrieben");
            cloud.deleteImage(imageList.get(0).getId());
        }
        Server server = serverList.get(0);
        cloud.shutdownServer(server.getId());
        //statusMSG.add("Server wird heruntergefahren!");
        try {
            TimeUnit.SECONDS.sleep(6);
        } catch (Exception e) {

        }
        cloud.createImage(server.getId(), CreateImageRequest.builder().description(imageCurrent).type("snapshot").build());

        try {
            TimeUnit.SECONDS.sleep(6);
        } catch (Exception e) {

        }

        cloud.powerOnServer(server.getId());

        return 0;
    }

    /**
     * IP Adresse des Servers wird abgerufen und ausgegeben
     * @param statusMSG Liste mit IP Adressen an den ersten beiden Poitionen und darauf Folgend eine Statusmeldung von UpdateDNS
     * @return 1 Es existiert kein Server von dem eine Adresse ausgegeben werden kann. statusMSG ist leer!
     * @return 0 Adresse erfolgreich ermittelt
     */
    public int getIP(List<String> statusMSG) {
        List<Server> serverList = cloud.getServerByName(serverName).getServers();
        if (serverList.size() == 0) {
            statusMSG.add("Es existiert kein Server!");
            return 1;
        }
        Server server = serverList.get(0);
        String ipv4 = server.getPublicNet().getIpv4().getIp();
        String ipv6 = server.getPublicNet().getIpv6().getIp().replace("/64", "1");
        statusMSG.add(ipv4);
        statusMSG.add(ipv6);
        if (updateDNS(statusMSG, ipv4, ipv6) != 0)
            statusMSG.add("Fehler beim Aktualisieren des DNS Eintrags!");
        return 0;
    }

    /**
     * Server wird neu gestartet
     * @param statusMSG Liste mit Statusmeldungen
     * @return 1 Es existiert kein Server der neu gestartet werden kann
     * @retuen 0 Server wird neu gestartet
     */

    public int restartServer(List<String> statusMSG) {
        List<Server> serverList = cloud.getServerByName(serverName).getServers();
        if (serverList.size() == 0) {
            System.out.println("Es existiert kein Server!");
            return 1;
        }
        if (serverList.size() > 1) {
            System.out.println("Es existiert mehr als 1 Server. Es handelt sich um einen unbekannten Zustand!");
            return 1;
        }

        Server server = serverList.get(0);
        cloud.shutdownServer(server.getId());
        System.out.println("Server wird heruntergefahren!");
        try {
            TimeUnit.SECONDS.sleep(6);
        } catch (Exception e) {

        }

        cloud.powerOnServer(server.getId());

        return 0;
    }

    /**
     * Der DNS Eintrag des Servers wird aktualisiert
     * @param statusMSG Liste in die genau eine Statusmeldung eingefügt wird
     * @param ipv4 IPv4 Adresse die in den DNS Eintrag geschrieben werden soll
     * @param ipv6 IPv6 Adresse die in den DNS Eintrag geschrieben werden soll
     * @return 1 URI Fehler (sollte nicht auftreten)
     * @return 2 Fehler beim HTTP Request
     * @return 3 unerwarteter Statuscode
     * @return 4 unerwartete Serverantwort (Eintrag konnte Serverseitig nicht aktualisiert werden)
     * @return 0 Eintrag erfolgreich aktualisiert
     */
    private int updateDNS(List<String> statusMSG, String ipv4, String ipv6) {
        URI uri = null;
        try {
            uri = new URI("https://www.duckdns.org/update?domains=jsminecraft&token=000c1aca-133f-4921-b4c8-b12007cef4b5&ipv6=" + ipv6 + "&ip=" + ipv4);
        } catch (URISyntaxException e) {
            statusMSG.add("URI Fehler");
            return 1;
        }
        try {
            ClientHttpRequest request = new SimpleClientHttpRequestFactory().createRequest(uri, HttpMethod.GET);
            ClientHttpResponse response = request.execute();

            String scode = response.getStatusCode().toString();
            if (!scode.equals("200")) {
                statusMSG.add("Unerwarteter Statuscode beim Aktualisieren des DNS Eintrags: " + scode);
                return 3;
            }
            byte[] bytearr = new byte[2];
            response.getBody().read(bytearr, 0, 2);
            String outStr = "";
            for (int i = 0; i < 2; i++) {
                outStr += (char)bytearr[i];
            }
            if (!outStr.equals("OK")) {
                statusMSG.add("unerwartete Serverantwort beim aktualisieren des DNS Eintrages: " + outStr);
                return 4;
            }

            statusMSG.add("DNS Eintrag wurde aktualisiert!");

        } catch (IOException e) {
            statusMSG.add("Fehler beim HTTP Request");
            return 2;
        }
        statusMSG.add("DNS Eintrag aktualisiert!");
        return 0;

    }

}
