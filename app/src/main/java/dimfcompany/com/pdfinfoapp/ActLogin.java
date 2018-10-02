package dimfcompany.com.pdfinfoapp;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import dimfcompany.com.pdfinfoapp.Interfaces.IGetPassCallback;
import spencerstudios.com.bungeelib.Bungee;

public class ActLogin extends AppCompatActivity
{
    private static final String TAG = "ActLogin";
    EditText etPass;
    Button ok,cancel;
    boolean canExit = false;
    GlobalHelper gh;
    String currentPass;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        init();
        getPass();
    }

    private void getPass()
    {
        gh.getCurrentPass(this, new IGetPassCallback()
        {
            @Override
            public void onSuccess(String pass)
            {
                Log.e(TAG, "onSuccess: " +pass);
                currentPass = pass;
            }

            @Override
            public void onError()
            {
                Toast.makeText(gh, "Got some Errrororor", Toast.LENGTH_SHORT).show();
                currentPass="12411241";
            }
        });
    }

    private void init()
    {
        etPass = findViewById(R.id.etPass);
        ok = findViewById(R.id.btnEnter);
        cancel = findViewById(R.id.btnCancel);
        gh = (GlobalHelper)getApplicationContext();

        ok.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String pass = etPass.getText().toString();
                if (pass.equals(currentPass))
                {
                    canExit = true;
                    gh.setNeedToLogin(false);
                    onBackPressed();
                }
                else
                    {
                        Toast.makeText(ActLogin.this, "Пароль не верен.", Toast.LENGTH_SHORT).show();
                    }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                canExit = true;
                ActivityCompat.finishAffinity(ActLogin.this);
            }
        });

        etPass.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
            {
                if (i == EditorInfo.IME_ACTION_DONE)
                {
                    ok.performClick();
                    return true;
                }
                return false;
            }
        } );
    }

    @Override
    public void onBackPressed()
    {
        if (canExit)
        {
            super.onBackPressed();
            Bungee.zoom(this);
        }
    }
}
