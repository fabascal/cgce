package cg.ce.app.chris.com.cgce;

import android.content.Context;
import android.content.res.Configuration;

public class ValidateTablet {

    public boolean esTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

}
