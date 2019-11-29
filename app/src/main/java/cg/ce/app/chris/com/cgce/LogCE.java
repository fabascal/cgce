package cg.ce.app.chris.com.cgce;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * Created by chris on 17/04/17.
 */

public class LogCE {

    public void EscirbirLog(Context con,JSONObject jsonObject)
    {
        OutputStreamWriter escritor=null;
        try
        {
            File ruta_sd = con.getExternalFilesDir(null);

            File f = new File(ruta_sd.getAbsolutePath(), "CE_Log.txt");

            OutputStreamWriter fout = new OutputStreamWriter(new FileOutputStream(f,true));

            jsonObject.put("fecha",fecha());
            fout.append(jsonObject.toString() + "\n");
            fout.close();
            Log.w("log","correcto");
        }
        catch (Exception ex)
        {
            Log.w("LogE",ex);
            Toast.makeText(con,"Error al escribir LOG"+ex,Toast.LENGTH_LONG).show();
        }
        finally
        {
            try {
                if(escritor!=null)
                    escritor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String fecha(){
        Calendar c = Calendar.getInstance();
        System.out.println("Current time => "+c.getTime());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

}
