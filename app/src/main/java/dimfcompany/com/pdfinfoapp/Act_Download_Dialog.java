package dimfcompany.com.pdfinfoapp;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.daimajia.numberprogressbar.OnProgressBarListener;

import java.util.Timer;
import java.util.TimerTask;

import spencerstudios.com.bungeelib.Bungee;


public class Act_Download_Dialog extends AppCompatActivity implements OnProgressBarListener
{
    private static final String TAG = "Act_Download_Dialog";

    private TextView tv;
    private NumberProgressBar progressBar;
    GlobalHelper gh;
    Timer timer;

    boolean canExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_dialog);

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        tv = findViewById(R.id.progressTv);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setOnProgressBarListener(this);

        gh = (GlobalHelper)getApplicationContext();
        gh.setCurrentDialog(this);

        tv.setText(gh.getDialogTitle());

        Log.e(TAG, "onCreate: on create dialog finished" );

    }



    public TextView getTv()
    {
        return tv;
    }

    public void setTv(TextView tv)
    {
        this.tv = tv;
    }

    public NumberProgressBar getProgressBar()
    {
        return progressBar;
    }

    public void setProgressBar(NumberProgressBar progressBar)
    {
        this.progressBar = progressBar;
    }

    @Override
    public void onProgressChange(int current, int max)
    {
        Log.e(TAG, "onProgressChange: called");
        Log.e(TAG, "onProgressChange: maxx" + max );
        Log.e(TAG, "onProgressChange: current" + current);
        if(current == max)
        {
            Toast.makeText(this, "База успешно обновлена", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "onProgressChange: now will finishh" );
            canExit = true;
            onBackPressed();
        }
    }


    @Override
    public void onBackPressed()
    {
        if(canExit)
        {
            super.onBackPressed();
            Bungee.shrink(this);
        }
    }
}
