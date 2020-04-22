package com.example.demo;

import cn.hutool.core.io.FileUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @program: mybatis
 * @description: 异步Http请求 测试
 * @author: xinyao.Wang
 * @create: 2020-01-15 10:13
 **/
public class AsyncHttpClientTest2 {

    private static final CloseableHttpAsyncClient closeableHttpAsyncClient;

    static {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(50000)
                .setSocketTimeout(50000)
                .setConnectionRequestTimeout(1000)
                .build();

        //配置io线程
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom().
                setIoThreadCount(Runtime.getRuntime().availableProcessors())
                .setSoKeepAlive(true)
                .build();
        //设置连接池大小
        ConnectingIOReactor ioReactor=null;
        try {
            ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);
        } catch (IOReactorException e) {
            e.printStackTrace();
        }
        PoolingNHttpClientConnectionManager connManager = new PoolingNHttpClientConnectionManager(ioReactor);
        connManager.setMaxTotal(15);
        connManager.setDefaultMaxPerRoute(15);

        closeableHttpAsyncClient = HttpAsyncClients.custom().
                setConnectionManager(connManager)
                .setDefaultRequestConfig(requestConfig)
                .build();

        closeableHttpAsyncClient.start();
    }



    // TODO 20200116

    public static void main(String[] args) {
        File file = FileUtil.file("text/C00045677_Z12_XW_MR_C0004567720200114135523.xml");

        HttpPost request = new HttpPost("http://api.de-well.com/basApi/synTracking");
        request.setHeader("Content-Type","text/plain;charset=utf-8" );
//        request.setEntity(new StringEntity(aa, StandardCharsets.UTF_8));
        request.setEntity(new FileEntity(file));
        for (int i = 0; i < 100; i++) {
            // 传入HttpPost request
//        CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
            System.out.println(" caller thread id is : " + i + Thread.currentThread().getId());
            closeableHttpAsyncClient.execute(request, new FutureCallback<HttpResponse>() {
                @Override
                public void completed(final HttpResponse response) {
                    System.out.println(" callback thread id is : " + Thread.currentThread().getId());
                    System.out.println(request.getRequestLine() + "->" + response.getStatusLine());
                    try {
                        String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                        System.out.println(" response content is : " + content);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void failed(final Exception ex) {
                    System.out.println(request.getRequestLine() + "->" + ex);
                    System.out.println(" callback thread id is : " + Thread.currentThread().getId());
                }
                @Override
                public void cancelled() {
                    System.out.println(request.getRequestLine() + " cancelled");
                    System.out.println(" callback thread id is : " + Thread.currentThread().getId());
                }

            });
        }

    }

}
