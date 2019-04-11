package com.jonoj.plugin;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.google.android.gms.location.LocationRequest;
import com.patloew.rxlocation.RxLocation;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.disposables.Disposable;

@NativePlugin(
        permissions = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        },
        permissionRequestCode = FusedLocation.PERMISSIONS_REQUEST_CODE
)
public class FusedLocation extends Plugin {
    private static final String TAG = FusedLocation.class.getSimpleName();

    public static final int PERMISSIONS_REQUEST_CODE = 9090;

    private Map<String, Disposable> watchingCalls = new HashMap<>();
    private RxLocation mRxLocation;
    private Disposable mDisposable;

    @PluginMethod()
    public void getCurrentPosition(final PluginCall call) {
        Log.d(TAG, "Requesting current position");

        call.save();
        if (!hasRequiredPermissions()) {
            Log.d(TAG, "Not permitted. Asking permission...");
            saveCall(call);
            pluginRequestAllPermissions();
        } else {
            getLastPosition(call);
        }

    }

    @SuppressWarnings("MissingPermission")
    private void getLastPosition(final PluginCall call) {
        getRxLocation().location().lastLocation().subscribe(location -> {
            if (location == null) {
                Log.d(TAG, "Last position is null");
                call.success();
            } else {
                Log.d(TAG, "Last position is not null");
                call.success(getJSObjectForLocation(location));
            }
        });
    }

    @PluginMethod(returnType = PluginMethod.RETURN_CALLBACK)
    public void watchPosition(PluginCall call) {
        Log.d(TAG, "Requesting to watch position");
        call.save();
        if (!hasRequiredPermissions()) {
            Log.d(TAG, "Not permitted. Asking permission...");
            saveCall(call);
            pluginRequestAllPermissions();
        } else {
            startWatch(call);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void startWatch(PluginCall call) {
        LocationRequest locationRequest;
        locationRequest = new LocationRequest();
        locationRequest.setInterval(7500);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        Disposable disposable = getRxLocation().location().updates(locationRequest).subscribe(location -> {
            if (location == null) {
                call.success();
            } else {
                call.success(getJSObjectForLocation(location));
            }
        });
        watchingCalls.put(call.getCallbackId(), disposable);
    }

    @SuppressWarnings("MissingPermission")
    @PluginMethod()
    public void clearWatch(PluginCall call) {
        Log.d(TAG, "Requesting to clear watch");
        String callbackId = call.getString("id");
        if (callbackId != null) {
            Disposable removed = watchingCalls.remove(callbackId);
            if (removed != null) {
                removed.dispose();
            }
        }

        call.success();
    }

    @Override
    protected void handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.handleRequestPermissionsResult(requestCode, permissions, grantResults);

        PluginCall savedCall = getSavedCall();
        if (savedCall == null) {
            return;
        }

        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                savedCall.error("User denied location permission");
                return;
            }
        }

        Log.d(TAG, "Continuing saved call: " + savedCall.getMethodName());

        if (savedCall.getMethodName().equals("getCurrentPosition")) {
            getLastPosition(savedCall);
        } else if (savedCall.getMethodName().equals("watchPosition")) {
            startWatch(savedCall);
        }
    }

    private JSObject getJSObjectForLocation(Location location) {
        JSObject ret = new JSObject();
        JSObject coords = new JSObject();
        ret.put("coords", coords);
        coords.put("latitude", location.getLatitude());
        coords.put("longitude", location.getLongitude());
        coords.put("accuracy", location.getAccuracy());
        coords.put("altitude", location.getAltitude());
        coords.put("speed", location.getSpeed());
        coords.put("heading", location.getBearing());
        return ret;
    }

    private RxLocation getRxLocation() {
        if (this.mRxLocation == null) {
            this.mRxLocation = new RxLocation(getContext());
        }
        return this.mRxLocation;
    }
}
