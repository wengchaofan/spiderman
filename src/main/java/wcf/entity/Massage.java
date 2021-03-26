package wcf.entity;


/**
 * @author 43574
 */
public class Massage {

    private String title;
    private int attention;
    private int browse;
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Massage() {

    }

    public Massage(String title, int attention, int browse) {
        this.title = title;
        this.attention = attention;
        this.browse = browse;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getAttention() {
        return attention;
    }

    public void setAttention(int attention) {
        this.attention = attention;
    }

    public int getBrowse() {
        return browse;
    }

    public void setBrowse(int browse) {
        this.browse = browse;
    }
}