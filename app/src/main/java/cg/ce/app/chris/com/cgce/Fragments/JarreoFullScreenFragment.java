package cg.ce.app.chris.com.cgce.Fragments;


import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;


import org.json.JSONException;
import org.json.JSONObject;

import java.net.SocketException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import cg.ce.app.chris.com.cgce.DataBaseCG;
import cg.ce.app.chris.com.cgce.R;
import cg.ce.app.chris.com.cgce.cgticket;

/**
 * A simple {@link Fragment} subclass.
 */
public class JarreoFullScreenFragment extends DialogFragment {

    Toolbar toolbar;
    private final static String QUERY="select numero_logico as logico from posicion ";
    Connection connect;
    PreparedStatement stmt;
    ResultSet rs;
    Spinner spn_tiptrn, spn_fab_contado;
    ImageButton btn_print;
    cgticket cgticket = new cgticket();

    public JarreoFullScreenFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL,R.style.AppTheme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_jarreo_full_screen, container, false);
        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.jarreo);
        spn_tiptrn = view.findViewById(R.id.spn_tiptrn);
        spn_fab_contado = view.findViewById(R.id.spn_fab_contado);
        btn_print = view.findViewById(R.id.btn_print);
        try {
            fillPosicion();
            fillTiptrn();
        } catch (SQLException | JSONException e) {
            Toast.makeText(getActivity(),e.toString(),Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        btn_print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tiptrnSelect = spn_tiptrn.getSelectedItem().toString();
                String posicion = spn_fab_contado.getSelectedItem().toString();
                String nrotrn = null;
                /*Iniciamos variable en autojarreo que es el numero 65*/
                String tiptrn = "65";
                if (tiptrnSelect.equals("Jarreo")){
                    tiptrn = "74";
                }
                try {
                    JSONObject ticket = cgticket.consulta_servicio(getActivity(),posicion);
                    nrotrn = ticket.getString("nrotrn");
                    Log.w("nrotrn",nrotrn);
                    cgticket.setTipTrn(getActivity(),tiptrn,nrotrn);
                } catch (SQLException | IllegalAccessException | java.lang.InstantiationException |
                        ClassNotFoundException | JSONException | SocketException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getActivity(),tiptrn +"|"+posicion,Toast.LENGTH_LONG).show();
            }
        });
        return view;
    }

    public void fillPosicion() throws SQLException, JSONException {
        DataBaseCG gc = new DataBaseCG();
        connect = gc.control_gas(getActivity());
        stmt = connect.prepareStatement(QUERY);
        rs = stmt.executeQuery();

        ArrayList<String> data = new ArrayList<String>();
        while (rs.next()) {
            String id = rs.getString("logico");
            data.add(id);
        }
        String[] array = data.toArray(new String[0]);
        ArrayAdapter NoCoreAdapter = new ArrayAdapter(getActivity(),
                R.layout.spinner_bombas, data);
        connect.close();
        spn_fab_contado.setAdapter(NoCoreAdapter);
    }
    public void fillTiptrn(){
        ArrayList<String> data = new ArrayList<String>();
        data.add("Jarreo");
        data.add("AutoJarreo");
        ArrayAdapter NoCoreAdapter = new ArrayAdapter(getActivity(),
                R.layout.spinner_tiptrn, data);
        spn_tiptrn.setAdapter(NoCoreAdapter);
    }


}
