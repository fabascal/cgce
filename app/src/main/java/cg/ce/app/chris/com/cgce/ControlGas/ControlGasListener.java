package cg.ce.app.chris.com.cgce.ControlGas;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cg.ce.app.chris.com.cgce.DataCustomerCG;

public interface ControlGasListener {
    void processFinish(JSONObject output) throws JSONException;
}
