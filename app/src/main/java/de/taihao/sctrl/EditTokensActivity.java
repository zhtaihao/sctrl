package de.taihao.sctrl;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class EditTokensActivity extends AppCompatActivity {
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edittokens);
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
    }

    public void AddTokens(@NotNull Map<String, String> tokens) {
        /**
         * adds all of the user defined tokens to the sharedPreferences file
         */
        for (Map.Entry<String, String> token : tokens.entrySet()) {
            editor.putString(token.getKey(), token.getValue());
        }
    }
}
