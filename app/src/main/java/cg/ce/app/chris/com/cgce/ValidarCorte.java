package cg.ce.app.chris.com.cgce;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.epson.epos2.printer.Printer;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by chris on 6/03/17.
 */

public class ValidarCorte {
    JSONObject cursor=null;
    private Printer mPrinter = null;
    public int corte(Context con) throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        ValidarDispositivo mac_add=new ValidarDispositivo();
        String mac= mac_add.getMacAddress();
        Integer res=0;
        ResultSet r = null;
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();
        if (!cursor.has("ip")){
            Toast.makeText(con,"Dispositivo sin ODBC",Toast.LENGTH_LONG).show();
        }
        String base = null;
        try {
            base = cursor.getString("db");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cg(con);
        Statement stmt = conn.createStatement();
        r = stmt.executeQuery("SELECT top 1 c.status FROM ["+base+"].[dbo].[corte] as c left outer join ["+base+"].[dbo].[dispositivos] as d on d.id=c.id_dispositivo where c.status = 0 and d.mac_adr = '"+mac+"'");
        if (!r.next()){
            res=1;
        }else {
            res=r.getInt(1);
        }
        conn.close();
        r.close();
        Log.w("Corte",res.toString());
        return res;
    }

}
