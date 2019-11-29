package cg.ce.app.chris.com.cgce;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;

/**
 * Created by chris on 28/08/17.
 *  * clase creada con el fin de validar sensores
 * la funcion bluetooth valida que el adaptador este prendido en caso contrario lo inicia
 * la funcion wifi valida que el adaptador este prendido en caso contrario lo inicia
 * la funcion nfc retorna el estado del adaptador
 */

public class Sensores {
    public boolean bluetooth(){
        boolean actividad=false;
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // el equipo no cuenta con bluetooth
            actividad=false;
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                // Bluetooth se encuentra apagado, por lo que se procede a iniciarlo
                mBluetoothAdapter.enable();
            }else{
                actividad=true;
            }
        }
        return actividad;
    }
    public boolean wifi(Context context, Boolean status){
        WifiManager wifiManager =  (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        // Wifi se encuentra apagado, por lo que se procede a iniciarlo
        wifiManager.setWifiEnabled(status);
        return true;
    }
    public boolean nfc(Context context){
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        return nfcAdapter.isEnabled();
    }
}
