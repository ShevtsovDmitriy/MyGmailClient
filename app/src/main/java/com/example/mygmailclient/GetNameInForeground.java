package com.example.mygmailclient;


import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;

import org.json.JSONException;


public class GetNameInForeground extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "TokenInfoTask";
    protected SplashActivity mActivity;
    public static String GOOGLE_USER_DATA="No_data";
    protected String mScope;
    protected String mEmail;
    protected int mRequestCode;


    public GetNameInForeground(SplashActivity activity, String email, String scope) {
        this.mActivity = activity;
        this.mScope = scope;
        this.mEmail = email;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            fetchNameFromProfileServer();

        } catch (IOException ex) {
            onError("Following Error occured, please try again. "
                    + ex.getMessage(), ex);
        } catch (JSONException e) {
            onError("Bad response: " + e.getMessage(), e);
        }
        return null;
    }

    protected void onError(String msg, Exception e) {
        if (e != null) {
            Log.e(TAG, "Exception: ", e);
        }
    }

    private void fetchNameFromProfileServer() throws IOException, JSONException {
        //Toast.makeText(mActivity, "in background", Toast.LENGTH_SHORT).show();
        String token = fetchToken();
        URL url = new URL("https://www.googleapis.com/oauth2/v1/userinfo?access_token="+ token);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        int sc = con.getResponseCode();
        if (sc == 200) {
            InputStream is = con.getInputStream();
            GOOGLE_USER_DATA = readResponse(is);
            is.close();

            Intent intent=new Intent(mActivity,MainActivity.class);
            intent.putExtra("email_id", mEmail);
            mActivity.startActivity(intent);
            mActivity.finish();
        } else if (sc == 401) {
            GoogleAuthUtil.invalidateToken(mActivity, token);
            onError("Server auth error, please try again.", null);
            //Toast.makeText(mActivity, "Please try again", Toast.LENGTH_SHORT).show();
            //mActivity.finish();
        } else {
            onError("Server returned the following error code: " + sc, null);
        }
    }
    private static String readResponse(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] data = new byte[2048];
        int len = 0;
        while ((len = is.read(data, 0, data.length)) >= 0) {
            bos.write(data, 0, len);
        }
        return new String(bos.toByteArray(), "UTF-8");
    }



    protected String fetchToken() throws IOException {
        try {
            return GoogleAuthUtil.getToken(mActivity, mEmail, mScope);
        } catch (GooglePlayServicesAvailabilityException playEx) {
            // GooglePlayServices.apk is either old, disabled, or not present.
        } catch (UserRecoverableAuthException userRecoverableException) {
            // Unable to authenticate, but the user can fix this.
            // Forward the user to the appropriate activity.
            mActivity.startActivityForResult(userRecoverableException.getIntent(), mRequestCode);
        } catch (GoogleAuthException fatalException) {
            onError("Unrecoverable error " + fatalException.getMessage(), fatalException);
        }
        return null;
    }



}
