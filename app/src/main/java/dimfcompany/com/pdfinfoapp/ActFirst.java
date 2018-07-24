package dimfcompany.com.pdfinfoapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ActFirst extends AppCompatActivity
{
    private static final String TAG = "ActFirst";
    GlobalHelper gh;

    RelativeLayout laLite,laSmart,laPanir,laDrinks,laKitchen,laFirstStart,laRefresh;
    TextView tvUpdate;
    ImageView imgArrow;
    DatabaseHelper dbHelper;
    SQLiteDatabase db;
    Boolean rowExists;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_first);
        init();
        dbCheck();
        settingListeners();



    }

    private void settingListeners()
    {
        laRefresh.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(!gh.isNetworkAvailable())
                {
                    Toast.makeText(gh, "Для загрузки неоходимо соединение с сетью.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (rowExists)
                {

                }
                else
                    {
                        Log.e(TAG, "onClick: DownloadFull begin" );
                        DownloadHelper dh = new DownloadHelper(ActFirst.this);
                        dh.firstDownload();
                        laFirstStart.setVisibility(View.GONE);
                    }
            }
        });

        laLite.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(ActFirst.this,ActLiteMenu.class);
                startActivity(intent);
            }
        });
    }


    //region InitVoid
    private void init()
    {
        gh = (GlobalHelper)this.getApplicationContext();
        dbHelper = new DatabaseHelper(this);

        laLite = findViewById(R.id.laLiteMenu);
        laSmart = findViewById(R.id.laSmartMenu);
        laPanir = findViewById(R.id.laPanir);
        laDrinks = findViewById(R.id.laDrinks);
        laKitchen = findViewById(R.id.laKitchen);
        laRefresh = findViewById(R.id.laRefresh);

        laFirstStart = findViewById(R.id.laFirstStart);
        tvUpdate = findViewById(R.id.tvUpdate);
        imgArrow = findViewById(R.id.imgArrow);
    }
    //endregion

    private void dbCheck()
    {
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

        Cursor mCursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_PDF_CELLS, null);

        if (mCursor.moveToFirst())
        {
            rowExists = true;
        } else
        {
            rowExists = false;
        }

        if (rowExists)
        {
            laFirstStart.setVisibility(View.GONE);
            tvUpdate.setText("Проверить обновления");
        }
        else
            {
                laFirstStart.setVisibility(View.VISIBLE);
                tvUpdate.setText("Скачать файлы");

                int animDuration = 600;
                TranslateAnimation move = new TranslateAnimation(0,0,0,40);
                move.setDuration(animDuration);
                move.setRepeatCount(Animation.INFINITE);
                move.setRepeatMode(Animation.REVERSE);

                imgArrow.startAnimation(move);
            }

    }
}
