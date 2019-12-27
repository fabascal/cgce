package cg.ce.app.chris.com.cgce;

public class ValesList {
    private Integer logo, imagenEstado;
    private String folio;
    private Double monto;

    public Integer getLogo(){ return logo;}
    public Integer getImagenEstado(){return imagenEstado;}
    public String getFolio(){return folio;}
    public Double getMonto(){return monto;}

    public ValesList(Integer logo, Integer imagenEstado, String folio, Double monto){
        this.logo = logo;
        this.imagenEstado = imagenEstado;
        this.folio = folio;
        this.monto = monto;
    }
}
