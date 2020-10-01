package cg.ce.app.chris.com.cgce;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.StrictMode;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by chris on 4/10/16.
 */
public class DataBaseCG {

    public Statement stmt=null;
    Cursor c;
    JSONObject cursor= null;
    private static Context context;




    public Statement odbc(Context con){
        context = con;
        DataBaseManager manager = new DataBaseManager(context);
        cursor = manager.cargarcursorodbc2();

        String direccion=null;
        String puerto=null;
        String user=null;
        String base=null;
        String pass = null;
        try {
            direccion = cursor.getString("ip");
            puerto = cursor.getString("puerto");
            user = cursor.getString("userdb");
            base = cursor.getString("db");
            pass = cursor.getString("passdb");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection conn = null;
        String connString = null;
        try {
            String driver = "net.sourceforge.jtds.jdbc.Driver";
            Class.forName(driver).newInstance();
            //test = com.microsoft.sqlserver.jdbc.SQLServerDriver.class;
            connString = "jdbc:jtds:sqlserver://" + direccion +":"+puerto+"/" + base + ";encrypt=false;user="+user+";password="+pass+";";
            Log.w("Connection",connString);
            conn = DriverManager.getConnection(connString);
            Log.w("Connection","open");
            stmt = conn.createStatement();
        } catch (IllegalAccessException e) {
            Log.w("IllegalAccessException",e);
            e.printStackTrace();
        } catch (InstantiationException e) {
            Log.w("InstantiationException",e);
            e.printStackTrace();
        } catch (SQLException e) {
            Log.w("SQLException",e);
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            Log.w("ClassNotFoundException",e);
            e.printStackTrace();
        }
        return stmt;
    }


    public Connection odbc_cg(Context con) throws IllegalAccessException, ClassNotFoundException, InstantiationException, SQLException {
        context = con;
        DataBaseManager manager = new DataBaseManager(context);
        cursor = manager.cargarcursorodbc2();
        Log.w("Cursor",cursor.toString());
        String direccion=null;
        String puerto=null;
        String user=null;
        String base=null;
        String pass = null;
        try {
            direccion = cursor.getString("ip");
            puerto = cursor.getString("puerto");
            user = cursor.getString("userdb");
            base = cursor.getString("db_cg");
            pass = cursor.getString("passdb");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection conn = null;
        String connString = null;
        String ConnURL = null;

        String driver = "net.sourceforge.jtds.jdbc.Driver";
        Class.forName(driver).newInstance();
        //test = com.microsoft.sqlserver.jdbc.SQLServerDriver.class;
        connString = "jdbc:jtds:sqlserver://" + direccion +":"+puerto+"/" + base + ";encrypt=false;user="+user+";password="+pass+";";

        conn = DriverManager.getConnection(connString);
        Log.w("ConnectionCG","open");

        return conn;
    }
    public Connection odbc_cecg_app(Context con) throws JSONException, ClassNotFoundException,
            InstantiationException, IllegalAccessException, SQLException {
        context = con;
        DataBaseManager manager = new DataBaseManager(context);
        cursor = manager.cargarcursorodbc2();

        String direccion=null;
        String puerto=null;
        String user=null;
        String base=null;
        String pass = null;

        direccion = cursor.getString("ip");
        puerto = cursor.getString("puerto");
        user = cursor.getString("userdb");
        base = cursor.getString("db");
        pass = cursor.getString("passdb");


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection conn = null;
        String connString = null;
        String ConnURL = null;

        String driver = "net.sourceforge.jtds.jdbc.Driver";
        Class.forName(driver).newInstance();
        //test = com.microsoft.sqlserver.jdbc.SQLServerDriver.class;
        connString = "jdbc:jtds:sqlserver://" + direccion +":"+puerto+"/" + base + ";encrypt=false;user="+user+";password="+pass+";";

        conn = DriverManager.getConnection(connString);
        Log.w("ConnectionCG","open");
        return conn;
    }
    public Connection control_gas(Context con) throws JSONException{
        Connection connect;
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();
        String direccion=null;
        String puerto=null;
        String user=null;
        String base=null;
        String pass = null;
        direccion = cursor.getString("ip");
        puerto = cursor.getString("puerto");
        user = cursor.getString("userdb");
        base = cursor.getString("db");
        pass = cursor.getString("passdb");
        connect = CONN(user, pass, base, direccion, Integer.valueOf(puerto));
        return connect;
    }
    @SuppressLint("NewApi")
    private Connection CONN(String _user, String _pass, String _DB,
                            String _server, Integer _puerto) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection conn = null;
        String ConnURL = null;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            ConnURL = "jdbc:jtds:sqlserver://" + _server + ":"+_puerto+";"
                    + "databaseName=" + _DB + ";user=" + _user + ";password="
                    + _pass + ";";
            conn = DriverManager.getConnection(ConnURL);
        } catch (SQLException se) {
            Log.e("ERRO", se.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e("ERRO", e.getMessage());
        } catch (Exception e) {
            Log.e("ERRO", e.getMessage());
        }
        return conn;
    }
}
