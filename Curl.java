package com.mingge.pingo.Utils;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @Description: 后端回话远程读取url 支持http和https，跟jq的ajax用法是非常相似的，方便使用，
 *           国际规范必须导入httpclient4以上依赖
 *           <dependency>
 *             <groupId>org.apache.httpcomponents</groupId>
 *             <artifactId>httpclient</artifactId>
 *             <version>4.5.3</version>
 *            </dependency>
 * @Author: 开发作者：明哥
 * @CreateDate: 2018/10/19
 * @Version: 小牛试刀，第一个插件项目1.0
 */
public class Curl {

    protected String result;
    protected Integer error;

    protected Map<String, Object> extend(Map<String, Object> obj) { //参数扩展
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("method", "get");
        parameter.put("timeout", 5000);
        parameter.put("url", null);
        parameter.put("data", null);
        parameter.put("contentType", "UTF-8");
        Map<String, Object> resultMap = new HashMap<String, Object>() {{
            putAll(parameter);
            putAll(obj);
        }};
        return resultMap;
    }

    protected RequestConfig getTimeout(int outtime) { //设置超时
        return RequestConfig.custom()
                .setConnectTimeout(outtime)   //设置连接超时时间
                .setConnectionRequestTimeout(outtime) // 设置请求超时时间
                .setSocketTimeout(outtime)
                .setRedirectsEnabled(true) //默认允许自动重定向
                .build();
    }

    protected HttpEntity handleParameter(Map<String, String> params, Charset code) throws IOException { //处理data参数
        List<NameValuePair> list = new ArrayList<>();

        if (params == null) {
            return null;
        }

        for (Map.Entry<String, String> entry : params.entrySet()) {
            list.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        return new UrlEncodedFormEntity(list, code);
    }


    protected void addHeader(HttpGet get, Map<String, String> params) {//设置头，get
        for (Map.Entry<String, String> entry : params.entrySet()) {
            get.addHeader(entry.getKey(), entry.getValue());
        }
    }

    protected void addHeader(HttpPost post, Map<String, String> params) {//设置头，POST
        for (Map.Entry<String, String> entry : params.entrySet()) {
            post.addHeader(entry.getKey(), entry.getValue());
        }
    }

    /**
     * @param obj 通过Map类型批量传入参数
     *            obj.data   发送的数据
     *            obj.timeout  超时时间
     *            obj.contentType  输出编码
     *            obj.method   get or post
     *            obj.url  回话url地址
     */
    public Curl(Map<String, Object> obj) throws IOException {//回话，核心函数
        try {
            obj = extend(obj);//参数扩展
            Map<String, String> sendData = (Map<String, String>) obj.get("data");
            HttpEntity parameter;
            if (sendData != null) {
                parameter = handleParameter(sendData, Consts.UTF_8);//传递参
            } else {
                parameter = null;
            }
            String url = obj.get("url").toString(); //网址
            int timeout = (int) (obj.get("timeout"));//超时
            String method = obj.get("method").toString().toLowerCase();
            CloseableHttpClient httpClient;
            httpClient = HttpClients.createDefault();//创建HttpClients
            CloseableHttpResponse response;//公共 结果变量
            if (method.equals("post")) {
                //如果为POST时
                HttpPost httpPost = new HttpPost(url);
                httpPost.setConfig(getTimeout(timeout));//设置超时
                if (parameter != null) {
                    httpPost.setEntity(parameter);
                }
                addHeader(httpPost, (Map<String, String>) obj.get("header"));
                response = httpClient.execute(httpPost);
            } else {
                //如果为GET时
                if (parameter != null) {
                    url = url + (url.contains("?") ? "&" : "?") + EntityUtils.toString(parameter);
                }
                System.out.println(url);
                HttpGet httpGet = new HttpGet(url);
                addHeader(httpGet, (Map<String, String>) obj.get("header"));
                httpGet.setConfig(getTimeout(timeout));//设置超时
                response = httpClient.execute(httpGet);
            }
            if (response != null) {
                int codes = response.getStatusLine().getStatusCode();
                System.out.println(codes);
                if (codes < 201) {
                    HttpEntity entity = response.getEntity();
                    String contentType = obj.get("contentType").toString();
                    result = EntityUtils.toString(entity, contentType); //得到数据
                    error = 0; //没有错误，并成功返回
                    return;
                }
                error = codes;
            }

            if (response != null) {
                response.close();
            }
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            error = 400;
        }

        result = null;
    }

    public String fetch() { //返回结果
        return result;
    }

    public int getError() {//返回错误代码
        return error;
    }
}