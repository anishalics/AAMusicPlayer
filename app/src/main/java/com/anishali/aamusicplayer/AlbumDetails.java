package com.anishali.aamusicplayer;

import static com.anishali.aamusicplayer.MainActivity.musicFiles;

import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class AlbumDetails extends AppCompatActivity {
    RecyclerView recyclerView;
    ImageView albumPhoto;
    String albumName;
    ArrayList<MusicFiles> albumSongs = new ArrayList<>();
    AlbumDetailsAdapter albumDetailsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_details);

        getWindow().setStatusBarColor(getResources().getColor(R.color.DarkOrange));

        recyclerView = findViewById(R.id.recyclerView);
        albumPhoto = findViewById(R.id.albumPhoto);
        albumName = getIntent().getStringExtra("albumName");

        for (int i = 0; i < musicFiles.size(); i++) {
            assert albumName != null;
            if (albumName.equals(musicFiles.get(i).getAlbum()))
            {
                albumSongs.add(musicFiles.get(i));
            }
        }

        byte[] image = getAlbumArt(albumSongs.get(0).getPath());

        if (image != null)
        {
            Glide.with(this)
                    .load(image)
                    .into(albumPhoto);
        }
        else
        {
            Glide.with(this)
                    .load(R.drawable.playingbg)
                    .into(albumPhoto);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!(albumSongs.size() < 1))
        {
            albumDetailsAdapter = new AlbumDetailsAdapter(this, albumSongs);
            recyclerView.setAdapter(albumDetailsAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this,
                    RecyclerView.VERTICAL, false));
        }
    }

    private byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(uri);
            byte[] art = retriever.getEmbeddedPicture();

            return art;
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                retriever.release();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return null; // Return null in case of failure
    }
}