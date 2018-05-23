package drupal.forumapp.file;

import java.io.File;
import java.net.URL;
import java.util.*;

public class DownloadParams {
    private HashMap<String, String> headers;

    public DownloadParams() {
        headers = new HashMap<String, String>();
    }

    public URL src;
    public File dest;
    public float progressDivider;
    public int readTimeout;
    public int connectionTimeout;

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }
}