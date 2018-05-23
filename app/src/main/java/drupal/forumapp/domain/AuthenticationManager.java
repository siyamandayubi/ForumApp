package drupal.forumapp.domain;

import android.content.Context;
import android.util.Log;

import java.util.Calendar;
import drupal.forumapp.model.RolePermissionModel;
import drupal.forumapp.model.PermissionListModel;
import drupal.forumapp.model.TopicModel;
import drupal.forumapp.model.CommentModel;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import drupal.forumapp.serverAccess.AuthenticationAccess;
import drupal.forumapp.model.AuthenticationResponseModel;
/**
 * Created by serva on 10/13/2017.
 */

public class AuthenticationManager {
    private Context context;

    private static final String ADMIN_COMMENTS_PERMISSION = "administer comments";
    private static final String EDIT_OWN_COMMENTS_PERMISSION = "edit own comments";
	private static final String ADMIN_NODES_PERMISSION = "administer nodes";
	private static final String EDIT_OWN_ARTICLES_PERMISSION =  "edit own article content";
	private static final String DELETE_OWN_PAGE_PERMISSION = "delete own page content";

	private SessionManager sessionManager;
    public AuthenticationManager(Context context) {
        this.context = context;
		sessionManager = new SessionManager(this.context);
    }

    public boolean login(String username, String password){
        try {
            AuthenticationAccess authenticationAccess = new AuthenticationAccess(sessionManager);
            AuthenticationResponseModel model = authenticationAccess.authenticate(username, password);
            if (model == null) {
                return false;
            }

            Gson gson = new Gson();
            sessionManager.setAuthenticationToken(model);
			PermissionListModel permissionListModel = authenticationAccess.getRoles();
			sessionManager.persistUserPermissions(gson.toJson(permissionListModel));

            return true;
        }catch (Exception ex){
            Log.e("keyStoreManager", ex.getMessage());
            Log.e("keyStoreManager", ex.getStackTrace().toString());
            return  false;
        }
    }

	public boolean canDeleteTopic(TopicModel topic){
         String username = sessionManager.getUsername();
        if (!generalPermissionCheck(username)) {
            return false;
        }
        
        String userPermissionsJson = sessionManager.getUserPermissions();
        Gson gson = new Gson();
        PermissionListModel permissionListModel = new PermissionListModel();
         if (!TextUtils.isEmpty(userPermissionsJson)){
            permissionListModel = gson.fromJson(userPermissionsJson, permissionListModel.getClass());
        }

        if (topic.name == username){
            hasPermission(DELETE_OWN_PAGE_PERMISSION, permissionListModel);
        }

        return hasPermission(ADMIN_NODES_PERMISSION , permissionListModel);
    }

	public boolean canEditTopic(TopicModel topic){
         String username = sessionManager.getUsername();
        if (!generalPermissionCheck(username)) {
            return false;
        }
        
        String userPermissionsJson = sessionManager.getUserPermissions();
        Gson gson = new Gson();
        PermissionListModel permissionListModel = new PermissionListModel();
         if (!TextUtils.isEmpty(userPermissionsJson)){
            permissionListModel = gson.fromJson(userPermissionsJson, permissionListModel.getClass());
        }

        if (!TextUtils.isEmpty(topic.name) && topic.name.equals(username)){
            hasPermission(EDIT_OWN_COMMENTS_PERMISSION, permissionListModel);
        }

        return hasPermission(ADMIN_NODES_PERMISSION, permissionListModel);
    }

    public boolean canEditComment(CommentModel comment){
         String username = sessionManager.getUsername();
        if (!generalPermissionCheck(username)) {
            return false;
        }
        
        String userPermissionsJson = sessionManager.getUserPermissions();
        Gson gson = new Gson();
        PermissionListModel permissionListModel = new PermissionListModel();
         if (!TextUtils.isEmpty(userPermissionsJson)){
            permissionListModel = gson.fromJson(userPermissionsJson, permissionListModel.getClass());
        }

        if (comment.name == username){
            hasPermission(EDIT_OWN_COMMENTS_PERMISSION, permissionListModel);
        }

        return hasPermission(ADMIN_COMMENTS_PERMISSION, permissionListModel);
    }

    public boolean hasPermission(String permission){
        String userPermissionsJson = sessionManager.getUserPermissions();
        Gson gson = new Gson();
        PermissionListModel permissionListModel = new PermissionListModel();
         if (!TextUtils.isEmpty(userPermissionsJson)){
            permissionListModel = gson.fromJson(userPermissionsJson, permissionListModel.getClass());
        }

        return hasPermission(permission, permissionListModel);
    }

	protected boolean generalPermissionCheck(String username){
        if (TextUtils.isEmpty(username)) {
            return false;
        }
 
        AuthenticationResponseModel token = sessionManager.getAuthenticationToken();
        Boolean tokenExpired = (token == null || token.expirationDateTime.before(Calendar.getInstance().getTime()));
        if(tokenExpired){
            return false;
        }

		return true;
	}

    protected boolean hasPermission(String permision,  PermissionListModel permissionListModel){
        for (int i = 0; i < permissionListModel.data.size(); i++){
            if ( permissionListModel.data.get(i).permission.equals(permision)){
                return true;
            }
        }

        return false;
    }
}
