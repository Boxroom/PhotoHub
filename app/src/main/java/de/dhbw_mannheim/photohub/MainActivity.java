package de.dhbw_mannheim.photohub;

import android.content.Intent;
import android.graphics.Bitmap;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static final int PICK_PHOTO_REQUEST = 1;
    static final int LOAD_PHOTO_REQUEST = 2;

    private ItemsAdapter adapter;
    private String tmpOutputFile;

    @Override
    protected void onSaveInstanceState(Bundle extra) {
        super.onSaveInstanceState(extra);
        extra.putString("tmpOutputFile", tmpOutputFile);
        extra.putParcelableArrayList("adapter_bitmaps", adapter.bitmaps);
        extra.putStringArrayList("adapter_paths", adapter.paths);
        extra.putStringArrayList("adapter_titles", adapter.titles);
        extra.putStringArrayList("adapter_descriptions", adapter.descriptions);
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


        if (savedInstanceState != null) {
            tmpOutputFile = savedInstanceState.getString("tmpOutputFile");
            ArrayList<Bitmap> ad = savedInstanceState.getParcelableArrayList("adapter_bitmaps");
            ArrayList<String> tit = savedInstanceState.getStringArrayList("adapter_titles");
            ArrayList<String> path = savedInstanceState.getStringArrayList("adapter_paths");
            ArrayList<String> desc = savedInstanceState.getStringArrayList("adapter_descriptions");
            adapter = new ItemsAdapter(this, ad, tit, path, desc);
        }else{
            tmpOutputFile = "";
            adapter = new ItemsAdapter(this);
            File[] files = getPicturePath().listFiles();
            for (File file : files) {
                adapter.add(file.getPath());
            }
        }
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openImage(position);
            }
        });
    }

    private void openImage(int position){
        Intent intent = new Intent(this, FullscreenActivity.class);
        intent.putExtra("image", adapter.paths.get(position));
        startActivity(intent);
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
        //int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    private File getPicturePath() {
        String subdirectory = "/PhotoHub/";
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES.concat(subdirectory));
        path.mkdirs();
        return path;
    }

    private String getPictureName() {
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
            File pictureDirectory = getPicturePath();
            String picName = getPictureName();
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
                    adapter.add(tmpOutputFile);
                }
                break;
            case LOAD_PHOTO_REQUEST:
                if(resultCode == RESULT_OK) {
                    String[] imagesPath = null;
                    for (String path : imagesPath){
                        adapter.add(path);
                    }
                }
                break;
        }
    }
}
