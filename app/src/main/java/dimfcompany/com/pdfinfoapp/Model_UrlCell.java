package dimfcompany.com.pdfinfoapp;

import android.view.View;

public class Model_UrlCell
{
    String name;
    String urlImg;
    String url;
    int v;

    int Id;

    String localImgName;

    View scrollView;

    //region Constractor
    public Model_UrlCell()
    {

    }
    //endregion

    //region Getter Setter
    public View getScrollView()
    {
        return scrollView;
    }

    public void setScrollView(View scrollView)
    {
        this.scrollView = scrollView;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getUrlImg()
    {
        return urlImg;
    }

    public void setUrlImg(String urlImg)
    {
        this.urlImg = urlImg;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public int getV()
    {
        return v;
    }

    public void setV(int v)
    {
        this.v = v;
    }

    public int getId()
    {
        return Id;
    }

    public void setId(int id)
    {
        Id = id;
    }

    public String getLocalImgName()
    {
        return localImgName;
    }

    public void setLocalImgName(String localImgName)
    {
        this.localImgName = localImgName;
    }
    //endregion
}
