package cg.ce.app.chris.com.cgce.common;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class QrString {
    DecimalFormat formateador2 = new DecimalFormat("###,###.##");

    public String Aceiteqrstring (JSONObject jsticket, JSONObject jsdomicilio) throws JSONException {

        String qr = "COMBUGO|"+jsticket.getString("cveest")+"|"+jsdomicilio.getString
                ("estacion")+"|"+jsticket.getString("fecha")+"|"+jsticket.getString
                ("hora")+"|"+jsticket.getString("nrotrn")+"|"+jsticket.getString("total");
        Log.w("qr",qr);
        return qr;
    }

    public String Combustibleqrstring (JSONObject jsticket, JSONObject jsdomicilio) throws JSONException {
        Double ieps = jsticket.getDouble("ieps");
        Double iva_factor = jsticket.getDouble("iva");
        Double lts = jsticket.getDouble("cantidad");
        Double pre = jsticket.getDouble("precio");
        Double ivaentre = Double.valueOf("1" + iva_factor);
        Double precioneto = ((Double.valueOf(pre) - ieps) / ivaentre) * 100;
        Double iva = ((precioneto * lts) * iva_factor) / 100;
        Double subtotal = (precioneto + ieps) * lts;
        String precio_total = String.valueOf(precioneto + ieps);
        String substr = ".";
        String inicio = precio_total.substring(0, precio_total.indexOf(substr));
        String fin = precio_total.substring(precio_total.indexOf(substr) + substr.length());
        String precio_impresion = inicio + "." + fin.substring(0, 2);
        String qr = "COMBUGO|" + jsticket.getString("cveest") + "|" + jsdomicilio.getString
                ("estacion") + "|" + jsticket.getString("fecha") + "|" + jsticket.getString
                ("hora") + "|" + precio_impresion + "|" + formateador2.format(iva) + "|" +
                jsticket.getString("total") + "|" + jsticket.getString("nrotrn") + "|";
        return qr;
    }
}
