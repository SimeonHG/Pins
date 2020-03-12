package com.example.pins;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;


import java.util.Objects;

public class PicassoMarker implements Target {
    Marker mMarker;
    ImageView mImgBike;
    IconGenerator mIconGenerator;

    public PicassoMarker(Marker mMarker) {
        this.mMarker = mMarker;

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PicassoMarker that = (PicassoMarker) o;
        return Objects.equals(mMarker, that.mMarker) &&
                Objects.equals(mImgBike, that.mImgBike) &&
                Objects.equals(mIconGenerator, that.mIconGenerator);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return Objects.hash(mMarker, mImgBike, mIconGenerator);
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        mImgBike.setImageBitmap(bitmap);
        Bitmap icon = mIconGenerator.makeIcon();
        try {
            mMarker.setIcon(BitmapDescriptorFactory.fromBitmap(icon));
        } catch (IllegalArgumentException exception) {
            //just in case that marker is dead, it caused crash
        }
    }

    @Override
    public void onBitmapFailed(Exception e, Drawable errorDrawable) {

    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {

    }
}
