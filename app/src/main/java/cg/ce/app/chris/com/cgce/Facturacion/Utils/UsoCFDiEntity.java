package cg.ce.app.chris.com.cgce.Facturacion.Utils;

public class UsoCFDiEntity {
    private int Id;
    private String Clave;
    private String Descripcion;
    private int Activo;

    public UsoCFDiEntity(int id, String clave, String descripcion, int activo) {
        Id = id;
        Clave = clave;
        Descripcion = descripcion;
        Activo = activo;
    }

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
