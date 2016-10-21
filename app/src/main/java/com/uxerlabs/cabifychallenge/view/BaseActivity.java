package com.uxerlabs.cabifychallenge.view;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.uxerlabs.cabifychallenge.R;
import butterknife.ButterKnife;
import rx.functions.Action1;

/**
 * Base activity with ButterKnife initialization and request the location permission
 * @author Francisco Cuenca on 19/10/16.
 */

public abstract class BaseActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        RxPermissions
                .getInstance(this)
                .request(Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean granted) {
                        if (granted) {
                            onLocationPermissionGranted();
                        } else {
                            Toast.makeText(BaseActivity.this, getString(R.string.error_no_gps), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    protected abstract void onLocationPermissionGranted();

    public abstract int getLayoutId();

}
