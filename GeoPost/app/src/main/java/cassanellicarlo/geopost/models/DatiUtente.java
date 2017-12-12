package cassanellicarlo.geopost.models;

import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by carlo on 14/11/2017.
 */

// Singleton che contiene i Dati dell'utente loggato (condivisi tra le varie activities)

public class DatiUtente {


    public String session_id; // Session id dell'utente (dato al momento del login corretto)
    public Location posizioneAttuale; // Posizione attuale dell'utente
    public ArrayList<Amico> amiciSeguiti=null; // Lista amici seguiti
    public boolean amiciScaricati=false; // Gli amici sono già scaricati?

    public static final DatiUtente ourInstance = new DatiUtente();

    public static DatiUtente getInstance() {
        return ourInstance;
    }

    public DatiUtente() {
        amiciSeguiti=new ArrayList<Amico>();
    }

    public ArrayList<Amico> getAmiciSeguiti() {
        return amiciSeguiti;
    }

    public void setAmiciSeguiti(ArrayList<Amico> amiciSeguiti) {
        this.amiciSeguiti = amiciSeguiti;
    }

    public void resetAmici (){
        amiciSeguiti.clear();
    }

    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public boolean isAmiciScaricati() {
        return amiciScaricati;
    }

    public void setAmiciScaricati(boolean amiciScaricati) {
        this.amiciScaricati = amiciScaricati;
    }

    public Location getPosizioneAttuale() {
        return posizioneAttuale;
    }

    public void setPosizioneAttuale(Location posizioneAttuale) {
        this.posizioneAttuale = posizioneAttuale;
    }

    // Ordina gli amici per distanza (ordine crescente)
    public void sortAmiciByDistance (){
        Collections.sort(amiciSeguiti);
    }

    // Stampo gli amici nel LOG
    public void stampaAmiciSeguiti (){
        for(int i=0;i<amiciSeguiti.size();i++){
            String username=amiciSeguiti.get(i).getUsername();
            String msg=amiciSeguiti.get(i).getMsg();
            double lat=amiciSeguiti.get(i).getLat();
            double lon=amiciSeguiti.get(i).getLon();
            Log.d("AMICO:",username+ " "+msg+" "+lat+ " "+lon );
        }
    }
}
