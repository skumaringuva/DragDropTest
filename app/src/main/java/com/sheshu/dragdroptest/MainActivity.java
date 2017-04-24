package com.sheshu.dragdroptest;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;

import com.sheshu.dragdroptest.Utils.Utils;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements IImageDownloaded{

    private final String LAST_IMAGE = "lastImage";
    @BindView(R.id.photoGrid)
    GridView mPhotoGrid;
    @BindView(R.id.search_filed)
    EditText mSearchFiled;
    @BindView(R.id.search_button)
    Button mSearchButton;
    private ImageAdapter mImageAdapter;
    private UIHandler uihandler;
    private ArrayList<FlickerItem> imageList;
    View.OnClickListener imageThumbClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = v.getId();
            Intent intent = new Intent(MainActivity.this,ShowPhotoActivity.class);
            // Send the flicker item to the other activity, where it will download the image.
            intent.putExtra(Utils.ARG_IMAGE,mImageAdapter.mFlickerItemList.get(position));
            startActivity(intent);

        }


    };
    final private MyDragListener mViewDragListener = new MyDragListener();
    final private MyLongClickListener mViewLongClickListener = new MyLongClickListener();
    private int mCellWidth = 0;
    private int mCellHeight = 0;
    /**
     * Runnable to get metadata from Flickr API
     */
    private Runnable getMetadata = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            String tag = mSearchFiled.getText().toString().trim();
            if (tag != null && tag.length() >= 3)
                FlickerFetcher.searchImagesByTag(uihandler, getApplicationContext(), tag);
        }
    };


    public static Bitmap getThumbnail(FlickerItem imgCon) {
        Bitmap bm = null;
        try {
            URL aURL = new URL(imgCon.thumbURL);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (Exception e) {
            Log.e("FlickrManager", e.getMessage());
        }
        return bm;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mImageAdapter = new ImageAdapter(this, null);
        mPhotoGrid.setAdapter(mImageAdapter);
        hideKeyboard(mSearchFiled);
        configureGridView();
        uihandler = new UIHandler();
        // Get prevoiusly downloaded list after orientation change
        if (getLastCustomNonConfigurationInstance() != null && getLastCustomNonConfigurationInstance() instanceof ArrayList)
            imageList = (ArrayList<FlickerItem>) getLastCustomNonConfigurationInstance();
        if (imageList != null) {
            mImageAdapter = new ImageAdapter(getApplicationContext(), imageList);
            ArrayList<FlickerItem> ic = mImageAdapter.getmFlickerItemList();
            mPhotoGrid.setAdapter(mImageAdapter);
            mImageAdapter.notifyDataSetChanged();
            int lastImage = -1;
            if (savedInstanceState.containsKey(LAST_IMAGE)) {
                lastImage = savedInstanceState.getInt(LAST_IMAGE);
            }

        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @OnClick(R.id.search_button)
    void onSearch(View view) {
        if (mPhotoGrid.getAdapter() != null) {
            mImageAdapter.mFlickerItemList = new ArrayList<FlickerItem>();
            mPhotoGrid.setAdapter(mImageAdapter);
        }
        new Thread(getMetadata).start();
        hideKeyboard(mSearchFiled);
    }

    private void configureGridView() {
        int gridWidth = mPhotoGrid.getWidth();
        int gridHeight = mPhotoGrid.getHeight();
        mCellHeight = (int) Utils.pxFromDp(this, 200.0f);
        mCellWidth = (int) Utils.pxFromDp(this, 200.0f);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        if (mImageAdapter != null)
            return this.mImageAdapter.getmFlickerItemList();
        else
            return null;
    }

    @Override
    public void imageDownloaded(Message msg) {
        // Do nothing
    }

    public static class GetThumbnailsThread extends Thread {
        UIHandler uih;
        FlickerItem imgContener;

        public GetThumbnailsThread(UIHandler uih, FlickerItem imgCon) {
            this.uih = uih;
            this.imgContener = imgCon;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            imgContener.thumb = getThumbnail(imgContener);
            if (imgContener.thumb != null) {
                Message msg = Message.obtain(uih, UIHandler.ID_UPDATE_ADAPTER);
                uih.sendMessage(msg);
            }
        }
    }


    private final class MyLongClickListener implements View.OnLongClickListener{

        @Override
        public boolean onLongClick(View view) {
              ClipData data = ClipData.newPlainText("", "");
             View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
             view.startDrag(data, shadowBuilder, view, 0);
            view.setVisibility(View.INVISIBLE);
            return true;
        }
    }


    private class MyDragListener implements View.OnDragListener {

        Drawable enterShape;
        Drawable normalShape;

        @Override
        public boolean onDrag(View v, DragEvent event) {
            if (enterShape == null) {
                enterShape = ContextCompat.getDrawable(MainActivity.this, R.drawable.drop_target);
            }
            if (normalShape == null) {
                normalShape = ContextCompat.getDrawable(MainActivity.this, R.drawable.rectangle);
            }
            int action = event.getAction();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    // do nothing
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    //v.setBackground(enterShape);
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                   // v.setBackground(normalShape);
                    break;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign View to ViewGroup
                    View view = (View) event.getLocalState();
                    view.setVisibility(View.VISIBLE);
                    int dropPosition = mPhotoGrid.pointToPosition((int) event.getX(), (int) event.getY());
                    if (dropPosition != -1) {
                        int focusedPosition = mPhotoGrid.getPositionForView(view);
                        FlickerItem removedItem = mImageAdapter.mFlickerItemList.remove(focusedPosition);
                        mImageAdapter.mFlickerItemList.add(dropPosition, removedItem);
                        mImageAdapter.notifyDataSetInvalidated();
                        mPhotoGrid.invalidateViews();
                    }
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    //v.setBackground(normalShape);
                    v.setVisibility(View.VISIBLE);
                default:
                    break;
            }
            return true;
        }
    }



    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private int defaultItemBackground;
        private ArrayList<FlickerItem> mFlickerItemList;

        public ImageAdapter(Context c, ArrayList<FlickerItem> aFlickerItemList) {
            mContext = c;
            this.mFlickerItemList = aFlickerItemList;
        }

        public ArrayList<FlickerItem> getmFlickerItemList() {
            return mFlickerItemList;
        }

        public void setmFlickerItemList(ArrayList<FlickerItem> aFlickerItemList) {
            this.mFlickerItemList = aFlickerItemList;
        }

        public int getCount() {
            return mFlickerItemList != null ? mFlickerItemList.size() : 0;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView i = new ImageView(mContext);
            if (mFlickerItemList.get(position).thumb != null) {
                i.setImageBitmap(mFlickerItemList.get(position).thumb);
                i.setLayoutParams(new GridView.LayoutParams(mCellWidth, mCellHeight));
                i.setBackgroundResource(defaultItemBackground);
            } else
                i.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.color.black));

            i.setId(position);
            i.setOnClickListener(imageThumbClickListener);
            i.setOnLongClickListener(mViewLongClickListener);
            i.setOnDragListener(mViewDragListener);
            return i;
        }
    }

    class UIHandler extends Handler {
         static final int ID_METADATA_DOWNLOADED = 0;
         static final int ID_SHOW_IMAGE = 1;
         static final int ID_UPDATE_ADAPTER = 2;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ID_METADATA_DOWNLOADED:
                    // Set of information required to download thumbnails is
                    // available now
                    if (msg.obj != null) {
                        imageList = (ArrayList<FlickerItem>) msg.obj;
                        mImageAdapter = new ImageAdapter(getApplicationContext(), imageList);
                        mPhotoGrid.setAdapter(mImageAdapter);
                        for (int i = 0; i < mImageAdapter.getCount(); i++) {
                            new GetThumbnailsThread(uihandler, mImageAdapter.getmFlickerItemList().get(i)).start();
                        }
                    }
                    break;
                case ID_SHOW_IMAGE:
                    // Display large image
                    if (msg.obj != null) {

                    }
                    break;
                case ID_UPDATE_ADAPTER:
                    // Update adapter with thumnails
                    mImageAdapter.notifyDataSetChanged();
                    break;
            }
            super.handleMessage(msg);
        }
    }
}