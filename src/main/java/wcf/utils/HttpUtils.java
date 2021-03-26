package wcf.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HttpUtils {


    public static JSONObject getHttpContent(String url) {
        try {
            URL HttpsUrl = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) HttpsUrl.openConnection();
            httpURLConnection.setInstanceFollowRedirects(false);
            httpURLConnection.connect();
            InputStream inputStream = httpURLConnection.getInputStream();
            byte[] bytes = IOUtils.toByteArray(inputStream);
            String json = StringUtils.toEncodedString(bytes, StandardCharsets.UTF_8);
            return JSONObject.parseObject(json);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
