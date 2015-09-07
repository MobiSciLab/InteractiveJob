package msl.com.httpclient;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.UnsupportedEncodingException;
import java.util.Observer;

public class HttpRegGeoHelper extends HttpClientHelper implements IRegApis {
	private static final String TAG = "HttpRegGeoHelper";
	Context mContext;

    final String USER_NAME = "user_email";
    final String USER_PASSWORD = "user_password";

    final String EMAIL = "email";
    final String PASSWORD = "password";
    final String KEYWORDS = "keywords";
    final String CATEGORIES = "job_categories";
    final String LOCATIONS = "job_locations";
    final String LEVELS = "job_level";
    final String MIN_SALARY = "min_salary";
    final String FREQUENCY = "frequency";
    final String LANG = "lang";

    final String FIRSTNAME = "firstname";
    final String FIRST_NAME = "first_name";
    final String LASTNAME = "lastname";
    final String LAST_NAME = "last_name";
    final String BIRTHDAY = "birthday";
    final String GENDERID = "genderid";
    final String NATIONALITY = "nationality";
    final String RESIDENCE = "residence";
    final String HOME_PHONE = "home_phone";
    final String CELL_PHONE = "cell_phone";

    final String KEYWORD = "job_title";

	public HttpRegGeoHelper(Context context, Observer observer) {
		super(observer);
		mContext = context;
	}

    public HttpRegGeoHelper(Observer observer) {
        super(observer);
    }

	@Override
	protected void execute(HttpHost httpHost, HttpRequest httpRequest, HttpContext httpContext) {
        if(mContext != null) {
            ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni == null) {
                Object data = null;
                try {
                    data = new JSONObject(new JSONStringer().object().key("code").value(Error.NETWORK_CONNECTION).endObject().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setChanged();
                notifyObservers(data.toString());
                deleteObservers();
            }
        }
		super.execute(httpHost, httpRequest, httpContext);
	}

    @Override
    public void signupNoConfirm(String username, String password, String firstname, String lastname, String birthday, int gender, int nationality, int city, String homephone, String cellphone, int language) {
        if(MSConfig.DEBUG) {
            Log.i(TAG, "editProfile: ");
        }
        final String URI = IRegApis.SIGNUP_NO_CONFIRM_URL;

        HttpRequest httpRequest;
        HttpHost httpHost = null;
        HttpContext httpContext = null;

        Uri uri = Uri.parse(URI);
        httpHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
        httpRequest = new BasicHttpEntityEnclosingRequest(HttpPost.METHOD_NAME, URI, HttpVersion.HTTP_1_1);

        try {
            JSONStringer jobj = new JSONStringer();
            jobj.object();
            jobj.key(EMAIL).value(username);
            jobj.key(PASSWORD).value(password);
            jobj.key(FIRSTNAME).value(firstname);
            jobj.key(LASTNAME).value(lastname);
            jobj.key(BIRTHDAY).value(birthday);
            jobj.key(GENDERID).value(gender);
            jobj.key(NATIONALITY).value(nationality);
            jobj.key(RESIDENCE).value(city);
            jobj.key(HOME_PHONE).value(homephone);
            jobj.key(CELL_PHONE).value(cellphone);
            jobj.key(LANG).value(language);
            jobj.endObject();

            StringEntity entity = new StringEntity(jobj.toString());

            httpRequest.setHeader(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            httpRequest.addHeader(VNWORK_HEADER, VNWORK_HEADER_VALUE);

            entity.setContentType("application/json;charset=UTF-8");
            entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));

            ((BasicHttpEntityEnclosingRequest) httpRequest).setEntity(entity);

            if(MSConfig.DEBUG) {
                Log.i(TAG, "Sending entity:....  " + jobj.toString());
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        execute(httpHost, httpRequest, httpContext);
    }

    @Override
    public void login(String username, String password) {
        if(MSConfig.DEBUG) {
            Log.i(TAG, "login: " + username + ":" + password);
        }

        final String URI = IRegApis.LOGIN_URL;

        HttpRequest httpRequest;
        HttpHost httpHost = null;
        HttpContext httpContext = null;

        Uri uri = Uri.parse(URI);
        httpHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
        httpRequest = new BasicHttpEntityEnclosingRequest(HttpPost.METHOD_NAME, URI, HttpVersion.HTTP_1_1);


        try {
            JSONStringer jobj = new JSONStringer();
            jobj.object();
            jobj.key(USER_NAME).value(username);
            jobj.key(USER_PASSWORD).value(password);
            jobj.endObject();

            StringEntity entity = new StringEntity(jobj.toString());

            httpRequest.setHeader(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            httpRequest.addHeader(VNWORK_HEADER, VNWORK_HEADER_VALUE);

            entity.setContentType("application/json;charset=UTF-8");
            entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));

            ((BasicHttpEntityEnclosingRequest) httpRequest).setEntity(entity);

            if(MSConfig.DEBUG) {
                Log.i(TAG, "Sending entity:....  " + jobj.toString());
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        execute(httpHost, httpRequest, httpContext);
    }

    @Override
    public void searchJob(String title) {
        if(MSConfig.DEBUG) {
            Log.i(TAG, "searchJob: " + title);
        }


        final String URI = IRegApis.SEARCH_URL;

        HttpRequest httpRequest;
        HttpHost httpHost = null;
        HttpContext httpContext = null;

        Uri uri = Uri.parse(URI);
        httpHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
        httpRequest = new BasicHttpEntityEnclosingRequest(HttpPost.METHOD_NAME, URI, HttpVersion.HTTP_1_1);


        try {
            JSONStringer jobj = new JSONStringer();
            jobj.object();
            jobj.key(KEYWORD).value(title);
            jobj.endObject();

            StringEntity entity = new StringEntity(jobj.toString());

            httpRequest.setHeader(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            httpRequest.addHeader(VNWORK_HEADER, VNWORK_HEADER_VALUE);

            entity.setContentType("application/json;charset=UTF-8");
            entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));

            ((BasicHttpEntityEnclosingRequest) httpRequest).setEntity(entity);

            if(MSConfig.DEBUG) {
                Log.i(TAG, "Sending entity:....  " + jobj.toString());
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        execute(httpHost, httpRequest, httpContext);
    }

    @Override
    public void getGeneralConfig() {
        if(MSConfig.DEBUG) {
            Log.i(TAG, "getGeneralConfig " );
        }

        final String URI = IRegApis.GENERAL_CONFIG_URL;

        HttpRequest httpRequest;
        HttpHost httpHost = null;
        HttpContext httpContext = null;

        Uri uri = Uri.parse(URI);
        httpHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
        httpRequest = new BasicHttpEntityEnclosingRequest(HttpGet.METHOD_NAME, URI, HttpVersion.HTTP_1_1);
        httpRequest.setHeader(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        httpRequest.addHeader(VNWORK_HEADER, VNWORK_HEADER_VALUE);

        execute(httpHost, httpRequest, httpContext);
    }

    @Override
    public void subscribe(String username, String keywords, Object[] categories, Object[] locations, Object[] levels, String minSalary, String frequency, int language) {
        if(MSConfig.DEBUG) {
            Log.i(TAG, "subcribe: ");
        }

        final String URI = IRegApis.SUBSCRIPTION_URL;

        HttpRequest httpRequest;
        HttpHost httpHost = null;
        HttpContext httpContext = null;

        Uri uri = Uri.parse(URI);
        httpHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
        httpRequest = new BasicHttpEntityEnclosingRequest(HttpPost.METHOD_NAME, URI, HttpVersion.HTTP_1_1);


        try {
            JSONArray categoriesArr = new JSONArray();
            for(int i=0;i<categories.length; i++)
                categoriesArr.put(categories[i]);

            JSONArray locationsArr = new JSONArray();
            for(int i=0;i<locations.length; i++)
                locationsArr.put(locations[i]);

            JSONArray levelsArr = new JSONArray();
            for(int i=0;i<levels.length; i++)
                levelsArr.put(levels[i]);

            JSONStringer jobj = new JSONStringer();
            jobj.object();
            jobj.key(EMAIL).value(username);
            jobj.key(KEYWORDS).value(keywords);
            jobj.key(CATEGORIES).value(categoriesArr);
            jobj.key(LOCATIONS).value(locationsArr);
            jobj.key(LEVELS).value(levelsArr);
            jobj.key(MIN_SALARY).value(minSalary);
            jobj.key(FREQUENCY).value(frequency);
            jobj.key(LANG).value(language);
            jobj.endObject();

            StringEntity entity = new StringEntity(jobj.toString());

            httpRequest.setHeader(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            httpRequest.addHeader(VNWORK_HEADER, VNWORK_HEADER_VALUE);

            entity.setContentType("application/json;charset=UTF-8");
            entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));

            ((BasicHttpEntityEnclosingRequest) httpRequest).setEntity(entity);

            if(MSConfig.DEBUG) {
                Log.i(TAG, "Sending entity:....  " + jobj.toString());
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        execute(httpHost, httpRequest, httpContext);
    }

    @Override
    public void editProfile(String token, String firstname, String lastname, String birthday, int gender, int nationality, int city, String homephone, String cellphone, int language) {
        if(MSConfig.DEBUG) {
            Log.i(TAG, "editProfile: ");
        }
        final String URI = IRegApis.EDIT_URL + "/" + token;

        HttpRequest httpRequest;
        HttpHost httpHost = null;
        HttpContext httpContext = null;

        Uri uri = Uri.parse(URI);
        httpHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
        httpRequest = new BasicHttpEntityEnclosingRequest(HttpPost.METHOD_NAME, URI, HttpVersion.HTTP_1_1);


        try {

            JSONStringer jobj = new JSONStringer();
            jobj.object();
            jobj.key(FIRST_NAME).value(firstname);
            jobj.key(LAST_NAME).value(lastname);
            jobj.key(BIRTHDAY).value(birthday);
            jobj.key(GENDERID).value(gender);
            jobj.key(NATIONALITY).value(nationality);
            jobj.key(RESIDENCE).value(city);
            jobj.key(HOME_PHONE).value(homephone);
            jobj.key(CELL_PHONE).value(cellphone);
            jobj.key(LANG).value(language);
            jobj.endObject();

            StringEntity entity = new StringEntity(jobj.toString());

            httpRequest.setHeader(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            httpRequest.addHeader(VNWORK_HEADER, VNWORK_HEADER_VALUE);

            entity.setContentType("application/json;charset=UTF-8");
            entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));

            ((BasicHttpEntityEnclosingRequest) httpRequest).setEntity(entity);

            if(MSConfig.DEBUG) {
                Log.i(TAG, "Sending entity:....  " + jobj.toString());
            }
        } catch (JSONException e1) {
            e1.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        execute(httpHost, httpRequest, httpContext);
    }
}
