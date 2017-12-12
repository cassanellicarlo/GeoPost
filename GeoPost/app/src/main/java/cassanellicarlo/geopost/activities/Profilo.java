package cassanellicarlo.geopost.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import cassanellicarlo.geopost.R;
import cassanellicarlo.geopost.authenticator.Login;
import cassanellicarlo.geopost.models.Amico;
import cassanellicarlo.geopost.models.DatiUtente;

public class Profilo extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap = null;
    private TextView usernameTextView;
    private TextView msgTextView;
    private double myLat;
    private double myLon;
    private boolean datiScaricati=false;
    private boolean mappaPronta=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profilo);

        scaricaDati();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mappaUltimoMessaggio);
        mapFragment.getMapAsync(this);

        usernameTextView=(TextView)findViewById(R.id.nomeUtente);
        msgTextView=(TextView)findViewById(R.id.messaggioUtente);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mappaPronta=true;
        mMap=googleMap;
        creaMappa();

    }

    public void scaricaDati () {

        String s_id= DatiUtente.getInstance().getSession_id();
        RequestQueue queue = Volley.newRequestQueue(this);
        final String url = "https://ewserver.di.unimi.it/mobicomp/geopost/profile?session_id="+s_id;
        // prepare the Request
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        Log.d("Response", response.toString());
                        try {
                            String username=response.getString("username");
                            usernameTextView.setText(username);
                            String msg=response.getString("msg");
                            msgTextView.setText(msg);
                            if(!response.getString("lat").equals("null")){
                                myLat=response.getDouble("lat");
                            }
                            if(!response.getString("lon").equals("null")){
                                myLon=response.getDouble("lon");
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        datiScaricati=true;
                        creaMappa();

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

    // Mostra all'utente un Toast con l'errore corrispondente
    public void displayMessage(String toastString){
        Toast.makeText(getApplicationContext(), toastString, Toast.LENGTH_LONG).show();
    }

    // Inserisco un marker nella mappa nell'ultima posizione salvata sul server
    public void creaMappa (){

        if(datiScaricati && mappaPronta){
            LatLng myPosition=new LatLng(myLat,myLon);

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

    }

    public void logout(View view) {
        Log.d("LOGOUT","Logout button clicked");

        String s_id= DatiUtente.getInstance().getSession_id();
        RequestQueue queue = Volley.newRequestQueue(this);
        final String url = "https://ewserver.di.unimi.it/mobicomp/geopost/logout?session_id="+s_id;
        // prepare the Request
        StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // display response
                        Log.d("Response", response.toString());
                        Intent intent=new Intent(getApplicationContext(),Login.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // to clean all activities
                        startActivity(intent);
                        finish();
                        Log.d("LOGOUT RESPONSE","I am inside logout response");

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
