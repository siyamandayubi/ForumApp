package drupal.forumapp.serverAccess;

import android.net.Credentials;
import android.util.Base64;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import drupal.forumapp.domain.SessionManager;
import drupal.forumapp.model.AuthenticationResponseModel;
import drupal.forumapp.model.PermissionListModel;
import drupal.forumapp.model.ResponseModel;

/**
 * Created by serva on 10/21/2017.
 */

public class AuthenticationAccess extends BaseAccess<AuthenticationResponseModel> {
    public final String AUTHENTICATE_ADDRESS = "api/login-token";
    public final String GET_USER_ROLES_ADDRESS = "api/v2.0/userroles";
    public final String REVOKE_ADDRESS = "users/auth/revoke";
    public final String REFRESH_ADDRESS = "api/refresh-token";

    public AuthenticationAccess(SessionManager sessionManager) {
        super(sessionManager);
    }

    @Override
    public ResponseModel<String> delete(AuthenticationResponseModel data) {
        return null;
    }

    public AuthenticationResponseModel authenticate(String username, String password) throws IOException {
        String encodingString = Base64.encodeToString(String.format("%s:%s", username, password).getBytes(), Base64.NO_WRAP);
        String token = String.format("Basic %s", encodingString);

        return getToken(AUTHENTICATE_ADDRESS, token);
    }

    public AuthenticationResponseModel refresh(String token) throws IOException {
        return getToken(AUTHENTICATE_ADDRESS, token);
    }

    private AuthenticationResponseModel getToken(String localAddress, String token) throws IOException {
        URL url = new URL(buildAddress(localAddress));
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestProperty("Authorization", token);
        urlConnection.setReadTimeout(10000);
        urlConnection.setRequestMethod("GET");
        urlConnection.connect();
        int responseCode = urlConnection.getResponseCode();
        if (responseCode != 200) {
            return null;
        }
        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
        String data = readInputStream(in);
        AuthenticationResponseModel result = parse(data, AuthenticationResponseModel.class);
        return result;
    }

    public PermissionListModel getRoles() {
        PermissionListModel x = new PermissionListModel();
        return getData(GET_USER_ROLES_ADDRESS, x.getClass());
    }

    public void revoke() {

    }
}
