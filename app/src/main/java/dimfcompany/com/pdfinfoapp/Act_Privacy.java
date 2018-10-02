package dimfcompany.com.pdfinfoapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import spencerstudios.com.bungeelib.Bungee;

public class Act_Privacy extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act__privacy);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Bungee.zoom(this);
    }
}
