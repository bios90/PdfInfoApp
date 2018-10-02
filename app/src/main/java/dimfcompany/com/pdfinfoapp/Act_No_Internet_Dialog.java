package dimfcompany.com.pdfinfoapp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Act_No_Internet_Dialog extends AppCompatActivity
{
    GlobalHelper gh;
    Button ok,cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_no_internet_dialog);

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        init();

    }

    private void init()
    {
        gh=(GlobalHelper)getApplicationContext();
        ok= findViewById(R.id.btnEnter);
        cancel = findViewById(R.id.btnCancel);

        cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ActivityCompat.finishAffinity(Act_No_Internet_Dialog.this);
            }
        });

        ok.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(Act_No_Internet_Dialog.this,ActFirst.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}
