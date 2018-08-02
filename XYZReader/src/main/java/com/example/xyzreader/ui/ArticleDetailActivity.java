package com.example.xyzreader.ui;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.utils.GeneralAppHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Mohamed on 8/2/2018.
 * 
 */
@SuppressLint("SimpleDateFormat")
@SuppressWarnings("deprecation")
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String INTENT_KEY_TITLE = "INTENT_KEY_TITLE";

    public static final String TAG = "ArticleDetailActivity";

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);

    private Cursor mCursor;

    private long mStartId;
    private String mStarterTitle;

    private View mRootView;

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Toolbar toolbar;
    private ImageView expandedImageView;
    private TextView bylineView;
    private TextView bodyView;
    private CoordinatorLayout coordinatorLayout;
    private FrameLayout loadingFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        mRootView = findViewById(R.id.rootViewCoordinatorLayout);

        collapsingToolbarLayout = findViewById(R.id.collapsingToolBar);
        toolbar = mRootView.findViewById(R.id.toolBar);
        expandedImageView = mRootView.findViewById(R.id.expandedToolBarImageView);
        bylineView = mRootView.findViewById(R.id.byTextView);
        bodyView = mRootView.findViewById(R.id.bodyTextView);
        coordinatorLayout = mRootView.findViewById(R.id.rootViewCoordinatorLayout);
        loadingFrameLayout = findViewById(R.id.loadingFrameLayout);

        collapsingToolbarLayout.setExpandedTitleColor(Color.WHITE);
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        toolbar.setTitleTextColor(Color.WHITE);

        bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(),
                "Rosario-Regular.ttf"));

        findViewById(R.id.upButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSupportNavigateUp();
            }
        });

        findViewById(R.id.shareFab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(ArticleDetailActivity.this)
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
            }
        }
        if (getIntent() != null && getIntent().hasExtra(INTENT_KEY_TITLE)){
            mStarterTitle = getIntent().getStringExtra(INTENT_KEY_TITLE);
        }

        bindViews();

        // get data from database and put it into cursor
        getLoaderManager().destroyLoader(0);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getLoaderManager().initLoader(0, null, ArticleDetailActivity.this);
            }
        }, 1000);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(this, mStarterTitle);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;

        mCursor.moveToFirst();

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        if (mCursor != null) {
            loadingFrameLayout.setVisibility(View.GONE);

            String titleString = mCursor.getString(ArticleLoader.Query.TITLE);
            collapsingToolbarLayout.setTitle(titleString);
            toolbar.setTitle(titleString);

            Date publishedDate = parsePublishedDate();
            if (!publishedDate.before(START_OF_EPOCH.getTime())) {
                bylineView.setText(Html.fromHtml(
                        DateUtils.getRelativeTimeSpanString(
                                publishedDate.getTime(),
                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL).toString()
                                + " by <font color='#ffffff'>"
                                + mCursor.getString(ArticleLoader.Query.AUTHOR)
                                + "</font>"));
            } else {
                // If date is before 1902, just show the string
                bylineView.setText(Html.fromHtml(
                        outputFormat.format(publishedDate) + " by <font color='#ffffff'>"
                                + mCursor.getString(ArticleLoader.Query.AUTHOR)
                                + "</font>"));
            }
            bylineView.setText(bylineView.getText().toString());
            bylineView.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.almost_black));

            bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY).replaceAll("(\r\n|\n)", "<br />")));

            String photoUrl = mCursor.getString(ArticleLoader.Query.PHOTO_URL);
            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.ic_loading)
                    .error(R.drawable.ic_baseline_error_24px);
            Glide.with(getBaseContext())
                    .asBitmap()
                    .load(photoUrl/*mCursor.getString(ArticleLoader.Query.THUMB_URL)*/)
                    .apply(requestOptions)
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            expandedImageView.setScaleType(
                                    ImageView.ScaleType.FIT_CENTER);

                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            expandedImageView.setScaleType(
                                    ImageView.ScaleType.CENTER_CROP);

                            final Bitmap bitmap = resource;
                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    GeneralAppHelper.setBackgroundColor(
                                            bitmap,
                                            coordinatorLayout,
                                            Color.WHITE);
                                }
                            });
                            return false;
                        }
                    })
                    .into(expandedImageView);
        } else {
            loadingFrameLayout.setVisibility(View.VISIBLE);
        }
    }

    private Date parsePublishedDate() {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }
}
