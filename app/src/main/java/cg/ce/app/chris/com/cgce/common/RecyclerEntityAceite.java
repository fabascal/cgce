package cg.ce.app.chris.com.cgce.common;

public class RecyclerEntityAceite {
    private Integer imagen;
    private String descripcion;
    private Double precio;
    private Integer cantidad;
    private Integer codprd;
    private boolean showMenu;

    public boolean isShowMenu() {
        return showMenu;
    }
    public void setShowMenu(boolean showMenu) {
        this.showMenu = showMenu;
    }

    public Integer getImagen() {
        return imagen;
    }
    public void setImagen(Integer imagen) {
        this.imagen = imagen;
    }

    public String getDescripcion() {
        return descripcion;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getPrecio() {
        return precio;
    }
    public void setTitle(Double precio) {
        this.precio = precio;
    }

    public Integer getCantidad() {
        return cantidad;
    }
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Integer getCodprd() {
        return codprd;
    }
    public void setCodprd(Integer codprd) {
        this.codprd = codprd;
    }

}
