package cg.ce.app.chris.com.cgce;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import cg.ce.app.chris.com.cgce.common.Variables;

/**
 * Created by chris on 28/04/17.
 */

public class ValidacionFlotillero {
    DataBaseCG cg = new DataBaseCG();
    Variables variables = new Variables();

    int []dias={1,2,4,8,16,32,64};
    //la funcion validar_via ayuda a la funcion carga_dia para realizar las validaciones de dia en el flotillero
    /*public int carga_dia(Context context,String tag, String metodo) throws ClassNotFoundException,
            SQLException, InstantiationException, IllegalAccessException {
        int resultado=0;
        Date date = new Date();
        int dia=date.getDay();
        JSONObject  dias_permitidos=validar_dia(context,tag,metodo);
        int m=0;
        for (int i=0;i<dias_permitidos.length();i++) {
            Log.w("dias_permitido",String.valueOf(dias_permitidos.substring(m, i + 1)));
            Log.w("dia",String.valueOf(dia));
            if(dia==0){
                dia=7;
            }
            if (dia == Integer.parseInt(dias_permitidos.substring(m, i + 1))){
                resultado=1;
            }
        }
        return resultado;
    }*/
    public JSONObject validar_dia(Context context, String tag, String metodo)
            throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, JSONException {
        int diacar=0;
        ResultSet r;
        JSONObject res = new JSONObject();
        ArrayList<Integer>dias_carga = new ArrayList<>() ;
        Connection connection= cg.odbc_cg(context);
        Statement stmt = connection.createStatement();
        String query="";
        if (metodo==variables.KEY_RFID) {
            query = "select cv.diacar as diacar,cv.codgas as codgas, getdate() as fecha,\n" +
                    "datepart(WEEKDAY,getdate()) as dia, cv.est as est,\n" +
                    "(select cod from Gasolineras where codest=0 and cod !=0) as cveest,\n" +
                    "cv.hraini as hraini,cv.hrafin as hrafin,cv.hraini2 as hraini2,\n" +
                    "cv.hrafin2 as hrafin2,cv.hraini3 as hraini3,cv.hrafin3 as hrafin3," +
                    "cv.codprd as codprd,\n" +
                    "(select den from Productos where cod=codprd) as combustible\n" +
                    "from ClientesVehiculos as cv where cv.tag= '"+ tag +"'";
        }else if (metodo==variables.KEY_NIP){
            query = "select cv.diacar as diacar,cv.codgas as codgas, getdate() as fecha,\n" +
                    "datepart(WEEKDAY,getdate()) as dia, cv.est as est,\n" +
                    "(select cod from Gasolineras where codest=0 and cod !=0) as cveest,\n" +
                    "cv.hraini as hraini,cv.hrafin as hrafin,cv.hraini2 as hraini2,\n" +
                    "cv.hrafin2 as hrafin2,cv.hraini3 as hraini3,cv.hrafin3 as hrafin3,\n" +
                    "cv.codprd as codprd,\n" +
                    "(select den from Productos where cod=codprd) as combustible\n" +
                    "from ClientesVehiculos as cv where cv.tag= '"+ tag +"'";
        }else if(metodo==variables.KEY_NOMBRE){
            query = "select cv.diacar as diacar,cv.codgas as codgas, getdate() as fecha,\n" +
                    "datepart(WEEKDAY,getdate()) as dia, cv.est as est,\n" +
                    "(select cod from Gasolineras where codest=0 and cod !=0) as cveest,\n" +
                    "cv.hraini as hraini,cv.hrafin as hrafin,cv.hraini2 as hraini2,\n" +
                    "cv.hrafin2 as hrafin2,cv.hraini3 as hraini3,cv.hrafin3 as hrafin3,\n" +
                    "cv.codprd as codprd,\n" +
                    "(select den from Productos where cod=codprd) as combustible\n" +
                    "from ClientesVehiculos as cv where cv.tar= '"+ tag +"'";
        }
        r = stmt.executeQuery(query);
        while (r.next()) {
            diacar= r.getInt("diacar");
            res.put("Dia",r.getString("dia"));
            res.put("Fecha",r.getDate("fecha"));
            res.put("EstPermitida", r.getString("codgas"));
            res.put("EstLocal",r.getString("cveest"));
            res.put("Est",r.getString("est"));
            res.put("hraini", r.getInt("hraini"));
            res.put("hrafin", r.getInt("hrafin"));
            res.put("hraini2", r.getInt("hraini2"));
            res.put("hrafin2", r.getInt("hrafin2"));
            res.put("hraini3", r.getInt("hraini3"));
            res.put("hrafin3", r.getInt("hrafin3"));
            res.put("HoraActual",Integer.valueOf(hora_actual()));
            res.put("CodPrd",r.getInt("codprd"));
            res.put("Combustible",r.getString("combustible"));
        }
        Log.w("diacar",String.valueOf(diacar));
        int validador=0;
        for (int i=dias.length;i!=0;i--){
            validador+=dias[i-1];
            if (diacar>=validador){
                dias_carga.add(i);
            }else{
                validador-=dias[i-1];
            }
        }
        connection.close();
        r.close();
        stmt.close();
        connection.close();
        Collections.sort(dias_carga);
        res.put("PermisoEstacion",res.getString("EstPermitida").equals(res.getString("EstLocal")));
        res.put("Array",dias_carga);
        return res;
    }

    //la funcion cveest_app ayuda a la funcion validar_estacion para realizar las validaciones de estacion en el flotillero
    public boolean validar_estacion(Context context, String tag, String metodo) throws ClassNotFoundException,
            SQLException, InstantiationException, IllegalAccessException {
        ResultSet r;
        boolean resultado=false;
        Connection connection= cg.odbc_cg(context);
        Statement stmt = connection.createStatement();
        String query="";
        Log.w("estacion metodo" , metodo);
        if (metodo ==variables.KEY_RFID) {
            query = "select cv.codgas as codgas,g.cod as cveest from ClientesVehiculos as cv " +
                    "left outer join Gasolineras as g on g.cod=cv.codgas where cv.tag= '" + tag + "'";
        }else if (metodo == variables.KEY_NIP){
            query = "select cv.codgas as codgas,g.cod as cveest from ClientesVehiculos as cv " +
                    "left outer join Gasolineras as g on g.cod=cv.codgas where cv.tag= '" + tag + "'";
        }else if ( metodo == variables.KEY_NOMBRE){
            query = "select cv.codgas as codgas,g.cod as cveest from ClientesVehiculos as cv " +
                    "left outer join Gasolineras as g on g.cod=cv.codgas where cv.tar= '" + tag + "'";
        }
        Log.w("query estacion", query);
        r = stmt.executeQuery(query);
        while (r.next()) {
            int codgas= r.getInt("codgas");
            int cveest = r.getInt("cveest");
            if (codgas==0){
                resultado=true;
            }else if (cveest==codgas) {
                    resultado=true;
            }
        }
        connection.close();
        r.close();
        stmt.close();
        connection.close();
        return resultado;
    }
    public String sorteo_inicio (Context context) throws SQLException, ClassNotFoundException, InstantiationException, JSONException, IllegalAccessException {
        Date inicio =null;
        ResultSet r;
        Connection connection= cg.odbc_cecg_app(context);
        Statement stmt = connection.createStatement();
        String query="select nombre as sorteo,fecha_inicio as inicio, fecha_fin as fin, getdate() as hoy from sorteo where activo=1";
        r = stmt.executeQuery(query);
        Log.w("qwery", String.valueOf(r));
        while (r.next()) {
            inicio = r.getDate("inicio");
        }

        r.close();
        stmt.close();
        connection.close();
        return String.valueOf(inicio);
    }
    public String sorteo_fin (Context context) throws SQLException, ClassNotFoundException, InstantiationException, JSONException, IllegalAccessException {
        Date fin =null;
        ResultSet r;
        Connection connection= cg.odbc_cecg_app(context);
        Statement stmt = connection.createStatement();
        String query="select nombre as sorteo,fecha_inicio as inicio, fecha_fin as fin, getdate() as hoy from sorteo where activo=1";
        r = stmt.executeQuery(query);
        Log.w("qwery", String.valueOf(r));
        while (r.next()) {
            fin = r.getDate("fin");
        }
        r.close();
        stmt.close();
        connection.close();
        return String.valueOf(fin);
    }
    public String sorteo_nombre (Context context) throws SQLException, ClassNotFoundException, InstantiationException, JSONException, IllegalAccessException {
        String nombre =null;
        ResultSet r;
        Connection connection= cg.odbc_cecg_app(context);
        Statement stmt = connection.createStatement();
        String query="select nombre as sorteo,fecha_inicio as inicio, fecha_fin as fin, getdate() as hoy from sorteo where activo=1";
        r = stmt.executeQuery(query);
        Log.w("qwery", String.valueOf(r));
        while (r.next()) {
            nombre = r.getString("sorteo");
        }
        r.close();
        stmt.close();
        connection.close();
        return nombre;
    }

    public  int validar_sorteo(Context context) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, JSONException {
        int resultado = 0;
        Date hoy = null,inicio = null,fin = null;
        ResultSet r;
        Connection connection= cg.odbc_cecg_app(context);
        Statement stmt = connection.createStatement();
        String query="select nombre as sorteo,fecha_inicio as inicio, fecha_fin as fin, getdate() as hoy from sorteo where activo=1";
        r = stmt.executeQuery(query);
        Log.w("qwery", String.valueOf(r));
        while (r.next()) {
            inicio = r.getDate("inicio");
            fin = r.getDate("fin");
            hoy = r.getDate("hoy"); }
        if (inicio != null && fin != null){
            if(hoy.compareTo(inicio) >= 0 && hoy.compareTo(fin) <= 0){
                resultado=1;
            }
            Log.w("date",String.valueOf(hoy.compareTo(inicio) >= 0 && hoy.compareTo(fin) <= 0));
        }

        r.close();
        stmt.close();
        connection.close();
        return resultado;
    }
    public  int validar_cargas_turno(Context context,String metodo, String tag) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        ResultSet r;
        int resutltado =0;
        String query="";
        Connection connection= cg.odbc_cg(context);
        Statement stmt = connection.createStatement();
        if (metodo == "nombre"){
            query = "SELECT limtur as limtur,tar as tar,acutur as acutur FROM ClientesVehiculos where tar ='" + tag + "'";
        }else {
            query = "SELECT limtur as limtur,tar as tar,acutur as acutur FROM ClientesVehiculos where tag ='" + tag + "'";
        }
        r = stmt.executeQuery(query);
        while (r.next()) {
            int limtur = r.getInt("limtur");
            int acutur = r.getInt("acutur");
            Log.w("Limtur",String.valueOf(limtur));
            Log.w("acutur",String.valueOf(acutur));
            if (limtur == 0){
                resutltado=1;
            }else {
                if (limtur>=acutur){
                    resutltado=0;
                }else {
                    resutltado=0;
                }
            }
        }
        r.close();
        stmt.close();
        connection.close();
        Log.w("resutltado",String.valueOf(resutltado));
        return resutltado;
    }
    

    //Funcion para convertir fecha en un numero (Similar a Control-gas)
    public int date(Context context) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        Object O=null;
        ResultSet r;
        Connection connection = cg.odbc_cg(context);
        Statement stmt = connection.createStatement();
        String query = "select GETDATE() as date";
        r = stmt.executeQuery(query);
        while (r.next()){
            O = r.getObject("date");
        }
        stmt.close();
        connection.close();
        r.close();
        if (O instanceof Date) {
            Date d1 = (Date) O;
            Calendar cal = Calendar.getInstance();
            cal.setTime(d1);
            int dd, mm, yy;
            dd = cal.get(Calendar.DAY_OF_MONTH);
            mm = cal.get(Calendar.MONTH);
            yy = cal.get(Calendar.YEAR);

            if (dd == 29 && mm == 02 && yy == 1900)
                return 60;

            long nSerialDate = ((1461 * (yy + 4800 + ((mm - 14) / 12))) / 4)
                    + ((367 * (mm - 2 - 12 * ((mm - 14) / 12))) / 12)
                    - ((3 * (((yy + 4900 + ((mm - 14) / 12)) / 100))) / 4) + dd
                    - 2415019 - 32075;

            if (nSerialDate < 60) {
                // Because of the 29-02-1900 bug, any serial date
                // under 60 is one off... Compensate.
                nSerialDate--;
            }

            return (int) nSerialDate;
        }
        return -1;
    }
    public int validar_estado(Context context, String tag, String metodo)
            throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        ResultSet r;
        int resultado=0;
        Connection connection= cg.odbc_cg(context);

        Statement stmt = connection.createStatement();
        String query="";
        Log.w("estacion metodo" , metodo);
        if (metodo =="nfc") {
            query = "select est from ClientesVehiculos where tag = '" + tag + "'";
        }else if (metodo =="nip"){
            query = "select est from ClientesVehiculos where tag = '" + tag + "'";
        }else if (metodo == "nombre"){
            query = "select est from ClientesVehiculos where tar = '" + tag + "'";
        }
        Log.w("query estacion", query);
        r = stmt.executeQuery(query);
        while (r.next()) {
            int est= r.getInt("est");

            if (est==1){
                resultado=1;
            }else {
                resultado=0;
            }
        }
        connection.close();
        r.close();
        stmt.close();
        connection.close();
        return resultado;
    }
    public String cveest_app(Context context) throws ClassNotFoundException, SQLException,
            InstantiationException, JSONException, IllegalAccessException {
        ResultSet r;
        String resultado="";
        Connection connection=cg.odbc_cecg_app(context);
        Statement stmt = connection.createStatement();
        String query = "select cveest from datos_factura ";
        r = stmt.executeQuery(query);
        while (r.next()) {
            resultado= r.getString("cveest");
        }
        connection.close();
        r.close();
        stmt.close();
        connection.close();
        return resultado;
    }

    //la funcion hora_actual ayuda a la funcion validar_hora para realizar las validaciones de hora en el flotillero
    public boolean validar_hora(Context context,String tag, String metodo)
            throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        cgticket CGticket = new cgticket();
        ResultSet r;
        boolean resultado=false;
        Connection connection= cg.odbc_cg(context);
        Statement stmt = connection.createStatement();
        String query="";
        if (metodo==variables.KEY_RFID) {
            query = "select hraini,hrafin,hraini2,hrafin2,hraini3,hrafin3 from ClientesVehiculos where tag= '" + tag + "'";
        }else if(metodo==variables.KEY_NIP){
            query = "select hraini,hrafin,hraini2,hrafin2,hraini3,hrafin3 from ClientesVehiculos where tag= '" + tag + "'";
        }else if(metodo ==variables.KEY_NOMBRE){
            query = "select hraini,hrafin,hraini2,hrafin2,hraini3,hrafin3 from ClientesVehiculos where tar= '" + tag + "'";
        }
        r = stmt.executeQuery(query);
        while (r.next()) {
            String hraini= CGticket.hora(r.getString("hraini"));
            String hrafin= CGticket.hora(r.getString("hrafin"));
            String hraini2= CGticket.hora(r.getString("hraini2"));
            String hrafin2= CGticket.hora(r.getString("hrafin2"));
            String hraini3= CGticket.hora(r.getString("hraini3"));
            String hrafin3= CGticket.hora(r.getString("hrafin3"));
            Log.w("hra",hraini);
            Log.w("hra",hraini2);
            Log.w("hra",hraini3);
            Log.w("hra",hrafin);
            Log.w("hra",hraini2);
            Log.w("hra",hraini3);
            if (hraini.equals("00:-1") && hraini2.equals("00:-1") && hraini3.equals("00:-1")
                    && hrafin.equals("00:-1") && hrafin2.equals("00:-1") && hrafin3.equals("00:-1")){
                resultado = true;
            }else {
                String hora_actual=hora_actual();
                if (Integer.valueOf(hora_actual)>=Integer.valueOf(hraini.replace(":","").replace("-","")) &&
                        Integer.valueOf(hora_actual)<=Integer.valueOf(hrafin.replace(":","").replace("-","")) ||
                        Integer.valueOf(hora_actual)>=Integer.valueOf(hraini2.replace(":","").replace("-","")) &&
                                Integer.valueOf(hora_actual)<=Integer.valueOf(hrafin2.replace(":","").replace("-","")) ||
                        Integer.valueOf(hora_actual)>=Integer.valueOf(hraini3.replace(":","").replace("-","")) &&
                                Integer.valueOf(hora_actual)<=Integer.valueOf(hrafin3.replace(":","").replace("-",""))) {
                    resultado = true;
                }else{
                    resultado = false;
                }
            }
        }
        connection.close();
        r.close();
        stmt.close();
        connection.close();
        return resultado;
    }
    public String hora_actual(){
        String minutos,horas;
        Date hora= new Date();
        if(String.valueOf(String.valueOf(hora.getMinutes()).length()).equals("1")){
            minutos="0"+hora.getMinutes();
        }else{
            minutos= String.valueOf(hora.getMinutes());
        }
        if(String.valueOf(String.valueOf(hora.getHours()).length()).equals("1")){
            horas="0"+hora.getHours();
        }else{
            horas= String.valueOf(hora.getHours());
        }
        String hora_actual=horas+minutos;
        return hora_actual;
    }

    //la funcion validsar_monto es para realizar las validaciones de monto en el flotillero
    //si no esta establecido ningun limite regresa "1", si hay alguno establecido
    //valida que el limite sea menor o igual al acumulado, si se cumple regresa "1" si no un "0"
    public int validar_monto(Context context, String tag, String metodo) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        int resultado=0;
        ResultSet r;
        Connection connection= cg.odbc_cg(context);
        try {
            Statement stmt = connection.createStatement();
            String query="";
            if (metodo=="nfc") {
                query = "select carmax,candia,cansem,canmes,acudia,acusem,acumes from ClientesVehiculos where tag= '" + tag + "'";
            }else if(metodo=="nip"){
                query = "select carmax,candia,cansem,canmes,acudia,acusem,acumes from ClientesVehiculos where tag= '" + tag + "'";
            }else if(metodo=="nombre"){
                query = "select carmax,candia,cansem,canmes,acudia,acusem,acumes from ClientesVehiculos where tar= '" + tag + "'";
            }
            r = stmt.executeQuery(query);
            while (r.next()) {
                int carmax= r.getInt("carmax");
                int candia= r.getInt("candia");
                int cansem= r.getInt("cansem");
                int canmes= r.getInt("canmes");
                int acudia= r.getInt("acudia");
                int acusem= r.getInt("acusem");
                int acumes= r.getInt("acumes");
                if (carmax==0 && candia==0 && cansem==0 && canmes==0){
                    resultado=1;
                }else if(candia <= acudia || cansem <= acusem || canmes <= acumes ){
                    resultado=1;
                }else{
                    resultado=0;
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

    //la funcion validar_producto es para realizar las validaciones de producto en el flotillero
    //regresando todos o el producto que tiene asignado
    public String validar_producto(Context context, String tag, String metodo) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        String resultado="TODOS";
        ResultSet r;
        Connection connection= cg.odbc_cg(context);
        try {
            Statement stmt = connection.createStatement();
            String query="";
            if (metodo=="nfc") {
                query = "select p.den as combustible,cv.codprd as cod from ClientesVehiculos as cv " +
                        "left outer join Productos as p on p.cod=cv.codprd where cv.tag='" + tag + "'";
            }else if(metodo=="nip"){
                query = "select p.den as combustible,cv.codprd as cod from ClientesVehiculos as cv " +
                        "left outer join Productos as p on p.cod=cv.codprd where cv.tag='" + tag + "'";
            }else if(metodo=="nombre"){
                query = "select p.den as combustible,cv.codprd as cod from ClientesVehiculos as cv " +
                        "left outer join Productos as p on p.cod=cv.codprd where cv.tar='" + tag + "'";
            }
            r = stmt.executeQuery(query);
            while (r.next()) {
                int cod=r.getInt("cod");
                if (cod!=0) {
                    resultado = r.getString("combustible");
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

    //la funcion validar_ultimo_nrotrn sirve para obtener el ultimo servicio de la bomba, lo obiente antes de iniciar a sutir
    //es un candado para validar que el servicio que vamos a escribir sea el ultimo.
    public String validar_utlimo_nrotrn(Context context,String bomba) throws ClassNotFoundException,
            SQLException, InstantiationException, IllegalAccessException {
        String resultado="";
        ResultSet r;
        Connection connection= cg.odbc_cg(context);

        Statement stmt = connection.createStatement();
        String query = "select top 1 nrotrn from Despachos where nrobom ="+bomba+" order by nrotrn desc";
        r = stmt.executeQuery(query);
        while (r.next()) {
            resultado=r.getString("nrotrn");
        }
        connection.close();
        r.close();
        stmt.close();
        connection.close();
        return resultado;
    }

    //la funcion get_codcli regresa el codigo que se actualizara en el servicio
    public JSONObject get_codcli(Context context,String tag, String metodo)
            throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, JSONException {
        JSONObject resultado=new JSONObject();
        ResultSet r;
        Connection connection= cg.odbc_cg(context);

        Statement stmt = connection.createStatement();
        String query="";
        if (metodo=="nfc") {
            query = "select c.cod as cliente,cv.nroveh as vehiculo,cv.tar as tar from ClientesVehiculos as cv\n" +
                    "left outer join Clientes as c on c.cod=cv.codcli\n" +
                    "where cv.tag='" + tag + "'";
        }else if (metodo=="nip"){
            query = "select c.cod as cliente,cv.nroveh as vehiculo,cv.tar as tar from ClientesVehiculos as cv\n" +
                    "left outer join Clientes as c on c.cod=cv.codcli\n" +
                    "where cv.tag='" + tag + "'";
        }else if (metodo == "nombre"){
            query = "select c.cod as cliente,cv.nroveh as vehiculo,cv.tar as tar from ClientesVehiculos as cv\n" +
                    "left outer join Clientes as c on c.cod=cv.codcli\n" +
                    "where cv.tar='" + tag + "'";
        }
        r = stmt.executeQuery(query);
        while (r.next()) {
            resultado.put("cliente",r.getInt("cliente"));
            resultado.put("vehiculo",r.getInt("vehiculo"));
            resultado.put("tar",r.getInt("tar"));
        }
        connection.close();
        r.close();
        stmt.close();
        connection.close();
        return resultado;
    }

    //funcion para obtener datos del cliente
    public JSONObject get_vehiculo(Context context,String tag) throws ClassNotFoundException,
            SQLException, InstantiationException, IllegalAccessException, JSONException {
        JSONObject resultado = new JSONObject();
        ResultSet r;
        Connection connection= cg.odbc_cg(context);
        Statement stmt = connection.createStatement();
        String query = "select plc,rsp,nroeco,ultodm from ClientesVehiculos where tag='"+tag+"'";
        r = stmt.executeQuery(query);
        while (r.next()) {
            if ( r.getString("plc").length()<=0) {
                resultado.put("placa","S/P");
            }else {
                resultado.put("placa", r.getString("plc"));
            }
            if (r.getString("rsp").length()<=0){
                resultado.put("rps","S/R");
            }else {
                resultado.put("rsp", r.getString("rsp"));
            }
            if (r.getString("nroeco").length()<=0){
                resultado.put("nroeco","S/N");
            }else {
                resultado.put("nroeco", r.getString("nroeco"));
            }
            if (r.getString("ultodm").length()<=0){
                resultado.put("ultodm","S/O");
            }else {
                resultado.put("ultodm", r.getString("ultodm"));
            }
            Log.w("placa1",resultado.getString("placa"));
            Log.w("conductor1",resultado.getString("rsp"));
            Log.w("eco1",resultado.getString("nroeco"));
            Log.w("km1",resultado.getString("ultodm"));
        }
        connection.close();
        r.close();
        stmt.close();
        return resultado;
    }

    //funcion que revisa el servidor flotillero
    public boolean isServerReachable(String serverURL,Context context) {

        ConnectivityManager connMan = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connMan.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            try {
                URL urlServer = new URL(serverURL);
                HttpURLConnection urlConn = (HttpURLConnection) urlServer.openConnection();
                urlConn.setConnectTimeout(3000); //<- 3Seconds Timeout
                urlConn.connect();
                if (urlConn.getResponseCode() == 200) {
                    //Toast.makeText(context, "Server is Available", Toast.LENGTH_LONG).show();
                    urlConn.disconnect();
                    return true;
                } else {
                    urlConn.disconnect();
                    //Toast.makeText(context, "Server is not Available", Toast.LENGTH_LONG).show();
                    return false;
                }
            } catch (MalformedURLException e1) {
                Log.w("error1",e1);
                return false;
            } catch (IOException e) {
                Log.w("error",e);
                return false;
            }
        }
        return false;
    }

}
