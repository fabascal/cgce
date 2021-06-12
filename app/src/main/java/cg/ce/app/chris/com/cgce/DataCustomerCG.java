package cg.ce.app.chris.com.cgce;

import java.io.Serializable;

public class DataCustomerCG implements Serializable {
    public String codcli;
    public String den;
    public String rfc;
    public int imagen;
    public String rsp;
    public String plc;
    public String den_vehicle;
    public int tar;
    public int nroveh;
    public int tagadi;
    public String nroeco;
    public String tipval;
    public String codtip;

    public String getPlc(){return plc.toLowerCase(); }
}
