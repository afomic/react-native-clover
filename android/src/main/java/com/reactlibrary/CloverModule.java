package com.reactlibrary;

import android.os.AsyncTask;

import com.clover.sdk.util.CloverAuth;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

public class CloverModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private Promise promise;

    public CloverModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "Clover";
    }

    @ReactMethod
    public void getAuthToken(ReadableMap options, final Promise promise){
        this.promise=promise;
        new CloverAuthTask().execute();
    }
    private class CloverAuthTask extends AsyncTask<Void, Void,  CloverAuth.AuthResult> {

        @Override
        protected final  CloverAuth.AuthResult doInBackground(Void... params) {
            try {
                return  CloverAuth.authenticate(reactContext);
            } catch (Exception e) {
                promise.reject(e);

            }
            return null;
        }

        @Override
        protected final void onPostExecute( CloverAuth.AuthResult item) {
            if (item != null) {
                WritableMap resultMap = Arguments.createMap();
                resultMap.putString("token", item.authToken);
                promise.resolve(resultMap);
            }
        }
    }
}
