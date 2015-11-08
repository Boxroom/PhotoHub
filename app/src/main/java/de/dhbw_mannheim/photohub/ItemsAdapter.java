package de.dhbw_mannheim.photohub;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

class ItemsAdapter extends ArrayAdapter<String> {
    Context context;
    ArrayList<Bitmap> bitmaps;
    ArrayList<String> paths;
    ArrayList<String> titles;
    ArrayList<String> descriptions;
    ArrayList<String> locations;

    public ItemsAdapter(Context context, ArrayList<Bitmap> bitmaps, ArrayList<String> titles, ArrayList<String> paths, ArrayList<String> descriptions, ArrayList<String> locations) {
        super(context, R.layout.items_list_item, R.id.textView2, titles);
        this.context = context;
        this.bitmaps = bitmaps;
        this.paths = paths;
        this.titles = titles;
        this.descriptions = descriptions;
        this.locations = locations;
    }

    public ItemsAdapter(Context context) {
        super(context, R.layout.items_list_item, R.id.textView2, new ArrayList<String>());
        this.context = context;
        this.bitmaps = new ArrayList<>();
        this.paths = new ArrayList<>();
        this.titles = new ArrayList<>();
        this.descriptions = new ArrayList<>();
        this.locations = new ArrayList<>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.items_list_item, parent, false);
            holder = new ViewHolder(row);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }
        if (bitmaps.get(position) != null)
            holder.image.setImageBitmap(bitmaps.get(position));
        if (titles.get(position) != null)
            holder.title.setText(titles.get(position));
        if (descriptions.get(position) != null)
            holder.description.setText(descriptions.get(position));
        if (locations.get(position) != null)
            holder.location.setText(locations.get(position));

        return row;
    }

    @Override
    public void add(String filePath) {
        super.add(filePath);
        paths.add(filePath);
        File file = new File(filePath);
        titles.add(file.getName());
        String dateString = "";
        int rotation = 0;
        double lat = 0, lng = 0;
        SimpleDateFormat dateConverter = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
        try {
            ExifInterface exif = new ExifInterface(filePath);
            rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            float[] latlong = new float[2];
            if (exif.getLatLong(latlong)) {
                lat = latlong[0];
                lng = latlong[1];
            }

            dateString = exif.getAttribute(ExifInterface.TAG_DATETIME);
            if (dateString == null) {
                dateString = "";
            }
            SimpleDateFormat dateParser = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            dateString = dateConverter.format(dateParser.parse(dateString));
        } catch (IOException | ParseException e) {
            dateString = dateConverter.format(new Date(file.lastModified()));
        }
        descriptions.add(dateString);

        int rotationInDegrees = PreDef.exifToDegrees(rotation);
        Matrix matrix = new Matrix();
        Bitmap adjustedBitmap;
        if (rotationInDegrees != 0)
            matrix.preRotate(rotationInDegrees);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = 15;
        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath(), options);
        if (rotationInDegrees % 180 != 0)
            adjustedBitmap = Bitmap.createBitmap(bitmap, bitmap.getWidth() / 4, 0, bitmap.getWidth() / 2, bitmap.getHeight(), matrix, true);
        else
            adjustedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        bitmaps.add(adjustedBitmap);

        try {
            Geocoder gcd = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = gcd.getFromLocation(lat, lng, 1);
            if (addresses != null && addresses.size() > 0) {
                String subLocality = addresses.get(0).getSubLocality(), locality = addresses.get(0).getLocality(), country = addresses.get(0).getCountryName();
                locations.add((subLocality != null && locality != null && subLocality != "" && locality != "" ? (subLocality + " - " + locality) : ((subLocality == null ? "" : subLocality) + (locality == null ? "" : locality)))
                                + (country == null || country == "" ? "" : ((subLocality != null && subLocality != "") || (locality != null && locality != "") ? ", " + country : country))
                );
            } else
                locations.add("");
        } catch (IOException e) {
            locations.add("");
        }
    }

    @Override
    public void remove(String filePath) {
        super.remove(filePath);
        int position = paths.indexOf(filePath);
        paths.remove(position);
        titles.remove(position);
        descriptions.remove(position);
        locations.remove(position);
        bitmaps.remove(position);
    }
}