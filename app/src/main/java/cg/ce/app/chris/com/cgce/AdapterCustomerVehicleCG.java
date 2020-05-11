package cg.ce.app.chris.com.cgce;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

public class AdapterCustomerVehicleCG extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    List<DataCustomerCG> data= Collections.emptyList();

    public AdapterCustomerVehicleCG(Context context, List<DataCustomerCG> data){
        this.context=context;
        inflater= LayoutInflater.from(context);
        this.data=data;
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
        DataCustomerCG current=data.get(position);
        myHolder.den.setText(current.den_vehicle);
        myHolder.rsp.setText(current.rsp);
        myHolder.plc.setText(current.plc);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView plc;
        TextView rsp;
        TextView den;
        int image;


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
                CustomerCG_data.put("tar", data.get(getAdapterPosition()).tar);
                CustomerCG_data.put("vehiculo", data.get(getAdapterPosition()).den_vehicle);
                CustomerCG_data.put("rsp", data.get(getAdapterPosition()).rsp);
                CustomerCG_data.put("nroveh", data.get(getAdapterPosition()).nroveh);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ((ActivityCreditoDual)context).AdapterClickCustomerVehicleCG(CustomerCG_data);
        }
    }
}
