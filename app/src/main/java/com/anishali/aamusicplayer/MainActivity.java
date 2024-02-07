package com.anishali.aamusicplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import android.Manifest;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{
    private static final int REQUEST_CODE = 1;

    public static ArrayList<MusicFiles> musicFiles;

    static boolean shuffleBoolean = false, repeatBoolean = false;

    //Variable that tracks if the user wants to see duration of song or remaining duration of song e.g. 4:20 or -4:20 as in application like MX player
    static boolean isNegative = false;

    static ArrayList<MusicFiles> albums = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request permission to access the files
        permission();

        //Changing Status Bar color is possible in LOLLIPOP and it's above versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.DarkOrange));
        }
    }

    // Method to request file access permission
    private void permission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, REQUEST_CODE);
            }
            else {
                musicFiles = getAllAudio(this);

                initViewPager();
            }
        }
        else
        {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
            else {
                musicFiles = getAllAudio(this);

                initViewPager();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                musicFiles = getAllAudio(this);
                initViewPager();
            }
            else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_MEDIA_AUDIO}, REQUEST_CODE);
                }
                else
                {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
                }
            }
        }
    }

    //Method to initialize ViewPager
    private void initViewPager() {
        ViewPager viewPager = findViewById(R.id.viewpager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragments(new SongsFragment(), "Songs");
        viewPagerAdapter.addFragments(new AlbumFragment(), "Albums");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    // Nested class for ViewPager adapter
    public static class ViewPagerAdapter extends FragmentPagerAdapter{
        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        public ViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);

            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        // Method to add fragments to the adapter
        void addFragments(Fragment fragment, String title){
            fragments.add(fragment);
            titles.add(title);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    // Method to retrieve all audio files from the device
    public ArrayList<MusicFiles> getAllAudio(Context context)
    {
        ArrayList<String> duplicate = new ArrayList<>();

        ArrayList<MusicFiles> tempAudioList = new ArrayList<>();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID
        };

        Cursor cursor = context.getContentResolver().query(uri, projection,
                null, null, null);


        if (cursor != null && cursor.getCount() > 0)
        {
            while (cursor.moveToNext())
            {
                String album = cursor.getString(0);
                String title = cursor.getString(1);
                String duration = cursor.getString(2);
                String path = cursor.getString(3);
                String artist = cursor.getString(4);
                String id = cursor.getString(5);

                MusicFiles musicFiles = new MusicFiles(path, title, artist, album, duration, id);

                if (artist == null || artist.isEmpty())
                    artist = "<unknown>";

                //the if condition solves the crashing problem
                if (duration!= null) {
                    tempAudioList.add(musicFiles);

                    if (!duplicate.contains(album)) {
                        albums.add(musicFiles);
                        duplicate.add(album);
                    }
                }
            }

            cursor.close();
        }

        return tempAudioList;
    }

    //Setting up the search option
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        MenuItem menuItem = menu.findItem(R.id.search_option);
        SearchView searchView = (SearchView) menuItem.getActionView();
        assert searchView != null;
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    // Indicates that no specific action is taken when the user submits a query in the search view.
    // The method returns false to allow the default behavior to continue.
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    // Method to filter songs based on user input
    @Override
    public boolean onQueryTextChange(String newText) {
        if (musicFiles.size() >= 1)
        {
            String userInput = newText.toLowerCase();
            ArrayList<MusicFiles> myFiles = new ArrayList<>();
            ArrayList<MusicFiles> newAlbumFiles = new ArrayList<>();


            for (MusicFiles song : musicFiles)
            {
                if (song.getTitle().toLowerCase().contains(userInput))
                {
                    myFiles.add(song);
                }
            }

            for (MusicFiles albumSong : albums)
            {
                if (albumSong.getAlbum().toLowerCase().contains(userInput))
                {
                    newAlbumFiles.add(albumSong);
                }
            }

            // Update the list of songs in the SongsFragment
            SongsFragment.musicAdapter.updateList(myFiles);
            AlbumFragment.albumAdapter.updateList(newAlbumFiles);

            return true;
        }

        return false;
    }
}