package de.dhbw_mannheim.photohub;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static final int PICK_PHOTO_REQUEST = 1;
    static final int LOAD_PHOTO_REQUEST = 2;

    ArrayList<Integer> images = new ArrayList<>();
    ArrayList<String> titles = new ArrayList<>();
    ArrayList<String> descriptions = new ArrayList<>();

    private ItemsAdapter adapter;
    private String tmpOutputFile;

    @Override
    protected void onSaveInstanceState(Bundle extra) {
        super.onSaveInstanceState(extra);
        extra.putString("tmpOutputFile", tmpOutputFile);
        extra.putIntegerArrayList("images", images);
        extra.putStringArrayList("titles", titles);
        extra.putStringArrayList("descriptions", descriptions);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        final ListView listView = (ListView) findViewById(R.id.listView);

        if (savedInstanceState != null){
            tmpOutputFile = savedInstanceState.getString("tmpOutputFile");
            images = savedInstanceState.getIntegerArrayList("images");
            titles = savedInstanceState.getStringArrayList("titles");
            descriptions = savedInstanceState.getStringArrayList("descriptions");
        }

        adapter = new ItemsAdapter(this, titles, images, descriptions);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String itemValue = (String) listView.getItemAtPosition(position);

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle("ListView");
                alertDialog.setMessage(itemValue);
                alertDialog.show();
            }
        });
    }

    class ItemsAdapter extends ArrayAdapter<String> {
        Context context;
        ArrayList<Integer> images;
        ArrayList<String> titles;
        ArrayList<String> descriptions;

        public ItemsAdapter(Context context, ArrayList<String> titles, ArrayList<Integer> images, ArrayList<String> descriptions) {
            super(context, R.layout.items_list_item, R.id.textView2, titles);
            this.context = context;
            this.images = images;
            this.titles = titles;
            this.descriptions = descriptions;
        }

        class MyViewHolder {
            ImageView image;
            TextView title;
            TextView description;
            MyViewHolder(View v) {
                image = (ImageView) v.findViewById(R.id.imageView2);
                title = (TextView) v.findViewById(R.id.textView2);
                description = (TextView) v.findViewById(R.id.textView3);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row=convertView;
            MyViewHolder holder;
            if(row==null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.items_list_item, parent, false);
                holder = new MyViewHolder(row);
                row.setTag(holder);
            } else {
                holder = (MyViewHolder) row.getTag();
            }
            //holder.image.setImageResource(images.get(position));
            holder.title.setText(titles.get(position));
            holder.description.setText(descriptions.get(position));

            return row;
        }

        public void add(String title, int image, String description) {
            titles.add(title);
            images.add(image);
            descriptions.add(description);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    private String getPictrueName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        return timestamp+".jpg";
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camara) {
            Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES.concat("/PhotoHub/"));
            pictureDirectory.mkdirs();
            String picName = getPictrueName();
            File imageFile = new File(pictureDirectory, picName);
            Uri pictureUri = Uri.fromFile(imageFile);
            tmpOutputFile = imageFile.getAbsolutePath();
            it.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, pictureUri);
            startActivityForResult(it, PICK_PHOTO_REQUEST);
        } else if (id == R.id.nav_gallery) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), LOAD_PHOTO_REQUEST);
        } else if (id == R.id.nav_export) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case PICK_PHOTO_REQUEST:
                if(resultCode == RESULT_OK) {
                    if(tmpOutputFile == null)
                        break;
                    File imageFile = new File(tmpOutputFile);
                    String dateString = "";
                    try {
                        ExifInterface intf = new ExifInterface(imageFile.toString());
                        if(intf != null) {
                            dateString = intf.getAttribute(ExifInterface.TAG_DATETIME);
                            SimpleDateFormat dateParser = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                            SimpleDateFormat dateConverter = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
                            Date date = dateParser.parse(dateString);
                            dateString = dateConverter.format(date);
                        }
                    } catch(IOException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if(dateString.equals("")){
                        if(imageFile.exists()) {
                            long date = imageFile.lastModified();
                            Date fileData = new Date(date);
                            dateString = String.format("hh:mm:ss dd.MM.yyyy", fileData);
                        }
                    }
                    adapter.add(imageFile.getName(), 0, dateString);
                }
                break;
            case LOAD_PHOTO_REQUEST:
                if(resultCode == RESULT_OK) {

                }
                break;
        }
    }
}
