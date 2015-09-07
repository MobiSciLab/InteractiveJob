package msl.com.interactivejob;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import msl.com.httpclient.HttpRegGeoHelper;
import msl.com.httpclient.MSConfig;
import msl.com.utils.BuildUtils;
import msl.com.utils.Utils;
import msl.com.utils.img.ImageCache;
import msl.com.utils.img.ImageFetcher;
import msl.com.widgets.AbstractListBuilder;
import msl.com.widgets.MyAdapter;

/**
 * Created by caominhvu on 6/23/15.
 */
public class JobsFragment extends Fragment {
    public static final String TAG = "JobsFragment";
    private ViewGroup mLayout = null;
    private ImageFetcher mImgFetcher;
    private static final String IMAGE_CACHE_DIR = "thumbs";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mLayout == null) {
            mLayout = (ViewGroup) inflater.inflate(R.layout.fragment_jobs, container, false);
            mJobsListBuilder = new JobsListBuilder(getActivity(), mLayout);
            mJobsListBuilder.build();
            mJobsListBuilder.refresh();

            ImageCache.ImageCacheParams cacheParams =
                    new ImageCache.ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);
            cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory
            // The ImageFetcher takes care of loading images into our ImageView children asynchronously
            int imageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
            mImgFetcher = new ImageFetcher(getActivity(), imageThumbSize);
            mImgFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
            mImgFetcher.setLoadingImage(R.drawable.ic_now_hiring);
            mImgFetcher.setImageFadeIn(true);

        } else {
            if (mLayout.getParent() != null) {
                ((ViewGroup) mLayout.getParent()).removeView(mLayout);
            }
        }

        return mLayout;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        mImgFetcher.setExitTasksEarly(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        mImgFetcher.setPauseWork(false);
        mImgFetcher.setExitTasksEarly(true);
        mImgFetcher.flushCache();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        mImgFetcher.closeCache();
    }

    public void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        if (action.equals(Intent.ACTION_SEARCH)) {
            Log.i(TAG, "handle intent: SEARCH......." + intent.getStringExtra(SearchManager.QUERY));
            final String keyword = intent.getStringExtra(SearchManager.QUERY);
            if (mSearchView != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mSearchView != null) {
                            mSearchView.setQuery(keyword, false);
                            mSearchView.clearFocus();
                        }
                    }
                });
            }
            mJobsListBuilder.processQueryData(keyword);
        } else if (action.equals(Intent.ACTION_VIEW)) {
            Log.i(TAG, "handle intent: VIEW........");
        }
    }

    SearchView mSearchView;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.jobs_menu, menu);
        if (menu != null && menu.findItem(R.id.action_search_text) != null) {
            SearchManager searchManager =
                    (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

            if (!BuildUtils.isFromApisLevel11()) {
                mSearchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search_text));
            } else {
                mSearchView = (SearchView) menu.findItem(R.id.action_search_text).getActionView();
            }
            mSearchView.setSearchableInfo(
                    searchManager.getSearchableInfo(getActivity().getComponentName()));
            mSearchView.setIconifiedByDefault(true);

            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    mSearchView.clearFocus();
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
            mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
                @Override
                public boolean onSuggestionSelect(int position) {
                    return false;
                }

                @Override
                public boolean onSuggestionClick(int position) {
                    Cursor cursor = (Cursor) mSearchView.getSuggestionsAdapter().getItem(position);
                    String placeId = cursor.getString(cursor
                            .getColumnIndex(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID));
                    Log.i(TAG, "id............................." + placeId);

                    return false;
                }
            });

            if (mLastKeyword == null) {
                //Get last keyword
                mLastKeyword = queryLastQueryKeyword();
                doSearchByKeyword(mLastKeyword);
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search_text) {
            getActivity().onSearchRequested();
            return true;
        } else if (id == R.id.action_search_camera) {
            Intent intent = new Intent(getActivity(), CameraActivity.class);
            getActivity().startActivityForResult(intent, CAMERA_REQUEST_CODE);
        }
        return super.onOptionsItemSelected(item);
    }

    private static final int CAMERA_REQUEST_CODE = 0x0;

    public void doSearchByKeyword(final String keyword) {
        if (mSearchView != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mSearchView != null) {
                        mSearchView.setQuery(keyword, true);
                        mSearchView.setFocusable(true);
                        mSearchView.setIconified(false);
                        mSearchView.requestFocusFromTouch();
                    }
                }
            });
        } else {
            if (MSConfig.DEBUG) {
                Log.i(TAG, "SearchView is null!");
            }
        }


    }


    private JobsListBuilder mJobsListBuilder;

    private class JobsListBuilder extends AbstractListBuilder implements AdapterView.OnItemClickListener {
        Context mContext;
        List<Object> mJobs;
        private ProgressDialog mProgressDialog;

        public JobsListBuilder(Context context, ViewGroup parent) {
            super(context, parent);
            mContext = context;
            mJobs = new ArrayList<Object>();
        }

        @Override
        protected void buildLayout(ViewGroup parent) {
            ListView listView = (ListView) parent
                    .findViewById(android.R.id.list);
            mAdapter = new MyAdapter(this);
            listView.setAdapter(mAdapter);
            listView.setOnItemClickListener(this);
        }

        @Override
        protected void notifyDataChange() {
            showEmpty(mAdapter.getCount() == 0, mLastKeyword == null ? mContext.getString(R.string.txt_welcome) : null); //Show welcome message for the first time only
            mAdapter.notifyDataSetChanged();
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }

        public void processQueryData(String stringExtra) {
            mProgressDialog = ProgressDialog.show(getActivity(), "", "Loading...");
            mJobs.clear();
            saveLastKeyword(stringExtra);
            new HttpRegGeoHelper(new Observer() {
                @Override
                public void update(Observable observable, Object data) {
                    if (MSConfig.DEBUG) {
                        Log.i(TAG, "Response: " + (String) data);
                    }
                    if (data != null) {
                        try {
                            JSONObject jobjData = new JSONObject((String) data);
                            JSONArray jarrJobs = jobjData.getJSONObject("data").getJSONArray("jobs");
                            for (int i = 0; i < jarrJobs.length(); i++) {
                                mJobs.add(jarrJobs.getJSONObject(i));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    refresh();
                }
            }).searchJob(stringExtra);
        }

        @Override
        protected List<Object> buildData() {
            return mJobs;
        }

        @Override
        public View buildViewHolder() {
            return mLayoutInflater.inflate(R.layout.fragment_jobs_item, null);
        }

        @Override
        public List<View> buildViewHolderContent(View base) {
            ArrayList<View> views = new ArrayList<View>();
            views.add(base.findViewById(R.id.icon));
            views.add(base.findViewById(R.id.tv_job_title));
            views.add(base.findViewById(R.id.tv_job_company));
            views.add(base.findViewById(R.id.tv_job_level));

            views.add(base.findViewById(R.id.tv_salary));
            views.add(base.findViewById(R.id.tv_posted));
            views.add(base.findViewById(R.id.tv_viewed));
            views.add(base.findViewById(R.id.tv_skills));
            return views;
        }

        private static final int ICON = 0;
        private static final int TITLE = 1;
        private static final int COMPANY = 2;
        private static final int LEVEL = 3;
        private static final int SALARY = 4;
        private static final int POSTED_DATE = 5;
        private static final int VIEWED = 6;
        private static final int SKILLS = 7;

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void buildViewHolderContentData(final int position, final List<View> views,
                                               final List<Object> data) {

            try {
                final JSONObject jobDesct = (JSONObject) data.get(position);
                ((TextView) views.get(TITLE)).setText(jobDesct.getString("job_title"));
                ((TextView) views.get(COMPANY)).setText(jobDesct.getString("job_company"));
                ((TextView) views.get(LEVEL)).setText(jobDesct.getString("job_level"));
                ((TextView) views.get(SALARY)).setText(jobDesct.getString("salary"));
                ((TextView) views.get(POSTED_DATE)).setText(jobDesct.getString("posted_date"));
                ((TextView) views.get(VIEWED)).setText("Views: " + jobDesct.getString("views"));
                JSONArray skills = jobDesct.getJSONArray("skills");
                String strSkill = "";
                for (int i = 0; i < skills.length() - 1; ++i) {
                    strSkill += skills.getJSONObject(i).getString("skillName") + ", ";
                }
                strSkill += skills.getJSONObject(skills.length() - 1).getString("skillName");

                ((TextView) views.get(SKILLS)).setText(mContext.getString(R.string.str_skills) + strSkill);

                ImageView imgView = (ImageView) views.get(ICON);

                String imgUrl = null;
                if(jobDesct.has("job_image_url")) {
                    Object jobImageUrls = jobDesct.get("job_image_url");
                    if(jobImageUrls instanceof  JSONArray && ((JSONArray)jobImageUrls).length() > 0) {
                        imgUrl = ((JSONArray) jobImageUrls).getString(0);
                    }
                }

                if (imgUrl == null || imgUrl.length() == 0) {
                    Drawable drawable;
                    if (Utils.hasLollilop()) {
                        drawable = getResources().getDrawable(R.drawable.ic_now_hiring, null);
                    } else {
                        drawable = getResources().getDrawable(R.drawable.ic_now_hiring);
                    }
                    imgView.setImageDrawable(drawable);
                } else {
                    Log.i(TAG, "Loading url: " + imgUrl);
                    mImgFetcher.loadImage(imgUrl, imgView);
                }
            } catch (JSONException e) {
                if (MSConfig.DEBUG)
                    e.printStackTrace();
            }

        }


        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            JSONObject jobDesct = (JSONObject) getAdapter().getItem(position);
            Intent browserIntent = null;
            try {
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(jobDesct.getString("job_detail_url")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            startActivity(browserIntent);
        }
    }

    String mLastKeyword;

    private String queryLastQueryKeyword() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(PREFS_KEY_QUERY, Context.MODE_PRIVATE);
        return sharedPref.getString(LAST_KEYWORD, null);
    }


    protected static final String PREFS_KEY_QUERY = "PREFS_KEY_QUERY";
    protected static final String LAST_KEYWORD = "LAST_KEYWORD";

    private void saveLastKeyword(String keyword) {
        mLastKeyword = keyword;
        SharedPreferences sharedPref = getActivity().getSharedPreferences(PREFS_KEY_QUERY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(LAST_KEYWORD, keyword);
        editor.commit();
    }
}
