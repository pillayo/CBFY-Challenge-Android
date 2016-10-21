package com.uxerlabs.cabifychallenge;

import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * @author Francisco Cuenca on 21/10/16.
 */

public class CabifyApp extends android.support.multidex.MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }
}
