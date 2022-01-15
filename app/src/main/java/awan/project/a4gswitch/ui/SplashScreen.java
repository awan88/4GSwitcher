package awan.project.a4gswitch.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import awan.project.a4gswitch.R;
import awan.project.a4gswitch.ui.home.MainActivity;

/**
 * This class awan.project.a4gswitch.ui.home
 * Created by Awan on 14/01/2022.
 * Github github.com/awan88
 */

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {
    ImageView myView;
    int timer = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_sceen);

        myView = findViewById(R.id.imageview);

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(myView, "alpha",  1f, .3f);
        fadeOut.setDuration(1600);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(myView, "alpha", .3f, 1f);
        fadeIn.setDuration(1600);

        final AnimatorSet mAnimationSet = new AnimatorSet();

        mAnimationSet.play(fadeIn).after(fadeOut);

        mAnimationSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mAnimationSet.start();
                timer++;

                if(timer==2){
                    Intent next = new Intent(SplashScreen.this, MainActivity.class);
                    SplashScreen.this.startActivity(next);
                    SplashScreen.this.finish();

                }
            }
        });
        mAnimationSet.start();

    }

}
