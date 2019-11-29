package cg.ce.app.chris.com.cgce;

import android.content.Context;
import android.util.Log;

import java.net.NetworkInterface;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

/**
 * Created by chris on 4/10/16.
 */
public class ValidarDispositivo {
    private static Context context;


    public int validardisp(Context con){
        //valor de res 0-sin autorizacion, 1-autorizado
        context = con ;
        int res=0;
        String mac = getMacAddress();
        Log.w("Mac","Mac: "+mac);
        DataBaseCG dbcg = new DataBaseCG();
        Statement stmt = dbcg.odbc(con);
        try {
            ResultSet r = stmt.executeQuery("SELECT disp.activo FROM  [cecg_app].[dbo].[dispositivos] as disp where disp.mac_adr = '"+String.valueOf(mac)+"';");
            if (!r.next()){
                res=0;
            }else {
                res=r.getInt(1);
            }
            r.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }
    public String getMacAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "Device don't have mac address or wi-fi is disabled";
    }

}
