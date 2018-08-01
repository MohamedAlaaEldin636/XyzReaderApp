package com.example.xyzreader.utils;

import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;

/**
 * Created by Mohamed on 8/1/2018.
 *
 */
public class PaletteUtils {

    /**
     * This method MUST be called from background thread.
     * @param bitmap image to get palette from.
     * @param defaultColor returned color if no dominant or vibrant found.
     * @return dominant color or white if dominant not found.
     */
    public static int getDominantOrVibrantColorFromBitmapSync(Bitmap bitmap, int defaultColor){
        Palette palette = new Palette.Builder(bitmap).generate();

        defaultColor = palette.getVibrantColor(defaultColor);

        return palette.getDominantColor(defaultColor);
    }

}
