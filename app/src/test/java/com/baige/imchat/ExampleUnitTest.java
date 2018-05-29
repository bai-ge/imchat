package com.baige.imchat;

import android.util.Log;

import com.baige.util.Tools;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
        System.out.println(Tools.fromatPercent(0.0f));
        System.out.println(Tools.fromatPercent(0.1f));
        System.out.println(Tools.fromatPercent(0.5f));
        System.out.println(Tools.fromatPercent(1.0f));
        System.out.println(Tools.fromatPercent(1.2f));
    }
}