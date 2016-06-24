package com.tfg.evelyn.electroroute_v10;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Evelyn on 24/05/2016.
 */
public class CreateComment_Rating extends Activity implements View.OnClickListener {



    private float old_rating;
    private float new_rating;
    private boolean ratedDone;

    //ids
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();
    private static final String RATING = "http://electrorouteddbb.esy.es/loginElectroroute/rating.php";
    private static final String COMMENT = "http://electrorouteddbb.esy.es/loginElectroroute/rating_comments.php";

    //
    private EditText title_input, comment_input;
    private String title, comment,site_name,user,dia,mes,annio,date;
    private Button cancel_btn, send_btn;
    private RatingBar rating_b;
    private int cont;



    private int contador;
    LoginActivity user_data = new LoginActivity();
    Calendar c = Calendar.getInstance();
    float rating;

    //ShowInfoMarker info = new ShowInfoMarker();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.new_comment);

        this.rating_b = (RatingBar)findViewById(R.id.ratingBar);
        this.title_input = (EditText)findViewById(R.id.title_input);
        this.comment_input  = (EditText)findViewById(R.id.comentario_input);
        this.cancel_btn = (Button)findViewById(R.id.button_cancel);
        this.send_btn = (Button)findViewById(R.id.button_send);

        this.ratedDone = false;

        cancel_btn.setOnClickListener(this);
        send_btn.setOnClickListener(this);

        listenerForRatingBar();

        //Obteniendo la instancia del Intent
        Intent intent= getIntent();
        //Extrayendo el extra de tipo cadena
        site_name = intent.getStringExtra("EXTRA_NAME");
        old_rating = intent.getFloatExtra("EXTRA_RTNG", (float)0.1);
        cont = intent.getIntExtra("EXTRA_CONT", 1);
        //

        dia = Integer.toString(c.get(Calendar.DATE));
        if ((c.get(Calendar.MONTH)+1)<=9) {
            mes = "0" + Integer.toString(c.get(Calendar.MONTH)+1);
        }else{
            mes = Integer.toString(c.get(Calendar.MONTH)+1);
        }
        annio = Integer.toString(c.get(Calendar.YEAR));


        date= dia+"/"+mes+"/"+annio;

        user = user_data.getDefaults("USERNAME", CreateComment_Rating.this);
        Toast.makeText(CreateComment_Rating.this, user, Toast.LENGTH_SHORT).show();


    }


    public void setRating (float old, float new_r){
        this.rating = new_rating+old_rating;
    }

    public float getRating(){
        return rating;
    }

    public int getContador() {
        return contador;
    }

    public void setContador(int contador) {
        this.contador = contador;
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.button_cancel:
                finish();
                //gotoShowInfoMarker();
                break;

            case R.id.button_send:

                title= title_input.getText().toString();
                comment=comment_input.getText().toString();
                createCommment();
                setRating(old_rating,new_rating);
                setContador(cont+1);;
                new UpdateRating().execute();
                //info.actualizar(rating,contador);
                finish();
                //gotoShowInfoMarker();


                break;

            default:
                break;


        }
    }



    private void gotoShowInfoMarker(){
        Intent i = new Intent(this, ShowInfoMarker.class);
        startActivity(i);
    }

    public boolean listenerForRatingBar(){
        if (!ratedDone) {
            rating_b.setOnRatingBarChangeListener(
                    new RatingBar.OnRatingBarChangeListener() {
                        @Override
                        public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                            ratedDone = true;
                            new_rating=rating+old_rating;
                            Toast.makeText(CreateComment_Rating.this, String.valueOf(rating), Toast.LENGTH_SHORT).show();

                        }
                    }
            );
        }
        return ratedDone;
    }

    class UpdateRating extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {

            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... args) {
            // Check for success tag
            int success;

            try {
                // Building Parameters
                List params = new ArrayList();
                params.add(new BasicNameValuePair("sitename", site_name));
                params.add(new BasicNameValuePair("rating", String.valueOf(new_rating)));
                params.add(new BasicNameValuePair("contador",  String.valueOf(contador)));

                Log.d("request!", "starting");

                //Posting user data to script
                JSONObject json = jsonParser.makeHttpRequest(
                        RATING, "POST", params);

                // full json response
                Log.d("Registering attempt", json.toString());

                // json success element
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("User Created!", json.toString());
                    //finish();
                    return json.getString(TAG_MESSAGE);
                } else {
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

            Log.i("actualizar", String.valueOf(new_rating)+" "+ String.valueOf(contador));
        }
    }







    private void createCommment() {

        class newComment_Rating extends AsyncTask<String, String, String> {


            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                pDialog = new ProgressDialog(CreateComment_Rating.this);
                pDialog.setMessage("Attempting login...");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(true);
                pDialog.show();
                pDialog.dismiss();
            }

            @Override
            protected String doInBackground(String... args) {
                // Check for success tag
                int success;

                try {
                    // Building Parameters
                    Log.i("COMMENT!", "starting");
                    List params = new ArrayList();
                    params.add(new BasicNameValuePair("sitename", site_name));
                    Log.i("DATA COMMENT", site_name);
                    params.add(new BasicNameValuePair("title", title));
                    Log.i("DATA COMMENT", title);
                    params.add(new BasicNameValuePair("comment", comment));
                    Log.i("DATA COMMENT", comment);
                    params.add(new BasicNameValuePair("date", date));
                    Log.i("DATA COMMENT", date);
                    params.add(new BasicNameValuePair("user", user));
                    Log.i("DATA COMMENT", user);
                    params.add(new BasicNameValuePair("rating", String.valueOf(new_rating)));
                    Log.i("DATA COMMENT", String.valueOf(new_rating));



                    //Posting user data to script
                    JSONObject json = jsonParser.makeHttpRequest(
                            COMMENT, "POST", params);

                    // full json response
                    Log.d("Comments attempt", json.toString());

                    // json success element
                    success = json.getInt(TAG_SUCCESS);
                    if (success == 1) {
                        Log.d("COMMENT CREATED!", json.toString());
                        //finish();
                        return json.getString(TAG_MESSAGE);
                    } else {
                        Log.d("Comment Failure!", json.getString(TAG_MESSAGE));
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
                Toast.makeText(CreateComment_Rating.this, "Gracias por su opinion", Toast.LENGTH_LONG).show();
            }
        }
        final newComment_Rating newComment_rating = new newComment_Rating();
        newComment_rating.execute();


    }





}
