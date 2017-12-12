package cassanellicarlo.geopost.models;

import android.location.Location;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by carlo on 09/11/2017.
 */

public class Amico implements Comparable {

    private String username;
    private String msg;
    private double lat;
    private double lon;
    private double distanza;

    public Amico(String username, String msg, double lat, double lon) {
        this.username = username;
        this.msg = msg;
        this.lat = lat;
        this.lon = lon;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public LatLng getLatLon (){
        return new LatLng (this.getLat(),this.getLon());
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getDistanza() {
        return distanza;
    }

    public void setDistanza(double distanza) {
        this.distanza = distanza;
    }

    // Da latitudine e longitudine, crea un oggetto Location
    public Location getLocation (){
        Location crntLocation=new Location("crntlocation");
        crntLocation.setLatitude(lat);
        crntLocation.setLongitude(lon);
        return crntLocation;
    }

    // Implemento compareTo per ordinare gli amici per distanza
    @Override
    public int compareTo(@NonNull Object o) {

        Amico a=(Amico) o;
        double distanzaAmico=a.getDistanza();

        return (int) (distanza-distanzaAmico);
    }
}
