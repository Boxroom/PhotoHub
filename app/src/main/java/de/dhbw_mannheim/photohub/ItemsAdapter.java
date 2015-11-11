package de.dhbw_mannheim.photohub;

import android.content.Context;
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
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

class ItemsAdapter extends ArrayAdapter<ItemHolder> {
    public int loadCount;
    private Context context;
    private Comparator<ItemHolder> sortBy;

    public ItemsAdapter(Context context, ArrayList<ItemHolder> items, int sortBy) {
        super(context, R.layout.items_list_item, items);
        loadCount = 0;
        this.context = context;
        sortBy(sortBy);
    }

    public ItemsAdapter(Context context) {
        super(context, R.layout.items_list_item, new ArrayList<ItemHolder>());
        loadCount = 0;
        this.context = context;
        sortBy(0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if(getCount() > position){
            ItemHolder item = getItem(position);
            ViewHolder holder;
            if (row == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.items_list_item, parent, false);
                holder = new ViewHolder(row);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            if(item.bitmap == null) {
                holder.image.setImageResource(R.mipmap.ic_launcher);
                if(loadCount < 2){
                    ConvertImageTask imageTask = new ConvertImageTask(this);
                    imageTask.execute(position);
                }
            }
            if (item.bitmap != null)
                holder.image.setImageBitmap(item.bitmap);
            if (item.title != null)
                holder.title.setText(item.title);
            if (item.description != null)
                holder.description.setText(item.description);
            if (item.location != null)
                holder.location.setText(item.location);
        }
        return row;
    }


    public void add(String filePath) {
        File file = new File(filePath);
        String dateString;
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

        String location;
        try {
            Geocoder gcd = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = gcd.getFromLocation(lat, lng, 1);
            if (addresses != null && addresses.size() > 0) {
                String subLocality = addresses.get(0).getSubLocality(), locality = addresses.get(0).getLocality(), country = addresses.get(0).getCountryName();
                location = ((subLocality != null && locality != null && !subLocality.equals("") && !locality.equals("") ? (subLocality + " - " + locality) : ((subLocality == null ? "" : subLocality) + (locality == null ? "" : locality)))
                                + (country == null || country.equals("") ? "" : ((subLocality != null && !subLocality.equals("")) || (locality != null && !locality.equals("")) ? ", " + country : country)));
            } else
                location = "";
        } catch (IOException e) {
            location = "";
        }
        add(new ItemHolder(null, filePath, file.getName(), location, dateString, rotation));
    }

    @Override
    public void add(ItemHolder object) {
        super.add(object);
    }

    @Override
    public void remove(ItemHolder object) {
        super.remove(object);
    }

    public void remove(String filePath) {
        for(int position = 0; position < getCount(); ++position) {
            if(getItem(position).path.equals(filePath)){
                remove(getItem(position));
            }
        }
    }

    public void sortBy(int sortBy) {
        switch (sortBy){
            case 0:
                this.sortBy =  new Comparator<ItemHolder>() {
                    @Override
                    public int compare(ItemHolder lhs, ItemHolder rhs) {
                        return -lhs.title.compareToIgnoreCase(rhs.title);
                    }
                };
                break;
            case 1:
                this.sortBy =  new Comparator<ItemHolder>() {
                    @Override
                    public int compare(ItemHolder lhs, ItemHolder rhs) {
                        return lhs.title.compareToIgnoreCase(rhs.title);
                    }
                };
                break;
            case 2:
                this.sortBy =  new Comparator<ItemHolder>() {
                    @Override
                    public int compare(ItemHolder lhs, ItemHolder rhs) {
                        try {
                            SimpleDateFormat dateParser = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
                            Date date = dateParser.parse(lhs.description);
                            Date date2 = dateParser.parse(rhs.description);
                            return -date.compareTo(date2);
                        } catch (ParseException e) {
                            return -lhs.description.compareToIgnoreCase(rhs.description);
                        }
                    }
                };
                break;
            case 3:
                this.sortBy =  new Comparator<ItemHolder>() {
                    @Override
                    public int compare(ItemHolder lhs, ItemHolder rhs) {
                        try {
                            SimpleDateFormat dateParser = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
                            Date date = dateParser.parse(lhs.description);
                            Date date2 = dateParser.parse(rhs.description);
                            return date.compareTo(date2);
                        } catch (ParseException e) {
                            return lhs.description.compareToIgnoreCase(rhs.description);
                        }
                    }
                };
                break;
        }
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        this.setNotifyOnChange(false);
        this.sort(sortBy);
        this.setNotifyOnChange(true);
        super.notifyDataSetChanged();
    }
}