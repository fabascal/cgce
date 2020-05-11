package cg.ce.app.chris.com.cgce;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

public class AdapterCustomerCG extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    List<DataCustomerCG> data= Collections.emptyList();

    public AdapterCustomerCG(Context context, List<DataCustomerCG> data){
        this.context=context;
        inflater= LayoutInflater.from(context);
        this.data=data;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.container_customercg, parent,false);
        MyHolder holder=new MyHolder(view);
        return holder;
    }
    // create constructor to get widget reference


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MyHolder myHolder= (MyHolder) holder;
        DataCustomerCG current=data.get(position);
        myHolder.den.setText(current.den);
        myHolder.rfc.setText("RFC: " + current.rfc);
        myHolder.codcli.setText("Cod: " + current.codcli);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView codcli;
        TextView rfc;
        TextView den;
        int image;


        // create constructor to get widget reference
        public MyHolder(View itemView) {
            super(itemView);
            den= (TextView) itemView.findViewById(R.id.tvden);
            rfc = (TextView) itemView.findViewById(R.id.tvrfc);
            codcli = (TextView) itemView.findViewById(R.id.tvcodcli);
            itemView.setOnClickListener(this);
        }

        // Click event for all items
        @Override
        public void onClick(View v ) {
            JSONObject CustomerCG_data=new JSONObject();
            try {
                CustomerCG_data.put("rfc",data.get(getAdapterPosition()).rfc);
                CustomerCG_data.put("cliente",data.get(getAdapterPosition()).den);
                CustomerCG_data.put("codcli",data.get(getAdapterPosition()).codcli);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ((ActivityCreditoDual)context).AdapterClickCustomerCG(CustomerCG_data);
        }
    }
}
