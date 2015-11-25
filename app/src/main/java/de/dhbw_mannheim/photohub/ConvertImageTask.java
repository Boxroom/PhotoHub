package de.dhbw_mannheim.photohub;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;

/**
 * This AsyncTask loads the image from the file system
 */
public class ConvertImageTask extends AsyncTask<Integer, Void, Bitmap> {
    private ItemsAdapter adapter;
    public ConvertImageTask(ItemsAdapter adapter) {
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
        //Get image from path - just read every 15th pixel
        Bitmap bitmap = BitmapFactory.decodeFile(adapter.getItem(pos[0]).path, options);
        //Zoom image if it is captured vertical to minimize white areas
        if (rotationInDegrees % 180 != 0)
            adjustedBitmap = Bitmap.createBitmap(bitmap, bitmap.getWidth() / 4, 0, bitmap.getWidth() / 2, bitmap.getHeight(), matrix, true);
        else
            adjustedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        adapter.getItem(pos[0]).bitmap = adjustedBitmap;

        return adjustedBitmap;
    }

    @Override
    protected void onPreExecute() {
        ++adapter.loadCountImg;
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        --adapter.loadCountImg;
        adapter.notifyDataSetChanged();
    }
}