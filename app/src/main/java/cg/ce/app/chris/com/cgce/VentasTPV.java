package cg.ce.app.chris.com.cgce;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.List;

public class VentasTPV extends AppCompatActivity {
    RecyclerView recyclerView;
    RecyclerAdapterTPVs adapter;
    List<TPVs> tpVsList;
    Sensores sensores = new Sensores();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ventas_tpv);
        sensores.bluetooth();
        sensores.wifi(this,true);
        recyclerView = (RecyclerView) findViewById(R.id.rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);
        initializateData();

        adapter= new RecyclerAdapterTPVs(tpVsList,this);
        recyclerView.setAdapter(adapter);
    }

    public void initializateData(){
        cgticket cg = new cgticket();
        //se obtienen los datos a partir de esta funcion
        tpVsList=cg.getTPVs(this);

        /*
        tpVsList.add(new TPVs(1,"Banamex",1,1,1,1,R.drawable.banamex));
        tpVsList.add(new TPVs(2,"Santander",1,1,1,1,R.drawable.santander));
        tpVsList.add(new TPVs(3,"Ticket Car",0,1,1,0,R.drawable.tc));
        tpVsList.add(new TPVs(4,"Edenred",0,1,1,0,R.drawable.edenred));
        */
    }
}
