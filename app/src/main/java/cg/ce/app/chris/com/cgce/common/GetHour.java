package cg.ce.app.chris.com.cgce.common;

import java.util.Date;

public class GetHour {
    public static String hora_actual(){
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
}
