package com.ustadmobile.codec2.demo;

import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ustadmobile.codec2.Codec2;
import com.ustadmobile.codec2.Codec2Decoder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import prototypemanager.ustadmobile.com.appcodec2demo.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(() -> {
            AssetManager asMgr = getApplicationContext().getAssets();
            int intSize = AudioTrack.getMinBufferSize(8000,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

            AudioTrack track = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    8000,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    intSize * 3,
                    AudioTrack.MODE_STREAM);
            track.play();
            Codec2Decoder codec2 = null;
            try {
                InputStream is = asMgr.open("audio.c2");
                is.skip(Codec2.CODEC2_FILE_HEADER_SIZE);
                codec2 = new Codec2Decoder(is, Codec2.CODEC2_MODE_3200);

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
            }finally  {
                track.release();
                if(codec2 != null)
                    codec2.destroy();
            }
        }).start();
    }

}
