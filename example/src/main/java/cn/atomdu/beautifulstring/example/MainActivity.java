package cn.atomdu.beautifulstring.example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.View;
import android.widget.TextView;

import cn.atomdu.beautifulstring.BeautifulString;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void onClickTextView(View v){
        SpannableString ss = BeautifulString.get(this)
                .style(android.graphics.Typeface.BOLD, 2, BeautifulString.MAX_LENGTH, true)
                .append("Hello")
                .color(R.color.colorAccent)
                .append(" ")
                .append("World")
                .color(R.color.colorPrimary)
                .build();

        TextView textView = findViewById(R.id.textView);
        textView.setText(ss);
    }
}
