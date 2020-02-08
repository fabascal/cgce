package cg.ce.app.chris.com.cgce;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class MacActivity extends AppCompatActivity {

    private TextView tv_mac_equipo,tv_mac_propio;
    Cursor c;
    Context context;
    ValidateTablet tablet = new ValidateTablet();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mac);
        if (tablet.esTablet(getApplicationContext())){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        tv_mac_equipo = (TextView)findViewById(R.id.tv_mac_equipo);
        tv_mac_propio = (TextView )findViewById(R.id.tv_mac_propio);
        tv_mac_propio.setText(getMacAddress());
    }

    public String getMacAddress__(Context con) {
        WifiManager wimanager = (WifiManager) con.getSystemService(Context.WIFI_SERVICE);
        String macAddress = wimanager.getConnectionInfo().getMacAddress();
        if (macAddress == null) {
            macAddress = "Device don't have mac address or wi-fi is disabled";
        }
        return macAddress;
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
