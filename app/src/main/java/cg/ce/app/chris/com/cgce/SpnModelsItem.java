package cg.ce.app.chris.com.cgce;

/**
 * Created by chris on 6/06/17.
 */

public class SpnModelsItem {
    private String mModelName = "";
    private int mModelConstant = 0;

    SpnModelsItem(String modelName, int modelConstant) {
        mModelName = modelName;
        mModelConstant = modelConstant;
    }

    public int getModelConstant() {
        return mModelConstant;
    }

    @Override
    public String toString() {
        return mModelName;
    }
}
