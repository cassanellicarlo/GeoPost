package cassanellicarlo.geopost.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import cassanellicarlo.geopost.models.Amico;
import cassanellicarlo.geopost.R;

/**
 * Created by carlo on 15/11/2017.
 */

public class AmiciAdapter extends ArrayAdapter<Amico> {

    //devo definire questi due costruttori
    public AmiciAdapter(Context context, int
            textViewResourceId) {
        super(context, textViewResourceId);
    }
    public AmiciAdapter(Context context, int
            resource, List<Amico> items) {
        super(context, resource, items);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View v=convertView;

        if(v==null){
            LayoutInflater vi;
            vi=LayoutInflater.from(getContext());
            v=vi.inflate(R.layout.lista_amici,null);
        }

        Amico a=getItem(position);

        if(a!=null){
            TextView tt1=(TextView)v.findViewById(R.id.nomeUtente);
            TextView tt2=(TextView)v.findViewById(R.id.messaggio);
            TextView tt3=(TextView)v.findViewById(R.id.distanza);

            tt1.setText(a.getUsername());
            tt2.setText(a.getMsg());
            tt3.setText((int)a.getDistanza()+" m");
        }

        return v;



    }
}
