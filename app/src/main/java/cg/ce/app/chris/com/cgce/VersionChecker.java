package cg.ce.app.chris.com.cgce;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PortUnreachableException;
import java.net.URL;

/**
 * Created by chris on 9/07/17.
 */

public class VersionChecker {
    /**
     * El enlace al archivo autoupdate_info.txt guardado en el servidor pegaso.
     */
    public static final String MARCA = "Combu";
    public static final String INFO_FILE_COMBU = "http://189.206.183.110:1390/cecg_app/autoupdate_info.txt";
    public static final String INFO_FILE_REPSOL = "http://189.206.183.110:1390/cecg_app/autoupdate_info_repsol.txt";

    /**
     * El código de versión establecido en el AndroidManifest.xml de la versión
     * instalada de la aplicación. Es el valor numérico que usa Android para
     * diferenciar las versiones.
     */
    private int currentVersionCode;
    /**
     * El nombre de versión establecido en el AndroidManifest.xml de la versión
     * instalada. Es la cadena de texto que se usa para identificar al versión
     * de cara al usuario.
     */
    private String currentVersionName;
    /**
     * El código de versión establecido en el AndroidManifest.xml de la última
     * versión disponible de la aplicación.
     */
    private int latestVersionCode;
    /**
     * El nombre de versión establecido en el AndroidManifest.xml de la última
     * versión disponible.
     */
    private String latestVersionName;

    /**
     * Enlace de descarga directa de la última versión disponible.
     */
    private String downloadURL;
    private String mandatory;

    /**
     * Método para inicializar el objeto. Se debe llamar antes que a cualquie
     * otro, y en un hilo propio (o un AsyncTask) para no bloquear al interfaz
     * ya que hace uso de Internet.
     *
     * @param context
     *            El contexto de la aplicación, para obtener la información de
     *            la versión actual.
     */
    public void getData(Context context) throws PackageManager.NameNotFoundException, IOException, JSONException {

        // Datos locales
        PackageInfo pckginfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        currentVersionCode = pckginfo.versionCode;
        currentVersionName = pckginfo.versionName;
        // Datos remotos
        String data=null;
        if (MARCA == "Combu"){
            data = downloadHttp(new URL(INFO_FILE_COMBU));
        }else if(MARCA == "Repsol"){
            data = downloadHttp(new URL(INFO_FILE_REPSOL));
        }
        Log.w("json",data);
        JSONObject json = new JSONObject(data);
        latestVersionCode = json.getInt("versionCode");
        latestVersionName = json.getString("versionName");
        downloadURL = json.getString("downloadURL");
        mandatory = json.getString("mandatory");
        Log.d("AutoUpdate", "Datos obtenidos con éxito");

    }
    public void getDataFalse(){
        mandatory="0";
        latestVersionCode=getCurrentVersionCode();
        latestVersionName=getCurrentVersionName();

    }
    public boolean isNewVersionAvailable() {
        return getLatestVersionCode() > getCurrentVersionCode();
    }

    /**
     * Devuelve el código de versión actual.
     *
     * @return
     */
    public int getCurrentVersionCode() {
        return currentVersionCode;
    }

    /**
     * Devuelve el nombre de versión actual.
     *
     * @return
     */
    public String getCurrentVersionName() {
        return currentVersionName;
    }

    /**
     * Devuelve el código de la última versión disponible.
     *
     * @return
     */
    public int getLatestVersionCode() {
        return latestVersionCode;
    }

    /**
     * Devuelve el nombre de la última versión disponible.
     *
     * @return
     */
    public String getLatestVersionName() {
        return latestVersionName;
    }
    public String getMandatory(){
        return mandatory;
    }

    /**
     * Devuelve el enlace de descarga de la última versión disponible
     *
     * @return
     */
    public String getDownloadURL() {
        return downloadURL;
    }

    private static String downloadHttp(URL url) throws IOException {
        HttpURLConnection c = (HttpURLConnection)url.openConnection();
        c.setRequestMethod("GET");
        c.setReadTimeout(15 * 1000);
        c.setUseCaches(false);
        c.connect();
        BufferedReader reader = new BufferedReader(new InputStreamReader(c.getInputStream()));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        while((line = reader.readLine()) != null){
            stringBuilder.append(line + "n");
        }
        return stringBuilder.toString();
    }
}
