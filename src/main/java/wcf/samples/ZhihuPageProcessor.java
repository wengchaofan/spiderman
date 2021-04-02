package wcf.samples;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Selectable;
import wcf.dao.ZhiHuMassageDao;
import wcf.entity.ZhiHuMassage;
import wcf.utils.HttpUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author 43574
 */
@Component
public class ZhihuPageProcessor implements PageProcessor {
    private final Site site = Site.me().setRetryTimes(3).setSleepTime(2000);

    private static final String FAVLISTS = "https://www.zhihu.com/api/v4/favlists/discover?limit=1000&offset=20";
    private static final String EXPLORE = "https://www.zhihu.com/explore";
    private static final String COLLECTION_API = "https://www.zhihu.com/api/v4/collections/${id}/items?offset=0&limit=200";
    private static final String QUESTION_SIMILAR_API = "https://www.zhihu.com/api/v4/questions/${id}/similar-questions?limit=5";
    private static final String QUESTION_API = "https://www.zhihu.com/api/v4/questions/{id}}";
    private static final String COLLECTION = "https://www.zhihu.com/collection/";
    private static final String QUESTION = "https://www.zhihu.com/question/";
    private static final int QUESTION_ID_INDEX = "https://www.zhihu.com/question/".lastIndexOf('/') + 1;
    @Autowired
    private ZhiHuMassageDao zhihuMassageDao;

    private static volatile ZhiHuMassageDao staticZhiHuMassageDao;

    private static final Set<Long> IDS = new ConcurrentSkipListSet<>();

    public ZhihuPageProcessor() {
    }

    @Override
    public void process(Page page) {
        String url = page.getRequest().getUrl();
        if (url.contains(COLLECTION)) {
            page.addTargetRequests(getUrls(url));
        }
        if (url.contains(QUESTION)) {
            boolean record = dealQuestionId(url);
            if (record) {
                return;
            }
        }
        List<String> questionUrl = questionFilter(page.getHtml().links().regex("https://www.zhihu.com/question/\\d+$").all());
        page.addTargetRequests(questionUrl);
        page.addTargetRequests(page.getHtml().links().regex("https://www.zhihu.com/answer/\\d+$").all());
        page.addTargetRequests(page.getHtml().links().regex("https://www.zhihu.com/collection/.*$").all());
        Selectable itemInner = page.getHtml().css("div.NumberBoard-itemInner");
        List<Selectable> nodes = itemInner.nodes();
        String title = page.getHtml().xpath("//h1[@class='QuestionHeader-title']/text()").toString();

        if (title == null) {
            page.setSkip(true);
            return;
        }

        ZhiHuMassage zhiHuMassage = new ZhiHuMassage();
        zhiHuMassage.setTitle(title);
        zhiHuMassage.setUrl(url);
        zhiHuMassage.setId(getQuestionId(url));
        nodes.forEach(node -> {
            String name = node.xpath("div[@class='NumberBoard-itemName']/text()").toString();
            String value = node.xpath("strong[@class='NumberBoard-itemValue']/text()").toString().replace(",", "");
            if ("关注者".equals(name)) {
                zhiHuMassage.setAttention(Integer.parseInt(value));
                page.putField("attention", value);
            } else if ("被浏览".equals(name)) {
                zhiHuMassage.setBrowse(Integer.parseInt(value));
                page.putField("browse", value);
            }

        });
        staticZhiHuMassageDao.save(zhiHuMassage);
        page.putField("title", title);
    }

    private void getSimilarQuestions(List<Long> questionIds){
        questionIds.forEach(id ->{
            String api = QUESTION_SIMILAR_API.replace("${id}", id.toString());
            JSONObject httpContent = HttpUtils.getHttpContent(api);
            JSONArray data = httpContent.getJSONArray("data");
            for (int i = 0; i < data.size(); i++) {
                JSONObject jsonObject = data.getJSONObject(i);
            }
        });

    }

    private List<String> questionFilter(List<String> questionUrls) {
        return questionUrls.stream().filter(url -> !IDS.contains(getQuestionId(url)))
                .collect(Collectors.toList());
    }

    /**
     * 是否已经处理过该问题
     *
     * @param url 包含QuestionId的URL
     * @return  是否已经处理过该问题
     */
    private boolean dealQuestionId(String url) {

        Long id = getQuestionId(url);
        if (IDS.contains(id)) {
            return true;
        }
        IDS.add(id);
        return false;
    }

    private Long getQuestionId(String url) {
        String substring = url.substring(QUESTION_ID_INDEX);
        if (substring.contains("/")) {
            return Long.valueOf(substring.substring(0, substring.indexOf('/')));
        } else {
            return Long.valueOf(substring);
        }
    }

    /**
     * 获取收藏夹的中问题的URL
     *
     * @param collection 收藏夹url
     * @return  收藏夹的中问题的URL
     */
    private List<String> getUrls(String collection) {
        int n = collection.lastIndexOf("/") + 1;
        String id = collection.substring(n);
        String api = COLLECTION_API.replace("${id}", id);
        return getUrlsFromApi(api);
    }

    /**
     * 获取Api接口返回的url
     *
     * @param api  api接口
     * @return Api接口返回的url
     */
    private List<String> getUrlsFromApi(String api) {
        JSONObject httpContent = HttpUtils.getHttpContent(api);
        if (httpContent == null) {
            return new ArrayList<>(0);
        }
        JSONArray data = httpContent.getJSONArray("data");
        boolean isEnd = httpContent.getJSONObject("paging").getBoolean("is_end");
        List<String> urls = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            JSONObject jsonObject = data.getJSONObject(i).getJSONObject("content");
            String url = jsonObject.getString("url");
            urls.add(url);
        }
        if (!isEnd) {
            String apiNext = httpContent.getJSONObject("paging").getString("next");
            urls.addAll(getUrlsFromApi(apiNext));
        }
        return urls;
    }

    @Override
    public Site getSite() {
        return this.site;
    }

    @PostConstruct
    public void startUp() {
        staticZhiHuMassageDao = zhihuMassageDao;
        CompletableFuture.runAsync(this::spiderRun);
    }

    private void spiderRun(){
        getAllIds();
        JSONObject jsonObject = HttpUtils.getHttpContent(FAVLISTS);
        assert jsonObject != null;
        JSONArray data = jsonObject.getJSONArray("data");
        List<String> urls = new ArrayList<>(520);
        for (int i = 0; i < data.size(); i++) {
            String url = data.getJSONObject(i).getString("url");
            String collection = url.substring(url.lastIndexOf('/') + 1);
            url = COLLECTION + collection;
            urls.add(url);
        }
        data.add(EXPLORE);
        String[] objects = urls.toArray(new String[0]);
        Spider.create(new ZhihuPageProcessor())
                .thread(1).addUrl(objects).run();
        try {
            TimeUnit.HOURS.sleep(5);
            spiderRun();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 获取已经处理过的问题
     */
    private void getAllIds() {

        List<Long> idList = zhihuMassageDao.getIds();
        IDS.addAll(idList);
    }
}