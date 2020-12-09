package com.epson.epos2_cat;

public class SpnItems {
    private String mItemString = "";
    private int mItemValue = 0;

    SpnItems(String itemString, int itemValue) {
        mItemString = itemString;
        mItemValue = itemValue;
    }

    public int getItemConstant() {
        return mItemValue;
    }

    @Override
    public String toString() {
        return mItemString;
    }

}
