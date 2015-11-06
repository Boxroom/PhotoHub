package de.dhbw_mannheim.photohub;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewHolder {
    ImageView image;
    TextView title;
    TextView description;
    ViewHolder(View v) {
        image = (ImageView) v.findViewById(R.id.imageView2);
        title = (TextView) v.findViewById(R.id.textView2);
        description = (TextView) v.findViewById(R.id.textView3);
    }
}
