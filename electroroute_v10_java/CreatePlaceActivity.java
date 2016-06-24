package com.tfg.evelyn.electroroute_v10;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by Evelyn on 25/03/2016.
 *
 */
public class CreatePlaceActivity extends Activity implements View.OnClickListener {

    private EditText site_name, description;
    private static EditText direction;
    private Spinner site_type;
    private String sName, dir, des,type;
    private double longi, lati;
    private Button mCreate;
    private Button mCancel;
    private ImageButton imgBnt;
    private ImageView image_from_camera;
    private RelativeLayout mRlView;
    private static final String tag = "CREATE_PLACE_ACTIVITY";

    private static String APP_DIRECTORY = "ElectroRouteAPP/";
    private static String MEDIA_DIRECTORY = APP_DIRECTORY + "PictureApp";

    private final int MY_PERMISSION = 100;
    private final int PHOTO_CODE = 200;
    private final int SELECT_PICTURE = 300;

    private String mPath;

    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();
    MapsActivity mapsActivity;


    //si lo trabajan de manera local en xxx.xxx.x.x va su ip local
    // private static final String REGISTER_URL = "http://xxx.xxx.x.x:1234/cas/register.php";

    //testing on Emulator:
    private static final String CREATEPLACE_URL = "http://electrorouteddbb.esy.es/loginElectroroute/newplace.php";



    //ids
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    //camara
    private String imageName;
    private File newFile;

    @Override
    protected void onCreate (Bundle savedInstanceState){
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        //Obteniendo la instancia del Intent
        Intent intent= getIntent();
        //Extrayendo el extra de tipo cadena

        longi = intent.getDoubleExtra("EXTRA_LONGITUDE", 0.1);
        lati = intent.getDoubleExtra("EXTRA_LATITUDE", 0.2);

        setContentView(R.layout.new_place);

        site_name = (EditText) findViewById(R.id.sitename);
        direction = (EditText) findViewById(R.id.direction);
        description = (EditText) findViewById(R.id.description);
        mRlView = (RelativeLayout) findViewById(R.id.RelativeLayoutNewPlace);

        mCreate = (Button) findViewById(R.id.crear);
        mCancel = (Button) findViewById(R.id.cancelar);
        imgBnt = (ImageButton) findViewById(R.id.imageButton);
        image_from_camera = (ImageView) findViewById(R.id.image_from_camera);

        site_type = (Spinner) findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.site_types_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        site_type.setAdapter(adapter);

        if(myRequestStoragePermission())
            imgBnt.setEnabled(true);
        else
            imgBnt.setEnabled(false);


        mCreate.setOnClickListener(this);
        mCancel.setOnClickListener(this);
        imgBnt.setOnClickListener(this);
        site_type.setOnItemSelectedListener(new CustomOnItemSelectedListener());
        getAddressFromLocation(lati,longi, this, new GeocoderHandler());


    }

    @Override
    public void onClick (View v){
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.crear:
                sName = site_name.getText().toString();
                dir = direction.getText().toString();
                des = description.getText().toString();
                type = String.valueOf(site_type.getSelectedItem());
                new CreatePlace().execute();
                break;
            case R.id.cancelar:
                Intent i = new Intent(CreatePlaceActivity.this, MapsActivity.class);
                finish();
                startActivity(i);

            case R.id.imageButton:
                ShowOptions(v);

        }

    }

    private boolean myRequestStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;
        if ((checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                (checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED))
            return true;
        if ((shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) || (shouldShowRequestPermissionRationale(CAMERA))) {
            Snackbar.make(mRlView, "Los permisos son necesarios",
                    Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {
                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MY_PERMISSION);
                }

            }).show();
        }
        else {
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, CAMERA}, MY_PERMISSION);
        }

        return false;
    }



    //Tomar foto
    private void ShowOptions(View view){
        final CharSequence[] options = {"Tomar foto", "Galeria", "Cancelar"};
        final AlertDialog.Builder builder = new AlertDialog.Builder(CreatePlaceActivity.this);
        builder.setTitle("Elija una opción:");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int seleccion) {
                if(options[seleccion] == "Tomar foto") {
                    openCamera();
                }
                else if (options[seleccion] == "Galeria") {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("images/*");
                    startActivityForResult(intent.createChooser(intent, "Selecciona app de imagen"), SELECT_PICTURE);
                }
                else {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void openCamera() {
        File file = new File(Environment.getExternalStorageDirectory(), MEDIA_DIRECTORY);
        Boolean isDirectoryCreated = file.exists();

        if(!isDirectoryCreated) {
            isDirectoryCreated = file.mkdirs();
        }

        if(isDirectoryCreated) {
            Long timeStamp = System.currentTimeMillis() / 1000;
            imageName = timeStamp.toString() + ".jpg";

            mPath = Environment.getExternalStorageDirectory() + File.separator + MEDIA_DIRECTORY
                    + File.separator + imageName;

            newFile = new File(mPath);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newFile));
            startActivityForResult(intent, PHOTO_CODE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("file_path", mPath);
        outState.putString("image_name",imageName);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mPath = savedInstanceState.getString("file_path");
        imageName = savedInstanceState.getString("image_name");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {

            switch (requestCode) {
                case PHOTO_CODE:
                    MediaScannerConnection.scanFile(this,
                            new String[]{mPath}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Log.i("ExternalStorage", "Scanned" + path + ":");
                                }
                            });
                    Bitmap bitmap = BitmapFactory.decodeFile(mPath);
                    int nh = (int) ( bitmap.getHeight() * (512.0 / bitmap.getWidth()) );
                    Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 512, nh, true);

                    image_from_camera.setImageBitmap(scaled);
                    image_from_camera.setVisibility(View.VISIBLE);
                    imgBnt.setVisibility(View.GONE);
                    break;
                case SELECT_PICTURE:
                    Uri path = data.getData();
                    image_from_camera.setImageURI(path);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSION) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(CreatePlaceActivity.this, "Permisos aceptados", Toast.LENGTH_SHORT).show();
                imgBnt.setEnabled(true);
            }
            else
                showExplanation();
        }
    }

    private void showExplanation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CreatePlaceActivity.this);
        builder.setTitle("Permisos denegados");
        builder.setMessage("Para usar las funciones de la aplicación necesita aceptar los permisos");
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        builder.show();
    }

    //GEODECODER : Obtener la dirección a partir de las coordenadas.
    protected void getAddressFromLocation(
            final double lat, final double lng, final Context context, final Handler handler) {
        Thread thread = new Thread() {
            @Override public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                String result = null;
                try {
                    List<Address> list = geocoder.getFromLocation(lat,lng,1);
                    if (list != null && list.size() > 0) {
                        Address address = list.get(0);
                        // sending back first address line and locality
                        result = address.getAddressLine(0) + ", " + address.getLocality();
                    }
                } catch (IOException e) {
                    Log.e(tag, "Impossible to connect to Geocoder", e);
                } finally {
                    Message msg = Message.obtain();
                    msg.setTarget(handler);
                    if (result != null) {
                        msg.what = 1;
                        Bundle bundle = new Bundle();
                        bundle.putString("address", result);
                        msg.setData(bundle);
                    } else
                        msg.what = 0;
                    msg.sendToTarget();
                }
            }
        };
        thread.start();
    }


    //GEODECODER : Mostrar la dirección obtenida en pantalla

    public static class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String result;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    result = bundle.getString("address");
                    break;
                default:
                    result = null;
            }
            // replace by what you need to do
            direction.setText(eliminarCaracteresEspeciales(result));
        }
    }

    private static String eliminarCaracteresEspeciales(String s){
            // Cadena de caracteres original a sustituir.
            String original = "áàäéèëíìïóòöúùuñÁÀÄÉÈËÍÌÏÓÒÖÚÙÜÑçÇ";
            // Cadena de caracteres ASCII que reemplazarán los originales.
            String ascii = "aaaeeeiiiooouuunAAAEEEIIIOOOUUUNcC";
            String output = s;

            for(int i=0; i<s.length(); i++) {
                for (int j = 0; j < original.length(); j++) {
                    // Reemplazamos los caracteres especiales.
                    if (s.charAt(i) == original.charAt(j)){
                        output = output.replace(s.charAt(i), ascii.charAt(j));
                    }

                }//for i
            }
            return output;

    }

    public String prepareImage (String mPath){
        //creación del bitmap a partir del path
        Bitmap bitmap = BitmapFactory.decodeFile(mPath);
        Log.i("IMAGE PATH ON UPLOAD", mPath);
        //escalado de la imagen
        int nh = (int) ( bitmap.getHeight() * (512.0 / bitmap.getWidth()) );
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 512, nh, true);
        //
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scaled.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        byte[] byteArray = stream.toByteArray();
        String imageStr = Base64.encodeToString(byteArray,Base64.DEFAULT);
        Log.i("BYTEARRAY PATH ", imageStr);

        return imageStr;

    }

    //Guarda el sitio creado en la DDBB
    class CreatePlace extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(CreatePlaceActivity.this);
            pDialog.setMessage("Creating site...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();


        }

        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // Check for success tag
            int success;
            String imageStr = prepareImage(mPath);

            try {
                // Building Parameters
                List params = new ArrayList();
                params.add(new BasicNameValuePair("sitename", sName));
                params.add(new BasicNameValuePair("direction", dir));
                params.add(new BasicNameValuePair("longitude", Double.toString(longi)));
                params.add(new BasicNameValuePair("latitude", Double.toString(lati)));
                params.add(new BasicNameValuePair("description", des));
                params.add(new BasicNameValuePair("site_type", type));
                params.add(new BasicNameValuePair("imagen", imageStr));
                Log.d("request!", "starting");

                //Posting site_name data to script
                JSONObject json = jsonParser.makeHttpRequest(
                        CREATEPLACE_URL, "POST", params);

                // full json response
                Log.d("Registering attempt", json.toString());


               // jsonParser.uploadPhoto(UPLOAD_IMG_SERVER,mPath);

                // json success element
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    Log.d("site_name Created!", json.toString());
                    finish();
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
            pDialog.dismiss();
            if (file_url != null) {
                Toast.makeText(CreatePlaceActivity.this, file_url, Toast.LENGTH_LONG).show();
            }
        }


    }




}