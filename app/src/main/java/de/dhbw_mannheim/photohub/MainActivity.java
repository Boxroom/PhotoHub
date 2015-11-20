package de.dhbw_mannheim.photohub;

import android.content.ClipData;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
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

    private ItemsAdapter adapter;
    private ArrayList<String> selected = new ArrayList<>();
    private String tmpOutputFile;
    private int sortBy = 0;

    /**
     * Save current content before rebuild activity
     * @param extra
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
     *
     * @param savedInstanceState
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


        ListView listView = (ListView) findViewById(R.id.listView);

        if (savedInstanceState != null) {
            tmpOutputFile = savedInstanceState.getString("tmpOutputFile");
            selected = savedInstanceState.getStringArrayList("selected");
            ArrayList<ItemHolder> itm = savedInstanceState.getParcelableArrayList("adapter_items");
            sortBy = savedInstanceState.getInt("sortBy");
            adapter = new ItemsAdapter(this, itm, sortBy);
        } else {
            tmpOutputFile = "";
            adapter = new ItemsAdapter(this);
            File files[] = PreDef.getPicturePath().listFiles();
            if(files != null){
                for (File file : files) {
                    adapter.add(file.getPath());
                }
            }
        }
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openImage(position);
            }
        });

        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                if (selected.contains(adapter.getItem(position).path)) {
                    selected.remove(adapter.getItem(position).path);
                } else {
                    selected.add(adapter.getItem(position).path);
                }
                mode.setTitle(selected.size() + " ausgewählt");
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
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
                switch (item.getItemId()) {
                    case R.id.select_delete_id:
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
                        ArrayList<Uri> imageUris = new ArrayList<>();
                        for (String path : selected) {
                            File imageFile = new File(path);
                            imageUris.add(getImageContentUri(imageFile));
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

    private Uri getImageContentUri(File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            cursor.close();
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
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
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File imageFile = new File(PreDef.getPicturePath(), PreDef.getPictureName());
            Uri pictureUri = Uri.fromFile(imageFile);
            tmpOutputFile = imageFile.getAbsolutePath();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
            startActivityForResult(intent, PICK_PHOTO_REQUEST);
        } else if (id == R.id.nav_gallery) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            intent.setType("image/*");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            }
            startActivityForResult(Intent.createChooser(intent, "Fotos auswählen"), LOAD_PHOTO_REQUEST);
        } else if (id == R.id.nav_title) {
            if(sortBy == 0)
                adapter.sortBy(sortBy = 1);
            else
                adapter.sortBy(sortBy = 0);
        } else if (id == R.id.nav_date) {
            if(sortBy == 2)
                adapter.sortBy(sortBy = 3);
            else
                adapter.sortBy(sortBy = 2);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

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
                    adapter.add(tmpOutputFile);
                }
                break;
            case LOAD_PHOTO_REQUEST:
                if (resultCode == RESULT_OK) {
                    if (data.getData() != null) {
                        savePhoto(PreDef.getPath(getBaseContext(), data.getData()));
                    } else {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
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
