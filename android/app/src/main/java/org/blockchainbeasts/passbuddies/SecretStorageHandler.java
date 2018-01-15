package org.blockchainbeasts.passbuddies;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
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
        Map<String, Set<String>> allSecretsStrings = (Map<String, Set<String>>)preferences.getAll();
        ArrayList<Secret> allSecrets = new ArrayList<>();
        for(Set<String> secrets : allSecretsStrings.values()){
            try {
                for(String string : secrets) {
                    Secret secret = new Secret(string);
                    allSecrets.add(secret);
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return allSecrets;
    }

    /**
     * Stores a Set<String> of all messages with the same owner under the key 'owner'.
     */
    public static void storeSecret(Context context, Secret secret) throws JSONException{
        assert secret != null;
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);

        Set<String> messageSet;

        if(preferences.contains(secret.getOwner())){
            messageSet = preferences.getStringSet(secret.getName(), new HashSet<>());
            //TODO store by secret name instead of owner
        }else {
            messageSet = new HashSet<>();
        }
        messageSet.add(secret.toJSON());

        SharedPreferences.Editor edit = preferences.edit();
        edit.putStringSet(secret.getOwner(),messageSet);
        edit.apply();
    }
}
