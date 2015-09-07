package msl.com.httpclient;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Observable;
import java.util.Observer;

import msl.com.utils.IOUtils;

public abstract class HttpClientHelper extends Observable {
	private static final String TAG = "HttpClientHelper";


	public HttpClientHelper() {
	};

	public HttpClientHelper(Observer observer) {
		if (observer != null) {
			addObserver(observer);
		}
	}

	protected void execute(HttpHost httpHost, HttpRequest httpRequest,
			HttpContext httpContext) {
		new HttpTask().execute(httpHost, httpRequest, httpContext);
	}

    protected void execute(HttpHost httpHost, HttpRequest httpRequest,
                           HttpContext httpContext, IDataResponseParser parser) {
        new HttpTask().execute(httpHost, httpRequest, httpContext, parser);
    }

	
	private class HttpTask extends AsyncTask<Object, Void, Object> {
		private static final int HTTP_HOST = 0;
		private static final int HTTP_REQUEST = 1;
		private static final int HTTP_CONTEXT = 2;
        private static final int DATA_PARSER = 3;
        private static final int TOTAL_PARAMS = 4;
		private static final String TAG = "HttpTask";

        boolean isStatusCodeErr(int statusCode) {
            return (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_CREATED && statusCode != HttpStatus.SC_ACCEPTED && statusCode != HttpStatus.SC_NON_AUTHORITATIVE_INFORMATION && statusCode != HttpStatus.SC_NO_CONTENT);
        }

		private DefaultHttpClient getHttpClient(String schema,
				int port) {
			if (MSConfig.DEBUG)
				Log.i(TAG, schema);

			final String SCHEME_UNSECURE = "http";
			final String SCHEME_SECURE = "https";
			final int DEFAULT_SECURE_PORT = 443;
			final int DEFAULT_UNSECURE_PORT = 80;

			DefaultHttpClient defaultHttpClient = null;
			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
			SchemeRegistry scheme = new SchemeRegistry();
			if (schema.equals(SCHEME_UNSECURE)) {
				scheme.register(new Scheme(SCHEME_UNSECURE, PlainSocketFactory
						.getSocketFactory(), port != -1? port: DEFAULT_UNSECURE_PORT));
			} else if (schema.equals(SCHEME_SECURE)) {
				scheme.register(new Scheme(SCHEME_SECURE, SSLSocketFactory
						.getSocketFactory(), port != -1? port: DEFAULT_SECURE_PORT));
			}
			ClientConnectionManager clntConnMgr = new ThreadSafeClientConnManager(
					params, scheme);
			return new DefaultHttpClient(clntConnMgr, params);
		}

		@Override
		protected Object doInBackground(Object... params) {
            Object result = null;
            String errMsg = null;
            Error errCode = Error.NONE;

            IDataResponseParser parser = null;
            if(params.length >=TOTAL_PARAMS) {
                parser = (IDataResponseParser) params[DATA_PARSER];
            }
			HttpHost httpHost = (HttpHost) params[HTTP_HOST];
			HttpRequest httpRequest = (HttpRequest) params[HTTP_REQUEST];
			HttpContext httpContext = (HttpContext) params[HTTP_CONTEXT];

			DefaultHttpClient httpClient = getHttpClient(httpHost.getSchemeName(), httpHost.getPort());

            InputStream inputStream = null;
			try {
				HttpResponse response = httpClient.execute(httpHost, httpRequest, httpContext);

                int statusCode = response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    inputStream = entity.getContent();
                } else {
					errCode = Error.SERVER_COMMUNICATION;
				}
			} catch (Exception e) {
				Log.i(TAG, e.getMessage());
				errCode = Error.NETWORK_CONNECTION;
				errMsg = e.getMessage();
			}
            try {
                if (parser != null) {
                    result = parser.parseData(inputStream, errCode, errMsg);
                } else if(inputStream != null) {
                    result = IOUtils.toString(inputStream);
                }

            } catch (IOException e) {
                e.printStackTrace();
				result = null;
            } finally {
                try {
                    if(inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return result;
		}

		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			setChanged();
			notifyObservers(result);
			deleteObservers();
		}
	}
}
