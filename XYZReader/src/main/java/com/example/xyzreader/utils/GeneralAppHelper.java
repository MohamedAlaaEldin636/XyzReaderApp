package com.example.xyzreader.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Mohamed on 8/2/2018.
 *
 */
public class GeneralAppHelper {

    public static void setBackgroundAndImageAndTexts(final Bitmap bitmap, final ImageView imageView,
                                             final TextView header, final TextView subHeader,
                                             final CardView cardView, final int defaultColor,
                                             final Context context){
        new Palette.Builder(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(@NonNull Palette palette) {
                int backgroundColor = palette.getVibrantColor(defaultColor);
                backgroundColor = palette.getDominantColor(backgroundColor);

                ArrayList<Integer> listOfColors = ColorUtils.getHeaderAndSubHeaderColors(
                        context, backgroundColor);
                int colorOfHeader = listOfColors.get(0);
                int colorOfSubHeader = listOfColors.get(1);

                try {
                    imageView.setImageBitmap(bitmap);

                    cardView.setCardBackgroundColor(backgroundColor);

                    header.setTextColor(colorOfHeader);

                    subHeader.setTextColor(colorOfSubHeader);
                }catch (Exception e){
                    // In case of any exception occurred.
                }
            }
        });
    }

    public static void setBackgroundAndTexts(final Bitmap bitmap,
                                             final TextView header,
                                             final TextView subHeader,
                                             final CardView cardView,
                                             final int defaultColor,
                                             final Context context){
        new Palette.Builder(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(@NonNull Palette palette) {
                int backgroundColor = palette.getVibrantColor(defaultColor);
                backgroundColor = palette.getDominantColor(backgroundColor);

                ArrayList<Integer> listOfColors = ColorUtils.getHeaderAndSubHeaderColors(
                        context, backgroundColor);
                int colorOfHeader = listOfColors.get(0);
                int colorOfSubHeader = listOfColors.get(1);

                try {
                    cardView.setCardBackgroundColor(backgroundColor);

                    header.setTextColor(colorOfHeader);

                    subHeader.setTextColor(colorOfSubHeader);
                }catch (Exception e){
                    // In case of any exception occurred.
                }
            }
        });
    }

}
