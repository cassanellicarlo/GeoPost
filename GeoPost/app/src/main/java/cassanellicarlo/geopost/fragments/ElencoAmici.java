package cassanellicarlo.geopost.fragments;

/**
 * Created by carlo on 09/11/2017.
 */

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import cassanellicarlo.geopost.R;
import cassanellicarlo.geopost.adapters.AmiciAdapter;
import cassanellicarlo.geopost.models.DatiUtente;

public class ElencoAmici extends Fragment implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private static final int LOCATION_PERMISSION = 1;
    private GoogleApiClient mGoogleApiClient=null;
    private AmiciAdapter myAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.amici_elenco, container, false);

        //creaLista();

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser){ // Se il tab "Elenco" è quello attivo
            Log.d("FragmentElenco", "Fragment is visible.");
        }

        else{ // Se il tab "Elenco" non è attivo, mi disconnetto dal google api client
            Log.d("FragmentElenco", "Fragment is not visible.");
            //if(mGoogleApiClient!=null) mGoogleApiClient.disconnect();
        }

    }

    // Crea la lista degli amici basata su un Adapter personalizzato (AmiciAdapter)
    public void creaLista (){


        // Controllo Permessi per Location
        if (ContextCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            Log.d("Main Activity", "Permission granted!");

            // Controllo che il gps sia attivo, altrimenti mostro un Toast
            final LocationManager manager = (LocationManager) getContext().getSystemService( Context.LOCATION_SERVICE );
            if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                Context context = getContext();
                CharSequence text = "Attiva il GPS";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }

            if(mGoogleApiClient!=null) mGoogleApiClient.connect(); // Se il Google  Client è già istanziato, mi connetto

            else startGoogleApiClient(); // Instanzio il google api client




        } else{

            Log.d("Main Activity", "Not Granted yet.");

            // Richiesta dei permessi
            // Devo usare requestPermissions ( e non ActivityCompat.requestPermissions ) perché sono dentro il fragment
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION);
        }


        Log.d("ElencoAmici", DatiUtente.getInstance().isAmiciScaricati()+"");

        if(DatiUtente.getInstance().isAmiciScaricati()){

            myAdapter=new AmiciAdapter(getActivity(),
                    android.R.layout.list_content, DatiUtente.getInstance().getAmiciSeguiti() );

            ListView listView=(ListView)getView().findViewById(R.id.listaAmici);
            listView.setAdapter(myAdapter);
            myAdapter.notifyDataSetChanged();
        }


    }

    public void onStart() {
        if(mGoogleApiClient!=null) mGoogleApiClient.connect();
        super.onStart();
    }

    public void onStop() {
        if(mGoogleApiClient!=null) mGoogleApiClient.disconnect();
        super.onStop();
    }

    public void startGoogleApiClient (){

        // Creazione di un'istanza di GoogleAPIClient: permette di accedere
        // ai servizi di Google Play Services
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
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

                    Context context = getContext();
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
        mLocationRequest.setInterval(10*1000); // ogni 10 secondi
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ContextCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        }
    }

    @Override
    // Il metodo onLocationChanged viene richiamato quando c’è una
    // nuova posizione, passando come argomento la posizione stessa.
    public void onLocationChanged(Location location) {

        if(DatiUtente.getInstance().isAmiciScaricati()){ // solo se ho già scaricato gli amici
            Log.d("LOCATION",location.toString());
            DatiUtente.getInstance().setPosizioneAttuale(location); // Setto la posizione attuale dell'utente nel singleton

            // Per ogni utente, setto la distanza dalla posizione MIA ATTUALE a quella dell'utente in posizione i
            for(int i=0;i<DatiUtente.getInstance().getAmiciSeguiti().size();i++){
                float distanza=DatiUtente.getInstance().getAmiciSeguiti().get(i).getLocation().distanceTo(location);
                Log.d("DISTANZA",distanza+"");
                DatiUtente.getInstance().getAmiciSeguiti().get(i).setDistanza(distanza);
            }

            // Ordino gli amici per distanza (in ordine crescente)
            DatiUtente.getInstance().sortAmiciByDistance();

            // Dico all'adapter che i dati sono cambiati
            myAdapter.notifyDataSetChanged();
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


}
