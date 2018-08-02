package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

/**
 * Created by Mohamed on 8/2/2018.
 *
 */
@SuppressWarnings("deprecation")
public class ArticleDetailActivityOne extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String INTENT_KEY_TITLE = "INTENT_KEY_TITLE";

    private Cursor mCursor;

    private long mStartId;
    private String mStarterTitle;

    private ViewPager viewPager;
    private ArticleDetailActivityOne.CustomPagerAdapter pagerAdapter;

    private View upButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail_3);

        // get data from database and put it into cursor
        getLoaderManager().initLoader(0, null, this);

        pagerAdapter = new ArticleDetailActivityOne.CustomPagerAdapter(getFragmentManager());
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        viewPager.setPageMarginDrawable(new ColorDrawable(0x22000000));

        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                upButton.animate()
                        .alpha((state == ViewPager.SCROLL_STATE_IDLE) ? 1f : 0f)
                        .setDuration(300);
            }

            @Override
            public void onPageSelected(int position) {
                if (mCursor != null) {
                    mCursor.moveToPosition(position);
                }
            }
        });

        upButton = findViewById(R.id.upButton);
        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSupportNavigateUp();
            }
        });

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
                if (getIntent() != null && getIntent().hasExtra(INTENT_KEY_TITLE)){
                    mStarterTitle = getIntent().getStringExtra(INTENT_KEY_TITLE);
                }
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCursor = cursor;
        pagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (mStartId > 0) {
            if (mStarterTitle != null && ! mStarterTitle.isEmpty()){
                mCursor.moveToFirst();
                while (!mCursor.isAfterLast()) {
                    if (mCursor.getString(ArticleLoader.Query.TITLE).equals(mStarterTitle)) {
                        final int position = mCursor.getPosition();
                        viewPager.setCurrentItem(position, false);
                        break;
                    }
                    mCursor.moveToNext();
                }
            }else {
                mCursor.moveToFirst();
                while (!mCursor.isAfterLast()) {
                    if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                        final int position = mCursor.getPosition();
                        viewPager.setCurrentItem(position, false);
                        break;
                    }
                    mCursor.moveToNext();
                }
                mStartId = 0;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        pagerAdapter.notifyDataSetChanged();
    }

    private class CustomPagerAdapter extends FragmentStatePagerAdapter {

        CustomPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            return ArticleDetailFragmentOne.newInstance(mCursor.getString(ArticleLoader.Query.TITLE));
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }
    }
}
