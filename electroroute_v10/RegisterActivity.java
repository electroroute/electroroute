package com.tfg.evelyn.electroroute_v10;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class RegisterActivity extends Activity implements OnClickListener{
    private EditText user, pass, mail;
    private String usr, psw, email;


    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    private static final String REGISTER_URL = "http://electrorouteddbb.esy.es/loginElectroroute/register.php";

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        user = (EditText)findViewById(R.id.username);
        pass = (EditText)findViewById(R.id.password);
        mail = (EditText)findViewById(R.id.email);

        Button mRegister = (Button) findViewById(R.id.register);
        mRegister.setOnClickListener(this);

        //Declaración que permite ocultar teclado si pulsamos fuera
        RelativeLayout linear = (RelativeLayout) findViewById(R.id.register_page);
        linear.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                user.clearFocus();
                mail.clearFocus();
                pass.clearFocus();
                hideKeyboard(RegisterActivity.this);
                return true;
            }
        });

    }

    @Override
    public void onClick(View v) {

        if(!validateEmail(mail.getText().toString())) {
            Toast toast = Toast.makeText(getApplicationContext(), "El correo indicado no es válido." , Toast.LENGTH_SHORT);
            toast.show();
            pass.requestFocus();
        }else if(pass.getText().toString().length() < 1) {
            Toast toast = Toast.makeText(getApplicationContext(), "Es necesario indicar una contraseña." , Toast.LENGTH_SHORT);
            toast.show();
            pass.requestFocus();
        }else{
            usr = user.getText().toString();
            psw = pass.getText().toString();
            email = mail.getText().toString();
            new CreateUser().execute();
        }
    }

    private boolean validateEmail(String mail) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = mail;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            return true;
        }
        return false;
    }

    public static void hideKeyboard(Activity act) {
        InputMethodManager inputMethodManager = (InputMethodManager) act.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }



    class CreateUser extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RegisterActivity.this);
            pDialog.setMessage("Creating User...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            // Check for success tag
            int success;
            try {
                // Building Parameters
                List params = new ArrayList();
                params.add(new BasicNameValuePair("username", usr));
                params.add(new BasicNameValuePair("password", psw));
                params.add(new BasicNameValuePair("email", email));

                Log.d("request!", "starting");

                //Posting user data to script
                JSONObject json = jsonParser.makeHttpRequest(
                        REGISTER_URL, "POST", params);

                // full json response
                Log.d("Registering attempt", json.toString());

                // json success element
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("User Created!", json.toString());
                    finish();
                    return json.getString(TAG_MESSAGE);
                }else{
                    Log.d("Registering Failure!", json.getString(TAG_MESSAGE));
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
            if (file_url != null){
                Toast.makeText(RegisterActivity.this, file_url, Toast.LENGTH_LONG).show();
            }
        }
    }
}