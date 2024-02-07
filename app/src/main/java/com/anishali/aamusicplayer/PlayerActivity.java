package com.anishali.aamusicplayer;

import static com.anishali.aamusicplayer.AlbumDetailsAdapter.albumFiles;
import static com.anishali.aamusicplayer.MainActivity.isNegative;
import static com.anishali.aamusicplayer.MainActivity.repeatBoolean;
import static com.anishali.aamusicplayer.MainActivity.shuffleBoolean;
import static com.anishali.aamusicplayer.MusicAdapter.mFiles;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class PlayerActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    private TextView song_name, artist_name, duration_played, duration_total;
    private ImageView cover_art, nextBtn, prevBtn, backBtn, shuffleBtn, repeatBtn;

    private FloatingActionButton btn_play_pause;

    private SeekBar seekBar;

    private int position = -1;

    public static ArrayList<MusicFiles> listSongs = new ArrayList<>();

    public static Uri uri;

    public static MediaPlayer mediaPlayer;

    private Handler handler = new Handler();

    boolean isPlaying = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_player);

        Objects.requireNonNull(getSupportActionBar()).hide();

        initViews();

        if (repeatBoolean)
        {
            repeatBtn.setImageResource(R.drawable.ic_repeat_on);
        }
        else if (shuffleBoolean)
        {
            shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
        }

        getIntentMethod();

        metaData(uri);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser)
                {
                    mediaPlayer.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null)
                {
                    int mCurrentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                    duration_played.setText(formattedTime(mCurrentPosition));

                    if (isNegative)
                    {
                        int totalDuration = mediaPlayer.getDuration() / 1000;
                        duration_total.setText("-" + formattedTime(totalDuration - mCurrentPosition));
                    }
                }

                handler.postDelayed(this, 1000);
            }
        });

        shuffleBtn.setOnClickListener(v -> {
            if (!repeatBoolean)
            {
                if (shuffleBoolean)
                {
                    shuffleBoolean = false;
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle_off);
                }
                else
                {
                    shuffleBoolean = true;
                    shuffleBtn.setImageResource(R.drawable.ic_shuffle_on);
                }
            }
        });

        repeatBtn.setOnClickListener(v -> {
            if (!shuffleBoolean)
            {
                if (repeatBoolean)
                {
                    repeatBoolean = false;
                    repeatBtn.setImageResource(R.drawable.ic_repeat_off);
                }
                else
                {
                    repeatBoolean = true;
                    repeatBtn.setImageResource(R.drawable.ic_repeat_on);
                }
            }
        });

        btn_play_pause.setOnClickListener(v -> btn_play_pauseClicked());

        prevBtn.setOnClickListener(v -> btn_prevClicked());

        nextBtn.setOnClickListener(v -> btn_nextClicked());

        backBtn.setOnClickListener(v -> finish());

        duration_total.setOnClickListener(v -> {
            if (isNegative)
            {
                isNegative = false;
                duration_total.setText(formattedTime(mediaPlayer.getDuration() / 1000));
            }
            else {
                isNegative = true;
            }
        });
    }

    private void setFullScreen()
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public void btn_prevClicked() {
        //Deciding the value of position based on user's wish
        if (shuffleBoolean)
        {
            position = getRandom(listSongs.size() - 1);
        }
        else if (!repeatBoolean)
        {
            position = position - 1 == -1 ? listSongs.size() - 1 : position - 1;
        }
        //else position remains the same

        mediaPlayer.stop();
        mediaPlayer.release();

        uri = Uri.parse(listSongs.get(position).getPath());
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        metaData(uri);

        song_name.setText(listSongs.get(position).getTitle());
        artist_name.setText(listSongs.get(position).getArtist());

        seekBar.setMax(mediaPlayer.getDuration() / 1000);

        mediaPlayer.setOnCompletionListener(this);

        if (isPlaying)
        {
            btn_play_pause.setImageResource(R.drawable.ic_pause);
            mediaPlayer.start();
        }
        else
        {
            btn_play_pause.setImageResource(R.drawable.ic_play);
        }
    }

    public void btn_nextClicked() {
        //Deciding the value of position based on user's wish
        if (shuffleBoolean)
        {
            position = getRandom(listSongs.size() - 1);
        }
        else if (!repeatBoolean)
        {
            position = (position + 1) % listSongs.size();
        }
        //else position remains the same

        mediaPlayer.stop();
        mediaPlayer.release();

        uri = Uri.parse(listSongs.get(position).getPath());
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        metaData(uri);

        song_name.setText(listSongs.get(position).getTitle());
        artist_name.setText(listSongs.get(position).getArtist());

        seekBar.setMax(mediaPlayer.getDuration() / 1000);

        mediaPlayer.setOnCompletionListener(this);

        if (isPlaying)
        {
            btn_play_pause.setImageResource(R.drawable.ic_pause);
            mediaPlayer.start();
        }
        else
        {
            btn_play_pause.setImageResource(R.drawable.ic_play);
        }
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i+1);
    }

    public void btn_play_pauseClicked() {
        if (mediaPlayer.isPlaying())
        {
            isPlaying = false;

            btn_play_pause.setImageResource(R.drawable.ic_play);
            mediaPlayer.pause();
        }
        else
        {
            isPlaying = true;

            btn_play_pause.setImageResource(R.drawable.ic_pause);
            mediaPlayer.start();
        }
    }

    private String formattedTime(int mCurrentPosition) {
        String totalOut = "";
        String totalNew = "";
        String seconds = String.valueOf(mCurrentPosition % 60);
        String minutes = String.valueOf(mCurrentPosition / 60);

        totalOut = minutes + ":" + seconds;
        totalNew = minutes + ":" + "0" + seconds;

        if (seconds.length() == 1)
        {
            return totalNew;
        }

        return totalOut;
    }

    private void getIntentMethod() {
        position = getIntent().getIntExtra("position", -1);
        String sender = getIntent().getStringExtra("sender");

        if (sender != null && sender.equals("albumDetails"))
        {
            listSongs = albumFiles;
        }
        else {
            listSongs = mFiles;
        }

        if (listSongs != null)
        {
            btn_play_pause.setImageResource(R.drawable.ic_pause);
            uri = Uri.parse(listSongs.get(position).getPath());
        }

        if (mediaPlayer != null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();

        seekBar.setProgress(0);
        seekBar.setMax(mediaPlayer.getDuration() / 1000);

        mediaPlayer.setOnCompletionListener(this);
    }

    private void initViews() {
        song_name = findViewById(R.id.song_name);
        artist_name = findViewById(R.id.song_artist);
        duration_played = findViewById(R.id.durationPlayed);
        duration_total = findViewById(R.id.durationTotal);
        cover_art = findViewById(R.id.cover_art);
        nextBtn = findViewById(R.id.id_next);
        prevBtn = findViewById(R.id.id_prev);
        backBtn = findViewById(R.id.back_btn);
        shuffleBtn = findViewById(R.id.id_shuffle);
        repeatBtn = findViewById(R.id.id_repeat);
        btn_play_pause = findViewById(R.id.play_pause);
        seekBar = findViewById(R.id.seekBar);
    }

    private void metaData(Uri uri)
    {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());

        song_name.setText(listSongs.get(position).getTitle());
        artist_name.setText(listSongs.get(position).getArtist());

        int durationTotal = Integer.parseInt(listSongs.get(position).getDuration()) / 1000;
        if (isNegative)
        {
            duration_total.setText("-" + formattedTime((durationTotal - mediaPlayer.getCurrentPosition()/1000)));
        }
        else
        {
            duration_total.setText(formattedTime(durationTotal));
        }

        byte[] art = retriever.getEmbeddedPicture();
        Bitmap bitmap;

        if (art != null)
        {
            RelativeLayout relativeLayout = findViewById(R.id.layout_top_btn);
            relativeLayout.setBackgroundResource(R.drawable.gradient_bg);

            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);

            ImageAnimation(this, cover_art, bitmap);

            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable Palette palette) {
                    Palette.Swatch swatch = palette.getDominantSwatch();

                    if (swatch != null)
                    {
                        ImageView gradient = findViewById(R.id.imageViewGradient);
                        RelativeLayout mContainer = findViewById(R.id.mContainer);
                        gradient.setBackgroundResource(R.drawable.gradient_bg);
                        mContainer.setBackgroundResource(R.drawable.main_bg);

                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(), 0x00000000});
                        gradient.setBackground(gradientDrawable);

                        GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(), swatch.getRgb()});
                        mContainer.setBackground(gradientDrawableBg);

                        song_name.setTextColor(swatch.getTitleTextColor());
                        artist_name.setTextColor(swatch.getBodyTextColor());
                    }
                    else
                    {
                        ImageView gradient = findViewById(R.id.imageViewGradient);
                        RelativeLayout mContainer = findViewById(R.id.mContainer);
                        gradient.setBackgroundResource(R.drawable.gradient_bg);
                        mContainer.setBackgroundResource(R.drawable.main_bg);

                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000, 0x00000000});
                        gradient.setBackground(gradientDrawable);

                        GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{0xff000000, 0xff000000});
                        mContainer.setBackground(gradientDrawableBg);

                        song_name.setTextColor(Color.WHITE);
                        artist_name.setTextColor(Color.DKGRAY);
                    }
                }
            });
        }
        else
        {
            Glide.with(getApplicationContext())
                    .asBitmap()
                    .load(R.drawable.playingbg)
                    .into(cover_art);

            ImageView gradient = findViewById(R.id.imageViewGradient);
            RelativeLayout mContainer = findViewById(R.id.mContainer);
            gradient.setBackgroundResource(R.drawable.gradient_bg);
            mContainer.setBackgroundResource(R.drawable.no_bg);

            RelativeLayout relativeLayout = findViewById(R.id.layout_top_btn);
            relativeLayout.setBackgroundResource(R.drawable.no_bg);

            song_name.setTextColor(Color.WHITE);
            artist_name.setTextColor(Color.WHITE);
        }
    }

    public void ImageAnimation(final Context context, final ImageView imageView, final Bitmap bitmap)
    {
        Animation animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        Animation animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);

        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                imageView.startAnimation(animIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        imageView.startAnimation(animOut);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        btn_nextClicked();
    }
}