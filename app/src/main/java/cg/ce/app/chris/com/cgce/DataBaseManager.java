package cg.ce.app.chris.com.cgce;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by chris on 31/08/16.
 */
public class DataBaseManager {
    public static final String TABLE_NAME = "odbc";
    public static final String TABLE_NAME_PRINTER ="printer";
    public static final String TABLE_NAME_MARCA = "marca";

    public static final String CN_ID="_id";
    public static final String CN_DIRECCION="direccion";

    public static final String CN_PUERTO="puerto";
    public static final String CN_DB="db";
    public static final String CN_DB_CG="db_cg";
    public static final String CN_USERDB="user";
    public static final String CN_PASSDB="pass";
    public static final String CN_PRINTER="target";
    public static final String CN_MARCA="marca";
    public static final String CN_INTEGRA="integra";


    // create table odbc(
    //                      _id integer primary key autoincrement,
    //                      direccion text not null,
    //                      puerto text not null,
    //                      db text not null,
    //                      user text not null,
    //                      pass text not null);

    public static final String CREATE_TABLE = "create table " + TABLE_NAME+" ("
            + CN_ID + " integer primary key autoincrement,"
            + CN_DIRECCION + " text not null,"
            + CN_INTEGRA + " text not null,"
            + CN_PUERTO + " text not null,"
            + CN_DB + " text not null,"
            + CN_DB_CG + " text not null,"
            + CN_USERDB + " text not null,"
            + CN_PASSDB + " text not null);";

    public static final String CREATE_TABLE_PRINTER = "create table " + TABLE_NAME_PRINTER+" ("
            + CN_ID + " integer primary key autoincrement,"
            + CN_PRINTER + " text not null);";

    public static final String CREATE_TABLE_MARCA= "create table " + TABLE_NAME_MARCA+" ("
            + CN_ID + " integer primary key autoincrement,"
            + CN_MARCA + " text not null);";

    private DbHelper helper;
    private SQLiteDatabase db_sql;
    public DataBaseManager(Context context) {
        helper = new DbHelper(context);
        db_sql = helper.getWritableDatabase();

    }
    private ContentValues generaContentValues(String direccion, String integra, String  puerto, String db, String db_cg, String userdb,String passdb){
        ContentValues valores= new ContentValues();

        valores.put(CN_INTEGRA,integra);
        valores.put(CN_DIRECCION,direccion);
        valores.put(CN_PUERTO,puerto);
        valores.put(CN_DB,db);
        valores.put(CN_DB_CG,db_cg);
        valores.put(CN_USERDB,userdb);
        valores.put(CN_PASSDB,passdb);

        return valores;
    }
    private ContentValues generaContentValuesPrinter(String target){
        ContentValues valores = new ContentValues();
        valores.put(CN_PRINTER,target);
        return valores;
    }
    private ContentValues generaContentValuesMarca(String marca){
        ContentValues valores = new ContentValues();
        valores.put(CN_MARCA,marca);
        return valores;
    }
    public void insertar(String direccion, String integra, String  puerto, String db, String db_cg, String userdb,String passdb){

        db_sql.insert(TABLE_NAME,null,generaContentValues(direccion,integra,puerto,db,db_cg,userdb,passdb));
    }
    public void insertar_printer(String target){
        db_sql.insert(TABLE_NAME_PRINTER,null,generaContentValuesPrinter(target));
    }
    public void insertar_marca(String marca){
        db_sql.insert(TABLE_NAME_PRINTER,null,generaContentValuesMarca(marca));
    }
    public void eliminar(){
        db_sql.delete(TABLE_NAME,CN_ID+"=?",new String []{String.valueOf(1)});
    }
    public Cursor cargarcursorodbc(){
        String[] columnas = new String []{CN_ID,CN_DIRECCION,CN_PUERTO,CN_DB,CN_DB_CG,CN_USERDB,CN_PASSDB};
        JSONObject jsonObject= new JSONObject();
        return db_sql.query(TABLE_NAME,columnas,null,null,null,null,null);
    }
    public JSONObject cargarcursorodbc2(){
        JSONObject jsonObject=new JSONObject();
        String[] columnas = new String []{CN_ID,CN_DIRECCION,CN_INTEGRA,CN_PUERTO,CN_DB,CN_DB_CG,CN_USERDB,CN_PASSDB};
        try {
            Cursor cur =db_sql.query(TABLE_NAME,columnas,null,null,null,null,null);
            if (cur.moveToFirst()) {
                cur.moveToFirst();
                jsonObject.put("ip", cur.getString(1));
                jsonObject.put("integra",cur.getString(2));
                jsonObject.put("puerto", cur.getString(3));
                jsonObject.put("db", cur.getString(4));
                jsonObject.put("db_cg", cur.getString(5));
                jsonObject.put("userdb", cur.getString(6));
                jsonObject.put("passdb", cur.getString(7));
                cur.close();
                db_sql.close();
            }else{
                jsonObject.put("status","sin odbc");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
    public long contarodbc(){
        Cursor cur = db_sql.rawQuery("select count(_id) from odbc",null);
        cur.moveToFirst();
        int contador = cur.getInt(0);
        cur.close();
        return contador;
    }
    public long contarprinter(){
        Cursor cur = db_sql.rawQuery("select count(_id) from printer",null);
        cur.moveToFirst();
        int contador = cur.getInt(0);
        cur.close();
        return contador;
    }
    public String target(){
        String contador="";
        Cursor cur = db_sql.rawQuery("select target from printer",null);
        if(cur.moveToFirst()){
            contador = cur.getString(0);
        }else {
            contador ="Sin impresora ";
        }

        cur.close();
        db_sql.close();
        return contador;
    }
    public void actualizar (String direccion, String integra, String  puerto, String db, String db_cg, String userdb,String passdb){
        ContentValues valores= new ContentValues();
        valores.put(CN_DIRECCION,direccion);
        valores.put(CN_INTEGRA,integra);
        valores.put(CN_PUERTO,puerto);
        valores.put(CN_DB,db);
        valores.put(CN_DB_CG,db_cg);
        valores.put(CN_USERDB,userdb);
        valores.put(CN_PASSDB,passdb);
        db_sql.update(TABLE_NAME,valores,CN_ID+"=?",new String [] {String.valueOf(1)});
        db_sql.close();
    }
    public void actualizar_printer (String target){
        ContentValues valores = new ContentValues();
        valores.put(CN_PRINTER,target);
        db_sql.update(TABLE_NAME_PRINTER,valores,CN_ID+"=?",new String [] {String.valueOf(1)});
        db_sql.close();
    }

}
