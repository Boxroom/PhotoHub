package de.dhbw_mannheim.photohub;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewHolder {
    ImageView image;
    TextView title;
    TextView description;
    TextView location;
    ViewHolder(View v) {
        image = (ImageView) v.findViewById(R.id.imageView2);
        title = (TextView) v.findViewById(R.id.textView2);
        location = (TextView) v.findViewById(R.id.textView3);
        description = (TextView) v.findViewById(R.id.textView4);
    }
}
