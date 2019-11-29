package cg.ce.app.chris.com.cgce;



public class AceiteList  {
    private Integer imagen;
    private String descripcion;
    private Double precio;



    public Integer getImagen(){return imagen;}
    public String getDescripcion(){return descripcion;}
    public Double getPrecio(){return precio;}

    public AceiteList(Integer imagen, String descripcion, Double precio){
        this.imagen = imagen;
        this.descripcion = descripcion;
        this.precio = precio;
    }

}
