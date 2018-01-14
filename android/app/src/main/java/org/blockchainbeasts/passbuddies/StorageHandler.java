package org.blockchainbeasts.passbuddies;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;

import java.util.HashSet;
import java.util.Set;

public class StorageHandler {

    public static final String PREFERENCE_NAME = "keyshare";

    private Context context;

    public StorageHandler(Context context){
        this.context = context;
    }

    /**
     *Returns a set of all messages (as JSON strings) of the given owner
     * @param owner
     * @return Set<String> Set of messages
     */
    public Set<String> retrieveMessages(String owner) throws ClassCastException {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        return preferences.contains(owner) ? preferences.getStringSet(owner, new HashSet<>()) : new HashSet<>();
    }

    /**
     * Stores a Set<String> of all messages with the same owner under the key 'owner'.
     * @param message
     */
    public void storeMessage(Message message) {
        if(message == null) return;
        try{
            SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);

            Set<String> messageSet;
            try{
                messageSet = preferences.getStringSet(message.getOwner(), new HashSet<>());
            }catch(ClassCastException e){
                messageSet = new HashSet<>();
            }

            messageSet.add(message.toJSON());

            SharedPreferences.Editor edit = preferences.edit();
            edit.putStringSet(message.getOwner(),messageSet);
            edit.commit();

        } catch (JSONException e) {
            System.out.println(e.getMessage());
            System.out.println(e.getStackTrace().toString());
        }
    }
}
