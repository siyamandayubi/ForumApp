package drupal.forumapp.model;

import java.util.Date;

/**
 * Created by serva on 10/22/2017.
 */

public class AuthenticationResponseModel {
    public String refresh_token;
    public long expires_in;
    public String access_token;
    public Date expirationDateTime;
}
