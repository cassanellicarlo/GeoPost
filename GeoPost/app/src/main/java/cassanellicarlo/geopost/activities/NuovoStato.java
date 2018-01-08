package cassanellicarlo.geopost.activities;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import cassanellicarlo.geopost.models.DatiUtente;
import cassanellicarlo.geopost.R;

public class NuovoStato extends AppCompatActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int LOCATION_PERMISSION = 1;
    private GoogleApiClient mGoogleApiClient=null;
    private GoogleMap mMap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuovo_stato);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mappaPosizione);
        mapFragment.getMapAsync(this);

        // Controllo Permessi per Location
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            Log.d("Main Activity", "Permission granted!");

            // Controllo che il gps sia attivo, altrimenti mostro un Toast
            final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
            if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                CharSequence text = "Attiva il GPS";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(this, text, duration);
                toast.show();
            }


            startGoogleApiClient();




        } else{

            Log.d("Main Activity", "Not Granted yet.");

            // Richiesta dei permessi
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
    }

    protected void onStart() {
        if(mGoogleApiClient!=null) mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        if(mGoogleApiClient!=null) mGoogleApiClient.disconnect();
        super.onStop();
    }

    public void startGoogleApiClient (){

        // Creazione di un'istanza di GoogleAPIClient: permette di accedere
        // ai servizi di Google Play Services
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            mGoogleApiClient.connect();
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    startGoogleApiClient();

                    // Permessi Garantiti
                    Log.d("Main Activity", "Permessi OK");


                } else {
                    // Permessi Non Garantiti
                    Log.d("Main Activity", "Permessi NON garantiti");

                    Context context = getApplicationContext();
                    CharSequence text = "Calcolo posizione non autorizzato";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
                return;
            }

        }
    }



    @Override
    // Metodo chiamato quando il Google Api Client è pronto
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("onConnected:", "Google API Client Ready!");

        // Setto i parametri per l'acquisizione della posizione
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(60*1000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("onConnectionSuspended", "Connessione sospesa Google API Client");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("onConnectionFailed", "Connessione fallita Google API Client");
    }

    @Override
    // Il metodo onLocationChanged viene richiamato quando c’è una
    // nuova posizione, passando come argomento la posizione stessa.
    public void onLocationChanged(Location location) {

        Log.d("LOCATION",location.toString());
        DatiUtente.getInstance().setPosizioneAttuale(location);

        LatLng myPosition=new LatLng(location.getLatitude(),location.getLongitude());

        // Aggiungo il marker settando i parametri
        mMap.addMarker(new MarkerOptions()
                .position(myPosition)
                .title("La mia posizione")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        // Centro la mappa sulla mia posizione
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myPosition));

        // Zommo la mappa con un'animazione
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

    }

    public void aggiornaStato(View view) {
        Log.d("AGGIORNASTATO","Cliccato bottone aggiorna stato");
        TextView myMessage=(TextView)findViewById(R.id.nuovoMessaggio);
        String msg=myMessage.getText().toString();
        Log.d("MESSAGGIO",msg);

        // controllo: se il messaggio è vuoto, mostro un toast.
        if(msg.equals("")){
            displayMessage("Inserisci un messaggio");
        }

        // l'utente ha inserito il messaggio
        else{
            String encodedUrl=null;

            try {
                encodedUrl = URLEncoder.encode(msg, "UTF-8");
            } catch (UnsupportedEncodingException ignored) {
                // Can be safely ignored because UTF-8 is always supported
            }

            // Se non ho la posizione dell'utente, mostro un toast
            if(DatiUtente.getInstance().getPosizioneAttuale() == null){
                displayMessage("Non posso calcolare la posizione");
            }

            // Prendo session id e posizione dell'utente
            else {
                String session_id=DatiUtente.getInstance().getSession_id();
                Log.d("SESSIONID:",session_id);
                double lat=DatiUtente.getInstance().getPosizioneAttuale().getLatitude();
                Log.d("LATITUDINE",lat+"");
                double lon=DatiUtente.getInstance().getPosizioneAttuale().getLongitude();
                Log.d("LONGITUDINE",lon+"");

                RequestQueue queue = Volley.newRequestQueue(this);
                final String url = "https://ewserver.di.unimi.it/mobicomp/geopost/status_update?session_id="
                        +session_id+"&lat="+lat+"&lon="+lon+"&message="+encodedUrl;
                Log.d("URL STATUSUPDATE",url);



                // prepare the Request
                StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>()
                        {
                            @Override
                            public void onResponse(String response) {
                                // display response
                                Log.d("Response", response);
                                // OUTPUT VUOTO

                                Context context = getApplicationContext();
                                CharSequence text = "Stato aggiornato!";
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();

                                finish(); // finish the activity e torna agli amici


                            }
                        },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                if( error instanceof NetworkError) {
                                    displayMessage("Errore di rete!");
                                }

                                NetworkResponse networkResponse=error.networkResponse;
                                if(networkResponse != null && networkResponse.data != null){
                                    switch(networkResponse.statusCode){
                                        case 400:
                                            displayMessage("Errore");
                                            break;

                                        case 401:
                                            displayMessage("Numero di sessione non valido");
                                            break;
                                    }
                                    //Additional cases
                                }
                            }
                        }
                );

                // add it to the RequestQueue
                queue.add(getRequest);


            }
            }




    }

    // Mostra all'utente un Toast con l'errore corrispondente
    public void displayMessage(String toastString){
        Toast.makeText(getApplicationContext(), toastString, Toast.LENGTH_LONG).show();
    }
}
