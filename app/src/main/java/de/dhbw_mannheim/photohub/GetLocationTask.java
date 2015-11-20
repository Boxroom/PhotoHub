package de.dhbw_mannheim.photohub;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GetLocationTask extends AsyncTask<Integer, Void, Boolean> {
    private ItemsAdapter adapter;
    private Context context;
    public GetLocationTask(Context context, ItemsAdapter adapter) {
        this.context = context;
        this.adapter = adapter;
    }

    @Override
    protected Boolean doInBackground(Integer... pos) {
        if(adapter.getItem(pos[0]).lat + adapter.getItem(pos[0]).lng > 0){
            if(adapter.internet) {
                try {
                    Geocoder gcd = new Geocoder(context, Locale.getDefault());
                    List<Address> addresses = gcd.getFromLocation(adapter.getItem(pos[0]).lat, adapter.getItem(pos[0]).lng, 1);
                    if (addresses != null && addresses.size() > 0) {
                        String subLocality = addresses.get(0).getSubLocality(), locality = addresses.get(0).getLocality(), country = addresses.get(0).getCountryName();
                        adapter.getItem(pos[0]).location = ((subLocality != null && locality != null && !subLocality.equals("") && !locality.equals("") ? (subLocality + " - " + locality) : ((subLocality == null ? "" : subLocality) + (locality == null ? "" : locality)))
                                + (country == null || country.equals("") ? "" : ((subLocality != null && !subLocality.equals("")) || (locality != null && !locality.equals("")) ? ", " + country : country)));
                        return true;
                    }
                } catch (IOException e) {
                    adapter.internet = false;
                }
            }
            adapter.getItem(pos[0]).location = "Kein Internet";
            return true;
        }
        adapter.getItem(pos[0]).location = "";
        return false;
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