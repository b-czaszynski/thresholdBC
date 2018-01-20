package org.blockchainbeasts.passbuddies;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SecretStorageHandler {

    public static final String PREFERENCE_NAME = "secrets";

    /**
     *Returns a set of all messages (as JSON strings) of the given owner
     * @return Set<String> Set of messages or null
     */
    public static ArrayList<Secret> retrieveAllSecrets(Context context) throws ClassCastException {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        Map<String, ?> allSecretsStrings = preferences.getAll();
        ArrayList<Secret> allSecrets = new ArrayList<>();
        for(String key : allSecretsStrings.keySet()){
            try {
                if(allSecretsStrings.get(key) instanceof String) {
                    allSecrets.add(new Secret((String)allSecretsStrings.get(key)));
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return allSecrets;
    }

    /**
     *Retrieves a specific secret.
     * @return Get a specific secret
     */
    public static Secret getSecret(Context context, String secretOwner, String secretName) throws ClassCastException, JSONException {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        String secretString = preferences.getString(secretOwner+"-"+secretName, "");
        if(!secretString.equals("")){
           return new Secret(secretString);
        }
        return null;
    }


    /**
     * Stores a Set<String> of all secrets with the same owner under the key 'owner'.
     */
    public static void storeSecret(Context context, Secret secret) throws JSONException{
        assert secret != null;
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        Secret otherSecret = getSecret(context, secret.getOwner(), secret.getName());
        if(otherSecret!= null){
            for(Share share : secret.getShares()){
                if(secret.getShares().contains(share)) {
                    secret.addShare(share);
                }
            }
        }
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString(secret.getOwner()+"-"+secret.getName(), secret.toJSON());
        edit.apply();
    }

    /**
     * Deletes a the given secret from the stored secrets
     */
    public static void deleteSecret(Context context, Secret secret) throws JSONException{
        assert secret != null;
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);

        Set<String> messageSet;

        if(preferences.contains(secret.getOwner())){
            messageSet = preferences.getStringSet(secret.getName(), new HashSet<>());
            //TODO store by secret name instead of owner
        }else {
            messageSet = new HashSet<>();
        }
        messageSet.remove(secret.toJSON());

        SharedPreferences.Editor edit = preferences.edit();
        edit.putStringSet(secret.getOwner(),messageSet);
        edit.apply();
    }
}
