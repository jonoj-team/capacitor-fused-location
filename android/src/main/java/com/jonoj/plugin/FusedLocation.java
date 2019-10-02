package com.jonoj.plugin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.PluginRequestCodes;
import com.google.android.gms.location.LocationRequest;
import com.patloew.rxlocation.RxLocation;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * Geolocation plugin that uses the fused location service instead of the native API.
 * <p>
 * Getting a location under android is quite difficult. The standard API implemented now in capacitor returns the GPS provider
 * which results in never getting a position indoors. This is not the case under iOS. A better way under Android is the
 * Fused Location Provider which already handles that.
 * <p>
 * See docs here: https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderClient
 * <p>
 * This plugin currently relies on https://github.com/patloew/RxLocation
 */


@NativePlugin(
        permissions = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        },
        permissionRequestCode = PluginRequestCodes.GEOLOCATION_REQUEST_PERMISSIONS
)
public class FusedLocation extends Plugin {
    private Map<String, Disposable> callIdToDisposable = new HashMap<>();
    private Map<String, Observable<Location>> callIdToObservable = new HashMap<>();
    private RxLocation mRxLocation;

    @PluginMethod()
    public void getCurrentPosition(PluginCall call) {
        Log.d(getLogTag(), "Requesting current position");
        if (!hasRequiredPermissions()) {
            Log.d(getLogTag(), "Not permitted. Asking permission...");
            saveCall(call);
            pluginRequestAllPermissions();
        } else {
            sendLocation(call);
        }
    }

    private void onNewLocation(PluginCall call, Location location) {
        if (location == null) {
            Log.d(getLogTag(), "Last position is null");
            call.error("location unavailable");
        } else {
            Log.d(getLogTag(), "Last position is not null");
            call.success(getJSObjectForLocation(location));
        }
    }

    private void onSubscribeError(PluginCall call, Throwable throwable) {
        Log.e(getLogTag(), "error subscribing to location" + throwable.getMessage());
        call.error("subscribe throws " + throwable.getMessage());
    }

    private void sendLocation(PluginCall call) {
        getRxLocation().location().lastLocation().subscribe(location -> {
                    onNewLocation(call, location);
                },
                throwable -> {
                    onSubscribeError(call, throwable);
                }
        );
    }

    @PluginMethod(returnType = PluginMethod.RETURN_CALLBACK)
    public void watchPosition(PluginCall call) {
        call.save();
        if (!hasRequiredPermissions()) {
            saveCall(call);
            pluginRequestAllPermissions();
        } else {
            startWatch(call);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void startWatch(PluginCall call) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(7500);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        callIdToObservable.put(call.getCallbackId(),
                getRxLocation().location().updates(locationRequest)
                        .doOnNext(location -> {
                            onNewLocation(call, location);
                        }).onErrorResumeNext(Observable.empty()));

        callIdToDisposable.put(call.getCallbackId(), callIdToObservable.get(call.getCallbackId()).subscribe());
    }

    @SuppressWarnings("MissingPermission")
    @PluginMethod()
    public void clearWatch(PluginCall call) {
        Log.d(getLogTag(), "clearWatch");
        String callbackId = call.getString("id");
        if (callbackId != null) {
            callIdToObservable.remove(callbackId);
            Disposable removed = callIdToDisposable.remove(callbackId);
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

        if (savedCall.getMethodName().equals("getCurrentPosition")) {
            sendLocation(savedCall);
        } else if (savedCall.getMethodName().equals("watchPosition")) {
            startWatch(savedCall);
        } else {
            savedCall.resolve();
            savedCall.release(bridge);
        }
    }

    private JSObject getJSObjectForLocation(Location location) {
        JSObject ret = new JSObject();
        JSObject coords = new JSObject();
        ret.put("coords", coords);
        ret.put("timestamp", location.getTime());
        coords.put("latitude", location.getLatitude());
        coords.put("longitude", location.getLongitude());
        coords.put("accuracy", location.getAccuracy());
        coords.put("altitude", location.getAltitude());
        if (Build.VERSION.SDK_INT >= 26) {
            coords.put("altitudeAccuracy", location.getVerticalAccuracyMeters());
        }
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
