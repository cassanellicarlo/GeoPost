package cassanellicarlo.geopost.fragments;

/**
 * Created by carlo on 09/11/2017.
 */

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import cassanellicarlo.geopost.R;
import cassanellicarlo.geopost.models.Amico;
import cassanellicarlo.geopost.models.DatiUtente;

public class MappaAmici extends Fragment  implements OnMapReadyCallback {

    private boolean mappaPronta=false;
    private GoogleMap mappa=null;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState )  {
        View rootView = inflater.inflate(R.layout.amici_mappa, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        return rootView;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mappa=googleMap;
        mappaPronta=true;

        creaMappaAmici();
    }

    // per centrare la mappa e far vedere tutti i punti che voglio mostrare
    public LatLngBounds myArea (){

        ArrayList<Amico> utenti= DatiUtente.getInstance().getAmiciSeguiti();
        // calcolo la minima longitudine e la minima latitudine
        double minLon=utenti.get(0).getLon();
        double minLat=utenti.get(0).getLat();
        for(int i=1;i<utenti.size();i++){
            if(minLon>utenti.get(i).getLon())
                minLon=utenti.get(i).getLon();

            if(minLat>utenti.get(i).getLat())
                minLat=utenti.get(i).getLat();

        }

        // calcolo la massima longitudine e la massima latitudine
        double maxLon=utenti.get(0).getLon();
        double maxLat=utenti.get(0).getLat();
        for (int i=1;i<utenti.size();i++){
            if(utenti.get(i).getLon() > maxLon)
                maxLon=utenti.get(i).getLon();

            if(utenti.get(i).getLat() > maxLat)
                maxLat=utenti.get(i).getLat();
        }


        LatLngBounds myBounds = new LatLngBounds(new LatLng(minLat, minLon), new LatLng(maxLat, maxLon));

        return myBounds;

    }


    public void creaMappaAmici (){

        Log.d("AMICI SCARICATI",DatiUtente.getInstance().isAmiciScaricati()+"");
        Log.d("MAPPA PRONTA",mappaPronta+"");

        // Se la mappa è pronta e ho già scaricato gli amici, aggiungo i markers
        if(mappaPronta && DatiUtente.getInstance().isAmiciScaricati() ){

            // Se l'utente non ha amici, mostro un toast e non aggiungo nessun marker
            if(DatiUtente.getInstance().getAmiciSeguiti().size()==0){
                //Toast.makeText(getContext(), "Per ora non hai amici :(", Toast.LENGTH_LONG).show();
                Log.d("NO FRIENDS","nessun amico"); // da controllare perché se rieffettuo il login da sbagliato
            }


            else { // se l'utente ha amici
                ArrayList<Amico> utenti=DatiUtente.getInstance().getAmiciSeguiti();

                // per ogni utente aggiungio un Marker con Lat e Lon e come titolo il nome utente
                for(int i=0;i<utenti.size();i++){
                    LatLng posizione=utenti.get(i).getLatLon();
                    String nome=utenti.get(i).getUsername();
                    String msg=utenti.get(i).getMsg();
                    mappa.addMarker(new MarkerOptions()
                            .position(posizione)
                            .title(nome)
                            .snippet(msg)
                    );

                }

                // Calcolo la mia area per mostrare tutti i marker
                LatLngBounds myBounds=myArea();

                // Centro la mappa sull'area che ho calcolato
                mappa.moveCamera(CameraUpdateFactory.newLatLngZoom(myBounds.getCenter(),10));
            }


        }

    }
}
