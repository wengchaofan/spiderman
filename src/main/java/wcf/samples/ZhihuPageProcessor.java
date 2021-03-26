package wcf.samples;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;
import wcf.entity.Massage;
import wcf.utils.HttpUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 43574
 */
public class ZhihuPageProcessor implements PageProcessor {
    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000);

    private static final String FAVLISTS = "https://www.zhihu.com/api/v4/favlists/discover?limit=500&offset=20";
    private static final String EXPLORE = "https://www.zhihu.com/explore";
    private static final String API = "https://www.zhihu.com/api/v4/collections/${id}/items?offset=0&limit=200";
    private static final String COLLECTION = "https://www.zhihu.com/collection/";


    public ZhihuPageProcessor() {
    }

    @Override
    public void process(Page page) {
        String url = page.getRequest().getUrl();
        if (url.contains(COLLECTION)) {
            page.addTargetRequests(getUrls(url));
        }
        page.addTargetRequests(page.getHtml().links().regex("https://www.zhihu.com/question/\\d+$").all());
        page.addTargetRequests(page.getHtml().links().regex("https://www.zhihu.com/answer/\\d+$").all());
        page.addTargetRequests(page.getHtml().links().regex("https://www.zhihu.com/collection/.*$").all());
        Selectable itemInner = page.getHtml().css("div.NumberBoard-itemInner");
        List<Selectable> nodes = itemInner.nodes();
        String title = page.getHtml().xpath("//h1[@class='QuestionHeader-title']/text()").toString();
        Massage massage = new Massage();
        massage.setTitle(title);
        massage.setUrl(url);

        nodes.forEach(node -> {
            String name = node.xpath("div[@class='NumberBoard-itemName']/text()").toString();
            String value = node.xpath("strong[@class='NumberBoard-itemValue']/text()").toString().replace(",", "");
            if ("关注者".equals(name)) {
                massage.setAttention(Integer.parseInt(value));
                page.putField("attention", value);
            } else if ("被浏览".equals(name)) {
                massage.setBrowse(Integer.parseInt(value));
                page.putField("browse", value);
            }

        });
        page.putField("title", title);
        if (page.getResultItems().get("title") == null) {
            page.setSkip(true);
        }

    }

    private List<String> getUrls(String collection) {
        int n = collection.lastIndexOf("/")+1;
        String id = collection.substring(n);
        String api = API.replace("${id}", id);
        JSONObject httpContent = HttpUtils.getHttpContent(api);
        if (httpContent == null) {
            return new ArrayList<>(0);
        }
        JSONArray data = httpContent.getJSONArray("data");
        List<String> urls = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            JSONObject jsonObject = data.getJSONObject(i).getJSONObject("content");
            String url = jsonObject.getString("url");
            urls.add(url);
        }
        return urls;
    }

    @Override
    public Site getSite() {
        return this.site;
    }

    public static void main(String[] args) {

        JSONObject jsonObject = HttpUtils.getHttpContent(FAVLISTS);
        assert jsonObject != null;
        JSONArray data = jsonObject.getJSONArray("data");
        List<String> urls = new ArrayList<>(520);
        for (int i = 0; i < data.size(); i++) {
            String url = data.getJSONObject(i).getString("url");
            String collection = url.substring(url.lastIndexOf('/')+1);
            url = COLLECTION + collection;
            urls.add(url);
        }
        data.add(EXPLORE);
        String[] objects = urls.toArray(new String[0]);
        Spider.create(new ZhihuPageProcessor()).addUrl().addUrl(objects).run();
    }

}