package de.taihao.sctrl;

import android.content.Context;
import android.content.SharedPreferences;
import android.app.Activity;

import java.util.Map;


public class EditTokens extends Activity {
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    public void AddTokens(Map<String, String> tokens) {
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        /**
         * adds all of the user defined tokens to the sharedPreferences file
         */
        for (Map.Entry<String, String> token : tokens.entrySet()) {
            editor.putString(token.getKey(), token.getValue());
        }
    }
    

}
