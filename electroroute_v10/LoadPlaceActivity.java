package com.tfg.evelyn.electroroute_v10;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Evelyn on 14/04/2016.
 *Get places data from Database
 *
 */
public class LoadPlaceActivity{

    PlaceAsObject place;
    ArrayList<PlaceAsObject> placesList = new ArrayList<>();

    String resultJson;


    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();
    MapsActivity mapsActivity;

    private Context context;


    //si lo trabajan de manera local en xxx.xxx.x.x va su ip local
    // private static final String REGISTER_URL = "http://xxx.xxx.x.x:1234/cas/register.php";

    //testing on Emulator:
    private static final String LOAD_PLACES_URL = "http://electrorouteddbb.esy.es/loginElectroroute/loadplaces.php";

    //ids
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    public LoadPlaceActivity(Context c){

        this.context = c;
    }


    public String getResultJson() {
        return resultJson;
    }

    public void setResultJson(String resultJson) {
        this.resultJson = resultJson;
    }


    class LoadPlaces extends AsyncTask<Void, Void, String>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            int success;
            try {
                Log.d("request!", "starting");

                //Posting site_name data to script
                //JSONObject json = jsonParser.getJSONFromUrl(LOAD_PLACES_URL);
                JSONObject json = jsonParser.makeHttpRequest(LOAD_PLACES_URL, "POST", null);
                // full json response
                Log.i("Loading Sites", json.toString());

                // json success element
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.i("sites loaded!", json.toString());
                    return json.getString(TAG_MESSAGE);
                } else {
                    Log.i("Loading Failure!", json.getString(TAG_MESSAGE));
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
            createPlacesList ();
            Log.i("LoadPlace ***", ("Place: " + resultJson));
        }
    }


    public void runLoadPlacesActivity(){
        new LoadPlaces().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }





    public void createPlacesList (){
        try{
            // JSONObject jsonObject = new JSONObject(getResultJson());
            JSONArray jsonArray = new JSONArray(getResultJson());
            //Log.i("JSON Parser ROW****", "ARRAY" + jsonArray.getClass());
            int count = 0;

            while (count<jsonArray.length()){
                JSONObject row = jsonArray.getJSONObject(count);
                double lat = Double.parseDouble(row.getString("lat"));
                double lng = Double.parseDouble(row.getString("long"));
                float rat = Float.parseFloat(row.getString("rat"));
                int cont = Integer.parseInt(row.getString("cont"));
                place = new PlaceAsObject(row.getString("name"), lat, lng, row.getString("dir"), row.getString("desc"),row.getString("type"),rat,cont);
                placesList.add(place);
                //Log.i("LOAD_PLACE_ACTIVITY", "PLACE LIST *****: " + place.getName() + place.getDirection() + place.getLon());
                count ++;

            }

        }catch (JSONException e){
            Log.e("JSON Parser", "Error parsing data " + e.toString());
            e.printStackTrace();
        }

    }

    public ArrayList<PlaceAsObject> getPlacesList (){
        return placesList;
    }

}
