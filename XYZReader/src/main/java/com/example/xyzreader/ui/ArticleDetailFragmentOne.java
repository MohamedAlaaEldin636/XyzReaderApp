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
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class ArticleDetailFragmentOne extends android.app.Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetFragOne";

    public static final String ARG_ITEM_ID = "item_id";
    public static final String ARG_ITEM_TITLE = "ARG_ITEM_TITLE";

    private Cursor mCursor;

    private long mItemId;
    private String mItemTitle;

    private View mRootView;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragmentOne() {
    }

    /**
     * Note, we used the other newInstance with title instead of the title, due to the following flow
     * If we launch this activity before the refresh of the service ends in ArticleListActivity
     * an error will be thrown due to change of id of database rows.
     */
    @SuppressWarnings("unused")
    public static ArticleDetailFragmentOne newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragmentOne fragment = new ArticleDetailFragmentOne();
        fragment.setArguments(arguments);
        return fragment;
    }

    public static ArticleDetailFragmentOne newInstance(String itemTitle) {
        Bundle arguments = new Bundle();
        arguments.putString(ARG_ITEM_TITLE, itemTitle);
        ArticleDetailFragmentOne fragment = new ArticleDetailFragmentOne();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }
        if (getArguments() != null && getArguments().containsKey(ARG_ITEM_TITLE)) {
            mItemTitle = getArguments().getString(ARG_ITEM_TITLE);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(
                R.layout.fragment_for_activity_article_detail_2, container, false);

        mRootView.findViewById(R.id.shareFab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        bindViews();

        return mRootView;
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        //TextView titleView = (TextView) mRootView.findViewById(R.id.article_title);
        final CollapsingToolbarLayout collapsingToolbarLayout = mRootView.findViewById(R.id.collapsingToolBar);
        final Toolbar toolbar = mRootView.findViewById(R.id.toolBar);
        final ImageView expandedImageView = mRootView.findViewById(R.id.expandedToolBarImageView);
        final TextView bylineView = mRootView.findViewById(R.id.byTextView);
        final TextView bodyView = mRootView.findViewById(R.id.bodyTextView);
        final CoordinatorLayout coordinatorLayout = mRootView.findViewById(R.id.rootViewCoordinatorLayout);

        collapsingToolbarLayout.setExpandedTitleColor(Color.WHITE);
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        toolbar.setTitleTextColor(Color.WHITE);

        String titleString;

        bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(),
                "Rosario-Regular.ttf"));

        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().setDuration(300).alpha(1);

            titleString = mCursor.getString(ArticleLoader.Query.TITLE);
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
            bylineView.setTextColor(ContextCompat.getColor(getActivity().getBaseContext(), R.color.almost_black));

            bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY).replaceAll("(\r\n|\n)", "<br />")));

            String photoUrl = mCursor.getString(ArticleLoader.Query.PHOTO_URL);
            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.ic_loading)
                    .error(R.drawable.ic_baseline_error_24px);
            Glide.with(getActivity().getBaseContext())
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

                            GeneralAppHelper.setBackgroundColor(
                                    resource,
                                    coordinatorLayout,
                                    Color.WHITE);
                            return false;
                        }
                    })
                    .into(expandedImageView);

            /*ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            Bitmap bitmap = imageContainer.getBitmap();
                            if (bitmap != null) {
                                Palette p = Palette.generate(bitmap, 12);
                                mMutedColor = p.getDarkMutedColor(0xFF333333);
                                mPhotoView.setImageBitmap(imageContainer.getBitmap());
                                mRootView.findViewById(R.id.meta_bar)
                                        .setBackgroundColor(mMutedColor);
                                //updateStatusBar();
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    });*/
        } else {
            mRootView.setVisibility(View.GONE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if (mItemTitle != null && ! mItemTitle.isEmpty()){
            return ArticleLoader.newInstanceForItemId(getActivity(), mItemTitle);
        }else {
            return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null){
            mCursor.moveToFirst();
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
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
