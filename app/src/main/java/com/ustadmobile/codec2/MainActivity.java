package com.ustadmobile.codec2;

import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AssetManager asMgr = getApplicationContext().getAssets();
        int intSize = AudioTrack.getMinBufferSize(16000,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        AudioTrack track = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                16000,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                intSize,
                AudioTrack.MODE_STREAM);
        track.play();
        try {
            InputStream is = asMgr.open("video1-1300.c2");
            Codec2Decoder codec2 = new Codec2Decoder(is, Codec2.CODEC2_MODE_1300);

            while (true) {
                ByteBuffer buffer = codec2.readFrame();
                if (buffer != null) {
                    track.write(buffer.array(), 0, buffer.capacity());
                } else {
                    break;
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
