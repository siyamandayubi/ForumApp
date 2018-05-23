package drupal.forumapp.serverAccess;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import javax.net.ssl.HttpsURLConnection;

import drupal.forumapp.domain.SessionManager;
import drupal.forumapp.model.BaseModel;
import drupal.forumapp.model.ResponseModel;
import drupal.forumapp.model.AuthenticationResponseModel;
import drupal.forumapp.model.TopicModel;

/**
 * Created by serva on 10/15/2017.
 */

public abstract class BaseAccess<T extends Object> {
    private SessionManager sessionManager;
    protected String baseAddress = "";

    public BaseAccess(SessionManager sessionManager) {

        this.sessionManager = sessionManager;
        this.baseAddress = sessionManager.getBaseAddress();
    }

    public BaseAccess(String baseAddress) {
        this.baseAddress = baseAddress;
    }

    protected String buildAddress(String relativeAddress) {
        return baseAddress + "/" + relativeAddress;
    }

    protected String readInputStream(InputStream stream) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        String line = "";
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        return builder.toString();
    }

    private void checkAndSetAuthorization(URLConnection urlConnection) {

        if (sessionManager == null) {
            return;
        }

        AuthenticationResponseModel tokenModel = sessionManager.getAuthenticationToken();
        if (tokenModel != null) {
            if (tokenModel.expirationDateTime.before(Calendar.getInstance().getTime())) {
                AuthenticationAccess authenticationAccess = new AuthenticationAccess(sessionManager);
                try {
                    tokenModel = authenticationAccess.authenticate(sessionManager.getUsername(), sessionManager.getPassword());
                    sessionManager.setAuthenticationToken(tokenModel);
                } catch (Exception ex) {
                    Log.e("authentication", ex.getMessage());
                    Log.e("authentication", ex.getStackTrace().toString());
                    return;
                }
            }

            urlConnection.setRequestProperty("Authorization", "Bearer " + tokenModel.access_token);
            urlConnection.setRequestProperty("access-token", tokenModel.access_token);
        }
    }

    protected <TData extends Object> ResponseModel<TData> postData(String relativePath, Object data, String method, Class<TData> type) {
        ResponseModel<TData> returnValue = new ResponseModel<TData>();

        try {
            URL url = new URL(buildAddress(relativePath));
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            checkAndSetAuthorization(urlConnection);
            urlConnection.setReadTimeout(10000);
            urlConnection.setRequestProperty("content-type", "application/json");
            urlConnection.setConnectTimeout(15000);
            if (urlConnection instanceof HttpURLConnection) {
                ((HttpURLConnection) urlConnection).setRequestMethod(method);
            }
            urlConnection.setDoOutput(true);
            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            Gson gson = new Gson();
            writer.write(gson.toJson(data));
            writer.flush();
            writer.close();
            os.close();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            String receivedData = readInputStream(in);
            returnValue.result = parse(receivedData, type);

            urlConnection.connect();
            returnValue.statusCode = urlConnection.getResponseCode();
        } catch (MalformedURLException exception) {
            Log.e("postData:", exception.getMessage());
            Log.e("postData:", exception.getStackTrace().toString());
            returnValue.statusCode = -2;
        } catch (IOException exception) {
            Log.e("postData:", exception.getMessage());
            Log.e("postData:", exception.getStackTrace().toString());
            returnValue.error = exception.getMessage();
            returnValue.statusCode = -1;
        }

        return returnValue;
    }

    public abstract ResponseModel<String> delete(T data);

    protected <TData extends BaseModel> TData getData(String relativePath, Class<TData> type) {
        TData returnValue = null;
        try {
            try {
                returnValue = (TData) type.newInstance();
            }
            catch (Exception ex){
            }

            URL url = new URL(buildAddress(relativePath));
            URLConnection urlConnection = (URLConnection) url.openConnection();
            checkAndSetAuthorization(urlConnection);
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            String data = readInputStream(in);
            TData result = parse(data, type);
            return result;
        } catch (MalformedURLException exception) {
            Log.e("getData:", exception.getMessage());
            Log.e("getData:", exception.getStackTrace().toString());
                returnValue.hasError = true;
                returnValue.errorMessage = exception.getMessage();
				returnValue.errorCode = -2;
                return returnValue;
        } catch (IOException exception) {
            Log.e("getData:", exception.getMessage());
            Log.e("getData:", exception.getStackTrace().toString());
                returnValue.hasError = true;
                returnValue.errorMessage = exception.getMessage();
				returnValue.errorCode = -1;
                return returnValue;
		}
    }

    protected <TData extends Object> TData parse(String data, Class<TData> type) {
        Log.e("json", data);
        Gson gson = new Gson();
        return gson.fromJson(data, type);
    }
}
