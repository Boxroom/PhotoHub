package de.dhbw_mannheim.photohub;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ConvertImageTask extends AsyncTask<Integer, Void, Bitmap> {
    private ItemsAdapter adapter;
    private Context context;
    public ConvertImageTask(Context context, ItemsAdapter adapter) {
        this.context = context;
        this.adapter = adapter;
    }

    @Override
    protected Bitmap doInBackground(Integer... pos) {
        int rotationInDegrees = PreDef.exifToDegrees(adapter.getItem(pos[0]).rotation);
        Matrix matrix = new Matrix();
        Bitmap adjustedBitmap;
        if (rotationInDegrees != 0)
            matrix.preRotate(rotationInDegrees);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = 15;
        Bitmap bitmap = BitmapFactory.decodeFile(adapter.getItem(pos[0]).path, options);
        if (rotationInDegrees % 180 != 0)
            adjustedBitmap = Bitmap.createBitmap(bitmap, bitmap.getWidth() / 4, 0, bitmap.getWidth() / 2, bitmap.getHeight(), matrix, true);
        else
            adjustedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        adapter.getItem(pos[0]).bitmap = adjustedBitmap;

        if(adapter.getItem(pos[0]).lat + adapter.getItem(pos[0]).lng > 0){
            String location;
            try {
                Geocoder gcd = new Geocoder(context, Locale.getDefault());
                List<Address> addresses = gcd.getFromLocation(adapter.getItem(pos[0]).lat, adapter.getItem(pos[0]).lng, 1);
                if (addresses != null && addresses.size() > 0) {
                    String subLocality = addresses.get(0).getSubLocality(), locality = addresses.get(0).getLocality(), country = addresses.get(0).getCountryName();
                    location = ((subLocality != null && locality != null && !subLocality.equals("") && !locality.equals("") ? (subLocality + " - " + locality) : ((subLocality == null ? "" : subLocality) + (locality == null ? "" : locality)))
                            + (country == null || country.equals("") ? "" : ((subLocality != null && !subLocality.equals("")) || (locality != null && !locality.equals("")) ? ", " + country : country)));
                } else
                    location = "";
            } catch (IOException e) {
                location = "";
            }
            adapter.getItem(pos[0]).location = location;
        }

        return adjustedBitmap;
    }

    @Override
    protected void onPreExecute() {
        ++adapter.loadCount;
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        --adapter.loadCount;
        adapter.notifyDataSetChanged();
    }
}