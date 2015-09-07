package msl.com.interactivejob;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import msl.com.httpclient.HttpRegGeoHelper;
import msl.com.httpclient.MSConfig;
import msl.com.httpclient.VNWError;

/**
 * Created by caominhvu on 8/16/15.
 */
public class SubscriptionFragment extends Fragment implements View.OnClickListener{
    public static final String TAG = SubscriptionFragment.class.getSimpleName();
    private ViewGroup mLayout;
    private boolean isUsingEnglish = false;
    private EditText[] mEdtCatLocLevel = new EditText[TYPE_MAX];
    private EditText mEdtKeywords, mEdtMinSalary, mEdtFrequency;
    private TextView mTvInform;
    private String mUsername;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mLayout == null) {
            mLayout = (ViewGroup) inflater.inflate(R.layout.fragment_subscription, container, false);
            (mEdtCatLocLevel[MainActivity.TYPE_CATEGORIES] = (EditText) mLayout.findViewById(R.id.edt_categories)).setOnClickListener(this);
            mLayout.findViewById(R.id.btn_categories).setOnClickListener(this);

            (mEdtCatLocLevel[MainActivity.TYPE_LOCATIONS] = (EditText) mLayout.findViewById(R.id.edt_locations)).setOnClickListener(this);
            mLayout.findViewById(R.id.btn_locations).setOnClickListener(this);

            (mEdtCatLocLevel[MainActivity.TYPE_LEVELS] = (EditText) mLayout.findViewById(R.id.edt_levels)).setOnClickListener(this);
            mLayout.findViewById(R.id.btn_levels).setOnClickListener(this);

            mTvInform = (TextView) mLayout.findViewById(R.id.tv_inform);

            mEdtKeywords = (EditText) mLayout.findViewById(R.id.edt_keyword);
            mEdtMinSalary = (EditText) mLayout.findViewById(R.id.edt_min_salary);
            mEdtFrequency = (EditText) mLayout.findViewById(R.id.edt_frequency);

            mLayout.findViewById(R.id.btn_submit).setOnClickListener(this);

            init();
        } else {
            if(mLayout.getParent() != null) {
                ((ViewGroup) mLayout.getParent()).removeView(mLayout);
            }
        }
        return mLayout;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    private void init() {
        SharedPreferences sharedPref = getActivity().getSharedPreferences(LoginActivity.PREFS_KEY_LOGIN, Context.MODE_PRIVATE);
        mUsername = sharedPref.getString(LoginActivity.KEYWORD_USERNAME, "");

        isUsingEnglish = Locale.getDefault().getLanguage().equals("en");

        mJsonData = ((MainActivity) getActivity()).getGeneralInfo();
        mData = new CharSequence[TYPE_MAX][];
        mChecked = new boolean[TYPE_MAX][];
        try {
            for (int i = 0; i < TYPE_MAX; i++) {
                mData[i] = new CharSequence[mJsonData[i].length()];
                mChecked[i] = new boolean[mJsonData[i].length()];
                for (int j = 0; j < mJsonData[i].length(); j++) {
                    mData[i][j] = mJsonData[i].getJSONObject(j).getString(isUsingEnglish ? "lang_en" : "lang_vn");
                    mChecked[i][j] = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    JSONArray[] mJsonData;
    CharSequence[][] mData;
    boolean[][] mChecked;
    private static final int TYPE_MAX = 3;


    private static final String[] mTitles = new String[]{"Categories", "Locations", "Levels"};
    private void showCategoriesDialog(final int type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the dialog title
        builder.setTitle(mTitles[type])
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(mData[type], mChecked[type],
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                mChecked[type][which] = isChecked;
                            }
                        })
                        // Set the action buttons
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        String text = "";
                        for (int i = 0; i < mChecked[type].length; i++) {
                            if (mChecked[type][i]) {
                                text += mData[type][i] + ",";
                            }
                        }
                        mEdtCatLocLevel[type].setText(text);
                    }
                });
        builder.create().show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.edt_categories:
                showCategoriesDialog(MainActivity.TYPE_CATEGORIES);
                break;
            case R.id.btn_categories:
                showCategoriesDialog(MainActivity.TYPE_CATEGORIES);
                break;
            case R.id.edt_locations:
                showCategoriesDialog(MainActivity.TYPE_LOCATIONS);
                break;
            case R.id.btn_locations:
                showCategoriesDialog(MainActivity.TYPE_LOCATIONS);
                break;
            case R.id.edt_levels:
                showCategoriesDialog(MainActivity.TYPE_LEVELS);
                break;
            case R.id.btn_levels:
                showCategoriesDialog(MainActivity.TYPE_LEVELS);
                break;
            case R.id.btn_submit:
                List<List<Integer>> allTypeIds = new ArrayList<List<Integer>>();
                String[] ID_TYPE = new String[] {"category_id", "location_id", "job_level_id"};
                for (int i= 0; i<TYPE_MAX; i++) {
                    List<Integer> typeIds = new ArrayList<>();
                    for(int j=0; j<mChecked[i].length; j++) {
                        if(mChecked[i][j]) {
                            try {
                                typeIds.add(Integer.valueOf(mJsonData[i].getJSONObject(j).getString(ID_TYPE[i])));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    allTypeIds.add(typeIds);
                }

                new HttpRegGeoHelper(new Observer() {
                    @Override
                    public void update(Observable observable, Object data) {
                        if(data != null) {
                            if(MSConfig.DEBUG) {
                                Log.i(TAG, (String) data);
                            }
                            try {
                                JSONObject json = new JSONObject((String) data);
                                int code = json.getJSONObject("meta").getInt("code");
                                if(code == VNWError.SUCCESS) {
                                    mTvInform.setTextColor(getResources().getColor(android.R.color.primary_text_light));
                                    mTvInform.setText(getString(R.string.txt_subscription_successed));
                                } else {
                                    mTvInform.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                                    mTvInform.setText(json.getJSONObject("meta").getString("message"));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            mTvInform.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                            mTvInform.setText(R.string.txt_subscription_fail_general);
                        }
                    }
                }).subscribe(mUsername, mEdtKeywords.getText().toString(), allTypeIds.get(MainActivity.TYPE_CATEGORIES).toArray(), allTypeIds.get(MainActivity.TYPE_LOCATIONS).toArray(), allTypeIds.get(MainActivity.TYPE_LEVELS).toArray(), mEdtMinSalary.getText().toString(), mEdtFrequency.getText().toString(), isUsingEnglish ? 0 : 1);
                break;
        }

    }
}
