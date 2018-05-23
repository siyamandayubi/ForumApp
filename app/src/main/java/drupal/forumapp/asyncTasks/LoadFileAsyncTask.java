package drupal.forumapp.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.net.URL;
import java.util.Formatter;
import java.util.ArrayList;

import drupal.forumapp.file.Downloader;
import drupal.forumapp.file.DownloadResult;
import drupal.forumapp.file.DownloadParams;

import java.lang.String;

public class LoadFileAsyncTask extends AsyncTask<FileInfo, FileInfo, FileInfo[]> {
    private final Context context;
    private ArrayList<LoadFileTaskListener> loadFileTaskListeners;

    public LoadFileAsyncTask(Context context) {
        this.context = context;
        loadFileTaskListeners = new ArrayList<LoadFileTaskListener>();
    }

    public void setListener(LoadFileTaskListener listener){
        loadFileTaskListeners.add(listener);
    }

    @Override
    protected FileInfo[] doInBackground(FileInfo... files) {
        File dataDir = context.getDir("users", Context.MODE_PRIVATE);
        boolean folderHasBeenCreated = false;
        boolean usersFolderCreated = false;
        try {
            String usersFolderPath = String.format("%1$s%2$s%3$s", dataDir, File.separator, "users");
            //File usersFolder = new File(usersFolderPath);
            //if (!usersFolder.exists()){
              //  usersFolderCreated  = usersFolder.mkdirs();
            //}

            for (FileInfo fileInfo : files) {
                String folderPath = String.format("%1$s%2$s%3$s", dataDir.getAbsolutePath(), File.separator, "" + fileInfo.uid);
                File folder = new File(folderPath);
                if (!folder.exists()) {
                    folderHasBeenCreated = folder.mkdirs();
                }

                String filePath = String.format("%1$s%2$s%3$s", folderPath, File.separator, fileInfo.filename);
                File file = new File(filePath);
                if (file.exists()) {
                    fileInfo.localPath = filePath;
                    publishProgress(fileInfo);
                } else {
                    DownloadParams params = new DownloadParams();
                    params.src = new URL(fileInfo.url);
                    params.dest = new File(filePath);
                    DownloadResult downloadResult = Downloader.download(params);
                    if (downloadResult.statusCode == 200) {
                        fileInfo.localPath = filePath;
                        publishProgress(fileInfo);
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("LoadFileAsyncTask", ex.getMessage());
            Log.e("LoadFileAsyncTask", ex.getStackTrace().toString());
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(FileInfo... files) {
        if (files != null) {
            for (FileInfo file : files) {
                for(LoadFileTaskListener listener: loadFileTaskListeners){
                    listener.onFileAvailable(file);
                }
            }
        }
    }

    @Override
    protected void onPostExecute(FileInfo... files) {
        if (files != null) {
            for (FileInfo file : files) {
                for(LoadFileTaskListener listener: loadFileTaskListeners){
                    listener.onFileAvailable(file);
                }
            }
        }
    }
}