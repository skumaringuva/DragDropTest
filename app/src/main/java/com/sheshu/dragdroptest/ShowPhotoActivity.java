package com.sheshu.dragdroptest;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.sheshu.dragdroptest.Utils.Utils;

/**
 * Created by Sheshu on 4/23/17.
 */

public class ShowPhotoActivity extends AppCompatActivity implements IImageDownloaded {
    private UIHandler uihandler;

    ImageView mLargeImage;
    FlickerItem mFlickerPhoto;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_photo_activity);
        uihandler = new UIHandler();
        mLargeImage = (ImageView) findViewById(R.id.large_image);
        mFlickerPhoto = getIntent().getParcelableExtra(Utils.ARG_IMAGE);
        // Get large image of selected thumnail
        new GetLargePhotoThread(mFlickerPhoto, uihandler).start();
    }

    @Override
    public void imageDownloaded(Message msg) {
        mLargeImage.setImageBitmap((Bitmap) msg.obj);
    }


    public static class GetLargePhotoThread extends Thread {
        FlickerItem ic;
        UIHandler uih;

        public GetLargePhotoThread(FlickerItem ic, UIHandler uih) {
            this.ic = ic;
            this.uih = uih;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (ic.getPhoto() == null) {
                ic.setPhoto(FlickerFetcher.getImage(ic));
            }
            Bitmap bmp = ic.getPhoto();
            if (ic.getPhoto() != null) {
                Message msg = Message.obtain(uih, UIHandler.ID_SHOW_IMAGE);
                msg.obj = bmp;
                uih.sendMessage(msg);
            }
        }
    }

    class UIHandler extends Handler {
        static final int ID_SHOW_IMAGE = 1;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ID_SHOW_IMAGE:
                    // Display large image
                    if (msg.obj != null) {
                        imageDownloaded(msg);
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }
}
