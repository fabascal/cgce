package cg.ce.app.chris.com.cgce.ValesWS;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;


import cg.ce.app.chris.com.cgce.AceiteAdapterRV;
import cg.ce.app.chris.com.cgce.AceiteList;
import cg.ce.app.chris.com.cgce.R;



public class ValeAdapterRV extends RecyclerView.Adapter<ValeAdapterRV.ViewHolder>{

    private List<ValesList> valesLists;
    private Context context;
    private Activity activity;

    DecimalFormatSymbols symbols = new DecimalFormatSymbols();
    DecimalFormat decimalFormat = new DecimalFormat("$ #,###.00",symbols);

    public ValeAdapterRV(List<ValesList> valesLists, Context context, Activity activity){
        this.valesLists = valesLists;
        this.context = context;
        this.activity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // this method will be called whenever our ViewHolder is created
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.valeadaptador, parent, false);
        return new ViewHolder(v);
    }



    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        // this method will bind the data to the ViewHolder from whence it'll be shown to other Views
        final ValesList vales = valesLists.get(holder.getAdapterPosition());
        if (vales.getVal1() == 1 && vales.getVal2() == 0){
            holder.vale_image.setBackgroundColor(activity.getResources().getColor(android.R.color.holo_green_light));
        }
        holder.vale_folio.setText( vales.getFolio() );
        holder.vale_msj.setText(vales.getMsj());
        holder.vale_total.setText("$" + vales.getMonto());
        holder.vale_image.setImageResource(vales.getImagenEstado());
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //AceiteList aceiteList1 = aceiteLists.get(holder.getAdapterPosition());
                //openDialog(aceiteList1);*/
            }
        });
    }

    @Override
    //return the size of the listItems (developersList)
    public int getItemCount() {
        return valesLists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder  {

        // define the View objects
        public TextView vale_folio, vale_msj, vale_total;
        public ImageView vale_image;
        public LinearLayout linearLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            // initialize the View objects
            vale_folio = (TextView) itemView.findViewById(R.id.vale_folio);
            vale_msj = (TextView) itemView.findViewById(R.id.vale_msj);
            vale_total = (TextView) itemView.findViewById(R.id.vale_total);
            vale_image = (ImageView) itemView.findViewById(R.id.vale_image);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.ll_vale);
        }
    }
}
