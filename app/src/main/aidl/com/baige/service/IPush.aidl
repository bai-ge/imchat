// IPush.aidl
package com.baige.service;

// Declare any non-default types here with import statements

interface IPush {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
     int getPid();
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);
}
