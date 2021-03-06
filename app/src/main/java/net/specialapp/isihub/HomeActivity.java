package net.specialapp.isihub;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeActivity extends AppCompatActivity {

    private String token;
    private String username;
    protected LinearLayout homeLayout;
    private ProcessDefinitionTask ProcessDefinitionTaskObject = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        SharedPreferences sharedPref = getSharedPreferences("ISIHubPerf", Context.MODE_PRIVATE);
        int auth = sharedPref.getInt("auth", 0);
        token = sharedPref.getString("token", "");
        username = sharedPref.getString("username", "");
        if (auth == 0) {
            Intent start = new Intent(this, LoginActivity.class);
            startActivity(start);
            finish();
        }

        homeLayout = findViewById(R.id.home_layout);

        // Consider home_toolbar as an App Bar[=Action Bar] => adding app name
        Toolbar homeToolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(homeToolbar);

        // Show Logo on the App Bar
        /*getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.logo);
        getSupportActionBar().setDisplayUseLogoEnabled(true);*/

        ProcessDefinitionTaskObject = new ProcessDefinitionTask();
        ProcessDefinitionTaskObject.execute();

    }

    // Bind menu/appbar.xml view to menu  => adding menu options
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.appbar, menu);
        return true;
    }

    public void processStart(CardView view) {
        Intent intent = new Intent(this, ProcessActivity.class);
        intent.putExtra("process_id", (String) view.getTag(R.string.process_key));
        intent.putExtra("color", (Integer) view.getTag(R.string.color_key));
        ImageView iv = view.findViewById(R.id.imageView);
        intent.putExtra("icon", (Integer) iv.getTag());
        String transitionName = getString(R.string.transition_string);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, view, transitionName);
        //Start the Intent
        ActivityCompat.startActivity(this, intent, options.toBundle());
    }

    public class ProcessDefinitionTask extends AsyncTask<Void, Void, Boolean> {

        JSONArray processDefinitions;

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://52.17.63.166:8080/engine-rest/process-definition?startableBy=" + username)
                        .get()
                        .addHeader("Authorization", "Basic " + token)
                        .addHeader("Cache-Control", "no-cache")
                        .build();
                Response response = client.newCall(request).execute();
                if (response.code() == 200) {
                    String json = response.body().string();
                    Log.v("JSON Response body", json);
                    try {
                        processDefinitions = new JSONArray(json);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return true;
                } else {
                    return false;
                }

            } catch (IOException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                Toast.makeText(HomeActivity.this, "Process Definitions added successfully !", Toast.LENGTH_SHORT).show();
                try {
                    JSONObject obj;
                    int color = R.color.color1;
                    int color_lighter = R.color.color1_lighter;
                    int icon = R.drawable.ic_assignment_turned_in_white_48dp;
                    for (int i = 0; i < processDefinitions.length(); i++) {
                        obj = processDefinitions.getJSONObject(i);
                        if (obj.getString("name").equals("Attendance")) {
                            color = R.color.color1;
                            color_lighter = R.color.color1_lighter;
                            icon = R.drawable.ic_assignment_turned_in_white_48dp;
                        } else if (obj.getString("name").equals("Double Correction")) {
                            color = R.color.color5;
                            color_lighter = R.color.color5_lighter;
                            icon = R.drawable.ic_content_copy_white_48dp;
                        } else if (obj.getString("name").equals("Notes Record")) {
                            color = R.color.color2;
                            color_lighter = R.color.color2_lighter;
                            icon = R.drawable.ic_assignment_white_48dp;
                        }
                        homeLayout.addView(generateCard(obj.getString("id"), color, color_lighter, icon, obj.getString("name"), obj.getString("description")));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(HomeActivity.this, "Process Definitions Failure !", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            ProcessDefinitionTaskObject = null;
        }

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_item:
                Intent intent = new Intent(this, SettingsActivity.class);
                this.startActivity(intent);
                break;
            case R.id.account_item:
                Intent intent2 = new Intent(this, AccountActivity.class);
                this.startActivity(intent2);
                break;
            case R.id.server_item:
                Intent intent3 = new Intent(this, ProcessInstanceActivity.class);
                this.startActivity(intent3);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public CardView generateCard(String id, int color, int color_lighter, int icon, String name, String description) {
        CardView cv = new CardView(this);
        cv.setClickable(true);
        cv.setFocusable(true);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, convertDpToPx(100));
        params.setMargins(convertDpToPx(10), convertDpToPx(10), convertDpToPx(10), convertDpToPx(10));
        cv.setLayoutParams(params);
        cv.setCardBackgroundColor(getResources().getColor(color));
        cv.setRadius(convertDpToPx(4));
        cv.setTransitionName(getResources().getString(R.string.transition_string));
        cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processStart((CardView) view);
            }
        });
        cv.setId(this.getResources().getIdentifier(id, "string", this.getPackageName()));
        cv.setTag(R.string.process_key, id);
        cv.setTag(R.string.color_key, color);

        LinearLayout linearLayout_645 = new LinearLayout(this);
        linearLayout_645.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        linearLayout_645.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout linearLayout_951 = new LinearLayout(this);
        linearLayout_951.setOrientation(LinearLayout.VERTICAL);
        linearLayout_951.setGravity(Gravity.CENTER);
        linearLayout_951.setBackgroundResource(color_lighter);
        linearLayout_951.setLayoutParams(new LinearLayout.LayoutParams(convertDpToPx(100), LinearLayout.LayoutParams.MATCH_PARENT));

        ImageView imageView_280 = new ImageView(this);
        imageView_280.setImageResource(icon);
        imageView_280.setTag(icon);
        imageView_280.setId(R.id.imageView);
        imageView_280.setPaddingRelative(convertDpToPx(10), convertDpToPx(10), convertDpToPx(10), convertDpToPx(10));
        imageView_280.setContentDescription(getResources().getString(R.string.icon));
        imageView_280.setLayoutParams(new LinearLayout.LayoutParams(convertDpToPx(64), convertDpToPx(64)));
        linearLayout_951.addView(imageView_280);
        linearLayout_645.addView(linearLayout_951);

        LinearLayout linearLayout_854 = new LinearLayout(this);
        linearLayout_854.setOrientation(LinearLayout.VERTICAL);
        linearLayout_854.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams layout_938 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layout_938.setMarginStart(convertDpToPx(10));
        linearLayout_854.setLayoutParams(layout_938);

        TextView textView_954 = new TextView(this);
        textView_954.setTypeface(Typeface.DEFAULT_BOLD);
        textView_954.setTextColor(getResources().getColor(android.R.color.white));
        textView_954.setText(name);
        textView_954.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        linearLayout_854.addView(textView_954);

        TextView textView_373 = new TextView(this);
        textView_373.setGravity(Gravity.CENTER);
        textView_373.setText(description);
        textView_373.setPaddingRelative(convertDpToPx(0), convertDpToPx(0), convertDpToPx(0), convertDpToPx(0));
        textView_373.setTextColor(getResources().getColor(R.color.lightgray));
        textView_373.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        linearLayout_854.addView(textView_373);
        linearLayout_645.addView(linearLayout_854);
        cv.addView(linearLayout_645);
        return cv;
    }

    private int convertDpToPx(int dp) {
        return Math.round(dp * (getResources().getDisplayMetrics().xdpi / getResources().getDisplayMetrics().DENSITY_DEFAULT));
    }

}
