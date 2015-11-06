package de.dhbw_mannheim.photohub;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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

class ItemsAdapter extends ArrayAdapter<String> {
    Context context;
    ArrayList<Bitmap> bitmaps;
    ArrayList<String> paths;
    ArrayList<String> titles;
    ArrayList<String> descriptions;

    public ItemsAdapter(Context context, ArrayList<Bitmap> bitmaps, ArrayList<String> titles, ArrayList<String> paths, ArrayList<String> descriptions) {
        super(context, R.layout.items_list_item, R.id.textView2, titles);
        this.context = context;
        this.bitmaps = bitmaps;
        this.paths = paths;
        this.titles = titles;
        this.descriptions = descriptions;
    }

    public ItemsAdapter(Context context) {
        super(context, R.layout.items_list_item, R.id.textView2, new ArrayList<String>());
        this.context = context;
        this.bitmaps = new ArrayList<>();
        this.paths = new ArrayList<>();
        this.titles = new ArrayList<>();
        this.descriptions = new ArrayList<>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row=convertView;
        ViewHolder holder;
        if(row==null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.items_list_item, parent, false);
            holder = new ViewHolder(row);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }
        if(bitmaps.get(position) != null)
            holder.image.setImageBitmap(bitmaps.get(position));
        if(titles.get(position) != null)
            holder.title.setText(titles.get(position));
        if(descriptions.get(position) != null)
            holder.description.setText(descriptions.get(position));

        return row;
    }

    @Override
    public void add(String filePath){
        paths.add(filePath);
        File file = new File(filePath);
        titles.add(file.getName());
        String dateString = "";
        int rotation = 0;
        try {
            ExifInterface exif = new ExifInterface(filePath);
            rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            dateString = exif.getAttribute(ExifInterface.TAG_DATETIME);
            SimpleDateFormat dateParser = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            SimpleDateFormat dateConverter = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
            Date date = dateParser.parse(dateString);
            dateString = dateConverter.format(date);
        } catch(IOException | ParseException e) {
            long date = file.lastModified();
            Date fileData = new Date(date);
            dateString = String.format("hh:mm:ss dd.MM.yyyy", fileData);
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
        if (rotationInDegrees%180 != 0)
            adjustedBitmap = Bitmap.createBitmap(bitmap, bitmap.getWidth()/4, 0, bitmap.getWidth()/2, bitmap.getHeight(), matrix, true);
        else
            adjustedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        bitmaps.add(adjustedBitmap);

        this.notifyDataSetChanged();
    }
}