package de.dhbw_mannheim.photohub;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * This AsyncTask convert the GPS latitude and longitude to an city name
 * This requires a network connection
 */
public class GetLocationTask extends AsyncTask<Integer, Void, Boolean> {
    private ItemsAdapter adapter;
    private Context context;
    public GetLocationTask(Context context, ItemsAdapter adapter) {
        this.context = context;
        this.adapter = adapter;
    }

    @Override
    protected Boolean doInBackground(Integer... pos) {
        //Only when the GPS value is valid (lat+lng > 0)
        if(adapter.getItem(pos[0]).lat + adapter.getItem(pos[0]).lng > 0){
            if(adapter.internet) {
                try {
                    //Convert latitude and longitude to an address and display 'village - city, country' if available
                    Geocoder gcd = new Geocoder(context, Locale.getDefault());
                    List<Address> addresses = gcd.getFromLocation(adapter.getItem(pos[0]).lat, adapter.getItem(pos[0]).lng, 1);
                    if (addresses != null && addresses.size() > 0) {
                        String subLocality = addresses.get(0).getSubLocality(), locality = addresses.get(0).getLocality(), country = addresses.get(0).getCountryName();
                        adapter.getItem(pos[0]).location = ((subLocality != null && locality != null && !subLocality.equals("") && !locality.equals("") ? (subLocality + " - " + locality) : ((subLocality == null ? "" : subLocality) + (locality == null ? "" : locality)))
                                + (country == null || country.equals("") ? "" : ((subLocality != null && !subLocality.equals("")) || (locality != null && !locality.equals("")) ? ", " + country : country)));
                        return true;
                    }
                } catch (IOException e) {
                    //Something went wrong - lets assume that the user has no internet connection
                    adapter.internet = false;
                }
            }
            adapter.getItem(pos[0]).location = "Kein Internet";
            return true;
        }
        //Location not provided, so remember it in the data. So we don't analyse the location twice
        //We try to convert the location when location == null, so set location = ''
        adapter.getItem(pos[0]).location = "";
        return true;
    }

    @Override
    protected void onPreExecute() {
        ++adapter.loadCountLoc;
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        --adapter.loadCountLoc;
        if(result)
            adapter.notifyDataSetChanged();
    }
}