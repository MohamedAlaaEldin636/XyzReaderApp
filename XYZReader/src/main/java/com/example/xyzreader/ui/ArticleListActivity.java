package com.example.xyzreader.ui;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.example.xyzreader.data.UpdaterService;
import com.example.xyzreader.utils.GeneralAppHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 *
 * ==> All Below Not in starter code
 *
 * ==> Flow
 * 1- on activity launch (savedInstance == null only) we fetch data from internet and save it
 * in database plus in onCreate() (same place) we initialize the loader to get data from
 * the database and update the ui, by adding data to adapter and setting adapter to recyclerView.
 *
 * ==> Notes
 * 1- this is not my code, My job here only is to make UI better, I said that because the pattern
 *      of code here isn't organized, and almost spaghetti code
 *      ( for ex. we should 've made separate class for recyclerView's adapter )
 */
@SuppressLint("SimpleDateFormat")
public class ArticleListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> ,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = ArticleListActivity.class.toString();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);

    private LinearLayout emptyViewLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);

        // init views
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        mRecyclerView = findViewById(R.id.recycler_view);
        emptyViewLinearLayout = findViewById(R.id.emptyViewLinearLayout);

        // setup views
        mSwipeRefreshLayout.setOnRefreshListener(this);

        // load data from internet, then save it in database.
        if (savedInstanceState == null) {
            refresh();
        }

        // load data from database.
        getLoaderManager().initLoader(0, null, this);
    }

    // ---- Private Global Helper Methods

    private void refresh() {
        startService(new Intent(this, UpdaterService.class));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //      SwipeRefreshLayout.OnRefreshListener Implemented Methods
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onRefresh() {
        refresh();

        //getLoaderManager().initLoader(0, null, this);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //      Broadcast Receiver result from UpdaterService class
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean mIsRefreshing = false;

    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                mIsRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                updateRefreshingUI();
            }
        }
    };

    private void updateRefreshingUI() {
        mSwipeRefreshLayout.setRefreshing(mIsRefreshing);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //      Overridden Activity Methods
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //      Loader ( to get data from database )
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (! mIsRefreshing){
            if (cursor.getCount() == 0){
                emptyViewLinearLayout.setVisibility(View.VISIBLE);
            }else {
                emptyViewLinearLayout.setVisibility(View.GONE);
            }
        }

        /*Adapter adapter = new Adapter(cursor);
        adapter.setHasStableIds(true);*/
        CustomAdapter customAdapter = new CustomAdapter(cursor);
        customAdapter.setHasStableIds(true);

        int columnCount = getResources().getInteger(R.integer.list_number_of_columns);
        GridLayoutManager layoutManager = new GridLayoutManager(
                this, columnCount, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(customAdapter/*adapter*/);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerView.setAdapter(null);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //      Recycler View's Adapter
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class CustomAdapter extends RecyclerView.Adapter<CustomViewHolder> {
        private Cursor mCursor;

        CustomAdapter(Cursor cursor) {
            mCursor = cursor;
        }

        @Override
        public long getItemId(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getLong(ArticleLoader.Query._ID);
        }

        @NonNull
        @Override
        public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(
                    R.layout.list_item_article_2, parent, false);

            final CustomViewHolder holder = new CustomViewHolder(view);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            ItemsContract.Items.buildItemUri(getItemId(holder.getAdapterPosition()))));
                }
            });

            return holder;
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

        @Override
        public void onBindViewHolder(@NonNull final CustomViewHolder holder, int position) {
            mCursor.moveToPosition(position);
            holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            Date publishedDate = parsePublishedDate();
            if (!publishedDate.before(START_OF_EPOCH.getTime())) {

                holder.subtitleView.setText(Html.fromHtml(
                        DateUtils.getRelativeTimeSpanString(
                                publishedDate.getTime(),
                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL).toString()
                                + "<br/>" + " by "
                                + mCursor.getString(ArticleLoader.Query.AUTHOR)));
            } else {
                holder.subtitleView.setText(Html.fromHtml(
                        outputFormat.format(publishedDate)
                                + "<br/>" + " by "
                                + mCursor.getString(ArticleLoader.Query.AUTHOR)));
            }

            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.ic_loading)
                    .error(R.drawable.ic_baseline_error_24px);
            Glide.with(getBaseContext())
                    .asBitmap()
                    .load(mCursor.getString(ArticleLoader.Query.THUMB_URL))
                    .apply(requestOptions)
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            holder.thumbnailImageView.setScaleType(
                                    ImageView.ScaleType.FIT_CENTER);

                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            holder.thumbnailImageView.setScaleType(
                                    ImageView.ScaleType.CENTER_CROP);

                            GeneralAppHelper.setBackgroundAndTexts(
                                    resource,
                                    holder.titleView,
                                    holder.subtitleView,
                                    holder.cardView,
                                    Color.WHITE,
                                    getBaseContext());
                            return false;
                        }
                    })
                    .into(holder.thumbnailImageView);
        }

        @Override
        public int getItemCount() {
            return mCursor.getCount();
        }
    }

    static class CustomViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        ImageView thumbnailImageView;
        TextView titleView;
        TextView subtitleView;

        CustomViewHolder(View view) {
            super(view);

            cardView = view.findViewById(R.id.itemCardView);
            thumbnailImageView = view.findViewById(R.id.thumbnailImageView);
            titleView = view.findViewById(R.id.article_title);
            subtitleView = view.findViewById(R.id.article_subtitle);
        }
    }
}
