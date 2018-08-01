package com.example.xyzreader.utils;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;

import com.example.xyzreader.R;

import java.util.ArrayList;

/**
 * Created by Mohamed on 8/1/2018.
 *
 */
public class ColorUtils {

    /**
     * @param context to get colors from resources.
     * @param mainColor can be vibrant or vibrantDark, what ever is available.
     * @return list of size 2, header at index 0.
     */
    public static ArrayList<Integer> getHeaderAndSubHeaderColors(Context context, int mainColor){
        ArrayList<Integer> value = new ArrayList<>();

        int headerColor;
        int subHeaderColor;
        if (isBrightColor(mainColor)){
            headerColor = Color.BLACK;
            subHeaderColor = ContextCompat.getColor(context, R.color.almost_black);
        }else {
            headerColor = Color.WHITE;
            subHeaderColor = ContextCompat.getColor(context, R.color.almost_white);
        }

        value.add(headerColor);
        value.add(subHeaderColor);
        return value;
    }

    private static boolean isBrightColor(int color) {
        if (android.R.color.transparent == color)
            return true;

        boolean value = false;

        int[] rgb = { Color.red(color), Color.green(color), Color.blue(color) };

        int brightness = (int) Math.sqrt(rgb[0] * rgb[0] * .241 + rgb[1]
                * rgb[1] * .691 + rgb[2] * rgb[2] * .068);

        // color is light ( note result ranges from 0 dark and 255 is light )
        // pick whatever threshold that suits you.
        if (brightness >= 130) {
            value = true;
        }

        return value;
    }

}
