package cassanellicarlo.geopost.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cassanellicarlo.geopost.models.DatiUtente;
import cassanellicarlo.geopost.R;

public class NuovoAmico extends AppCompatActivity {

    private String url; // URL della chiamata di rete
    private ArrayList<String> utenti; // Lista degli utenti (risposta del server)
    private AutoCompleteTextView textView;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nuovo_amico);

        final RequestQueue queue = Volley.newRequestQueue(this);
        utenti = new ArrayList<String>();
        textView= (AutoCompleteTextView) findViewById(R.id.users);
        textView.setThreshold(1);
        // Aggiungo un Listener alla textView
        textView.addTextChangedListener(new TextWatcher() {

            // Metodo chiamato ad ogni modifica dell'AutoCompleteTextView
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                utenti.clear();
                String parametro=textView.getText().toString();
                Log.d("Parametro",parametro);
                String s_id=DatiUtente.getInstance().getSession_id();
                url="https://ewserver.di.unimi.it/mobicomp/geopost/users?limit=10&session_id="+s_id+"&usernamestart=";
                url+=parametro;
                Log.d("URL Richiesta",url);

                JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONObject>()
                        {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    // Inserisco gli utenti in un JSONArray
                                    JSONArray jsonArray=response.getJSONArray("usernames");

                                    // Dal JSONArray creo un ArrayList<String> con i nomi degli utenti cercati
                                    for(int i=0;i<jsonArray.length();i++){
                                        utenti.add(jsonArray.get(i).toString());
                                    }

                                    Log.d("Response", utenti+"");
                                    // Adapter per l'AutoCompleteTextView
                                    adapter = new ArrayAdapter<String>(getBaseContext(),
                                            android.R.layout.simple_dropdown_item_1line,utenti);
                                    textView.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();



                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }


                            }
                        },
                        new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                //Log.d("Error.Response", error.getMessage());

                                // preparo il Toast con l'errore da mostrare all'utente
                                Context context = getApplicationContext();
                                int duration = Toast.LENGTH_SHORT;
                                CharSequence text="";
                                Toast toast;

                                if( error instanceof NetworkError) {
                                    text = "Errore di rete!";
                                    toast = Toast.makeText(context, text, duration);
                                    toast.show();
                                }

                                NetworkResponse networkResponse=error.networkResponse;
                                if(networkResponse != null && networkResponse.data != null){
                                    switch(networkResponse.statusCode){
                                        case 400:
                                            text = "Errore!";
                                            toast = Toast.makeText(context, text, duration);
                                            toast.show();
                                            break;

                                        case 401:
                                            text = "Numero di sessione non valido!";
                                            toast = Toast.makeText(context, text, duration);
                                            toast.show();
                                            break;
                                    }
                                    //Additional cases
                                }
                            }
                        }



                );

                queue.add(getRequest);

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    public void seguiAmico(View view) {
        final Context context = getApplicationContext();
        AutoCompleteTextView autoCompleteTextView=(AutoCompleteTextView)findViewById(R.id.users);
        String amicoDaSeguire=autoCompleteTextView.getText().toString();
        Log.d("AMICODASEGUIRE",amicoDaSeguire);

        String session_id= DatiUtente.getInstance().getSession_id();
        Log.d("SESSIONID:",session_id);

        RequestQueue queue = Volley.newRequestQueue(this);
        final String url = "https://ewserver.di.unimi.it/mobicomp/geopost/follow?session_id="
                +session_id+"&username="+amicoDaSeguire;
        Log.d("URL FOLLOWFRIEND",url);

        // prepare the Request
        StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // display response
                        Log.d("Response", response);

                        if(response.equals("OK")) {
                            CharSequence text = "Amico seguito!";
                            int duration = Toast.LENGTH_SHORT;
                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();

                            Intent intent=new Intent();
                            setResult(RESULT_OK,intent);
                            finish(); // finish the activity e torna agli amici


                        }


                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        String json = null;

                        if( error instanceof NetworkError) {
                            displayMessage("Errore di rete");
                        }

                        NetworkResponse response = error.networkResponse;
                        if(response != null && response.data != null){
                            switch(response.statusCode){
                                case 400:
                                    json = new String(response.data);
                                    Log.d("JSON ERROR:",json);
                                    if(json != null) displayMessage(json);
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

}


