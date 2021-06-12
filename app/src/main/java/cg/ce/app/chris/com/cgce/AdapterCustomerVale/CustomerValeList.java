package cg.ce.app.chris.com.cgce.AdapterCustomerVale;

public class CustomerValeList {
    private Integer imagen;
    private Integer codcli;
    private String den;

    public Integer setImagen(){return imagen;}
    public Integer setCodcli(){return codcli;}
    public String setDen(){return den;}

    public Integer getImagen(){return imagen;}
    public Integer getCodcli(){return codcli;}
    public String getDen(){return den;}

    public CustomerValeList(Integer imagen, Integer codcli, String den) {
        this.imagen = imagen;
        this.codcli = codcli;
        this.den = den;
    }
}
