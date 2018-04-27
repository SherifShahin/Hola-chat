package com.downloader.hola;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.gms.tasks.Task;

public class Play_Video extends AppCompatActivity {

    private VideoView videoView;

    private ProgressBar bufferProgress;
    private ImageView play_bt;
    private TextView current_time;
    private TextView duration_time;
    private ProgressBar video_progress;
    private boolean isplaying=false;

    private int currentTime=0;
    private int durationTime=0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play__video);

        String video_uri=getIntent().getStringExtra("video_uri");

        Uri videourl = Uri.parse(video_uri);

        videoView=(VideoView) findViewById(R.id.video_play);
        bufferProgress=(ProgressBar) findViewById(R.id.buffer_progress);
        video_progress=(ProgressBar) findViewById(R.id.video_progress);
        video_progress.setMax(100);
        play_bt=(ImageView) findViewById(R.id.play_bt);
        current_time=(TextView) findViewById(R.id.current_time);
        duration_time=(TextView) findViewById(R.id.duration_time);


        videoView.setVideoURI(videourl);
        videoView.requestFocus();

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp)
            {
                durationTime=mp.getDuration()/1000;

                String durationString =String.format("%02d:%02d",durationTime / 60 , durationTime % 60);

                duration_time.setText(durationString);
            }
        });

        videoView.start();
        isplaying=true;
        new videoprogress().execute();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
        {
            videoView.setOnInfoListener(new MediaPlayer.OnInfoListener()
            {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra)
                {
                    if(what == mp.MEDIA_INFO_BUFFERING_START)
                    {
                        bufferProgress.setVisibility(View.VISIBLE);
                    }
                    else if(what == mp.MEDIA_INFO_BUFFERING_END)
                    {
                        bufferProgress.setVisibility(View.INVISIBLE);
                    }
                    return false;
                }
            });
        }

        play_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(isplaying)
                {
                    videoView.pause();
                    isplaying=false;
                    play_bt.setImageResource(R.mipmap.video_play_bt);
                }
                else if(!isplaying)
                {
                    videoView.start();
                    isplaying=true;
                    play_bt.setImageResource(R.drawable.ic_pause_white_24dp);
                }

            }
        });


    }

    @Override
    protected void onStop() {
        super.onStop();
        isplaying=false;
    }

    public class videoprogress extends AsyncTask <Void ,Integer,Void>
    {

        @Override
        protected Void doInBackground(Void... params)
        {
            if(isplaying) {
                do {
                    currentTime = videoView.getCurrentPosition() / 1000;
                    try {
                        int current = currentTime * 100 / durationTime;
                        publishProgress(current);
                    } catch (Exception e) {

                    }
                } while (video_progress.getProgress() <= 100);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            video_progress.setProgress(values[0]);

            String currentString =String.format("%02d:%02d",values[0] / 60, values[0] % 60);

            current_time.setText(currentString);
        }
    }
}
