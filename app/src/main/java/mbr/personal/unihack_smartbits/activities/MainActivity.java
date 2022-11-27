package mbr.personal.unihack_smartbits.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import mbr.personal.unihack_smartbits.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);
        ImageView loading = findViewById(R.id.loading_screen);
        Glide.with(this)
                .load("http://85.120.206.70:8080/api/v1/files/?query=loading_screen")
                .fitCenter()
                .into(loading);
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(loginIntent);
        }, 1000);
    }
}
