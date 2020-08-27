package cg.ce.app.chris.com.cgce.common;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import cg.ce.app.chris.com.cgce.R;

public class UtilsBrand extends Application {
    Context context;
    public static JSONObject CallSharedBrand(Context context){
        UtilsBrand utilsBrand = new UtilsBrand();
        return UtilsBrand.CallSharedBrand(context);
    }
    private JSONObject GetSharedBrand(Context context) throws JSONException {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Brand",
                context.MODE_PRIVATE);
        JSONObject result = new JSONObject();
        result.put(context.getResources().getString(R.string.BrandID),sharedPreferences.getString(context.getResources().getString(R.string.BrandID),"1"));
        result.put(context.getResources().getString(R.string.BrandName),sharedPreferences.getString(context.getResources().getString(R.string.BrandName),""));
        result.put(context.getResources().getString(R.string.CfdiURL),sharedPreferences.getString(context.getResources().getString(R.string.CfdiURL),""));
        result.put(context.getResources().getString(R.string.BrandImage),context.getResources().getDrawable(R.drawable.logo_impresion));
        return result;
    }

}
