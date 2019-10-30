package com.example.myproj;

import android.util.Log;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import com.alibaba.fastjson.JSON;
import com.example.myproj.baidu.LogoResult;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void test1() {
        File file = new File("C:\\Users\\Lh\\AndroidStudioProjects\\MyProj\\app\\src\\main\\java\\com\\example\\myproj\\nike.png");

        if (file.exists()) {
            System.out.println("1111111111");
        }
    }

    @Test
    public void test4() throws Exception {
        InputStream inputStream = new FileInputStream("C:\\Users\\Lh\\AndroidStudioProjects\\MyProj\\app\\src\\main\\res\\drawable\\nike.png");
        //文件读入缓存并编码
        byte[] buf = new byte[inputStream.available()];
        inputStream.read(buf);
        //编码
        String s = new String(Base64.getEncoder().encode(buf));
        System.out.println(s);
        String accessToken = "24.6b257a730d1333a8f1addc5df79c09d6.2592000.1574843800.282335-17616791";

        String url2 = "https://aip.baidubce.com/rest/2.0/image-classify/v2/advanced_general？access_token=" + accessToken;

        LogoResult lgr = new LogoResult();
        lgr.setImage(s);
        RequestBody body1 = RequestBody.create(MediaType.parse("json"), JSON.toJSONString(lgr));
        sslUtils.ignoreSsl();
        OkHttpClient client = new OkHttpClient();
        Request request1 = new Request.Builder()
                .url(url2)
                .post(body1)
                .build();
        Response response1 = client.newCall(request1).execute();
        String result1 = response1.body().string();
       System.out.println(result1);

    }

@Test
    public void test2(){
    String url = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=8FV2EYd2E2qlcS1HrITPtdCe&client_secret=M47XwIMwfrPBpVoBAL045qDI6pGsTllL";
    MediaType mediaType = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();
    RequestBody body = RequestBody.create(mediaType, JSON.toJSONString(LogoResult.class));
    Request request = new Request.Builder()
            .url(url)
            .post(body)
            .build();
    Response response = null;
    try {
        response = client.newCall(request).execute();

        String s = response.body().string();

        String accessToken = s.split(",")[3].split(":")[1].replace("\"", "");
        System.out.println(s);
    } catch (IOException e) {
        e.printStackTrace();
    }
}


    }