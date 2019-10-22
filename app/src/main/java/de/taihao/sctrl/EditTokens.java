package de.taihao.sctrl;

import android.content.Context;
import android.content.SharedPreferences;
import android.app.Activity;

import java.util.Map;


public class EditTokens extends Activity {
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    public EditTokens(Map<String, String> tokens) {
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        for (Map.Entry<String, String> token : tokens.entrySet()) {
            editor.putString(token.getKey(), token.getValue());
        }
        editor.putString();
    }


}
