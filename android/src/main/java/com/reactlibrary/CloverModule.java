package com.reactlibrary;

import android.accounts.Account;

import android.os.AsyncTask;
import android.os.RemoteException;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.util.CloverAuth;
import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v1.merchant.MerchantConnector;
import com.clover.sdk.v1.merchant.Merchant;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;

public class CloverModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private Promise promise;
    private MerchantConnector merchantConnector;
    private Account account;

    public CloverModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        account = CloverAccount.getAccount(reactContext);
    }

    @Override
    public String getName() {
        return "Clover";
    }

    @ReactMethod
    public void getAuthToken(final Promise promise){
        this.promise=promise;
        new CloverAuthTask().execute();
    }
    @ReactMethod
    public void getMerchant(final Promise promise){
        this.promise=promise;
        new CloverMerchantTask().execute();
    }
    @ReactMethod
    public void getDeviceId(final Promise promise){
        this.promise=promise;
        new CloverDeviceTask().execute();
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
    private void connect() {
        disconnect();
        if (account != null) {
            merchantConnector = new MerchantConnector(reactContext,account,null);
            merchantConnector.connect();
        }
    }

    private void disconnect() {
        if (merchantConnector != null) {
            merchantConnector.disconnect();
            merchantConnector = null;
        }
    }
    public class CloverMerchantTask extends AsyncTask<Void,Void, Merchant>{
        @Override
        protected Merchant doInBackground(Void... voids) {
            if(account==null)return null;
            connect();
            Merchant merchant = null;
            try {
                merchant = merchantConnector.getMerchant();
            } catch (RemoteException e) {
                e.printStackTrace();
                promise.reject(e);
            } catch (ClientException e) {
                e.printStackTrace();
                promise.reject(e);
            } catch (ServiceException e) {
                e.printStackTrace();
                promise.reject(e);
            } catch (BindingException e) {
                e.printStackTrace();
                promise.reject(e);
            }
            return merchant;

        }

        @Override
        protected void onPostExecute(Merchant merchant) {
            super.onPostExecute(merchant);
            if(merchant!=null){
                WritableMap resultMap = Arguments.createMap();
                resultMap.putString("Mid", merchant.getMid());
                promise.resolve(resultMap);
            }
        }
    }
    public class CloverDeviceTask extends AsyncTask<Void,Void, String>{
        @Override
        protected String doInBackground(Void... voids) {
            String deviceId=null;
            try {
                deviceId=merchantConnector.getMerchant().getDeviceId();
            }catch (Exception e){
                promise.reject(e);
            }
            return deviceId;

        }

        @Override
        protected void onPostExecute(String device) {
            super.onPostExecute(device);
            if(device!=null){
                WritableMap resultMap = Arguments.createMap();
                resultMap.putString("deviceId", device);
                promise.resolve(resultMap);
            }else {
                promise.reject(new Exception("Device is null"));
            }
        }
    }
}
