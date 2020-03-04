package cg.ce.app.chris.com.cgce;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.Log;

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

/**
 * Created by chris on 1/12/16.
 */

public class cgticket {


    ResultSet r;
    Connection connect;
    JSONObject cursor = null;
    LogCE logCE = new LogCE();

    public JSONObject consulta_servicio(Context con,String bomba) throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {

        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();

        String base = null;
        try {
            base = cursor.getString("db_cg");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ResultSet r = null;
        JSONObject st=new JSONObject();
        ValidarDispositivo vd = new ValidarDispositivo();
        MacActivity mac_add = new MacActivity();
        String mac = mac_add.getMacAddress();
        Log.w("Mac", "Mac: " + mac);
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cg(con);
        Statement stmt = conn.createStatement();
        try {
            /*r = stmt.executeQuery("\n" +
                    "SELECT top 1 desp.nrotrn,desp.can,desp.mto,desp.pre,prod.den,desp.nrobom,resp.den,\n" +
                    "Convert(VARCHAR(10), cast(cast(desp.fchtrn-1 as int) as datetime) , 111),desp.codprd,gas.cveest,desp.mtogto,desp.codcli,\n" +
                    "cli.den,desp.hratrn,desp.codgas,desp.codprd,desp.nroveh,desp.odm,desp.fchcor,desp.nrotur,desp.nrocte \n" +
                    "FROM ["+base+"].[dbo].[Despachos] as desp \n" +
                    "left outer join ["+base+"].[dbo].[Productos] as prod on prod.cod=desp.codprd \n" +
                    "left outer join ["+base+"].[dbo].[Responsables] as resp on resp.cod=desp.codres \n" +
                    "left outer join ["+base+"].[dbo].[Gasolineras] as gas on gas.cod=desp.codgas \n" +
                    "left outer join ["+base+"].[dbo].[Clientes] as cli on cli.cod=desp.codcli \n"      +
                    //"where desp.nrobom ="+bomba+" order by desp.nrotrn desc");
                    "where desp.nrotrn='3797870' order by desp.nrotrn desc");

             */
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
                    //"where desp.nrotrn='201249040' order by desp.nrotrn desc");
//            ResultSet r = stmt.executeQuery("SELECT disp.activo FROM  [cecg_app].[dbo].[dispositivos] as disp where disp.mac_adr = '" + String.valueOf(mac) + "';");
            if (!r.next()) {
            } else {
            }
            float a = r.getFloat(11);
            if (a<0){
                a=a*-1;

            }
            String despachador;
            if (r.getString(7) == null){
                despachador="DESPACHADOR";
            }else{
                despachador=r.getString(7);
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
            r.close();
            conn.close();
            stmt.close();
            r.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JSONObject jsonError=null;
            try {
                 jsonError = new JSONObject(e.toString());

            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            logCE.EscirbirLog(con,jsonError);

        } catch (JSONException e) {
            JSONObject jsonError=null;
            try {
                jsonError = new JSONObject(e.toString());

            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            logCE.EscirbirLog(con,jsonError);
            e.printStackTrace();
        }
        return st;
    }
    public JSONObject busca_producto (Context context, String barcode) throws SQLException, JSONException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        JSONObject res = new JSONObject();
        DataBaseManager manager = new DataBaseManager(context);
        cursor = manager.cargarcursorodbc2();

        String base = null;
        try {
            base = cursor.getString("db_cg");
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
                "(select top 1 preiie from ["+base+"].[dbo].[Precios] where codprd=prd.cod and fch<=convert(int,getdate()) order by fch desc) as ieps \n" +
                "from ["+base+"].[dbo].[Productos] as prd\n" +
                "where prd.codbar ='" + barcode + "'");
        if (!r.next()){
            res.put("error","No existe producto!");
            conn.close();
            stmt.close();
            r.close();
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
            conn.close();
            stmt.close();
            r.close();
            return res;
        }

    }
    public String getNipManager(Context context) throws SQLException {
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

    public JSONObject corte_cinepolis(Context context){
        JSONObject resultado = new JSONObject();
        DataBaseManager manager = new DataBaseManager(context);
        cursor = manager.cargarcursorodbc2();
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cecg_app(context);
        Log.w("si entro","1");
        try {
            Statement stmt = conn.createStatement();
            String query = "select sum(boletos) as boletos, sum(total) as total from despachos where corte="+get_corte(context)+" and folios is not null";
            r = stmt.executeQuery(query);
            if(!r.next()){
                try {
                    resultado.put("boletos","0");
                    resultado.put("total","0");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                stmt.close();
                conn.close();

            }else {
                try {
                    resultado.put("boletos",r.getString("boletos"));
                    resultado.put("total",r.getString("total"));
                }catch (JSONException e) {
                    e.printStackTrace();
                }
                stmt.close();
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultado;
    }
    public JSONObject corte_datos(Context context){
        JSONObject resultado = new JSONObject();
        DataBaseManager manager = new DataBaseManager(context);
        cursor = manager.cargarcursorodbc2();
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cecg_app(context);
        try {
            Statement stmt = conn.createStatement();
            String query = "select hora_entrada,hora_salida from corte where id="+get_corte(context)+"";
            r = stmt.executeQuery(query);
            if(!r.next()){
                try {
                    resultado.put("hora_entrada","0");
                    resultado.put("hora_salida","0");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }else {
                try {
                    resultado.put("hora_entrada",r.getString("hora_entrada"));
                    resultado.put("hora_salida",r.getString("hora_salida"));
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultado;
    }
    //funcion para obtener datos del cliente
    public JSONObject get_vehiculo(Context context,String nrotrn,String bomba) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        JSONObject resultado = new JSONObject();
        ResultSet r;
        DataBaseCG cg = new DataBaseCG();
        Connection connection= cg.odbc_cg(context);
        try {
            Statement stmt = connection.createStatement();
            String query = "select top 1 d.nrotrn,cv.plc,cv.rsp,cv.nroeco,d.odm,cv.codcli,cv.nroveh,d.codcli,d.nroveh, cv.tar from Despachos as d\n" +
                    "inner join ClientesVehiculos as cv on d.nroveh=cv.nroveh and d.codcli=cv.codcli\n" +
                    "where d.nrotrn="+nrotrn+" and d.nrobom="+bomba+" order by d.nrotrn desc";
            r = stmt.executeQuery(query);
            if(!r.next()){
                try {
                    resultado.put("placa","Sin Placa");
                    resultado.put("rsp","Sin Chofer");
                    resultado.put("nroeco","Sin No° Economico");
                    resultado.put("ultodm","Sin Odometro");
                    resultado.put("tar",0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                try {
                    resultado.put("placa", r.getString("plc"));
                    resultado.put("rsp", r.getString("rsp"));
                    resultado.put("nroeco", r.getString("nroeco"));
                    resultado.put("ultodm", r.getString("odm"));
                    resultado.put("tar",r.getString("tar"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            connection.close();
            r.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultado;
    }
    public JSONObject consulta_credito(Context con,Integer cliente) throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        JSONObject st=new JSONObject();
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();

        String base = null;
        try {
            base = cursor.getString("db_cg");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cg(con);
        Statement stmt = conn.createStatement();
        try {
            String query = "SELECT tipval as tipval FROM ["+base+"].[dbo].[Clientes] where cod = '"+cliente+"'";
            Log.w("qwery",query);
            r = stmt.executeQuery(query);
            while (r.next()) {
                st.put("tip_cliente", r.getInt("tipval"));
            }
            r.close();
            conn.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

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
        String hora_impresa="";
        hora_impresa=hora.substring(0,2)+":"+hora.substring(2,4);
        return hora_impresa;
    }
    public boolean guardarnrotrn2 (Context con, String ticket, int venta) {
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();

        String base = null;
        try {
            base = cursor.getString("db");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ValidarDispositivo vd = new ValidarDispositivo();
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cecg_app(con);
        String ticket2= ticket+"0";
        String query = "insert into ["+base+"].[dbo].[despachos] (nrotrn,nota,corte,impreso,tipo_venta,flotillero) values("+String.valueOf(ticket)+","+ticket2+","+get_corte(con)+",0,"+venta+",0)";
        try {
            Statement stmt = conn.createStatement();
            Log.w("query", query);
            stmt.execute(query);
            stmt.close();
            conn.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean guardarnrotrn3 (Context con, String folio, String correo, String boletos, String total, int venta) {
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();

        String base = null;
        try {
            base = cursor.getString("db");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ValidarDispositivo vd = new ValidarDispositivo();
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cecg_app(con);
        String query = "insert into ["+base+"].[dbo].[despachos] (nrotrn,nota,corte,impreso,tipo_venta,flotillero,folios,cinepolis_correo,boletos,total) values(0,0,"+get_corte(con)+",0,"+venta+",0, '"+String.valueOf(folio)+"','"+String.valueOf(correo)+"',"+ boletos+","+total+")";
        try {
            Statement stmt = conn.createStatement();
            Log.w("query", query);
            stmt.execute(query);
            stmt.close();
            conn.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public Integer get_corte(Context con){
        Integer res=0;
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();

        String base = null;
        try {
            base = cursor.getString("db");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String query = "select c.id as cor from ["+base+"].[dbo].[corte ] as c\n" +
                "left outer join dispositivos as d on d.id=c.id_dispositivo\n" +
                "where d.mac_adr='"+getMacAddress()+"' and c.status=0";
        try {
            Statement stmt = conn.createStatement();
            r=stmt.executeQuery(query);
            while (r.next()) {
                res = r.getInt("cor");
            }
            conn.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Log.w("qwery",query);
        Log.w("res", String.valueOf(res));
        return res;
    }
    public boolean actualizar_cant_impreso (Context con, String ticket ){
        String query = "update despachos set impreso=1 where nrotrn="+ticket+"";
        try {
            DataBaseCG dbcg = new DataBaseCG();
            Connection conn = dbcg.odbc_cecg_app(con);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(query);
            stmt.close();
            conn.close();
            return true;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }

    }
    public boolean guardarnrotrn (Context con,JSONObject ticket,int venta) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, JSONException {
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();
        String query;
        String base = null;
        try {
            base = cursor.getString("db_cg");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ResultSet r = null;
        JSONObject st=new JSONObject();
        ValidarDispositivo vd = new ValidarDispositivo();
        MacActivity mac_add = new MacActivity();
        String mac = mac_add.getMacAddress();
        Log.w("Mac", "Mac: " + mac);
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cg(con);
        Statement stmt = conn.createStatement();
        try {
            String ticket2=String.valueOf(ticket.getString("nrotrn"))+"0";
            Log.w("combu",String.valueOf(ticket)+"//"+String.valueOf(ticket2));
            if (ticket.has("rut")){
                Log.w("query tiptrn","update ["+base+"].[dbo].[Despachos] set nrocte="+ticket2+", rut="+ ticket.getString("rut") +", tiptrn= "+ ticket.getInt("tiptrn") +" where nrotrn = "+ticket.getString("nrotrn")+"");
                stmt.executeUpdate("update ["+base+"].[dbo].[Despachos] set nrocte="+ticket2+",codres='"+getCodDespCG(con)+"' , rut='"+ ticket.getString("rut") +"', tiptrn= "+ ticket.getInt("tiptrn") +" where nrotrn = "+ticket.getString("nrotrn")+"");
            }else{
                System.out.println("identificacion es credito");
                stmt.executeUpdate("update ["+base+"].[dbo].[Despachos] set nrocte="+ticket2+" where nrotrn = "+ticket.getString("nrotrn")+"");
            }

            stmt.close();
            conn.close();
            Log.w("combu","true");
            guardarnrotrn2(con,ticket.getString("nrotrn"),venta);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            Log.w("combu","false");
            stmt.close();
            conn.close();
            return false;
        }
    }public boolean guardarnrotrn_old (Context con,String ticket,int venta) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();

        String base = null;
        try {
            base = cursor.getString("db_cg");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cg(con);
        try {
            Statement stmt = conn.createStatement();
            String ticket2=String.valueOf(ticket)+"0";
            Log.w("combu",String.valueOf(ticket)+"//"+String.valueOf(ticket2));
            stmt.executeUpdate("update ["+base+"].[dbo].[Despachos] set nrocte="+ticket2+" where nrotrn = "+ticket+"");
            stmt.close();
            conn.close();
            Log.w("combu","true");
            guardarnrotrn2(con,ticket,venta);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            Log.w("combu","false");
            return false;
        }
    }
    public boolean setTipTrn (Context con,String tiptrn, String nrotrn) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();

        String base = null;
        try {
            base = cursor.getString("db_cg");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cg(con);
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("update ["+base+"].[dbo].[Despachos] set tiptrn="+tiptrn+" where nrotrn = "+nrotrn+"");
            stmt.close();
            conn.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            Log.w("combu","false");
            return false;
        }
    }
    public boolean put_tpv (Context con,String ticket,int tpv) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();
        String base = null;
        try {
            base = cursor.getString("db");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cg(con);
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("update ["+base+"].[dbo].[despachos] set tpv_id="+tpv+" where nrotrn = "+ticket+"");
            stmt.close();
            conn.close();
            Log.w("combu","true");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            Log.w("combu","false");
            return false;
        }
    }
    public boolean update_flotillero (Context con,String ticket) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();

        String base = null;
        try {
            base = cursor.getString("db");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cg(con);
        try {
            Statement stmt = conn.createStatement();
            String ticket2=String.valueOf(ticket)+"0";
            Log.w("combu",String.valueOf(ticket)+"//"+String.valueOf(ticket2));
            stmt.executeUpdate("update ["+base+"].[dbo].[despachos] set flotillero=1 where nrotrn = "+ticket+"");
            stmt.close();
            conn.close();
            Log.w("combu","true");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            Log.w("combu","false");
            return false;
        }
    }
    public boolean update_codcli (Context con,String ticket,String cliente,String vehiculo,String odm,String tar) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        if(odm.equals(null)){
            odm="0";
        }
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();

        String base = null;
        try {
            base = cursor.getString("db_cg");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cg(con);
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("update ["+base+"].[dbo].[Despachos] set codcli ="+cliente+", nroveh="+vehiculo+", odm="+odm+", tar="+tar+" where nrotrn = "+ticket+"");
            stmt.close();
            conn.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            Log.w("combu","false");
            return false;
        }
    }

    public String nip_desp (Context con){
        String res="";
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        String query = "select d.pass as pass from despachadores d \n" +
                "left outer join corte c on c.id_despachador=d.id \n" +
                "left outer join dispositivos dis on dis.id=c.id_dispositivo\n " +
                "where dis.mac_adr='"+getMacAddress()+"' and c.status=0";
        try {
            Statement stmt = conn.createStatement();
            r=stmt.executeQuery(query);
            if (r.next()) {
                res = r.getString("pass");
            }
            conn.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    public String get_rut (Context con, JSONObject jsonObject) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, JSONException {
        String res="";
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cg(con);
        String query = "select rut as rut from Despachos where nrotrn ='"+jsonObject.getString("nrotrn")+"'";
        try {
            Statement stmt = conn.createStatement();
            r=stmt.executeQuery(query);
            if (r.next()) {
                res = r.getString("rut");
            }
            conn.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        //Log.w("rut", res);
        return res;
    }
    public String getCodDespCG (Context con) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, JSONException {
        String res="";
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cg(con);
        String query = "select cod as cod from Responsables where tag = '"+nip_desp(con)+"'";
        try {
            Statement stmt = conn.createStatement();
            r=stmt.executeQuery(query);
            if (r.next()) {
                res = r.getString("cod");
            }
            conn.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        Log.w("rut", res);
        return res;
    }
    ///funcion para obtener el rfc del cliente en la base de cg mediante identificcion de nip
    public String get_rfc_nip (Context con,String tag, String metodo) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
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
        try {
            Statement stmt = conn.createStatement();
            r=stmt.executeQuery(query);
            if (r.next()) {
                res = r.getString("rfc");
            }
            conn.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }public String get_cliente_den (Context con,String tag, String metodo) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        String res="";
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cg(con);
        String query="";
        if (metodo=="nfc") {
            query = "select c.den from Clientes as c\n" +
                    "left outer join ClientesVehiculos as cv on cv.codcli=c.cod\n" +
                    "where cv.tag ='" + tag + "'";
        }else{
            query = "select c.den from Clientes as c\n" +
                    "left outer join ClientesVehiculos as cv on cv.codcli=c.cod\n" +
                    "where cv.tag ='" + tag + "'";
        }
        try {
            Statement stmt = conn.createStatement();
            r=stmt.executeQuery(query);
            if (r.next()) {
                res = r.getString("den");
            }
            conn.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }
    public String nombre_depsachador (Context con){
        String res="DESPACHADOR";
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();
        String query = "select d.nombre as pass from despachadores d \n" +
                "left outer join corte c on c.id_despachador=d.id \n" +
                "left outer join dispositivos dis on dis.id=c.id_dispositivo\n " +
                "where dis.mac_adr='"+getMacAddress()+"' and c.status=0";
        try {
            Statement stmt = conn.createStatement();
            r=stmt.executeQuery(query);
            if (r.next()) {
                res = r.getString("pass");
            }
            conn.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }
    public int id_depsachador (Context con){
        int res=0;
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        String query = "select d.id as pass from despachadores d \n" +
                "left outer join corte c on c.id_despachador=d.id \n" +
                "left outer join dispositivos dis on dis.id=c.id_dispositivo\n " +
                "where dis.mac_adr='"+getMacAddress()+"' and c.status=0";
        try {
            Statement stmt = conn.createStatement();
            r=stmt.executeQuery(query);
            if (r.next()) {
                res = r.getInt("pass");
            }
            conn.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
    public Integer get_bomba_libre(Context con, String bomba){
        Integer res=0;
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        String query = "select p.numero_logico as logico from posicion as p \n" +
                "left outer join dispensario as disp on disp.id=p.id_dispensario\n" +
                "left outer join corte as c on c.id_dispensario=disp.id\n" +
                "left outer join dispositivos as dispo on dispo.id=c.id_dispositivo\n" +
                "where c.status =0 and p.numero_logico not in ("+bomba+") and dispo.mac_adr='"+getMacAddress()+"'";
        try {
            Statement stmt = conn.createStatement();
            r=stmt.executeQuery(query);
            while (r.next()) {
                res = r.getInt("logico");
            }
            conn.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }
    public Integer get_configid(Context con){
        Integer res=0;
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();
        String base = null;
        try {
            base = cursor.getString("db");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String query = "select config_id from ["+base+"].[dbo].[datos_factura] ";
        try {
            Statement stmt = conn.createStatement();
            r=stmt.executeQuery(query);
            while (r.next()) {
                res = 34;//r.getInt("config_id");
            }
            conn.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }
    public String get_cveest(Context con){
        String res=null;
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();

        String base = null;
        try {
            base = cursor.getString("db");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String query = "select cveest from ["+base+"].[dbo].[datos_factura ] ";
        try {
            Statement stmt = conn.createStatement();
            r=stmt.executeQuery(query);
            if (r.next()) {
                res = r.getString("cveest");
            }
            conn.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }
    public String get_estacion(Context con){
        String res=null;
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();

        String base = null;
        try {
            base = cursor.getString("db");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String query = "select top 1 nombre from ["+base+"].[dbo].[estacion] where id in (select distinct(id_estacion) from ["+base+"].[dbo].[datos_factura])";
        try {
            Statement stmt = conn.createStatement();
            r=stmt.executeQuery(query);
            if (r.next()) {
                res = r.getString("nombre");
            }
            conn.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }
    public int guardaraceite (Context con, JSONObject jsonObject) {
        int res=0;
        String nota=null;
        JSONObject aceite = jsonObject;
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();

        String base = null;
        try {
            base = cursor.getString("db");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ValidarDispositivo vd = new ValidarDispositivo();
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cecg_app(con);
        String query = null;
        try {
            query = "insert into ["+base+"].[dbo].[aceites] (codigo,producto,precio,cantidad,corte,rfc,tipo_venta,web,cancelado,impreso,hora_venta) OUTPUT Inserted.nrotrn values('"+aceite.getString("codigo")+"','"+aceite.getString("producto")+"','"+aceite.getString("precio")+"','"+aceite.getString("cantidad")+"','"+aceite.getString("corte")+"','"+aceite.getString("rfc")+"','"+aceite.getString("tipo_venta")+"',default,default,default,getdate()) ";
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
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
        } catch (SQLException e) {
            e.printStackTrace();

        }
        return res;
    }

    public boolean updateaceiteweb (Context con, JSONObject jsonObject) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();

        String base = null;
        try {
            base = cursor.getString("db");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cg(con);
        try {
            Statement stmt = conn.createStatement();
            Log.w("query_update","update ["+base+"].[dbo].[aceites] set web=1 where nrotrn = "+jsonObject.getString("nrotrn")+"");
            stmt.executeUpdate("update ["+base+"].[dbo].[aceites] set web=1 where nrotrn = "+jsonObject.getString("nrotrn")+"");
            stmt.close();
            conn.close();
            Log.w("combu","true");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            Log.w("combu","false");
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean updateaceiteimpresion (Context con, JSONObject jsonObject) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();
        String base = null;
        try {
            base = cursor.getString("db");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DataBaseCG dbcg = new DataBaseCG();
        Connection conn = dbcg.odbc_cg(con);
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("update ["+base+"].[dbo].[aceites] set impreso=1 where nrotrn = "+jsonObject.getString("nrotrn")+"");
            stmt.close();
            conn.close();
            Log.w("combu","true");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            Log.w("combu","false");
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }
    public  int cantimpresoaceite(Context con, JSONObject jsonObject){
        int res=0;
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        String query = null;
        try {
            query = "select impreso from [cecg_app].[dbo].[aceites]  \n" +
                    "where nrotrn='"+jsonObject.getString("nota")+"'";
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            Statement stmt = conn.createStatement();
            r=stmt.executeQuery(query);
            if (r.next()) {
                res = r.getInt("impreso");
            }
            conn.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }
    public  int bandera(Context con){
        int res=0;
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        String query = null;
        query = "select id from [cecg_app].[dbo].[bandera]  \n" +
                    "where uso=1";
        try {
            Statement stmt = conn.createStatement();
            r=stmt.executeQuery(query);
            if (r.next()) {
                res = r.getInt("id");
            }
            conn.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }
    public  Integer urltimbre(Context con){
        Integer res=null;
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        String query = null;
        query = "select id from [cecg_app].[dbo].[bandera]  \n" +
                "where uso=1";
        try {
            Statement stmt = conn.createStatement();
            r=stmt.executeQuery(query);
            if (r.next()) {
                res = r.getInt("id");
            }
            conn.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }
    public  String nombrebandera(Context con){
        String res=null;
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        String query = null;
        query = "select nombre from [cecg_app].[dbo].[bandera]  \n" +
                "where uso=1";
        try {
            Statement stmt = conn.createStatement();
            r=stmt.executeQuery(query);
            if (r.next()) {
                res = r.getString("nombre");
            }
            conn.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }
    public  ArrayList<String> banderas(Context con){
        String res=null;
        ArrayList<String> data = new ArrayList<String>();
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        String query = null;
        query = "select nombre from [cecg_app].[dbo].[bandera]";
        try {
            Statement stmt = conn.createStatement();
            r=stmt.executeQuery(query);
            while (r.next()) {
                String id = r.getString("nombre");
                data.add(id);
            }
            conn.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }


    public boolean limpiarbandera (Context con){
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        String query = null;
        query = "update [cecg_app].[dbo].[bandera]  \n" +
                "set uso = 0";
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(query);
            stmt.close();
            conn.close();
            Log.w("combu","true");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            Log.w("combu","false");
            return false;
        }
    }
    public boolean insertarbandera (Context con, String bandera){
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        String query = null;
        query = "update [cecg_app].[dbo].[bandera]  \n" +
                "set uso = 1 where nombre='"+bandera+"'";
        Log.w("query",query);
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(query);
            stmt.close();
            conn.close();
            Log.w("combu","true");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            Log.w("combu","false");
            return false;
        }
    }
    public boolean insertarurl(Context con, String bandera,String url){
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        String query = null;
        query = "update [cecg_app].[dbo].[bandera]  \n" +
                "set urltimbre = '"+url+"' where nombre='"+bandera+"'";
        Log.w("query",query);
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(query);
            stmt.close();
            conn.close();
            Log.w("combu","true");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            Log.w("combu","false");
            return false;
        }
    }
    public  String fechaaceite (Context con, JSONObject jsonObject){
        String res=null;
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);

        String query = null;
        try {
            query = "select hora_venta from [cecg_app].[dbo].[aceites]  \n" +
                    "where nrotrn='"+jsonObject.getString("nrotrn")+"'";
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            Statement stmt = conn.createStatement();
            r=stmt.executeQuery(query);
            if (r.next()) {
                res = String.valueOf(r.getDate("hora_venta"));
            }
            conn.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Log.w("fecha_res",res);
        return res;
    }
    public int get_isla (Context con, JSONObject jsonObject){
        int res=0;
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();
        //String base = c.getString(3);

        String query = null;
        try {
            query = "select d.numero_logico as disp from dispensario as d \n" +
                    "left outer join corte as c on c.id_dispensario=d.id\n" +
                    "where c.id="+jsonObject.getString("corte")+"";
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            Statement stmt = conn.createStatement();
            r=stmt.executeQuery(query);
            if (r.next()) {
                res = r.getInt("disp");
            }
            conn.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return res;
    }
    public Integer cant_impreso (Context con, String ticket ){
        Integer res=10;
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        DataBaseManager manager = new DataBaseManager(con);
        cursor = manager.cargarcursorodbc2();
        String query = "select impreso from despachos where nrotrn="+ticket+"";
        Log.w("query impreso", query);
        try {
            Statement stmt = conn.createStatement();
            r=stmt.executeQuery(query);
            if (r.next()) {
                res = r.getInt("impreso");
            }
            conn.close();

            stmt.close();

        }catch(SQLException e){
            e.printStackTrace();
        }
        Log.w("res impreso",String.valueOf(res));
        return res;
    }
    public JSONObject estacion_domicilio (Context con){
        ResultSet rs;
        JSONObject domicilio = new JSONObject();
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(con);
        String query = "select df.calle as calle,df.num_exterior as exterior,df.num_interior as interior,df.colonia as colonia,df.codigo_postal as cp,\n" +
                "df.localidad as localidad,df.municipio as municipio,df.estado as estado,df.pais as pais,df.rfc as rfc,df.telefono as telefono,\n" +
                "df.regimen_fiscal as regimen,df.cveest as cveest,e.nombre as estacion  \n" +
                "from datos_factura as df left outer join estacion as e on e.id=df.id_estacion";
        try {
            Statement stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                try {
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            conn.close();
        }catch(SQLException e){
            e.printStackTrace();
        }
        return domicilio;
    }
    //funcion para obtener las tpv de la estacion
    public List<TPVs> getTPVs(Context context){
        List<TPVs> tpVsList;
        tpVsList=new ArrayList<>();
        ResultSet rs;
        JSONObject tpvs = new JSONObject();
        DataBaseCG cg = new DataBaseCG();
        Connection conn = cg.odbc_cecg_app(context);
        String query = "select id,nombre,se_factura,activo,copia,bancaria,imagen from tpv where activo =1";
        try {
            Statement stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next()){
                tpVsList.add(new TPVs(rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getInt("se_factura"),
                        rs.getInt("activo"),
                        rs.getInt("copia"),
                        rs.getInt("bancaria"),
                        context.getResources().getIdentifier(rs.getString("imagen"),"drawable", context.getPackageName())));
            }
            conn.close();
            stmt.close();
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tpVsList;
    }
}
