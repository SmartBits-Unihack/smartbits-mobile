package mbr.personal.unihack_smartbits.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import mbr.personal.unihack_smartbits.R;
import mbr.personal.unihack_smartbits.exceptions.AuthenticationException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    public static String accessToken = null;
    public static String refreshToken = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        EditText username = findViewById(R.id.etx_username);
        EditText password = findViewById(R.id.etx_password);
        Button login = findViewById(R.id.btn_login);

        login.setOnClickListener(v -> {
            try {
                login(username.getText().toString(), password.getText().toString());
                // goto next activity
                Intent bluetoothIntent = new Intent(this, BluetoothActivity.class);
                startActivity(bluetoothIntent);
            } catch (InterruptedException exc) {
                Log.e(TAG, "The thread running the log in function was interrupted.");
            }
        });

    }

    private void login(String username, String password)
            throws InterruptedException {
        String url = "http://85.120.206.70:8080/api/v1/users/signin";
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        JSONObject payload = new JSONObject();
        try {
            payload.put("username", username);
            payload.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(payload.toString(), JSON);
        Thread loginThread = new Thread(() -> {
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    Objects.requireNonNull(response.body());
                    JSONObject responseJson = new JSONObject(response.body().string());
                    LoginActivity.accessToken = responseJson.get("access_token").toString();
                    LoginActivity.refreshToken = responseJson.get("refresh_token").toString();
                } else {
                    throw new AuthenticationException(response.body().string());
                }
            } catch (AuthenticationException exc) {
                Log.e(TAG, "Authentication failed! With message below: ");
                Log.e(TAG, exc.getMessage());
            } catch (JSONException exc) {
                Log.e(TAG, "Failed packing the payload for the request!");
                Log.e(TAG, exc.getMessage());
            } catch (IOException exc) {
                Log.e(TAG, "Failed to make the request to the server.");
                Log.e(TAG, exc.getMessage());
            }
        });

        loginThread.start();

        loginThread.join();

    }

}
