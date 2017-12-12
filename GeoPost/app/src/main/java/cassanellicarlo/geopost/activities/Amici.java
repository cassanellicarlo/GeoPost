package cassanellicarlo.geopost.activities;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cassanellicarlo.geopost.fragments.ElencoAmici;
import cassanellicarlo.geopost.fragments.MappaAmici;
import cassanellicarlo.geopost.R;
import cassanellicarlo.geopost.models.Amico;
import cassanellicarlo.geopost.models.DatiUtente;

public class Amici extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private MappaAmici mappa=null; // Fragment MappaAmici
    private ElencoAmici elenco=null; // Fragment ElencoAmici
    private FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amici);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

       // Nuovo stato (status_update)
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("FloatingButton","NUOVO STATO");
                Intent intent=new Intent(getApplicationContext(),NuovoStato.class);
                startActivity(intent);
            }
        });

        scaricaAmici();
    }


    // Devo riscaricare gli amici qui nella onResume
    // perché se aggiunge un amico e poi torna agli amici non vede
    // il nuovo amico
    @Override
    protected void onResume() {


        super.onResume();
    }

    public void scaricaAmici (){

        DatiUtente.getInstance().resetAmici(); // clear delll'arraylist
        String session_id= DatiUtente.getInstance().getSession_id();
        Log.d("SESSIONID:",session_id);

        RequestQueue queue = Volley.newRequestQueue(this);
        final String url = "https://ewserver.di.unimi.it/mobicomp/geopost/followed?session_id="+session_id;

        // prepare the Request
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        Log.d("Response", response.toString());

                        try {
                            JSONArray jsonArray=response.getJSONArray("followed");
                            for(int i=0;i<jsonArray.length();i++){
                                Log.d("JSON ARRAY",jsonArray.get(i).toString());
                                JSONObject amico=jsonArray.getJSONObject(i);
                                boolean noMessage=false;
                                String username=amico.getString("username");
                                String msg=amico.getString("msg");
                                double lat=0;
                                double lon=0;
                                if(!amico.getString("lat").equals("null"))
                                    lat=amico.getDouble("lat");
                                else
                                    noMessage=true;
                                if(!amico.getString("lon").equals("null"))
                                    lon=amico.getDouble("lon");
                                else
                                    noMessage=true;
                                Log.d("Dati utente:",username+" "+msg+" "+lat+" "+lon);

                                if(!noMessage){
                                    DatiUtente.getInstance().getAmiciSeguiti().add(new Amico(username,msg,lat,lon));
                                }
                            }

                            // Stampa gli amici che l'utente segue nel LOG
                            DatiUtente.getInstance().stampaAmiciSeguiti();
                            DatiUtente.getInstance().setAmiciScaricati(true);

                            // Creo mappa degli amici
                            mappa.creaMappaAmici();

                            // Creo lista degli amici
                            elenco.creaLista();



                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


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


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            // returning the current tab
            switch (position){
                case 0:
                    mappa=new MappaAmici();
                    return mappa;
                case 1:
                    elenco=new ElencoAmici();
                    return elenco;
                default:
                    return null;
            }

        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }
    }


    // Crea il menu con le due icone nella bar in alto
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.amici_menu,menu);
        return true;
    }

    // In base all'icona cliccata, passo a un'altra activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.nuovoAmico: // Passo all'activity NuovoAmico
                Intent intent=new Intent(getApplicationContext(),NuovoAmico.class);
                startActivityForResult(intent,1);
                break;
            case R.id.profilo: // Passo all'activity Profilo
                Intent intent2=new Intent(getApplicationContext(),Profilo.class);
                startActivity(intent2);
                break;
        }
        return true;
    }

    // Mostra all'utente un Toast con l'errore corrispondente
    public void displayMessage(String toastString){
        Toast.makeText(getApplicationContext(), toastString, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1){
            if(resultCode==RESULT_OK){
                // devo scaricare gli amici perché ne ho aggiunto uno nuovo
                Log.d("NuovoAmico","Nuovo Amico -> Riscarico amici..");
                scaricaAmici();
            }
        }
    }
}
