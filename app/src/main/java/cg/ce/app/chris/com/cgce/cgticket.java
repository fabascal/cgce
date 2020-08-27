package cg.ce.app.chris.com.cgce;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.NetworkInterface;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cg.ce.app.chris.com.cgce.listeners.StringListener;

/**
 * Created by chris on 1/12/16.
 */

public class cgticket {


    ResultSet r;
    Connection connect;
    JSONObject cursor = null;
    LogCE logCE = new LogCE();

    public JSONObject consulta_servicio(Context con,String bomba) throws SQLException,
            IllegalAccessException, InstantiationException, ClassNotFoundException, JSONException {
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();
        String base;
        base = cursor.getString("db_cg");
        ResultSet r;
        JSONObject st=new JSONObject();
        ValidarDispositivo vd = new ValidarDispositivo();
        MacActivity mac_add = new MacActivity();
        String mac = mac_add.getMacAddress();
        Log.w("Mac", "Mac: " + mac);
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cg(con);
        Statement stmt = conn.createStatement();
        r = stmt.executeQuery("SELECT top 1 desp.nrotrn,desp.can,desp.mto,desp.pre,prod.den,desp.nrobom,resp.den,\n" +
                "Convert(VARCHAR(10), cast(cast(desp.fchtrn-1 as int) as datetime) , 111),desp.codprd,gas.cveest,desp.mtogto,desp.codcli,\n" +
                "cli.den,desp.hratrn,desp.codgas,desp.codprd,desp.nroveh,desp.odm,desp.fchcor,desp.nrotur,desp.nrocte,\n" +
                "(select top 1 pre from ["+base+"].[dbo].[Precios] where codprd=desp.codprd and codgas=desp.codgas and fch<=desp.fchtrn order by fch desc),\n" +
                "(select top 1 iva from ["+base+"].[dbo].[Precios] where codprd=desp.codprd and codgas=desp.codgas and fch<=desp.fchtrn order by fch desc) ,\n" +
                "(select top 1 preiie from ["+base+"].[dbo].[Precios] where codprd=desp.codprd and codgas=desp.codgas and fch<=desp.fchtrn order by fch desc) \n" +
                "FROM ["+base+"].[dbo].[Despachos] as desp\n" +
                "left outer join ["+base+"].[dbo].[Productos] as prod on prod.cod=desp.codprd \n" +
                "left outer join ["+base+"].[dbo].[Responsables] as resp on resp.cod=desp.codres \n" +
                "left outer join ["+base+"].[dbo].[Gasolineras] as gas on gas.cod=desp.codgas \n" +
                "left outer join ["+base+"].[dbo].[Clientes] as cli on cli.cod=desp.codcli \n" +
                "where desp.nrobom ="+bomba+" order by desp.nrotrn desc");
               /* "where desp.nrotrn='39848930' order by desp.nrotrn desc");*/
        if (!r.next()) {
        }
        float a = r.getFloat(11);
        if (a<0){
            a=a*-1;
        }
        st.put("nrotrn",r.getInt(1));
        st.put("cantidad",(r.getFloat(3)+a)/r.getFloat(4));
        st.put("precio",r.getFloat(4));
        st.put("total",String.format("%.2f",r.getFloat(3)+a));
        st.put("producto",r.getString(5));
        st.put("bomba",r.getInt(6));
        st.put("despachador",nombre_depsachador(con));
        st.put("fecha",r.getString(8));
        st.put("id_producto",r.getInt(9));
        st.put("cveest",r.getString(10));
        st.put("mtogto",r.getFloat(11));
        st.put("codcli",r.getInt(12));
        st.put("dencli",r.getString(13));
        st.put("hora",hora(String.valueOf(r.getInt(14))));
        st.put("codgas",r.getInt(15));
        st.put("codprd",r.getInt(16));
        st.put("nroveh",r.getInt(17));
        st.put("odm",r.getString(18));
        st.put("fchcor",r.getString(19));
        st.put("nrotur",r.getString(20));
        st.put("nrocte",r.getString(21));
        st.put("mtogto",0);
        st.put("logusu",1);
        st.put("iva", r.getDouble(23));
        st.put("ieps", r.getDouble(24));
        conn.close();
        stmt.close();
        return st;
    }
    public String get_nrotrn_aceite(Context con, String bomba) throws JSONException,
            ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();
        String base = null;
        base = cursor.getString("db_cg");
        ResultSet r = null;
        String result;
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cg(con);
        Statement stmt = conn.createStatement();
        r = stmt.executeQuery("select top 1 nrotrn as nrotrn from ["+base+"].[dbo].[Despachos] " +
                "where nrobom="+bomba+" order by nrotrn");
        if (!r.next() ) {
            System.out.println("No data");
        }
        result =  String.valueOf(r.getInt("nrotrn"));
        stmt.close();
        conn.close();
        System.out.println("nro de aceite");
        System.out.println(result);
        return result;
    }
    public JSONObject consulta_servicio_aceite(Context con,String bomba) throws SQLException,
            IllegalAccessException, InstantiationException, ClassNotFoundException, JSONException {
        String nrotrn_mayor = get_nrotrn_aceite(con, bomba);
        String nrotrn_menor = nrotrn_mayor.substring(0,nrotrn_mayor.length()-1) + "0";
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();
        int qty=0;
        double total=0;
        String base = null;
        base = cursor.getString("db_cg");
        ResultSet r = null;
        JSONObject st=new JSONObject();
        JSONObject st_list ;
        JSONArray items = new JSONArray();
        ValidarDispositivo vd = new ValidarDispositivo();
        MacActivity mac_add = new MacActivity();
        String mac = mac_add.getMacAddress();
        Log.w("Mac", "Mac: " + mac);
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cg(con);
        Statement stmt = conn.createStatement();
        r = stmt.executeQuery("SELECT desp.nrotrn as nrotrn,desp.can as can,desp.mto as mto,desp.pre as pre,prod.den as prod_den,desp.nrobom as nrobom ,resp.den as resp_den,\n" +
                "Convert(VARCHAR(10), cast(cast(desp.fchtrn-1 as int) as datetime) , 111) as fecha,desp.codprd as codprd,gas.cveest as cveest,desp.mtogto as mtogto,desp.codcli as codcli,\n" +
                "cli.den as cli_den,desp.hratrn as hratrn,desp.codgas as codgas,desp.nroveh as nroveh,desp.odm as odm,desp.fchcor as fchcor,desp.nrotur as nrotur,desp.nrocte as nrocte,\n" +
                "(select top 1 pre as precio from ["+base+"].[dbo].[Precios] where codprd=desp.codprd and codgas=desp.codgas and fch<=desp.fchtrn order by fch desc) as precio,\n" +
                "(select top 1 iva as iva from ["+base+"].[dbo].[Precios] where codprd=desp.codprd and codgas=desp.codgas and fch<=desp.fchtrn order by fch desc) as iva,\n" +
                "(select top 1 preiie as preiie from ["+base+"].[dbo].[Precios] where codprd=desp.codprd and codgas=desp.codgas and fch<=desp.fchtrn order by fch desc) as ieps \n" +
                "FROM ["+base+"].[dbo].[Despachos] as desp\n" +
                "left outer join ["+base+"].[dbo].[Productos] as prod on prod.cod=desp.codprd \n" +
                "left outer join ["+base+"].[dbo].[Responsables] as resp on resp.cod=desp.codres \n" +
                "left outer join ["+base+"].[dbo].[Gasolineras] as gas on gas.cod=desp.codgas \n" +
                "left outer join ["+base+"].[dbo].[Clientes] as cli on cli.cod=desp.codcli \n" +
                "where desp.nrotrn between " + nrotrn_mayor + " and "+ nrotrn_menor +" order by desp.nrotrn desc ");

        String despachador;
        if (!r.next() ) {
            System.out.println("No data");
        }
        if (r.getString("resp_den") == null){
            despachador="DESPACHADOR";
        }else{
            despachador=r.getString(7);
        }
        st.put("nrotrn",nrotrn_menor);
        st.put("bomba",r.getInt("nrobom"));
        st.put("despachador",nombre_depsachador(con));
        st.put("fecha",r.getString("fecha"));
        st.put("cveest",r.getString("cveest"));
        st.put("codcli",r.getInt("codcli"));
        st.put("dencli",r.getString("cli_den"));
        st.put("hora",hora(String.valueOf(r.getInt("hratrn"))));
        st.put("codgas",r.getInt("codgas"));
        st.put("nroveh",r.getInt("nroveh"));
        st.put("odm",r.getString("odm"));
        st.put("fchcor",r.getString("fchcor"));
        st.put("nrotur",r.getString("nrotur"));
        st.put("nrocte",r.getString("nrocte"));
        st.put("mtogto",0);
        st.put("logusu",1);
        while (r.next()){
            qty+= r.getInt("can");
            total += r.getDouble("mto");
            st_list = new JSONObject();
            st_list.put("cantidad",r.getString("can"));
            st_list.put("precio",r.getString("precio"));
            st_list.put("total",String.format("%.2f",r.getDouble("mto")));
            st_list.put("producto",r.getString("prod_den"));
            st_list.put("id_producto",r.getString("codprd"));
            items.put(st_list);
            st.put("items",items);
        }
        st.put("qty",qty);
        st.put("total",total);
        conn.close();
        stmt.close();
        System.out.println("result oil for print" + String.valueOf(st));
        return st;
    }
    public JSONObject busca_producto (Context context, String barcode) throws SQLException,
            JSONException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        JSONObject res = new JSONObject();
        DataBaseManager manager = new DataBaseManager(context);
        cursor = manager.cargarcursorodbc2();
        String base = null;
        base = cursor.getString("db_cg");
        ResultSet r = null;
        ValidarDispositivo vd = new ValidarDispositivo();
        MacActivity mac_add = new MacActivity();
        String mac = mac_add.getMacAddress();
        Log.w("Mac", "Mac: " + mac);
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cg(context);
        Statement stmt = conn.createStatement();
        r = stmt.executeQuery("select prd.cod,prd.den,prd.tip,prd.uni,prd.codsat,\n" +
                "(select top 1 pre from ["+base+"].[dbo].[Precios] where codprd=prd.cod  and fch<=convert(int,getdate()) order by fch desc) as precio,\n" +
                "(select top 1 iva from ["+base+"].[dbo].[Precios] where codprd=prd.cod and  fch<=convert(int,getdate()) order by fch desc) as iva,\n" +
                "(select top 1 preiie from ["+base+"].[dbo].[Precios] where codprd=prd.cod and fch<=convert(int,getdate()) order by fch desc) as ieps," +
                "prd.codext as codext \n" +
                "from ["+base+"].[dbo].[Productos] as prd\n" +
                "where prd.codbar ='" + barcode + "'");
        if (!r.next()){
            res.put("error","No existe producto!");
            conn.close();
            stmt.close();
            return res;
        }else{
            res.put("codprd",r.getInt(1));
            res.put("descripcion",r.getString(2));
            res.put("tipo",r.getString(3));
            res.put("unidadsat",r.getString(4));
            res.put("codsat",r.getString(5));
            res.put("precio",r.getDouble(6));
            res.put("iva",r.getDouble(7));
            res.put("ieps",r.getDouble(8));
            res.put("codext",r.getString(9));
            conn.close();
            stmt.close();
            return res;
        }

    }
    public String getNipManager(Context context) throws SQLException, ClassNotFoundException,
            InstantiationException, JSONException, IllegalAccessException {
        String nip = null;
        DataBaseManager manager = new DataBaseManager(context);
        cursor = manager.cargarcursorodbc2();
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cecg_app(context);
        Statement stmt = conn.createStatement();
        String query = "select pass as pass from despachadores where gerente = 1 ";
        r = stmt.executeQuery(query);
        while (r.next()){
            nip = r.getString("pass");
        }
        conn.close();
        stmt.close();
        return nip;
    }
    public JSONObject corte_cinepolis(Context context) throws ClassNotFoundException, SQLException,
            InstantiationException, JSONException, IllegalAccessException {
        JSONObject resultado = new JSONObject();
        DataBaseManager manager = new DataBaseManager(context);
        cursor = manager.cargarcursorodbc2();
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cecg_app(context);
        Log.w("si entro","1");
        Statement stmt = conn.createStatement();
        String query = "select sum(boletos) as boletos, sum(total) as total from despachos where corte="+get_corte(context)+" and folios is not null";
        r = stmt.executeQuery(query);
        if(!r.next()){
            resultado.put("boletos","0");
            resultado.put("total","0");
            stmt.close();
            conn.close();
        }else {
            resultado.put("boletos",r.getString("boletos"));
            resultado.put("total",r.getString("total"));
            stmt.close();
            conn.close();
        }
        return resultado;
    }
    public JSONObject corte_datos(Context context) throws ClassNotFoundException, SQLException,
            InstantiationException, JSONException, IllegalAccessException {
        JSONObject resultado = new JSONObject();
        DataBaseManager manager = new DataBaseManager(context);
        cursor = manager.cargarcursorodbc2();
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cecg_app(context);
        Statement stmt = conn.createStatement();
        String query = "select hora_entrada,hora_salida from corte where id="+get_corte(context)+"";
        r = stmt.executeQuery(query);
        if(!r.next()){
            resultado.put("hora_entrada","0");
            resultado.put("hora_salida","0");
        }else {
            resultado.put("hora_entrada",r.getString("hora_entrada"));
            resultado.put("hora_salida",r.getString("hora_salida"));
        }
        conn.close();
        stmt.close();
        return resultado;
    }
    //funcion para obtener datos del cliente
    public JSONObject get_vehiculo(Context context,String nrotrn,String bomba) throws
            ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, JSONException {
        JSONObject resultado = new JSONObject();
        ResultSet r;
        DataBaseCG cg = new DataBaseCG();
        Connection connection= cg.odbc_cg(context);
        Statement stmt = connection.createStatement();
        String query = "select top 1 d.nrotrn,cv.plc,cv.rsp,cv.nroeco,d.odm,cv.codcli,cv.nroveh," +
                "d.codcli,d.nroveh, cv.tar from Despachos as d\n" +
                "inner join ClientesVehiculos as cv on d.nroveh=cv.nroveh and d.codcli=cv.codcli\n" +
                "where d.nrotrn="+nrotrn+" and d.nrobom="+bomba+" order by d.nrotrn desc";
        r = stmt.executeQuery(query);
        if(!r.next()){
            resultado.put("placa","Sin Placa");
            resultado.put("rsp","Sin Chofer");
            resultado.put("nroeco","Sin No° Economico");
            resultado.put("ultodm","Sin Odometro");
            resultado.put("tar",0);
        }else{
                resultado.put("placa", r.getString("plc"));
                resultado.put("rsp", r.getString("rsp"));
                resultado.put("nroeco", r.getString("nroeco"));
                resultado.put("ultodm", r.getString("odm"));
                resultado.put("tar",r.getString("tar"));
        }
        connection.close();
        stmt.close();
        return resultado;
    }
    public JSONObject consulta_credito(Context con,Integer cliente) throws SQLException,
            IllegalAccessException, InstantiationException, ClassNotFoundException, JSONException {
        JSONObject st=new JSONObject();
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();
        String base = null;
        base = cursor.getString("db_cg");
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cg(con);
        Statement stmt = conn.createStatement();
        String query = "SELECT tipval as tipval FROM ["+base+"].[dbo].[Clientes] where cod = '"+cliente+"'";
        Log.w("qwery",query);
        r = stmt.executeQuery(query);
        while (r.next()) {
            st.put("tip_cliente", r.getInt("tipval"));
        }
        conn.close();
        stmt.close();
        return st;
    }
    public String hora (String hora){
        if (hora.length()==1){
            hora="000"+hora;
        }else if (hora.length()==2){
            hora="00"+hora;
        }else if (hora.length()==3){
            hora="0"+hora;
        }
        Log.w("hora",hora);
        String hora_impresa;
        hora_impresa=hora.substring(0,2)+":"+hora.substring(2,4);
        return hora_impresa;
    }
    public boolean guardarnrotrn2 (Context con, String ticket, int venta) throws
            ClassNotFoundException, SQLException, InstantiationException, JSONException,
            IllegalAccessException {
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();
        String base = null;
        base = cursor.getString("db");
        ValidarDispositivo vd = new ValidarDispositivo();
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cecg_app(con);
        String ticket2= ticket+"0";
        String query = "insert into ["+base+"].[dbo].[despachos] (nrotrn,nota,corte,impreso,tipo_venta," +
                "flotillero) values("+ticket+","+ticket2+","+get_corte(con)+",0,"+venta+",0)";
        Statement stmt = conn.createStatement();
        Log.w("query", query);
        stmt.execute(query);
        stmt.close();
        conn.close();
        return true;
    }
    public boolean guardarnrotrn3 (Context con, String folio, String correo, String boletos,
                                   String total, int venta) throws ClassNotFoundException,
            SQLException, InstantiationException, JSONException, IllegalAccessException {
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();
        String base = null;
        base = cursor.getString("db");
        ValidarDispositivo vd = new ValidarDispositivo();
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cecg_app(con);
        String query = "insert into ["+base+"].[dbo].[despachos] (nrotrn,nota,corte,impreso,tipo_venta," +
                "flotillero,folios,cinepolis_correo,boletos,total) values(0,0,"+get_corte(con)+",0," +
                ""+venta+",0, '"+folio+"','"+correo+"',"+ boletos+","+total+")";
        Statement stmt = conn.createStatement();
        Log.w("query", query);
        stmt.execute(query);
        stmt.close();
        conn.close();
        return true;
    }
    public Integer get_corte(Context con) throws ClassNotFoundException, SQLException,
            InstantiationException, JSONException, IllegalAccessException {
        Integer res=0;
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();
        String base = null;
        base = cursor.getString("db");
        String query = "select c.id as cor from ["+base+"].[dbo].[corte ] as c\n" +
                "left outer join dispositivos as d on d.id=c.id_dispositivo\n" +
                "where d.mac_adr='"+getMacAddress()+"' and c.status=0";
        Statement stmt = conn.createStatement();
        r=stmt.executeQuery(query);
        while (r.next()) {
            res = r.getInt("cor");
        }
        conn.close();
        stmt.close();
        Log.w("qwery",query);
        Log.w("res", String.valueOf(res));
        return res;
    }
    public boolean actualizar_cant_impreso (Context con, String ticket ) throws
            ClassNotFoundException, InstantiationException, JSONException, IllegalAccessException, SQLException {
        String query = "update despachos set impreso=1 where nrotrn="+ticket+"";
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cecg_app(con);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(query);
        stmt.close();
        conn.close();
        return true;
    }
    public boolean guardarnrotrn (Context con,JSONObject ticket,int venta) throws
            ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, JSONException {
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();
        String query;
        String base = null;
        base = cursor.getString("db_cg");
        ResultSet r = null;
        JSONObject st=new JSONObject();
        ValidarDispositivo vd = new ValidarDispositivo();
        MacActivity mac_add = new MacActivity();
        String mac = mac_add.getMacAddress();
        Log.w("Mac", "Mac: " + mac);
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cg(con);
        Statement stmt = conn.createStatement();
        String ticket2=ticket.getString("nrotrn")+"0";
        if (ticket.has("rut")){
            Log.w("query tiptrn","update ["+base+"].[dbo].[Despachos] set nrocte="+ticket2+"," +
                    " rut="+ ticket.getString("rut") +", tiptrn= "+ ticket.getInt("tiptrn") +
                    " where nrotrn = "+ticket.getString("nrotrn")+"");
            if(ticket.getString("rut").substring(0,1).equals("6")){
                stmt.executeUpdate("update ["+base+"].[dbo].[Despachos] set codres='"+
                        getCodDespCG(con)+"' , rut='"+ ticket.getString("rut") +"', tiptrn= "+
                        ticket.getInt("tiptrn") +" where nrotrn = "+ticket.getString("nrotrn")+"");
            }else{
                stmt.executeUpdate("update ["+base+"].[dbo].[Despachos] set nrocte="+ticket2+"," +
                        "codres='"+getCodDespCG(con)+"' , rut='"+ ticket.getString("rut") +"', " +
                        "tiptrn= "+ ticket.getInt("tiptrn") +" where nrotrn = "+ticket.getString("nrotrn")+"");
            }

        }else{
            System.out.println("identificacion es credito");
            stmt.executeUpdate("update ["+base+"].[dbo].[Despachos] set nrocte="+ticket2+" where nrotrn = "+ticket.getString("nrotrn")+"");
        }
        stmt.close();
        conn.close();
        Log.w("combu","true");
        guardarnrotrn2(con,ticket.getString("nrotrn"),venta);
        return true; }
    public boolean guardarnrotrn_old (Context con,String ticket,int venta) throws
            ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, JSONException {
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();
        String base = null;
        base = cursor.getString("db_cg");
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cg(con);
        Statement stmt = conn.createStatement();
        String ticket2=String.valueOf(ticket)+"0";
        Log.w("combu",String.valueOf(ticket)+"//"+String.valueOf(ticket2));
        stmt.executeUpdate("update ["+base+"].[dbo].[Despachos] set nrocte="+ticket2+" where nrotrn = "+ticket+"");
        stmt.close();
        conn.close();
        Log.w("combu","true");
        guardarnrotrn2(con,ticket,venta);
        return true;
    }
    public boolean setTipTrn (Context con,String tiptrn, String nrotrn) throws
            ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, JSONException {
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();
        String base = null;
        base = cursor.getString("db_cg");
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cg(con);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("update ["+base+"].[dbo].[Despachos] set tiptrn="+tiptrn+" where nrotrn = "+nrotrn+"");
        stmt.close();
        conn.close();
        return true;
    }
    public boolean put_tpv (Context con,String ticket,int tpv) throws ClassNotFoundException,
            SQLException, InstantiationException, IllegalAccessException, JSONException {
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();
        String base = null;
        base = cursor.getString("db");
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cg(con);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("update ["+base+"].[dbo].[despachos] set tpv_id="+tpv+" where nrotrn = "+ticket+"");
        stmt.close();
        conn.close();
        Log.w("combu","true");
        return true;
    }
    public boolean update_flotillero (Context con,String ticket) throws ClassNotFoundException,
            SQLException, InstantiationException, IllegalAccessException, JSONException {
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();
        String base = null;
        base = cursor.getString("db");
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cg(con);
        Statement stmt = conn.createStatement();
        String ticket2=String.valueOf(ticket)+"0";
        Log.w("combu",String.valueOf(ticket)+"//"+String.valueOf(ticket2));
        stmt.executeUpdate("update ["+base+"].[dbo].[despachos] set flotillero=1 where nrotrn = "+ticket+"");
        stmt.close();
        conn.close();
        Log.w("combu","true");
        return true;
    }
    public boolean update_codcli (Context con,String ticket,String cliente,String vehiculo,
                                  String odm,String tar) throws ClassNotFoundException, SQLException,
            InstantiationException, IllegalAccessException, JSONException {
        if(odm.equals(null)){
            odm="0";
        }
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();
        String base = null;
        base = cursor.getString("db_cg");
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cg(con);
        Statement stmt = conn.createStatement();
        Log.i("update_cli","update ["+base+"].[dbo].[Despachos] set codcli ="+cliente+", nroveh="+vehiculo+", odm="+odm+", tar="+tar+" where nrotrn = "+ticket+"");
        stmt.executeUpdate("update ["+base+"].[dbo].[Despachos] set codcli ="+cliente+", nroveh="+vehiculo+", odm="+odm+", tar="+tar+" where nrotrn = "+ticket+"");
        stmt.close();
        conn.close();
        return true;
    }
    public String nip_desp (Context con) throws ClassNotFoundException, SQLException,
            InstantiationException, JSONException, IllegalAccessException {
        String res="";
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        String query = "select d.pass as pass from despachadores d \n" +
                "left outer join corte c on c.id_despachador=d.id \n" +
                "left outer join dispositivos dis on dis.id=c.id_dispositivo\n " +
                "where dis.mac_adr='"+getMacAddress()+"' and c.status=0";
        Statement stmt = conn.createStatement();
        r=stmt.executeQuery(query);
        if (r.next()) {
            res = r.getString("pass");
        }
        conn.close();
        stmt.close();
        return res;
    }
    public String get_rut (Context con, JSONObject jsonObject) throws ClassNotFoundException,
            SQLException, InstantiationException, IllegalAccessException, JSONException {
        String res="";
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cg(con);
        String query = "select rut as rut from Despachos where nrotrn ='"+jsonObject.getString("nrotrn")+"'";
        Statement stmt = conn.createStatement();
        r=stmt.executeQuery(query);
        if (r.next()) {
            res = r.getString("rut");
        }
        conn.close();
        stmt.close();
        return res;
    }
    public String getCodDespCG (Context con) throws ClassNotFoundException, SQLException,
            InstantiationException, IllegalAccessException, JSONException {
        String res="";
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cg(con);
        String query = "select cod as cod from Responsables where tag = '"+nip_desp(con)+"'";
        Statement stmt = conn.createStatement();
        r=stmt.executeQuery(query);
        if (r.next()) {
            res = r.getString("cod");
        }
        conn.close();
        stmt.close();
        Log.w("rut", res);
        return res;
    }
    ///funcion para obtener el rfc del cliente en la base de cg mediante identificcion de nip
    public String get_rfc_nip (Context con,String tag, String metodo) throws ClassNotFoundException,
            SQLException, InstantiationException, IllegalAccessException {
        String res="";
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cg(con);
        String query="";
        if (metodo=="nfc") {
            query = "select c.rfc as rfc from ClientesVehiculos as cv \n" +
                    "left outer join Clientes as c on cv.codcli=c.cod \n" +
                    "where cv.tag ='" + tag + "'";
        }else{
            query = "select c.rfc as rfc from ClientesVehiculos as cv \n" +
                    "left outer join Clientes as c on cv.codcli=c.cod \n" +
                    "where cv.tag ='" + tag + "'";
        }
        Statement stmt = conn.createStatement();
        r=stmt.executeQuery(query);
        if (r.next()) {
            res = r.getString("rfc");
        }
        conn.close();
        stmt.close();
        return res;
    }
    public String get_cliente_den (Context con,String tag, String metodo) throws
            ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        String res="";
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cg(con);
        String query="";
        if (metodo=="nfc") {
            query = "select c.den from Clientes as c\n" +
                    "left outer join ClientesVehiculos as cv on cv.codcli=c.cod\n" +
                    "where cv.tag ='" + tag + "'";
        }else if(metodo=="nip"){
            query = "select c.den from Clientes as c\n" +
                    "left outer join ClientesVehiculos as cv on cv.codcli=c.cod\n" +
                    "where cv.tag ='" + tag + "'";
        }else if(metodo=="nombre"){
            query = "select c.den from Clientes as c\n" +
                    "left outer join ClientesVehiculos as cv on cv.codcli=c.cod\n" +
                    "where cv.tar ='" + tag + "'";
        }
        Statement stmt = conn.createStatement();
        r=stmt.executeQuery(query);
        if (r.next()) {
            res = r.getString("den");
        }
        conn.close();
        stmt.close();
        return res;
    }
    public String nombre_depsachador (Context con) throws ClassNotFoundException, SQLException,
            InstantiationException, JSONException, IllegalAccessException {
        String res="DESPACHADOR";
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        String query = "select d.nombre as pass from despachadores d \n" +
                "left outer join corte c on c.id_despachador=d.id \n" +
                "left outer join dispositivos dis on dis.id=c.id_dispositivo\n " +
                "where dis.mac_adr='"+getMacAddress()+"' and c.status=0";
        Statement stmt = conn.createStatement();
        r=stmt.executeQuery(query);
        if (r.next()) {
            res = r.getString("pass");
        }
        conn.close();
        stmt.close();
        return res;
    }
    public int id_depsachador (Context con) throws ClassNotFoundException, SQLException,
            InstantiationException, JSONException, IllegalAccessException {
        int res=0;
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        String query = "select d.id as pass from despachadores d \n" +
                "left outer join corte c on c.id_despachador=d.id \n" +
                "left outer join dispositivos dis on dis.id=c.id_dispositivo\n " +
                "where dis.mac_adr='"+getMacAddress()+"' and c.status=0";
        Statement stmt = conn.createStatement();
        r=stmt.executeQuery(query);
        if (r.next()) {
            res = r.getInt("pass");
        }
        conn.close();
        stmt.close();
        return res;
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
    public Integer get_bomba_libre(Context con, String bomba) throws ClassNotFoundException,
            SQLException, InstantiationException, JSONException, IllegalAccessException {
        Integer res=0;
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        String query = "select p.numero_logico as logico from posicion as p \n" +
                "left outer join dispensario as disp on disp.id=p.id_dispensario\n" +
                "left outer join corte as c on c.id_dispensario=disp.id\n" +
                "left outer join dispositivos as dispo on dispo.id=c.id_dispositivo\n" +
                "where c.status =0 and p.numero_logico not in ("+bomba+") and dispo.mac_adr='"+getMacAddress()+"'";
        Statement stmt = conn.createStatement();
        r=stmt.executeQuery(query);
        while (r.next()) {
            res = r.getInt("logico");
        }
        conn.close();
        stmt.close();
        return res;
    }
    public Integer get_configid(Context con) throws ClassNotFoundException, SQLException,
            InstantiationException, JSONException, IllegalAccessException {
        Integer res=0;
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();
        String base = null;
        base = cursor.getString("db");
        String query = "select config_id from ["+base+"].[dbo].[datos_factura] ";
        Statement stmt = conn.createStatement();
        r=stmt.executeQuery(query);
        while (r.next()) {
            res = 34;//r.getInt("config_id");
        }
        conn.close();
        stmt.close();
        return res;
    }
    public String get_cveest(Context con) throws ClassNotFoundException, SQLException,
            InstantiationException, JSONException, IllegalAccessException {
        String res=null;
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();
        String base = null;
        base = cursor.getString("db");
        String query = "select cveest from ["+base+"].[dbo].[datos_factura ] ";
        Statement stmt = conn.createStatement();
        r=stmt.executeQuery(query);
        if (r.next()) {
            res = r.getString("cveest");
        }
        conn.close();
        stmt.close();
        return res;
    }
    public String get_estacion(Context con) throws ClassNotFoundException, SQLException,
            InstantiationException, JSONException, IllegalAccessException {
        String res=null;
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();
        String base = null;
        base = cursor.getString("db");
        String query = "select top 1 nombre from ["+base+"].[dbo].[estacion] where id in (select " +
                "distinct(id_estacion) from ["+base+"].[dbo].[datos_factura])";
        Statement stmt = conn.createStatement();
        r=stmt.executeQuery(query);
        if (r.next()) {
            res = r.getString("nombre");
        }
        conn.close();
        stmt.close();
        return res;
    }
    public int guardaraceite (Context con, JSONObject jsonObject) throws ClassNotFoundException,
            SQLException, InstantiationException, JSONException, IllegalAccessException {
        int res=0;
        String nota=null;
        JSONObject aceite = jsonObject;
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();
        String base = null;
        base = cursor.getString("db");
        ValidarDispositivo vd = new ValidarDispositivo();
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cecg_app(con);
        String query = null;
            query = "insert into ["+base+"].[dbo].[aceites] (codigo,producto,precio,cantidad,corte," +
                    "rfc,tipo_venta,web,cancelado,impreso,hora_venta) OUTPUT Inserted.nrotrn " +
                    "values('"+aceite.getString("codigo")+"','"+aceite.getString("producto")+
                    "','"+aceite.getString("precio")+"','"+aceite.getString("cantidad")+
                    "','"+aceite.getString("corte")+"','"+aceite.getString("rfc")+"','"+
                    aceite.getString("tipo_venta")+"',default,default,default,getdate()) ";
        Statement stmt = conn.createStatement();
        r=stmt.executeQuery(query);
        if (r.next()) {
            res = r.getInt("nrotrn");
            String query_update ="update ["+base+"].[dbo].[aceites] set nota ="+res*10+" where nrotrn="+res+" ";
            stmt.executeUpdate(query_update);
            String query_select ="select nota,codigo,producto,precio,cantidad from ["+base+"].[dbo].[aceites] where nrotrn="+res+"";
            ResultSet resultSet=stmt.executeQuery(query_select);
            if (resultSet.next()){
                nota=resultSet.getString("nota");
            }
        }
        stmt.close();
        conn.close();
        return res;
    }
    public boolean updateaceiteweb (Context con, JSONObject jsonObject) throws ClassNotFoundException,
            SQLException, InstantiationException, IllegalAccessException, JSONException {
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();
        String base = null;
        base = cursor.getString("db");
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cg(con);
        Statement stmt = conn.createStatement();
        Log.w("query_update","update ["+base+"].[dbo].[aceites] set web=1 where nrotrn = "+jsonObject.getString("nrotrn")+"");
        stmt.executeUpdate("update ["+base+"].[dbo].[aceites] set web=1 where nrotrn = "+jsonObject.getString("nrotrn")+"");
        stmt.close();
        conn.close();
        Log.w("combu","true");
        return true;
    }
    public boolean updateaceiteimpresion (Context con, JSONObject jsonObject) throws
            ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException,
            JSONException {
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();
        String base = null;
        base = cursor.getString("db");
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cg(con);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("update ["+base+"].[dbo].[aceites] set impreso=1 where nrotrn = "+jsonObject.getString("nrotrn")+"");
        stmt.close();
        conn.close();
        Log.w("combu","true");
        return true;
    }
    public  int cantimpresoaceite(Context con, JSONObject jsonObject) throws ClassNotFoundException,
            SQLException, InstantiationException, JSONException, IllegalAccessException {
        int res=0;
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        String query = null;
        query = "select impreso from [cecg_app].[dbo].[aceites]  \n" +
                "where nrotrn='"+jsonObject.getString("nota")+"'";
        Statement stmt = conn.createStatement();
        r=stmt.executeQuery(query);
        if (r.next()) {
            res = r.getInt("impreso");
        }
        conn.close();
        stmt.close();
        return res;
    }
    public  ArrayList<String> banderas(Context con) throws ClassNotFoundException, SQLException,
            InstantiationException, JSONException, IllegalAccessException {
        String res=null;
        ArrayList<String> data = new ArrayList<String>();
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        String query = null;
        query = "select nombre from [cecg_app].[dbo].[bandera]";
        Statement stmt = conn.createStatement();
        r=stmt.executeQuery(query);
        while (r.next()) {
            String id = r.getString("nombre");
            data.add(id);
        }
        conn.close();
        stmt.close();
        return data;
    }
    public boolean limpiarbandera (Context con) throws ClassNotFoundException, SQLException,
            InstantiationException, JSONException, IllegalAccessException {
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        String query = null;
        query = "update [cecg_app].[dbo].[bandera]  \n" +
                "set uso = 0";
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(query);
        stmt.close();
        conn.close();
        Log.w("combu","true");
        return true;
    }
    public boolean insertarbandera (Context con, String bandera) throws ClassNotFoundException,
            SQLException, InstantiationException, JSONException, IllegalAccessException {
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        String query = null;
        query = "update [cecg_app].[dbo].[bandera]  \n" +
                "set uso = 1 where nombre='"+bandera+"'";
        Log.w("query",query);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(query);
        stmt.close();
        conn.close();
        Log.w("combu","true");
        return true;
    }
    public boolean insertarurl(Context con, String bandera,String url) throws ClassNotFoundException,
            SQLException, InstantiationException, JSONException, IllegalAccessException {
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        String query = null;
        query = "update [cecg_app].[dbo].[bandera]  \n" +
                "set urltimbre = '"+url+"' where nombre='"+bandera+"'";
        Log.w("query",query);
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(query);
        stmt.close();
        conn.close();
        Log.w("combu","true");
        return true;
    }
    public  String fechaaceite (Context con, JSONObject jsonObject) throws ClassNotFoundException,
            SQLException, InstantiationException, JSONException, IllegalAccessException {
        String res=null;
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        String query = null;
        query = "select hora_venta from [cecg_app].[dbo].[aceites]  \n" +
                "where nrotrn='"+jsonObject.getString("nrotrn")+"'";
        Statement stmt = conn.createStatement();
        r=stmt.executeQuery(query);
        if (r.next()) {
            res = String.valueOf(r.getDate("hora_venta"));
        }
        conn.close();
        stmt.close();
        Log.w("fecha_res",res);
        return res;
    }
    public int get_isla (Context con, JSONObject jsonObject) throws ClassNotFoundException,
            SQLException, InstantiationException, JSONException, IllegalAccessException {
        int res=0;
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();
        String query = null;
        query = "select d.numero_logico as disp from dispensario as d \n" +
                "left outer join corte as c on c.id_dispensario=d.id\n" +
                "where c.id="+jsonObject.getString("corte")+"";
        Statement stmt = conn.createStatement();
        r=stmt.executeQuery(query);
        if (r.next()) {
            res = r.getInt("disp");
        }
        conn.close();
        stmt.close();
        return res;
    }
    public Integer cant_impreso (Context con, String ticket ) throws SQLException,
            IllegalAccessException, InstantiationException, ClassNotFoundException, JSONException {
        Integer res=10;
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();
        String query = "select impreso from despachos where nrotrn="+ticket+"";
        Log.w("query impreso", query);
        Statement stmt = conn.createStatement();
        r = stmt.executeQuery(query);
        if (r.next()) {
            res = r.getInt("impreso");
        }
        conn.close();
        stmt.close();
        Log.w("res impreso",String.valueOf(res));
        return res;
    }
    public JSONObject estacion_domicilio (Context con) throws ClassNotFoundException, SQLException,
            InstantiationException, JSONException, IllegalAccessException {
        ResultSet rs;
        JSONObject domicilio = new JSONObject();
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        String query = "select df.calle as calle,df.num_exterior as exterior,df.num_interior as interior,df.colonia as colonia,df.codigo_postal as cp,\n" +
                "df.localidad as localidad,df.municipio as municipio,df.estado as estado,df.pais as pais,df.rfc as rfc,df.telefono as telefono,\n" +
                "df.regimen_fiscal as regimen,df.cveest as cveest,e.nombre as estacion  \n" +
                "from datos_factura as df left outer join estacion as e on e.id=df.id_estacion";
        Statement stmt = conn.createStatement();
        rs = stmt.executeQuery(query);
        while (rs.next()) {
            domicilio.put("calle",rs.getString("calle"));
            domicilio.put("exterior",rs.getString("exterior"));
            domicilio.put("interior",rs.getString("interior"));
            domicilio.put("colonia",rs.getString("colonia"));
            domicilio.put("cp",rs.getString("cp"));
            domicilio.put("localidad",rs.getString("localidad"));
            domicilio.put("municipio",rs.getString("municipio"));
            domicilio.put("estado",rs.getString("estado"));
            domicilio.put("pais",rs.getString("pais"));
            domicilio.put("rfc",rs.getString("rfc"));
            domicilio.put("telefono",rs.getString("telefono"));
            domicilio.put("regimen",rs.getString("regimen"));
            domicilio.put("cveest",rs.getString("cveest"));
            domicilio.put("estacion",rs.getString("estacion"));
            //domicilio.put("permiso",rs.getString("permiso"));
        }
        conn.close();
        stmt.close();
        rs.close();
        return domicilio;
    }
    //funcion para obtener los datos de los clientes mediante una parte de su nombre
    public List<DataCustomerCG> getCustomerCG(Context context, String nombre) throws
            ClassNotFoundException, SQLException, InstantiationException, JSONException, IllegalAccessException {
        List<DataCustomerCG> dataCustomerCGS = new ArrayList<>();
        ResultSet rs;
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cg(context);
        String query = "select cod as cod,den as den,rfc as rfc from Clientes where den like '%" + nombre + "%'";
        Statement stmt = conn.createStatement();
        rs = stmt.executeQuery(query);
        while (rs.next()){
            DataCustomerCG data = new DataCustomerCG();
            data.codcli = rs.getString("cod");
            data.den = rs.getString("den");
            data.rfc = rs.getString("rfc");
            dataCustomerCGS.add(data);
        }
        return dataCustomerCGS;
    }
    //Funcion para obtener los vehiculos mediante el codigo de cliente
    public List<DataCustomerCG> getCustomerVehicleCG(Context context, String codcli) throws
            ClassNotFoundException, SQLException, InstantiationException, JSONException, IllegalAccessException {
        List<DataCustomerCG> dataCustomerCGS = new ArrayList<>();
        ResultSet rs;
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cg(context);
        String query = "select rsp as rsp,plc as plc,den as den,tar as tar,nroveh as nroveh from ClientesVehiculos where codcli="+codcli+" order by nroveh";
        Statement stmt = conn.createStatement();
        rs = stmt.executeQuery(query);
        while (rs.next()){
            DataCustomerCG data = new DataCustomerCG();
            data.rsp = rs.getString("rsp");
            data.plc = rs.getString("plc");
            data.den_vehicle = rs.getString("den");
            data.tar = rs.getInt("tar");
            data.nroveh = rs.getInt("nroveh");
            dataCustomerCGS.add(data);
            Log.i("vehicle_db", String.valueOf(data.den_vehicle));
        }
        return dataCustomerCGS;
    }
    //funcion para obtener las tpv de la estacion
    public ArrayList<String> getTPVs(Context context, String tipo) throws ClassNotFoundException,
            SQLException, InstantiationException, JSONException, IllegalAccessException {
        List<TPVs> tpVsList;
        ArrayList<String> data = new ArrayList<String>();
        tpVsList=new ArrayList<>();
        ResultSet rs;
        JSONObject tpvs = new JSONObject();
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(context);
        String query = "select id,nombre from tpv where bancaria = '"+tipo+"' and activo =1";
        Statement stmt = conn.createStatement();
        rs = stmt.executeQuery(query);
        while (rs.next()){
            String id = rs.getString("nombre");
            data.add(id);
        }
        conn.close();
        stmt.close();
        rs.close();
        return data;
    }
}
