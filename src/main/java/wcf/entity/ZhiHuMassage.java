package wcf.entity;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author 43574
 */
//@TableName("zhi_hu_massage")
@Entity
@Table(name = "zhi_hu_massage")
public class ZhiHuMassage {

    @Id
    private long id;
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

    public ZhiHuMassage() {

    }

    public ZhiHuMassage(String title, int attention, int browse) {
        this.title = title;
        this.attention = attention;
        this.browse = browse;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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