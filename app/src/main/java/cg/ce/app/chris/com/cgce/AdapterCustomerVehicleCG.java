package cg.ce.app.chris.com.cgce;

import android.content.Context;
import android.content.SyncAdapterType;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdapterCustomerVehicleCG extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private Context context;
    private LayoutInflater inflater;
    List<DataCustomerCG> data= Collections.emptyList();
    List<DataCustomerCG> dataFiltered= Collections.emptyList();

    public AdapterCustomerVehicleCG(Context context, List<DataCustomerCG> data){
        this.context=context;
        inflater= LayoutInflater.from(context);
        this.data=data;
        this.dataFiltered=data;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.container_customercg_vehicle, parent,false);
        MyHolder holder=new MyHolder(view);
        return holder;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MyHolder myHolder= (MyHolder) holder;
        DataCustomerCG current=dataFiltered.get(position);
        myHolder.den.setText(current.den_vehicle);
        myHolder.rsp.setText(current.rsp);
        myHolder.plc.setText(current.plc);
    }

    @Override
    public int getItemCount() {
        return dataFiltered.size();
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if(constraint == null || constraint.length() == 0){
                    filterResults.count = data.size();
                    filterResults.values = data;
                }else{
                    List<DataCustomerCG> resultsModel = new ArrayList<>();
                    String searchStr = constraint.toString().toLowerCase();
                    for(DataCustomerCG data1:data){
                        if(data1.plc.contains(searchStr) ){
                            resultsModel.add(data1);
                        }
                        filterResults.count = resultsModel.size();
                        filterResults.values = resultsModel;
                    }
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                dataFiltered = (List<DataCustomerCG>) results.values;
                notifyDataSetChanged();
            }
        };
        return filter;
    }

    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView plc;
        TextView rsp;
        TextView den;


        // create constructor to get widget reference
        public MyHolder(View itemView) {
            super(itemView);
            den= (TextView) itemView.findViewById(R.id.den_vehicle);
            rsp = (TextView) itemView.findViewById(R.id.rsp);
            plc = (TextView) itemView.findViewById(R.id.plc);
            itemView.setOnClickListener(this);
        }

        // Click event for all items
        @Override
        public void onClick(View v ) {
            JSONObject CustomerCG_data=new JSONObject();
            try {
                CustomerCG_data.put("tar", dataFiltered.get(getAdapterPosition()).tar);
                CustomerCG_data.put("vehiculo", dataFiltered.get(getAdapterPosition()).den_vehicle);
                CustomerCG_data.put("rsp", dataFiltered.get(getAdapterPosition()).rsp);
                CustomerCG_data.put("nroveh", dataFiltered.get(getAdapterPosition()).nroveh);
                CustomerCG_data.put("tagadi", dataFiltered.get(getAdapterPosition()).tagadi);
                CustomerCG_data.put("placa", dataFiltered.get(getAdapterPosition()).plc);
                CustomerCG_data.put("chofer", dataFiltered.get(getAdapterPosition()).rsp);
                CustomerCG_data.put("nroeco", dataFiltered.get(getAdapterPosition()).nroeco);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ((Credito)context).AdapterClickCustomerVehicleCG(CustomerCG_data);
        }
    }
}
