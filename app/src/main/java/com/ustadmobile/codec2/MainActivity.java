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



        byte[] boutBuf = new byte[2];

        try {
            InputStream is = asMgr.open("demo.c2");

            long con = Codec2.create(Codec2.CODEC2_MODE_1300);
            char[] codec2InBuf = new char[Codec2.getBitsSize(con)];
            byte[] codec2InBufBytes = new byte[codec2InBuf.length];
            ByteBuffer buffer = ByteBuffer.allocate(Codec2.getSamplesPerFrame(con) * 2 * 2);
            buffer.order(ByteOrder.nativeOrder());
            while (is.read(codec2InBufBytes) == codec2InBufBytes.length) {
                //should be read from stream as character array of this length. The c2dec method reads in as char
                for (int i = 0; i < codec2InBufBytes.length; i++) {
                    codec2InBuf[i] = (char) codec2InBufBytes[i];
                }
                short[] rawAudioOutBuf = new short[Codec2.getSamplesPerFrame(con) * 2];

                Codec2.decode(con, rawAudioOutBuf, codec2InBuf);

                buffer.clear();
                for (int i = 0; i < rawAudioOutBuf.length; i++) {
                    buffer.putShort(rawAudioOutBuf[i]);
                }
                track.write(buffer.array(), 0, buffer.capacity());
            }

            Codec2.destroy(con);

            System.out.println();


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
