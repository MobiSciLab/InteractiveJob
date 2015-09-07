package msl.com.interactivejob;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import msl.com.httpclient.HttpRegGeoHelper;
import msl.com.httpclient.MSConfig;
import msl.com.httpclient.VNWError;

/**
 * Created by caominhvu on 8/22/15.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = "ProfileFragment";
    private ViewGroup mLayout = null;

    private EditText mEdtFirstname, mEdtLastname, mEdtBirthday, mEdtHomephone, mEdtCellphone;
    private Spinner mSpnGender, mSpnResidence;
    private TextView mTvInform;

    private JSONArray mJArrlocs;
    private boolean mIsUsingEnglish;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "call onCreateView");
        if (mLayout == null) {
            mLayout = (ViewGroup) inflater.inflate(R.layout.fragment_profile, container, false);

            mEdtFirstname = (EditText) mLayout.findViewById(R.id.edt_firstname);
            mEdtLastname = (EditText) mLayout.findViewById(R.id.edt_lastname);
            mEdtBirthday = (EditText) mLayout.findViewById(R.id.edt_birthday);
            mEdtHomephone = (EditText) mLayout.findViewById(R.id.edt_homephone);
            mEdtCellphone = (EditText) mLayout.findViewById(R.id.edt_mobilephone);

            mTvInform = (TextView) mLayout.findViewById(R.id.tv_inform_submit);

            mLayout.findViewById(R.id.btn_submit).setOnClickListener(this);

            mSpnGender = (Spinner) mLayout.findViewById(R.id.spn_gender);
            ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(getActivity(),
                    R.array.gender, android.R.layout.simple_spinner_item);
            genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpnGender.setAdapter(genderAdapter);


            mIsUsingEnglish = Locale.getDefault().getLanguage().equals("en");
            mJArrlocs = ((MainActivity) getActivity()).getGeneralInfo()[MainActivity.TYPE_LOCATIONS];
            CharSequence[] locs = new CharSequence[mJArrlocs.length()];
            for (int i = 0; i < mJArrlocs.length(); i++) {
                try {
                    locs[i] = mJArrlocs.getJSONObject(i).getString(mIsUsingEnglish ? "lang_en" : "lang_vn");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            mSpnResidence = (Spinner) mLayout.findViewById(R.id.spn_residence);
            ArrayAdapter<CharSequence> residentAdapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item, locs);
            residentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpnResidence.setAdapter(residentAdapter);

            setUpProfile();

            setHasOptionsMenu(false);
        } else {
            if (mLayout.getParent() != null) {
                ((ViewGroup) mLayout.getParent()).removeView(mLayout);
            }
        }

        return mLayout;
    }

    private void setUpProfile() {
        try {
            JSONObject profile = ((MainActivity) getActivity()).getPersonalInfo();
            if (MSConfig.DEBUG) {
                Log.i(TAG, "Profile: " + profile.toString());
            }
            if (profile.has("first_name")) {
                mEdtFirstname.setText(profile.getString("first_name"));
            }

            if (profile.has("last_name")) {
                mEdtLastname.setText(profile.getString("last_name"));
            }

            if (profile.has("birthday")) {
                SimpleDateFormat[] sdfs = new SimpleDateFormat[]{new SimpleDateFormat("dd/mm/yyyy", Locale.US), new SimpleDateFormat("yyyy-mm-dd", Locale.US)};
                Log.i(TAG, profile.getString("birthday"));
                Date formatedBirthday = sdfs[0].parse(profile.getString("birthday"));
                mEdtBirthday.setText(sdfs[1].format(formatedBirthday));
            }

            if (profile.has("home_phone")) {
                mEdtHomephone.setText(profile.getString("home_phone"));
            }

            if (profile.has("cell_phone")) {
                mEdtCellphone.setText(profile.getString("cell_phone"));
            }

            if (profile.has("gender")) {

                mSpnGender.setSelection(genderToPos(profile.getInt("gender")));
            }

            if (profile.has("residence")) {
                String locId = profile.getString("residence");
                if (locId != null) {
                    for (int i = 0; i < mJArrlocs.length(); i++) {
                        if (mJArrlocs.getJSONObject(i).getInt("location_id") == Integer.valueOf(locId)) {
                            mSpnResidence.setSelection(i);
                            break;
                        }
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_submit:
                new HttpRegGeoHelper(new Observer() {
                    @Override
                    public void update(Observable observable, Object data) {
                        if (data == null) {

                        } else {
                            if (MSConfig.DEBUG) {
                                Log.i(TAG, (String) data);
                            }
                            try {
                                JSONObject jObj = new JSONObject((String) data);
                                JSONObject jObjMeta = jObj.getJSONObject("meta");
                                if (jObjMeta.getInt("code") != VNWError.SUCCESS) {
                                    mTvInform.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                                    mTvInform.setText(jObjMeta.getString("message"));
                                } else {
                                    JSONObject jObjData = jObj.getJSONObject("data");
                                    mTvInform.setTextColor(getResources().getColor(android.R.color.primary_text_light));
                                    mTvInform.setText(getString(R.string.txt_edit_successed));
                                    ((MainActivity) getActivity()).setLoginToken(jObjData.getString("login_token"));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).editProfile(((MainActivity) getActivity()).getLoginToken(), mEdtFirstname.getText().toString(), mEdtLastname.getText().toString(), mEdtBirthday.getText().toString(), 1, posToGender(mSpnGender.getSelectedItemPosition()), posToResidence(mSpnResidence.getSelectedItemPosition()), mEdtHomephone.getText().toString(), mEdtCellphone.getText().toString(), mIsUsingEnglish ? 2 : 1);
                break;
        }
    }

    private int genderToPos(int gender) {
        return gender == 1 ? 0 : 1;
    }

    private int posToGender(int pos) {
        return pos == 0 ? 1 : 2;
    }

    private int posToResidence(int pos) {
        try {
            return mJArrlocs.getJSONObject(pos).getInt("location_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }
}
