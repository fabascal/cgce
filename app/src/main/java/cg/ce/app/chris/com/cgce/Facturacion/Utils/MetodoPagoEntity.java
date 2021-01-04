package cg.ce.app.chris.com.cgce.Facturacion.Utils;

public class MetodoPagoEntity {
    private int Id;
    private String Clave;
    private String Descripcion;
    private int Activo;

    public void SetId(int id){
        this.Id=id;
    }
    public void SetClave(String clave){
        this.Clave=clave;
    }
    public void SetDescripcion(String descripcion){
        this.Descripcion=descripcion;
    }
    public void SetActivo(int activo){
        this.Activo=activo;
    }

    public int GetId(){return Id;}
    public String GetClave(){return Clave;}
    public String GetDescripcion(){return Descripcion;}
    public int GetActivo(){return Activo;}
}
