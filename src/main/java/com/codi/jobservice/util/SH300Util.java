package com.codi.jobservice.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.http.HttpStatus;

/**
 * 深沪300
 * 
 * @author shi.pengyan
 * @date 2016年11月2日 下午8:05:16
 */
public final class SH300Util {

    private static final Logger logger = LoggerFactory.getLogger(SH300Util.class);

    /**
     * 加载远程数据
     * 
     * @param url
     * @param op
     */
    public static void loadData(String url, BoundHashOperations<String, String, String> op) {
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(url);
        try {
            int responseCode = httpClient.executeMethod(getMethod);
            if (responseCode == HttpStatus.OK.value()) {
                InputStream in = getMethod.getResponseBodyAsStream();

                BufferedReader bufferReader = new BufferedReader(new InputStreamReader(in));
                String line = null;

                while ((line = bufferReader.readLine()) != null) {
                    line = line.replace("\\n\\", "");
                    String[] array = line.split(" ");
                    if (array.length != 6) {
                        continue;
                    }
                    String tradingDay = array[0];
                    op.put(tradingDay, line); // 每次都是一次http请求
                }
            }

        } catch (HttpException e) {
            logger.error("fail to http get", e);
        } catch (IOException e) {
            logger.error("io exception", e);
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * 加载远程数据， key无序，暂时不是用，不过性能很好
     * 
     * @param url
     */
    // public static void loadData(String url) {
    // Map<byte[], byte[]> map = new HashMap<>();
    //
    // HttpClient httpClient = new HttpClient();
    // GetMethod getMethod = new GetMethod(url);
    // try {
    // int responseCode = httpClient.executeMethod(getMethod);
    // if (responseCode == HttpStatus.OK.value()) {
    // InputStream in = getMethod.getResponseBodyAsStream();
    //
    // BufferedReader bufferReader = new BufferedReader(new
    // InputStreamReader(in));
    // String line = null;
    //
    // while ((line = bufferReader.readLine()) != null) {
    // line = line.replace("\\n\\", "");
    // String[] array = line.split(" ");
    // if (array.length != 6) {
    // continue;
    // }
    // String tradingDay = array[0];
    // map.put(tradingDay.getBytes(), line.getBytes());
    // }
    // }
    // } catch (HttpException e) {
    // logger.error("fail to http get", e);
    // } catch (IOException e) {
    // logger.error("io exception", e);
    // } finally {
    // getMethod.releaseConnection();
    // }
    //
    // String key = CacheUtil.getKey("SH300", "ORIGIN");
    // RedisConnectionFactory factory = redisTemplate.getConnectionFactory();
    // RedisConnection redisConnection = factory.getConnection();
    // List<Object> results;
    // try {
    // redisConnection.openPipeline();
    // redisConnection.hMSet(key.getBytes(), map); // map中无法排序,顺序很重要
    // results = redisConnection.closePipeline();
    // } finally {
    // RedisConnectionUtils.releaseConnection(redisConnection, factory);
    // }
    // if (results == null) {
    // System.out.println("result is null");
    // return;
    // }
    // for (Object item : results) {
    // System.out.println(item.toString());
    // }
    // }
}
