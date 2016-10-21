package com.uxerlabs.cabifychallenge.utils;

import android.os.Build;
import android.view.View;

/**
 * @author Francisco Cuenca on 20/10/16.
 */

public class Utils {

    public static void changeElevation(View v, float elevation){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            v.setElevation(elevation);
        }
    }

}
