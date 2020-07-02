package cg.ce.app.chris.com.cgce;



public class AceiteList  {
    private Integer imagen;
    private String descripcion;
    private Double precio;
    private Integer cantidad;
    private Integer codprd;



    public Integer getImagen(){return imagen;}
    public String getDescripcion(){return descripcion;}
    public Double getPrecio(){return precio;}
    public Integer getCantidad(){return cantidad;}
    public Integer getCodprd(){return codprd;}


    public AceiteList(Integer imagen, String descripcion, Double precio, Integer cantidad, Integer codprd){
        this.imagen = imagen;
        this.descripcion = descripcion;
        this.precio = precio;
        this.cantidad = cantidad;
        this.codprd = codprd;
    }

}
