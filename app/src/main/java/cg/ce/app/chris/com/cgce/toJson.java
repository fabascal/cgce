package cg.ce.app.chris.com.cgce;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chris on 31/01/17.
 */

public class toJson {
    public JSONObject strtojson(String str){
        JSONObject json = new JSONObject();
        String[] result = str.split("\\|");

        Integer i =0;
        for (String s : result) {
            System.out.println(">" + s + "<");
            try {
                json.put(String.valueOf(i),s);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            i++;
        }
        return json;
    }
}
