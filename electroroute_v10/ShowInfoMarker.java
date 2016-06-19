package com.tfg.evelyn.electroroute_v10;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;


import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by Evelyn on 13/05/2016.
 */
public class ShowInfoMarker extends Activity implements View.OnClickListener{


    private ViewGroup infoWindow;
    private TextView type ;
    private TextView site_name;
    private TextView direction;
    private TextView description ;
    private TextView ranking_num,num_votes;
    private Button commentsBtn,commentInputBtn,btnFindPath;
    private RatingBar rating_info;
    private ImageView placeIMG;
    private String name,tipo,dir,des,imagen;
    private int cont;

    private boolean ratedDone,new_coment;
    private boolean commentsOpened = false;
    private float old_rating;
    private float new_rating,rating;
    private double lat, lng, marker_lat, marker_lng;

    String def = "";

    String resultJson;

    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();


    private static final String GET_IMG = "http://electrorouteddbb.esy.es/loginElectroroute/get_image.php";
    private static final String LOAD_COMMENTS_URL = "http://electrorouteddbb.esy.es/loginElectroroute/load_comments.php";


    //ids
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    //Comments
    private String user,date,comment_text;
    ArrayList<CommentsAsListObject> commentItems_list = new ArrayList<CommentsAsListObject>();
    CommentsAsListObject commentItem;
    ListView comment_listview;
    CommentsListAdapter commentsListAdapter;
    CreateComment_Rating createComment_rating = new CreateComment_Rating();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Obteniendo la instancia del Intent
        Intent intent= getIntent();
        //Extrayendo el extra de tipo cadena

        tipo = intent.getStringExtra("EXTRA_TIPO");
        name = intent.getStringExtra("EXTRA_NAME");
        dir = intent.getStringExtra("EXTRA_DIR");
        des = intent.getStringExtra("EXTRA_DES");
        old_rating = intent.getFloatExtra("EXTRA_RTNG", (float)0.1);
        cont  = intent.getIntExtra("EXTRA_CONT", 1);
        rating = old_rating;
        lat = intent.getDoubleExtra("EXTRA_LAT", 1.0);
        lng= intent.getDoubleExtra("EXTRA_LON", 1.0);
        marker_lat =intent.getDoubleExtra("EXTRA_LATITUDE", 1.0);
        marker_lng =intent.getDoubleExtra("EXTRA_LONGITUDE", 1.0);


        //new GetImg().execute();

        setContentView(R.layout.marker_info_window);


        // Getting reference to the xml fileds to set marker info
        this.type = (TextView)findViewById(R.id.infocontent_sitetype);
        this.site_name = (TextView)findViewById(R.id.infocontent_sitename);
        this.direction = (TextView)findViewById(R.id.infocontent_siteaddr);
        this.description = (TextView)findViewById(R.id.infocontent_sitedescr);
        this.commentsBtn = (Button)findViewById(R.id.comments_btn);
        this.rating_info = (RatingBar)findViewById(R.id.ratingBar_info);
        this.placeIMG = (ImageView) findViewById(R.id.infocontent_image);
        this.comment_listview = (ListView) findViewById(R.id.infocontent_comments_list);
        this.commentInputBtn = (Button)findViewById(R.id.write_comment);
        this.ranking_num=(TextView)findViewById(R.id.ranking_number);
        this.num_votes=(TextView)findViewById(R.id.num_votes);
        this.btnFindPath = (Button)findViewById(R.id.navigate);

        commentsBtn.setOnClickListener(this);
        commentInputBtn.setOnClickListener(this);
        btnFindPath.setOnClickListener(this);


        getImage();
        String numVotes = "Numero de votos: " + String.valueOf(cont);

        type.setText(tipo);
        site_name.setText(name);
        direction.setText(dir);
        description.setText(des);
        ranking_num.setText(String.valueOf(rating/cont));
        num_votes.setText(numVotes);

        rating_info.setRating(rating/cont);

        //placeIMG.setImageBitmap(decodeIMG());
        //listenerForRatingBar();

        if (new_coment){
            new_rating=createComment_rating.getRating();
            cont=createComment_rating.getContador();
            rating=rating+new_rating;
            actualizar(rating,cont);
            new_coment=false;

        }


    }

    public void actualizar(float rating,int cont ){
        this.rating_info.setRating((float)(rating/cont));
        this.ranking_num.setText(String.valueOf(rating/cont));
        this.num_votes.setText(cont);
    }



    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.comments_btn:

                if (!commentsOpened) {
                    runLoadComments();
                    commentsBtn.setText("Hide Comments");
                    commentsOpened = true;
                } else {
                    comment_listview.setVisibility(View.GONE);
                    commentsBtn.setText("Show Comments");
                    commentsOpened = false;
                }

                break;

            case R.id.write_comment:
                new_coment=true;
                Intent i = new Intent(ShowInfoMarker.this, CreateComment_Rating.class);
                i.putExtra("EXTRA_NAME", name);
                i.putExtra("EXTRA_RTNG", rating);
                i.putExtra("EXTRA_CONT", cont);
                startActivity(i);
                finish();
                break;



            case R.id.navigate:
                Intent intent = new Intent(ShowInfoMarker.this, MapsDirectionsActivity.class);
                intent.putExtra("EXTRA_ORIGEN_LAT", lat);
                intent.putExtra("EXTRA_ORIGEN_LON", lng);
                intent.putExtra("EXTRA_DESTINATION_LAT", marker_lat);
                intent.putExtra("EXTRA_DESTINATION_LON", marker_lng);
                intent.putExtra("EXTRA_DESTINATION_DIR", dir);
                startActivity(intent);
                finish();
            default:
                break;


        }

    }


    private void getImage() {
        String id = name;
        class GetImage extends AsyncTask<String,Void,Bitmap>{
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(ShowInfoMarker.this, "Loading...", null,true,true);

            }

            @Override
            protected void onPostExecute(Bitmap b) {
                super.onPostExecute(b);
                placeIMG.setImageBitmap(b);
                loading.dismiss();

            }

            @Override
            protected Bitmap doInBackground(String... params) {
                String id = params[0];
                id = id.replace(" ","+");
                String add = GET_IMG+"?sitename="+id;
                Log.d("GET_IMG", add);
                URL url = null;
                Bitmap image = null;
                try {
                    url = new URL(add);
                    image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return image;
            }
        }

        GetImage gi = new GetImage();
        gi.execute(id);
    }

    class LoadComments extends AsyncTask<Void, Void, String>
    {
        ProgressDialog loading;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loading = ProgressDialog.show(ShowInfoMarker.this, "Loading...", null,true,true);
        }

        @Override
        protected String doInBackground(Void... args) {
            int success;
            try {
                Log.d("request!", "starting");
                List params = new ArrayList();
                params.add(new BasicNameValuePair("sitename", name));

                //Posting site_name data to script
                //JSONObject json = jsonParser.getJSONFromUrl(LOAD_PLACES_URL);
                JSONObject json = jsonParser.makeHttpRequest(LOAD_COMMENTS_URL, "POST", params);
                // full json response
                Log.i("Loading Sites", json.toString());

                // json success element
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.i("COMMENTS LOADED", json.toString());
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.i("COMMENTS Failure!", json.getString(TAG_MESSAGE));
                    return json.getString(TAG_MESSAGE);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;

        }


        @Override
        protected void onPostExecute(String result) {
            setResultJson(result);
            Log.i("LoadComments ***", ("Comment: " + resultJson));
            createCommentsList ();
            loading.dismiss();

        }
    }


    public void runLoadComments(){
        new LoadComments().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }






    public void createCommentsList (){
        try{
            // JSONObject jsonObject = new JSONObject(getResultJson());
            JSONArray jsonArray = new JSONArray(getResultJson());
            Log.i("JSON Parser ROW****", "ARRAY" + jsonArray.length());
            int count = 0;

            while (count<jsonArray.length()){
                JSONObject row = jsonArray.getJSONObject(count);
                float rat = Float.parseFloat(row.getString("rating"));
                commentItem = new CommentsAsListObject(row.getString("name"),row.getString("date"), row.getString("comm"),rat,row.getString("title"));
                Log.i("commentItem", "COM **: " + commentItem.getUser() + commentItem.getDate() + commentItem.getRating());
                commentItems_list.add(commentItem);

                count ++;

            }

        }catch (JSONException e){
            Log.e("JSON Parser", "Error parsing data " + e.toString());
            e.printStackTrace();
        }

        commentsListAdapter = new CommentsListAdapter(this,commentItems_list);
        comment_listview.setAdapter(commentsListAdapter);
        comment_listview.setVisibility(View.VISIBLE);

    }

    public ArrayList<CommentsAsListObject> getCommentItemsList (){
        return commentItems_list;
    }



    public String getResultJson() {
        return resultJson;
    }

    public void setResultJson(String resultJson) {
        this.resultJson = resultJson;
    }




}



