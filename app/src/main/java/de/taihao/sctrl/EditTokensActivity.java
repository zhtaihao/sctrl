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
    Map<String, String> tokens;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edittokens);
        findViewById(R.id.button_done).setEnabled(false);
        tokens = new HashMap<>();
    }

    public void AddTokens(@NotNull Map<String, String> tokens) {
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        /**
         * adds all of the user defined tokens to the sharedPreferences file
         */
        for (Map.Entry<String, String> token : tokens.entrySet()) {
            editor.putString(token.getKey(), token.getValue());
        }
        editor.commit();
    }

    public void buttonAddOnClick(View view){
        Button buttonAdd = findViewById(R.id.button_add);
        Button buttonDone = findViewById(R.id.button_done);
        EditText editName = findViewById(R.id.editTextName);
        EditText editToken = findViewById(R.id.editTextToken);
        String name = editName.getText().toString();
        String token = editToken.getText().toString();

        tokens.put(name, token);

        editName.setText("");
        editToken.setText("");

        buttonAdd.setText(getString(R.string.button_add_another));
        buttonDone.setEnabled(true);
    }

    public void buttonDoneOnClick(View view){
        AddTokens(tokens);
    }
}
