package cg.ce.app.chris.com.cgce;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

public class ValesAdapterRV extends RecyclerView.Adapter<ValesAdapterRV.ViewHolder>{
    public static final int LOGO_IMAGE = R.drawable.aceite_logo;
    public static final int STATUS_IMAGE = R.drawable.aceite_logo;
    public static final String VALE_FOLIO = "Producto";
    public static final Double VALE_MONTO = 0.0;

    DecimalFormatSymbols symbols = new DecimalFormatSymbols();
    DecimalFormat decimalFormat = new DecimalFormat("$ #,###.00",symbols);

    private List<ValesList> valesLists;
    private Context context;

    public ValesAdapterRV(List<ValesList> valesLists, Context context){
        this.valesLists = valesLists;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // this method will be called whenever our ViewHolder is created
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.valesadapter, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ValesList  valesList = valesLists.get(position);
        holder.vale_monto.setText(valesList.getMonto().toString());
        holder.vale_folio.setText(valesList.getFolio());
        holder.vale_imagen.setImageResource(R.drawable.ic_combuvale);
        holder.vale_status.setImageResource(R.drawable.ic_thumb_down);

    }

    @Override
    public int getItemCount() {
        return valesLists.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder  {

        // define the View objects

        public TextView vale_monto, vale_folio;
        public ImageView vale_imagen, vale_status;
        public LinearLayout linearLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            // initialize the View objects

            vale_monto = (TextView) itemView.findViewById(R.id.vale_monto);
            vale_folio = (TextView) itemView.findViewById(R.id.vale_folio);
            vale_imagen = (ImageView) itemView.findViewById(R.id.vale_imagen);
            vale_status = (ImageView) itemView.findViewById(R.id.vale_status);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.ll_vale);
        }



    }
}
