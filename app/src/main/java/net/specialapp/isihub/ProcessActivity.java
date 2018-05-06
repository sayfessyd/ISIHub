package net.specialapp.isihub;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProcessActivity extends AppCompatActivity {

    private LinearLayout LayoutView;
    private View FormView;
    private Button RequestButton;
    private RequestTask RequestTaskObject = null;
    private FormTask FormTaskObject = null;
    private List<String> fields = new ArrayList<String>();
    private List<String> values = new ArrayList<String>();
    private List<String> types = new ArrayList<String>();
    private String token;
    private String username;
    private String process_id;
    private int color;
    private int icon;
    private int auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process);

        SharedPreferences sharedPref = getSharedPreferences("ISIHubPerf", Context.MODE_PRIVATE);
        auth = sharedPref.getInt("auth", 0);
        token = sharedPref.getString("token", "");
        username = sharedPref.getString("username", "");
        if (auth == 0) {
            Intent start = new Intent(this, LoginActivity.class);
            startActivity(start);
            finish();
        }

        process_id = getIntent().getStringExtra("process_id");
        color = getIntent().getIntExtra("color", R.color.color5);
        icon = getIntent().getIntExtra("icon", R.drawable.ic_content_copy_white_48dp);

        LayoutView = findViewById(R.id.process_layout);
        FormView = findViewById(R.id.process_form);
        ImageView iv = findViewById(R.id.imageView);
        LayoutView.setBackgroundResource(color);
        iv.setImageResource(icon);

        FormTaskObject = new FormTask();
        FormTaskObject.execute();

        RequestButton = (Button) findViewById(R.id.request_button);
        RequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendRequest();
            }
        });
    }

    private void sendRequest() {
        if (RequestTaskObject != null) {
            return;
        }
        RequestTask task = new RequestTask();
        task.execute();
    }

    public class RequestTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            EditText et;
            String json_string = "{\n\"variables\":";
            String variables = "{\n\t";
            String suffix;
            for (int i = 0; i < fields.size(); i++) {
                et = (EditText) findViewById(i);
                values.add(et.getText().toString());
                suffix = ",\n\t";
                if (i == fields.size() - 1)
                    suffix = "\n}";
                variables += "\"" + fields.get(i) + "\":{\"type\":\"" + types.get(i) + "\",\"value\":\"" + et.getText().toString() + "\",\"valueInfo\":{}}" + suffix;
            }
            json_string += variables;
            json_string += ",\n\"businessKey\":\"" + username + "\"\n}";
            Log.v("JSON Request Body", json_string);
            try {
                OkHttpClient client = new OkHttpClient();
                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, json_string);
                Request request = new Request.Builder()
                        .url("http://52.17.63.166:8080/engine-rest/process-definition/" + process_id + "/submit-form")
                        .post(body)
                        .addHeader("Authorization", "Basic " + token)
                        .addHeader("Cache-Control", "no-cache")
                        .addHeader("Content-Type", "application/json")
                        .build();
                Response response = client.newCall(request).execute();
                if (response.code() == 200) {
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
                Toast.makeText(ProcessActivity.this, "Request sent successfully !", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ProcessActivity.this, "Request Failure !", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            RequestTaskObject = null;
        }

    }

    public class FormTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://52.17.63.166:8080/engine-rest/process-definition/" + process_id + "/form-variables")
                        .get()
                        .addHeader("Authorization", "Basic " + token)
                        .addHeader("Cache-Control", "no-cache")
                        .build();
                Response response = client.newCall(request).execute();
                if (response.code() == 200) {
                    String json = response.body().string();
                    Log.v("JSON Response body", json);
                    try {
                        JSONObject obj = new JSONObject(json);
                        String field;
                        for (Iterator<String> keys = obj.keys(); keys.hasNext(); ) {
                            field = keys.next();
                            fields.add(field);
                            types.add(obj.getJSONObject(field).getString("type"));
                        }
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
                Toast.makeText(ProcessActivity.this, "Form added successfully !", Toast.LENGTH_SHORT).show();
                String field;
                String type;
                for (int i = fields.size() - 1; i >= 0; i--) {
                    field = fields.get(i);
                    type = types.get(i);
                    LinearLayout ll = (LinearLayout) findViewById(R.id.form_layout);
                    TextInputLayout til = new TextInputLayout(ProcessActivity.this);
                    til.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    EditText et = new EditText(ProcessActivity.this);
                    et.setId(i);
                    et.setHint(field);
                    int inputType = View.TEXT_ALIGNMENT_CENTER;
                    if (type.equals("String"))
                        inputType = View.TEXT_ALIGNMENT_CENTER;
                    et.setRawInputType(inputType);
                    et.setMaxLines(1);
                    et.setTextColor(getResources().getColor(android.R.color.primary_text_dark));
                    et.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    til.addView(et);
                    ll.addView(til, 1);
                }
            } else {
                Toast.makeText(ProcessActivity.this, "Form Failure !", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled() {
            FormTaskObject = null;
        }

    }

    @Override
    public void onBackPressed() {
        FormView.setVisibility(View.GONE);
        super.onBackPressed();
    }

}
