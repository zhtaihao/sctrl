package de.taihao.sctrl;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class EditTokensActivity extends AppCompatActivity {
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    Map<String, String> tokens = new HashMap<>();

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

    public void buttonAddOnClick(View view){
        Button buttonAdd = findViewById(R.id.button_add);
        String name = ((EditText) findViewById(R.id.editTextName)).getText().toString();
        String token = ((EditText) findViewById(R.id.editTextToken)).getText().toString();

        tokens.put(name, token);
        buttonAdd.setText(getString(R.string.button_add_another));

    }
}
