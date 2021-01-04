package cg.ce.app.chris.com.cgce.Facturacion.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class JsonFromString {
    private String[] titulos ={
            "claveprodserv",
            "noidentificacion",
            "cantidad",
            "claveunidad",
            "unidad_medida",
            "descripcion",
            "unitario",
            "subtotal",
            "base",
            "09",
            "10",
            "11",
            "siniva",
    };
    public JSONObject strtojson(String str, String split){
        JSONObject json = new JSONObject();
        String result[] = new String[0];
        if (split.equals("|")){
            result = str.split("\\|");}
        else if(split.equals(",")) {
            result = str.split(",");
        }

        Integer i =0;
        for (String s : result) {
            System.out.println(">" + s + "<");
            try {
                json.put(titulos[i],s);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            i++;
        }
        return json;
    }
}
