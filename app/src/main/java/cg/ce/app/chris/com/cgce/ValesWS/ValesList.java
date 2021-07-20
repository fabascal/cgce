package cg.ce.app.chris.com.cgce.ValesWS;

public class ValesList {
    private Integer logo, imagenEstado, val1, val2;
    private String folio;
    private String msj;
    private Double monto;

    public Integer getLogo(){ return logo;}
    public Integer getImagenEstado(){return imagenEstado;}
    public String getFolio(){return folio;}
    public String getMsj(){return msj;}
    public Double getMonto(){return monto;}
    public Integer getVal1(){return val1;}
    public Integer getVal2(){return val2;}

    public ValesList(Integer logo, Integer imagenEstado, String folio, String msj, Double monto, Integer val1, Integer val2){
        this.logo = logo;
        this.imagenEstado = imagenEstado;
        this.folio = folio;
        this.msj = msj;
        this.monto = monto;
        this.val1 = val1;
        this.val2 = val2;
    }
}
