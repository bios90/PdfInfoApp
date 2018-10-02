package dimfcompany.com.pdfinfoapp;

import android.content.pm.ActivityInfo;
import android.media.audiofx.BassBoost;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;

public class ActPdfView extends AppCompatActivity implements OnPageChangeListener
{
    PDFView pdfView;
    GlobalHelper gh;
    TextView tvPagesIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_view);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        init();
        loadPdf();
    }



    private void init()
    {
        pdfView = findViewById(R.id.pdfView);
        gh = (GlobalHelper)getApplicationContext();
        tvPagesIndicator = findViewById(R.id.tvPagesIndicactor);
    }

    private void loadPdf()
    {
        try
        {
            pdfView.fromFile(gh.getPdfToShow())
                    .enableDoubletap(true)
                    .pageSnap(true)
                    .onPageChange(this)
                    .spacing(4)
                    .load();
        }
        catch (Exception e)
        {

        }
    }

    @Override
    public void onPageChanged(int page, int pageCount)
    {
        tvPagesIndicator.setText("Страница "+(page+1)+"/"+pageCount);
    }
}
