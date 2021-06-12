package cg.ce.app.chris.com.cgce.AdapterCustomerVale;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cg.ce.app.chris.com.cgce.ConfiguracionValeActivity;
import cg.ce.app.chris.com.cgce.DataCustomerCG;
import cg.ce.app.chris.com.cgce.R;

public class CustomerAdapterRV extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<DataCustomerCG> data;
    private LayoutInflater inflater;
    private Context context;


    public CustomerAdapterRV(Context context, List<DataCustomerCG> data){
        this.context=context;
        inflater= LayoutInflater.from(context);
        this.data=data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.customervalecontainer, parent,false);
        CustomerAdapterRV.MyHolder holder=new CustomerAdapterRV.MyHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        CustomerAdapterRV.MyHolder myHolder= (CustomerAdapterRV.MyHolder) holder;
        DataCustomerCG current=data.get(position);
        myHolder.den.setText(current.den);
        myHolder.codcli.setText("Cod: " + current.codcli);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView codcli;
        TextView den;


        // create constructor to get widget reference
        public MyHolder(View itemView) {
            super(itemView);
            den= (TextView) itemView.findViewById(R.id.den);
            codcli = (TextView) itemView.findViewById(R.id.codcli);
            itemView.setOnClickListener(this);
        }

        // Click event for all items
        @Override
        public void onClick(View v ) {
            ((ConfiguracionValeActivity) context).AdapterClickCustomerVale(
                    data.get(getAdapterPosition()).codcli,data.get(getAdapterPosition()).den);
        }
    }
}
