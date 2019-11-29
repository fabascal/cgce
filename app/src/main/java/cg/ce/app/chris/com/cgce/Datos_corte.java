package cg.ce.app.chris.com.cgce;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Datos_corte extends AppCompatActivity implements View.OnClickListener{
    TextView tv_sesion_despachador,tv_sesion_dispensario,tv_sesion_entrada;
    ResultSet rs;
    Button btn_sesion_regresar;
    MacActivity macActivity = new MacActivity();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datos_corte);
        tv_sesion_despachador = (TextView) findViewById(R.id.tv_sesion_despachador);
        tv_sesion_dispensario = (TextView) findViewById(R.id.tv_sesion_dispensario);
        tv_sesion_entrada = (TextView) findViewById(R.id.tv_sesion_entrada);
        btn_sesion_regresar = (Button) findViewById(R.id.btn_sesion_regresar);
        btn_sesion_regresar.setOnClickListener(this);
        DataBaseCG cg = new DataBaseCG();
        Statement stmt = cg.odbc(getApplicationContext());

        String query = "select top 1 d.nombre as despachador,dis.numero_logico as dispensario,c.hora_entrada as entrada from corte as c\n" +
                "left outer join despachadores as d on d.id=c.id_despachador\n" +
                "left outer join dispensario as dis on dis.id=c.id_dispensario\n" +
                "left outer join dispositivos as dispo on dispo.id=c.id_dispositivo\n" +
                "where dispo.mac_adr='"+macActivity.getMacAddress()+"' and c.status = 0 order by hora_entrada desc";
        try {
            rs = stmt.executeQuery(query);
            while (rs.next()) {
                tv_sesion_despachador.setText( "Despachador : "+rs.getString("despachador"));
                tv_sesion_dispensario.setText( "Isla : "+rs.getString("dispensario"));
                tv_sesion_entrada.setText( "Entrada : "+rs.getString("entrada"));
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_sesion_regresar:
                Intent ventas = new Intent(Datos_corte.this,VentaActivity.class);
                startActivity(ventas);
                break;
        }
    }
}
