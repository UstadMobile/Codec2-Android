# Codec2 Android Library

This is an early version of an Android library for Codec2. It will compile Codec2's original source 
code for the specified android native platforms (ABIs eg x86_64, armeabi-v7a, etc) using Gradle and
compile an aar file. It provides JNI intefaces that can be used to access decode Codec2 files.

## Credits

This is an Android wrapper for Codec2 developed by David Rowe et. al, used under the LGPL license.
See [http://www.rowetel.com/?page_id=452](http://www.rowetel.com/?page_id=452). This project also
uses elements of a previous wrapper by Ahmed Obaidi used under the LGPL license. See 
[https://github.com/AhmedObaidi/codec2-android](https://github.com/AhmedObaidi/codec2-android).

## Building

By default the aar will build for x86_64 and armeabi-v7a. You can change this by changing the
buildconfig. Copy buildconfig.default.properties to buildconfig.local.properties and adjust

## Publish

This project uses a Maven publishing plugin. Simply use:

./gradlew libcodec2-android:publish

You can set the publishing directory property in buildconfig properties (as above) using the
property repo.dir

## Android API Level

Unfortunately, at the moment this library requires API 23+. This is because Android's NDK does not 
support compiling various complex.h math functions on prior API versions. See the 
[Bionic Release Status](https://android.googlesource.com/platform/bionic/+/master/docs/status.md)
for details.


