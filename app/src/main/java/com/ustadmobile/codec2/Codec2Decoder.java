package com.ustadmobile.codec2;

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
 * int intSize = AudioTrack.getMinBufferSize(16000,
 *                 AudioFormat.CHANNEL_CONFIGURATION_MONO,
 *                 AudioFormat.ENCODING_PCM_16BIT);
 *
 * AudioTrack track = new AudioTrack(
 *                 AudioManager.STREAM_MUSIC,
 *                 16000,
 *                 AudioFormat.CHANNEL_OUT_MONO,
 *                 AudioFormat.ENCODING_PCM_16BIT,
 *                 intSize,
 *                 AudioTrack.MODE_STREAM);
 */
public class Codec2Decoder {

    private InputStream input;

    private long codec2Con;

    private char[] codec2InBuf;

    private byte[] codec2InBufBytes;

    private short[] rawAudioOutBuf;

    private ByteBuffer rawAudioOutBytesBuffer;

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

        codec2InBuf = new char[Codec2.getBitsSize(codec2Con)];
        codec2InBufBytes = new byte[codec2InBuf.length];

        //multiply by two because the JNI will upsample from 8Khz to 16Khz
        rawAudioOutBuf = new short[Codec2.getSamplesPerFrame(codec2Con) * 2];

        //multiply by two to handle upsampling, and two to handle the fact that output is in shorts (2 bytes)
        rawAudioOutBytesBuffer = ByteBuffer.allocate(Codec2.getSamplesPerFrame(codec2Con) * 2 * 2);
        rawAudioOutBytesBuffer.order(ByteOrder.nativeOrder());
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
            //should be read from stream as character array of this length. The c2dec method reads in as char
            for (int i = 0; i < codec2InBufBytes.length; i++) {
                codec2InBuf[i] = (char) codec2InBufBytes[i];
            }

            Codec2.decode(codec2Con, rawAudioOutBuf, codec2InBuf);

            rawAudioOutBytesBuffer.clear();
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
