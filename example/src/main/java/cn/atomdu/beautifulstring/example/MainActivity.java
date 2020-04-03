package cn.atomdu.beautifulstring.example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import cn.atomdu.beautifulstring.BeautifulString;

public class MainActivity extends AppCompatActivity {

    TextView textView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
    }

    public void onClickButton(View v){
        SpannableString ss = BeautifulString.get(textView)
                .style(android.graphics.Typeface.BOLD, 2, BeautifulString.MAX_LENGTH, true)
                .append(" Hello")
                .color(R.color.colorAccent)
                .append(" Beautiful")
                .color(R.color.colorPrimary)
                .append(" String")
                .onClick(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this,"click",Toast.LENGTH_SHORT).show();
                    }
                })
                .color(R.color.colorAccent)
                .build();

        textView.setText(ss);
    }
}
