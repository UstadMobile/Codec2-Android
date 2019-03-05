package com.ustadmobile.codec2;

import android.support.annotation.RequiresApi;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Helper class to manage decoding Codec2 audio into something that can be played in Android.  The
 * decoded data is 16Khz MONO PCM 16Bit encoded.
 *
 * This can be played back using something along the lines of the following:
 *
 * int minBufferSize = AudioTrack.getMinBufferSize(8000,
 *                 AudioFormat.CHANNEL_CONFIGURATION_MONO,
 *                 AudioFormat.ENCODING_PCM_16BIT);
 *
 * AudioTrack track = new AudioTrack(
 *                 AudioManager.STREAM_MUSIC,
 *                 8000,
 *                 AudioFormat.CHANNEL_OUT_MONO,
 *                 AudioFormat.ENCODING_PCM_16BIT,
 *                 Math.max(minBufferSize, decoder.getOutputBufferSize(),
 *                 AudioTrack.MODE_STREAM);
 */
@RequiresApi(23)
public class Codec2Decoder {

    private InputStream input;

    private long codec2Con;

    private byte[] codec2InBufBytes;

    private short[] rawAudioOutBuf;

    private ByteBuffer rawAudioOutBytesBuffer;

    private int samplesPerFrame;

    /**
     * Create and allocate new decoder
     *
     * @param input InputStream
     *
     * @param codec2Mode As per Codec2.MODE flags
     */
    public Codec2Decoder(InputStream input, int codec2Mode) {
        this.input = input;
        codec2Con = Codec2.create(codec2Mode);

        int nBytes = Codec2.getBitsSize(codec2Con);
        int nByte = (nBytes + 7) / 8;
        codec2InBufBytes = new byte[nBytes];

        rawAudioOutBuf = new short[Codec2.getSamplesPerFrame(codec2Con)];

        //multiply by two to handle the fact that output is in shorts (2 bytes)
        samplesPerFrame =Codec2.getSamplesPerFrame(codec2Con);
        rawAudioOutBytesBuffer = ByteBuffer.allocate(samplesPerFrame * 2);

        rawAudioOutBytesBuffer.order(ByteOrder.nativeOrder());
    }

    public int getOutputBufferSize() {
        return rawAudioOutBytesBuffer.capacity();
    }

    public int getSamplesPerFrame() {
        return samplesPerFrame;
    }

    public int getInputBufferSize() {
        return codec2InBufBytes.length;
    }

    /**
     * Read and decode a frame of audio.
     *
     * @return ByteBuffer containing the decoded audio, or null if there is nothing left
     *
     * @throws IOException if an IOException occurs in the underlying system
     */
    public ByteBuffer readFrame() throws IOException {
        int bytesRead = input.read(codec2InBufBytes);
        if(bytesRead == codec2InBufBytes.length) {
            Codec2.decode(codec2Con, rawAudioOutBuf, codec2InBufBytes);

            rawAudioOutBytesBuffer.rewind();
            for (int i = 0; i < rawAudioOutBuf.length; i++) {
                rawAudioOutBytesBuffer.putShort(rawAudioOutBuf[i]);
            }


            return rawAudioOutBytesBuffer;
        }else {
            return null;
        }
    }

    /**
     * Release resources held by the JNI system. Must be called once finished to release resources.
     */
    public void destroy() {
        Codec2.destroy(codec2Con);
    }

}
