package drupal.forumapp.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.util.Log;
import drupal.forumapp.domain.ImageHelper;
import android.os.AsyncTask;

public class Downloader {

    public static DownloadResult download(DownloadParams param) throws Exception {
        DownloadResult res = new DownloadResult();
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) param.src.openConnection();

            Iterator<Map.Entry<String, String>> iterator = param.getHeaders().entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, String> pairs = (Map.Entry<String, String>) iterator.next();
                String key = pairs.getKey();
                String value = pairs.getValue();
                connection.setRequestProperty(key, value);
            }

            connection.setConnectTimeout(param.connectionTimeout);
            connection.setReadTimeout(param.readTimeout);
            connection.connect();

            int statusCode = connection.getResponseCode();
            int lengthOfFile = connection.getContentLength();

            boolean isRedirect = (
                    statusCode != HttpURLConnection.HTTP_OK &&
                            (
                                    statusCode == HttpURLConnection.HTTP_MOVED_PERM ||
                                            statusCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                                            statusCode == 307 ||
                                            statusCode == 308
                            )
            );

            if (isRedirect) {
                String redirectURL = connection.getHeaderField("Location");
                connection.disconnect();

                connection = (HttpURLConnection) new URL(redirectURL).openConnection();
                connection.setConnectTimeout(5000);
                connection.connect();

                statusCode = connection.getResponseCode();
                lengthOfFile = connection.getContentLength();
            }

            Map<String, List<String>> headers = connection.getHeaderFields();

            Map<String, String> headersFlat = new HashMap<String, String>();

            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                String headerKey = entry.getKey();
                String valueKey = entry.getValue().get(0);

                if (headerKey != null && valueKey != null) {
                    headersFlat.put(headerKey, valueKey);
                }
            }

            input = new BufferedInputStream(connection.getInputStream(), 8 * 1024);
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            bitmap = ImageHelper.getRoundedCornerBitmap(bitmap);
            if(!param.dest.exists()){
                param.dest.createNewFile();
            }

            output = new FileOutputStream(param.dest);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
            output.flush();
            output.close();

            res.statusCode = statusCode;
        } catch (Exception ex) {
            Log.e("Downloader", ex.getMessage());
            Log.e("Downloader", ex.getStackTrace().toString());
        } finally {
            if (output != null) output.close();
            if (input != null) input.close();
            if (connection != null) connection.disconnect();
        }

        return res;
    }
}
