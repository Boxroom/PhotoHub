package de.dhbw_mannheim.photohub;

import android.media.ExifInterface;

/**
 * Created by Florian on 06.11.2015.
 */
public class PreDef {
    public static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }
}
