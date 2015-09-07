package msl.com.interactivejob;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import msl.com.httpclient.HttpRegGeoHelper;
import msl.com.httpclient.MSConfig;
import msl.com.httpclient.VNWError;

/**
 * Created by caominhvu on 8/4/15.
 */
public class LoginActivity extends Activity implements View.OnClickListener{
    public static final String TAG = LoginActivity.class.getSimpleName();
    private CallbackManager mCallbackManager;
    private EditText mEdtUsername, mEdtPassword;
    private boolean mKeepLoggedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext()); //Must state here, before rendering layout
        //Init state
        setContentView(R.layout.activity_login);
        SharedPreferences sharedPref = getSharedPreferences(PREFS_KEY_LOGIN, Context.MODE_PRIVATE);
        final String username = sharedPref.getString(KEYWORD_USERNAME, "");
        final String password = sharedPref.getString(KEYWORD_PASSWORD, "");
        mKeepLoggedIn = sharedPref.getBoolean(KEYWORD_KEEP_LOGGED_IN, false);

        (mEdtUsername = (EditText) findViewById(R.id.edt_username)).setText(username);
        mEdtPassword = (EditText) findViewById(R.id.edt_password);
        CheckBox cbKeepLoggedIn = (CheckBox) findViewById(R.id.cb_keep_logged_in);
        cbKeepLoggedIn.setChecked(mKeepLoggedIn);
        cbKeepLoggedIn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(MSConfig.DEBUG) {
                    Log.i(TAG, "Keep me logged in changed state to: " + isChecked);
                }
                mKeepLoggedIn = isChecked;
            }
        });
        findViewById(R.id.btn_login).setOnClickListener(this);

        //Auto login if set
        if(mKeepLoggedIn && password.length() > 0) {
            processLogin(username, password);
        }


        //Facebook Login

        final LoginButton loginBtn = (LoginButton) findViewById(R.id.btn_login_facebook);
        loginBtn.setReadPermissions(Arrays.asList("public_profile, email, user_birthday"));

        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(
                                            JSONObject object,
                                            GraphResponse response) {
                                        // Application code
                                        Log.i(TAG, response.toString());
                                        try {
                                            JSONObject jsonObject = response.getJSONObject();
                                            final String email =jsonObject.getString("email");
                                            final String name = jsonObject.has("name") ? jsonObject.getString("name") : "";

                                            if(!email.equals(username)) {
                                                //Do sign up
                                                final ViewGroup layout = (ViewGroup) getLayoutInflater().inflate(R.layout.dialog_input_password, null);
                                                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                                builder.setTitle("Please confirm password");
                                                builder.setView(layout)
                                                        // Add action buttons
                                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                dialog.cancel();
                                                                final String password = ((EditText)layout.findViewById(R.id.edt_password)).getText().toString();
                                                                new HttpRegGeoHelper(new Observer() {
                                                                    @Override
                                                                    public void update(Observable observable, Object data) {
                                                                        //Sign up do silently, so don't need to handle UI but do login immediately
                                                                        //(Consider this is login process only)
                                                                        if(MSConfig.DEBUG) {
                                                                            if(data != null) {
                                                                                Log.i(TAG, "Sign up responsed: " + (String) data);
                                                                            }
                                                                        }
                                                                        processLogin(email, password);
                                                                    }
                                                                }).signupNoConfirm(email, password, name, name, "", 1, 1, 1, "","", 2);
                                                            }
                                                        })
                                                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                LoginManager.getInstance().logOut();
                                                                dialog.cancel();
                                                            }
                                                        });
                                                builder.create().show();
                                            } else {
                                                if(password.length() > 0) { //Two accounts merged
                                                   processLogin(username, password);
                                                } else { //Hasn't signed up or cleared
                                                    //Ask for password
                                                    Log.i(TAG, "Ask for password");
                                                    final ViewGroup layout = (ViewGroup) getLayoutInflater().inflate(R.layout.dialog_input_password, null);
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                                    builder.setTitle("Please confirm password");
                                                    builder.setView(layout)
                                                            // Add action buttons
                                                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int id) {
                                                                    dialog.cancel();
                                                                    processLogin(email, ((EditText)layout.findViewById(R.id.edt_password)).getText().toString());
                                                                }
                                                            })
                                                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int id) {
                                                                    LoginManager.getInstance().logOut();
                                                                    dialog.cancel();
                                                                }
                                                            });
                                                    builder.create().show();
                                                }
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email,gender, birthday");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LoginManager.getInstance().logOut();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                processLogin(mEdtUsername.getText().toString(), mEdtPassword.getText().toString());
                break;
        }
    }



    private void processLogin(final String username, final String password) {
        LoginManager.getInstance().logOut();
        new HttpRegGeoHelper(new Observer() {
            @Override
            public void update(Observable observable, Object data) {
                if(MSConfig.DEBUG) {
                    Log.i(TAG, "Response data: " + (String) data);
                }
                if(data == null) {
                    Toast.makeText(LoginActivity.this, "Login Failed! Can't get response from server!", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    JSONObject jobjData = new JSONObject((String) data);
                    JSONObject meta = jobjData.getJSONObject("meta");
                    if(meta.getString("message").equals(VNWError.FAIL)) {
                        Toast.makeText(LoginActivity.this, "Login Failed! Please check again!", Toast.LENGTH_SHORT).show();
                    } else {
                        saveLoginInfo(username, password);

                        JSONObject profile = jobjData.getJSONObject("data").getJSONObject("profile");
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra(MainActivity.PROFILE, profile.toString());
                        startActivity(intent);
                        finish();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }).login(username, password);
    }
    protected static final String PREFS_KEY_LOGIN = "PREFS_KEY_LOGIN";
    protected static final String KEYWORD_USERNAME = "KEYWORD_USERNAME";
    private static final String KEYWORD_PASSWORD = "KEYWORD_PASSWORD";
    protected static final String KEYWORD_KEEP_LOGGED_IN = "KEYWORD_KEEP_LOGGED_IN";
    private void saveLoginInfo(String username, String password) {
        SharedPreferences sharedPref = getSharedPreferences(PREFS_KEY_LOGIN, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(KEYWORD_USERNAME, username);
        editor.putString(KEYWORD_PASSWORD, password);
        editor.putBoolean(KEYWORD_KEEP_LOGGED_IN, mKeepLoggedIn);
        editor.commit();
    }
}


