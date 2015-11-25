package de.dhbw_mannheim.photohub;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final int PICK_PHOTO_REQUEST = 1;
    private final int LOAD_PHOTO_REQUEST = 2;

    private ItemsAdapter adapter;   //provides all list entries - data and layout
    private ArrayList<String> selected = new ArrayList<>();     //contains all paths from selected entries
    private String tmpOutputFile;       //ensure that we keep our location where the system camera safe our photo
    private int sortBy = 0;         //represents the value how our list is sorted

    /**
     * Save current content before rebuild activity
     * @param extra - container
     */
    @Override
    protected void onSaveInstanceState(Bundle extra) {
        super.onSaveInstanceState(extra);
        extra.putString("tmpOutputFile", tmpOutputFile);
        ArrayList<ItemHolder> items = new ArrayList<>();
        for(int position = 0; position < adapter.getCount(); ++position) {
            items.add(adapter.getItem(position));
        }
        extra.putParcelableArrayList("adapter_items", items);
        extra.putStringArrayList("selected", selected);
        extra.putInt("sortBy", sortBy);
    }

    /**
     * Build activity
     * @param savedInstanceState - Old content
     */
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

        //initialize data
        if (savedInstanceState != null) {
            //rebuild activity - based on savedInstanceState
            tmpOutputFile = savedInstanceState.getString("tmpOutputFile");
            selected = savedInstanceState.getStringArrayList("selected");
            ArrayList<ItemHolder> itm = savedInstanceState.getParcelableArrayList("adapter_items");
            sortBy = savedInstanceState.getInt("sortBy");
            adapter = new ItemsAdapter(this, itm, sortBy);
        } else {
            //build new activity
            tmpOutputFile = "";
            adapter = new ItemsAdapter(this);
            //Add all files from our directory
            File files[] = PreDef.getPicturePath().listFiles();
            if(files != null){
                for (File file : files) {
                    adapter.add(file.getPath());
                }
            }
        }

        //initialize listView with our content provided by the adapter
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

        //initialize how to handle an clicked list item
        //on short click - open a new activity with the image
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openImage(position);
            }
        });

        //handle selection mode - (get in with long click)
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                //provide the abillity to select and deselect entries
                if (selected.contains(adapter.getItem(position).path)) {
                    selected.remove(adapter.getItem(position).path);
                } else {
                    selected.add(adapter.getItem(position).path);
                }
                mode.setTitle(selected.size() + " ausgewählt");
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                //initialize the select menu, displayed at the top
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.select_menu, menu);
                mode.setTitle(selected.size() + " ausgewählt");
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                //Declare what to do when the user press an option from the select menu
                switch (item.getItemId()) {
                    case R.id.select_delete_id:
                        //Remove all selected images from the list like from our memory
                        for (String path : selected) {
                            adapter.remove(path);
                            File file = new File(path);
                            file.delete();
                        }
                        Toast.makeText(getBaseContext(), selected.size() + " Bilder wurden gelöscht", Toast.LENGTH_SHORT).show();
                        selected.clear();
                        mode.finish();
                        return true;
                    case R.id.select_send_id:
                        //Send all selected images via an methode which the user choose, like email whatsapp facebook etc.
                        ArrayList<Uri> imageUris = new ArrayList<>();
                        for (String path : selected) {
                            File imageFile = new File(path);
                            //imageUris.add(PreDef.getImageContentUri(imageFile, getBaseContext()));
                            imageUris.add(Uri.fromFile(imageFile));
                        }
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
                        intent.putExtra(Intent.EXTRA_TEXT, "Von PhotoHub gesendet");
                        intent.setType("image/*");
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(intent, "Senden mit:"));
                        selected.clear();
                        mode.finish();
                        return true;
                    case R.id.select_export_id:
                        //Include all selected images in the system gallery
                        int count = selected.size();
                        for (String path : selected) {
                            try {
                                File file = new File(path);
                                MediaStore.Images.Media.insertImage(getContentResolver(), path, file.getName(), "Powered by PhotoHub");
                                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                Uri contentUri = Uri.fromFile(file);
                                mediaScanIntent.setData(contentUri);
                                sendBroadcast(mediaScanIntent);
                            } catch (FileNotFoundException e) {
                                Toast.makeText(getBaseContext(), "Bild konnte nicht hinzugefügt werden", Toast.LENGTH_SHORT).show();
                                --count;
                            }
                        }
                        Toast.makeText(getBaseContext(), count + " Bilder der Galerie hinzugefügt", Toast.LENGTH_SHORT).show();
                        selected.clear();
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                selected.clear();
            }
        });
    }

    private void openImage(int position) {
        Intent intent = new Intent(this, FullscreenActivity.class);
        intent.putExtra("image", adapter.getItem(position).path);
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
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camara) {
            //User clicked import from camera
            //Start system camera and save picture directly to a specified file (pictureUri)
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File imageFile = new File(PreDef.getPicturePath(), PreDef.getPictureName());
            Uri pictureUri = Uri.fromFile(imageFile);
            tmpOutputFile = imageFile.getAbsolutePath();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
            startActivityForResult(intent, PICK_PHOTO_REQUEST);
        } else if (id == R.id.nav_gallery) {
            //User clicked import from system gallery
            //Open system gallery and let the user select multiple images to copy in this app
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            intent.setType("image/*");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            }
            startActivityForResult(Intent.createChooser(intent, "Fotos auswählen"), LOAD_PHOTO_REQUEST);
        } else if (id == R.id.nav_title) {
            //Sort list by filename
            if(sortBy == 0)
                adapter.sortBy(sortBy = 1);
            else
                adapter.sortBy(sortBy = 0);
        } else if (id == R.id.nav_date) {
            //Sort list by image capture date
            if(sortBy == 2)
                adapter.sortBy(sortBy = 3);
            else
                adapter.sortBy(sortBy = 2);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Copy an image from the specified path to the app image directory
     * add it to our list if it was copied successfully
     * @param path      path to the image which should be copy
     */
    private void savePhoto(String path) {
        try {
            File source = new File(path);
            File destination = new File(PreDef.getPicturePath(), PreDef.getPictureName());
            if (source.exists()) {
                FileChannel src = new FileInputStream(source).getChannel();
                FileChannel dst = new FileOutputStream(destination).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                adapter.add(destination.getPath());
            }
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), "Bild konnte nicht geladen werden", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICK_PHOTO_REQUEST:
                if (resultCode == RESULT_OK) {
                    if (tmpOutputFile == null)
                        break;
                    //add the image taken by the system camera to our list
                    adapter.add(tmpOutputFile);
                }
                break;
            case LOAD_PHOTO_REQUEST:
                if (resultCode == RESULT_OK) {
                    if (data.getData() != null) {
                        //get one image from the gallery - so save it
                        savePhoto(PreDef.getPath(getBaseContext(), data.getData()));
                    } else {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                            //we got multiple images, so save one by one
                            ClipData items = data.getClipData();
                            for (int i = 0; i < items.getItemCount(); ++i) {
                                savePhoto(PreDef.getPath(getBaseContext(), items.getItemAt(i).getUri()));
                            }
                        }
                    }
                }
                break;
        }
    }
}
