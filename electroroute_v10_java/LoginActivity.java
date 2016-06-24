package com.tfg.evelyn.electroroute_v10;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity  extends Activity implements OnClickListener {

    //View data
    private EditText user, pass;



    private String usr, psw;

    //Show progress status
    private ProgressDialog pDialog;

    // Clase JSONParser
    JSONParser jsonParser = new JSONParser();
    //JSON Connect
    private static final String LOGIN_URL = "http://electrorouteddbb.esy.es/loginElectroroute/login.php";
    private static final String tag = "myloginfo";

    // La respuesta del JSON es
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    boolean isInternetPresent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // setup input fields
        user = (EditText) findViewById(R.id.username);
        pass = (EditText) findViewById(R.id.password);

        // setup buttons
        Button mSubmit = (Button) findViewById(R.id.login);
        Button mRegister = (Button) findViewById(R.id.register);

        //Declaración que permite ocultar teclado si pulsamos fuera
        RelativeLayout linear = (RelativeLayout) findViewById(R.id.principal);
        linear.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                user.clearFocus();
                pass.clearFocus();
                hideKeyboard(LoginActivity.this);
                return true;
            }
        });

        // register listeners
        mSubmit.setOnClickListener(this);
        mRegister.setOnClickListener(this);
        InternetDetector id = new InternetDetector(getApplicationContext());

        isInternetPresent = id.isConnectingToInternet(); // true or false

    }

    public String getUsr() {
        return usr;
    }

    public void setUsr(String usr) {
        this.usr = usr;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.login:
                usr = user.getText().toString();
                psw = pass.getText().toString();
                Log.i(tag, usr);
                Log.i(tag, psw);
                new AttemptLogin().execute();

                break;
            case R.id.register:
                Intent i = new Intent(this, RegisterActivity.class);
                startActivity(i);
                break;

            default:
                break;
        }
    }

    public static void hideKeyboard(Activity act) {
        InputMethodManager inputMethodManager = (InputMethodManager) act.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public static void setDefaults(String key, String value, Context context) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor edit = sp.edit();
        edit.putString(key, value);
        edit.commit();
    }

    public static String getDefaults(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(key, null);
    }



    class AttemptLogin extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!isInternetPresent){
                Toast.makeText(LoginActivity.this, "Se requiere conexión a Internet!", Toast.LENGTH_LONG).show();
                finish();
            }

            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("Attempting login...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
            pDialog.dismiss();
        }

        @Override
        protected String doInBackground(String... args) {
            int success;

            try {
                // Building Parameters
                List params = new ArrayList();
                params.add(new BasicNameValuePair("username", usr));
                params.add(new BasicNameValuePair("password", psw));

                Log.d("request!", "starting");
                // getting product details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST",
                        params);

                // check your log for json response
                Log.d("Login attempt", json.toString());

                // json success tag
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("Login Successful!", json.toString());
                    // save user data
                    setDefaults("USERNAME", usr, LoginActivity.this);

                    Intent i = new Intent(LoginActivity.this, MapsActivity.class);
                    finish();
                    startActivity(i);
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.d("Login Failure!", json.getString(TAG_MESSAGE));
                    return json.getString(TAG_MESSAGE);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;

        }

        protected void onPostExecute(String file_url) {
            // dismiss the dialog once product deleted
            pDialog.dismiss();
            setUsr(usr);
            if (file_url != null) {
                Toast.makeText(LoginActivity.this, file_url, Toast.LENGTH_LONG).show();
            }
        }
    }




}