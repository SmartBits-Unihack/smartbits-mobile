package mbr.personal.unihack_smartbits.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import mbr.personal.unihack_smartbits.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView loading = findViewById(R.id.loading_screen_animation);
        Glide.with(this)
                .load("http://85.120.206.70:8080/api/v1/files/?query=loading_screen")
                .fitCenter()
                .into(loading);

    }
}
