package net.brach.android.animview.example;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import net.brach.android.animview.TextAnimView;

public class MainActivity extends AppCompatActivity {
    private boolean toggleAnim;
    private boolean toggleText;
    private boolean toggleColor;
    private boolean toggleTextColor;
    private String name;
    private String eman;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name = getString(R.string.app_name);
        eman = new StringBuilder(name).reverse().toString();

        ((TextAnimView) findViewById(R.id.auto)).start();

        final TextAnimView anim = (TextAnimView) findViewById(R.id.anim);
        anim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toggleAnim) {
                    anim.stop();
                } else {
                    anim.start();
                }
                toggleAnim = !toggleAnim;
            }
        });

        final TextAnimView text = (TextAnimView) findViewById(R.id.text);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toggleText) {
                    text.setText(name, true);
                } else {
                    text.setText(eman, true);
                }
                toggleText = !toggleText;
            }
        });

        final TextAnimView color = (TextAnimView) findViewById(R.id.color);
        color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toggleColor) {
                    color.setTextColor(Color.BLACK, true);
                } else {
                    color.setTextColor(Color.BLUE, true);
                }
                toggleColor = !toggleColor;
            }
        });

        final TextAnimView textColor = (TextAnimView) findViewById(R.id.textColor);
        textColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toggleTextColor) {
                    textColor.setTextAndColor(name, Color.BLACK, true);
                } else {
                    textColor.setTextAndColor(eman, Color.BLUE, true);
                }
                toggleTextColor = !toggleTextColor;
            }
        });
    }
}
