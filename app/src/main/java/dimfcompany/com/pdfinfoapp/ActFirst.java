package dimfcompany.com.pdfinfoapp;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import spencerstudios.com.bungeelib.Bungee;

public class ActFirst extends AppCompatActivity
{
    private static final String TAG = "ActFirst";
    GlobalHelper gh;

    RelativeLayout laLite,laSmart,laPanir,laDrinks,laKitchen,laFirstStart,laRefresh,laUrls,laProduct;
    TextView tvUpdate;
    ImageView imgArrow;
    DatabaseHelper dbHelper;
    SQLiteDatabase db;
    Boolean rowExists;

    ImageView logoImg;
    ImageView infoImg;
    
    //////////
    RelativeLayout drawerLa;
    DrawerLayout drawer;
    TextView testTv;
    //////////

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_first);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        init();
        dbCheck();
        settingListeners();

        if(gh.isNeedToLogin())
        {

            if(!gh.isNetworkAvailable())
            {
                Intent intent = new Intent(ActFirst.this, Act_No_Internet_Dialog.class);
                startActivity(intent);
                Bungee.zoom(this);
                return;
            }

            Intent intent = new Intent(ActFirst.this, ActLogin.class);
            startActivity(intent);
            Bungee.zoom(this);
        }
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

                dbCheck();
                if (rowExists)
                {
                    laRefresh.setEnabled(false);
                    Log.e(TAG, "onClick: Update now Will be" );
                    DownloadHelper dh = new DownloadHelper(ActFirst.this);
                    dh.fullUpdate();
                    laFirstStart.setVisibility(View.GONE);
                }
                else
                    {
                        laRefresh.setEnabled(false);
                        Log.e(TAG, "onClick: First Download Will be" );
                        DownloadHelper dh = new DownloadHelper(ActFirst.this);
                        dh.firstDownload();
                        laFirstStart.setVisibility(View.GONE);
                        tvUpdate.setText("Проверить обновления");
                    }
            }
        });

        laLite.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                gh.setCurrentCategToShow(0);
                Intent intent = new Intent(ActFirst.this,ActItemsScroll.class);
                startActivity(intent);
            }
        });

        laSmart.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                gh.setCurrentCategToShow(1);
                Intent intent = new Intent(ActFirst.this,ActItemsScroll.class);
                startActivity(intent);
            }
        });

        laKitchen.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                gh.setCurrentCategToShow(2);
                Intent intent = new Intent(ActFirst.this,ActItemsScroll.class);
                startActivity(intent);
            }
        });

        laDrinks.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                gh.setCurrentCategToShow(3);
                Intent intent = new Intent(ActFirst.this,ActItemsScroll.class);
                startActivity(intent);
            }
        });

        laPanir.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                gh.setCurrentCategToShow(4);
                Intent intent = new Intent(ActFirst.this,ActItemsScroll.class);
                startActivity(intent);
            }
        });

        laProduct.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                gh.setCurrentCategToShow(5);
                Intent intent = new Intent(ActFirst.this,ActItemsScroll.class);
                startActivity(intent);
            }
        });

        laUrls.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(ActFirst.this,ActUrlScroll.class);
                startActivity(intent);
            }
        });

        infoImg.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(ActFirst.this,Act_Privacy.class);
                startActivity(intent);
                Bungee.zoom(ActFirst.this);
            }
        });

//        testTv.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View view)
//            {
//                drawer.closeDrawer(Gravity.START,true);
//                Toast.makeText(gh, "Buy Buy", Toast.LENGTH_SHORT).show();
//            }
//        });
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
        laUrls = findViewById(R.id.laUrls);
        laProduct = findViewById(R.id.laProduct);

        laFirstStart = findViewById(R.id.laFirstStart);
        tvUpdate = findViewById(R.id.tvUpdate);
        imgArrow = findViewById(R.id.imgArrow);

        logoImg = findViewById(R.id.logo);
        infoImg = findViewById(R.id.imgInfo);
        /////////////
//        drawer = findViewById(R.id.drawer);
//        drawerLa = findViewById(R.id.drawerLA);
//        testTv = findViewById(R.id.testTv);
        ////////////

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

    @Override
    protected void onResume()
    {
        super.onResume();
        laRefresh.setEnabled(true);
    }
}
