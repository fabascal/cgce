package cg.ce.app.chris.com.cgce;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by chris on 5/09/17.
 */

public class RecyclerAdapterTPVs extends RecyclerView.Adapter<RecyclerAdapterTPVs.ViewHolder> {
    List<TPVs> tpVsList;
    static int lastPosition = -1;
    Context context;


    public RecyclerAdapterTPVs(List<TPVs>tpVsList,Context context){
        this.tpVsList=tpVsList;
        this.context=context;
    }


    @Override
    public RecyclerAdapterTPVs.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_tpvs,parent,false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerAdapterTPVs.ViewHolder holder, final int position) {
        holder.nombre.setText(tpVsList.get(position).nombre);
        holder.image.setImageResource(tpVsList.get(position).imagen);
        if (tpVsList.get(position).se_factura==0){
            holder.image_sefactura.setImageResource( R.drawable.cancel );
        }else{
            holder.image_sefactura.setImageResource( R.drawable.cfdirepsol );
        }
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent  intent = new Intent(context,VentaTPVBomba.class);

                JSONObject tpv_data = new JSONObject();
                try {
                    tpv_data.put("id",tpVsList.get(position).id);
                    tpv_data.put("nombre",tpVsList.get(position).nombre);
                    tpv_data.put("se_factura",tpVsList.get(position).se_factura);
                    tpv_data.put("activo",tpVsList.get(position).activo);
                    tpv_data.put("copia",tpVsList.get(position).copia);
                    tpv_data.put("bancaria",tpVsList.get(position).bancaria);
                    tpv_data.put("imagen",tpVsList.get(position).imagen);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                intent.putExtra("tpv_data",tpv_data.toString());
                context.startActivity(intent);
                Toast.makeText(context,String.valueOf(tpVsList.get(position).nombre),Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return tpVsList.size();
    }
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView){
        super.onAttachedToRecyclerView(recyclerView);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView nombre;
        public ImageView image,image_sefactura;
        public CardView cardView;
        public ViewHolder(View view){
            super(view);
            cardView=(CardView)itemView.findViewById(R.id.cardview);
            nombre=(TextView) itemView.findViewById(R.id.nombre);
            image =(ImageView)itemView.findViewById(R.id.image);
            image_sefactura = (ImageView)itemView.findViewById(R.id.image_sefactura);
        }
    }
}
