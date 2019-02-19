#include <jni.h>
#include <stdlib.h>
#include "codec2/codec2_fdmdv.h"
#include "codec2/codec2.h"
#include "codec2/varicode.h"

namespace Java_com_ustadmobile_codec2_Codec2 {

    struct Context {
        struct CODEC2 *c2;
        short *buf;
        unsigned char *bits;
        short samples;
        short nbyte;
    };

    static Context *getContext(jlong jp) {
        unsigned long p = (unsigned long) jp;
        Context *con;
        con = (Context *) p;
        return con;
    }

    static jlong create(JNIEnv *env, jclass clazz, int mode) {
        struct Context *con;
        con = (struct Context *) malloc(sizeof(struct Context));
        struct CODEC2 *c;
        c = codec2_create(mode);
        con->c2 = c;
        con->samples = codec2_samples_per_frame(c);
        con->buf = (short *) malloc(2 * sizeof(short) * con->samples);
        int nbit = codec2_bits_per_frame(con->c2);
        con->nbyte = (nbit + 7) / 8;
        con->bits = (unsigned char *) malloc(con->nbyte * sizeof(char));
        unsigned long pv = (unsigned long) con;
        return pv;
    }

    static jint c2spf(JNIEnv *env, jclass clazz, jlong n) {
        Context *con = getContext(n);
        return con->samples;
    }

    static jint c2bits(JNIEnv *env, jclass clazz, jlong n) {
        Context *con = getContext(n);
        return con->nbyte;
    }

    static jint destroy(JNIEnv *env, jclass clazz, jlong n) {
        Context *con = getContext(n);
        codec2_destroy(con->c2);
        free(con->bits);
        free(con->buf);
        free(con);
        return 0;
    }

    static jlong encode(JNIEnv *env, jclass clazz, jlong n,
                        jshortArray inputBuffer,
                        jcharArray outputBits) {
        Context *con = getContext(n);
        int i;
        jshort *jbuf = env->GetShortArrayElements(inputBuffer, 0);
        for (i = 0; i < con->samples; i++) {
            // Downsampling to F/2
            short v = (short) jbuf[i * 2];
            con->buf[i] = v;
        }
        env->ReleaseShortArrayElements(inputBuffer, jbuf, 0);
        env->DeleteLocalRef(inputBuffer);
        codec2_encode(con->c2, con->bits, con->buf);
        jchar *jbits = env->GetCharArrayElements(outputBits, 0);
        for (i = 0; i < con->nbyte; i++) {
            jbits[i] = con->bits[i];
        }
        env->ReleaseCharArrayElements(outputBits, jbits, 0);
        env->DeleteLocalRef(outputBits);
        return 0;
    }

    static jlong decode(JNIEnv *env, jclass clazz, jlong n,
                        jshortArray inputBuffer,
                        jcharArray outputBits) {
        int i;
        Context *con = getContext(n);

        jchar *jbits = env->GetCharArrayElements(outputBits, 0);
        for (i = 0; i < con->nbyte; i++) {
            con->bits[i] = jbits[i];
        }
        env->ReleaseCharArrayElements(outputBits, jbits, 0);
        env->DeleteLocalRef(outputBits);

        codec2_decode(con->c2, con->buf, con->bits);

        jshort *jbuf = env->GetShortArrayElements(inputBuffer, 0);
        for (i = 0; i < con->samples; i++) {
            // Upsamling F*2
            jbuf[i * 2] = con->buf[i];
            jbuf[i * 2 + 1] = con->buf[i];
        }
        env->ReleaseShortArrayElements(inputBuffer, jbuf, 0);
        env->DeleteLocalRef(inputBuffer);
        return 0;
    }

    static JNINativeMethod method_table[] = {
            {"create",             "(I)J",     (void *) create},
            {"getSamplesPerFrame", "(J)I",     (void *) c2spf},
            {"getBitsSize",        "(J)I",     (void *) c2bits},
            {"destroy",            "(J)I",     (void *) destroy},
            {"encode",             "(J[S[C)J", (void *) encode},
            {"decode",             "(J[S[C)J", (void *) decode}};

}

using namespace Java_com_ustadmobile_codec2_Codec2;

extern "C" jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    } else {
        jclass clazz = env->FindClass("com/ustadmobile/codec2/Codec2");
        if (clazz) {
            jint ret = env->RegisterNatives(clazz, method_table,
                                            sizeof(method_table) / sizeof(method_table[0]));
            env->DeleteLocalRef(clazz);
            return ret == 0 ? JNI_VERSION_1_6 : JNI_ERR;
        } else {
            return JNI_ERR;
        }
    }
}