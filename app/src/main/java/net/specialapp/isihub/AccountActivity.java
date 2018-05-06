package net.specialapp.isihub;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class AccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = getSharedPreferences("ISIHubPerf", Context.MODE_PRIVATE);
        int auth = sharedPref.getInt("auth",0);
        if(auth==0)
        {
            Intent start = new Intent(this, LoginActivity.class);
            startActivity(start);
            finish();
        }

        setContentView(R.layout.activity_account);
    }

    protected void logout(View view)
    {
        SharedPreferences sharedPref = getSharedPreferences("ISIHubPerf",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("auth", 0);
        editor.putString("head", "");
        editor.putString("username", "");
        editor.commit();
        Intent start = new Intent(AccountActivity.this, LoginActivity.class);
        startActivity(start);
        finish();
    }
}
