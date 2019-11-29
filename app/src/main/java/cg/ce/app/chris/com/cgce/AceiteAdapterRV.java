package cg.ce.app.chris.com.cgce;

import android.content.Context;
import android.content.Intent;
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

public class AceiteAdapterRV extends RecyclerView.Adapter<AceiteAdapterRV.ViewHolder> {

    public static final int ACEITE_IMAGE = R.drawable.aceite_logo;
    public static final String ACEITE_NAME = "Producto";
    public static final Double ACEITE_PRICE = 0.0;
    DecimalFormatSymbols symbols = new DecimalFormatSymbols();


    DecimalFormat decimalFormat = new DecimalFormat("$ #,###.00",symbols);

    private List<AceiteList>aceiteLists;
    private Context context;

    public AceiteAdapterRV(List<AceiteList> aceiteLists, Context context){
        this.aceiteLists = aceiteLists;
        this.context = context;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // this method will be called whenever our ViewHolder is created
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.aceiteadapter, parent, false);
        return new ViewHolder(v);
    }



    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        // this method will bind the data to the ViewHolder from whence it'll be shown to other Views

        final AceiteList aceiteList = aceiteLists.get(position);
        holder.producto_descripcion.setText(aceiteList.getDescripcion());
        holder.producto_precio.setText(String.valueOf(decimalFormat.format(aceiteList.getPrecio())));
        holder.producto_imagen.setImageResource(aceiteList.getImagen());


        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AceiteList aceiteList1 = aceiteLists.get(position);
                Intent skipIntent = new Intent(v.getContext(), AceiteVenta.class);
                skipIntent.putExtra(ACEITE_NAME, aceiteList1.getDescripcion());
                skipIntent.putExtra(String.valueOf(ACEITE_PRICE), aceiteList1.getPrecio());
                skipIntent.putExtra(String.valueOf(ACEITE_IMAGE), aceiteList1.getImagen());
                v.getContext().startActivity(skipIntent);
            }
        });

    }

    @Override

    //return the size of the listItems (developersList)

    public int getItemCount() {
        return aceiteLists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder  {

        // define the View objects

        public TextView producto_descripcion;
        public ImageView producto_imagen;
        public TextView producto_precio;
        public LinearLayout linearLayout;
        public TextView txtDelete;

        public ViewHolder(View itemView) {
            super(itemView);

            // initialize the View objects

            producto_descripcion = (TextView) itemView.findViewById(R.id.producto_descripcion);
            producto_precio = (TextView) itemView.findViewById(R.id.producto_precio);
            producto_imagen = (ImageView) itemView.findViewById(R.id.producto_imagen);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.ll_aceite);
            txtDelete=(TextView)itemView.findViewById(R.id.txtDelete);
        }



    }
}
