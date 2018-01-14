package org.blockchainbeasts.passbuddies;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;

import java.util.HashSet;
import java.util.Set;

public class StorageHandler {

    public static final String PREFERENCE_NAME = "keyshare";

    /**
     *Returns a set of all messages (as JSON strings) of the given owner
     * @param owner
     * @return Set<String> Set of messages or null
     */
    public static Set<Message> retrieveMessages(Context context, String owner) throws ClassCastException {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        Set<String> set =  preferences.contains(owner) ? preferences.getStringSet(owner, new HashSet<>()) : new HashSet<>();
        Set<Message> messageSet = new HashSet<>();
        for(String s : set){
            try {
                messageSet.add(new Message(s));
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return messageSet;
    }

    /**
     * Stores a Set<String> of all messages with the same owner under the key 'owner'.
     * @param message
     */
    public static void storeMessage(Context context, Message message) {
        if(message == null) return;
        try{
            SharedPreferences preferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);

            Set<String> messageSet;

            if(preferences.contains(message.getOwner())){
                messageSet = preferences.getStringSet(message.getOwner(), new HashSet<>());
            }else {
                messageSet = new HashSet<>();
            }

            messageSet.add(message.toJSON());

            SharedPreferences.Editor edit = preferences.edit();
            edit.putStringSet(message.getOwner(),messageSet);
            edit.commit();

        } catch (JSONException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
