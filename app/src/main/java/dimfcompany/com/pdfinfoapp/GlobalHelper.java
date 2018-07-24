package dimfcompany.com.pdfinfoapp;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.File;
import java.util.Random;

public class GlobalHelper extends Application
{
    public static final String DATA = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    public static Random RANDOM = new Random();

    private File pdfToShow;

    public static String randomStr()
    {
        int len = 20;
        StringBuilder sb = new StringBuilder(len);
        for(int a = 0;a<=len;a++)
        {
            sb.append(DATA.charAt(RANDOM.nextInt(DATA.length())));
        }
        return sb.toString();
    }

    public boolean isNetworkAvailable()
    {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //region getter and setter
    public File getPdfToShow()
    {
        return pdfToShow;
    }

    public void setPdfToShow(File pdfToShow)
    {
        this.pdfToShow = pdfToShow;
    }
    //endregion
}
