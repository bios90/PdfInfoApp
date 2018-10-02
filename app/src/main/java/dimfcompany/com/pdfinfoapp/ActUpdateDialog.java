package dimfcompany.com.pdfinfoapp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import spencerstudios.com.bungeelib.Bungee;

public class ActUpdateDialog extends AppCompatActivity
{

    Button now,later;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_dialog);

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        init();
        settingListeners();
    }

    private void init()
    {
        now = findViewById(R.id.btnNow);
        later = findViewById(R.id.btnLater);
    }

    private void settingListeners()
    {
        later.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                onBackPressed();
            }
        });

        now.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                DownloadHelper dh = new DownloadHelper(ActUpdateDialog.this);
                dh.fullUpdate();
                Intent returnIntent = new Intent();
                setResult(ActItemsScroll.RESULT_OK, returnIntent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        Intent returnIntent = new Intent();
        setResult(ActItemsScroll.RESULT_CANCELED, returnIntent);
        finish();
        Bungee.shrink(this);
        super.onBackPressed();
    }
}
