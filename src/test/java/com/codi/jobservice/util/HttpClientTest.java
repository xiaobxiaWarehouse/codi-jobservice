package com.codi.jobservice.util;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import com.codi.base.cache.CacheUtil;
import com.codi.base.util.DateUtils;
import com.codi.jobservice.common.Const;
import com.codi.jobservice.job.BaseTest;

/**
 * 
 * @author shi.pengyan
 * @date 2016年11月2日 下午4:54:13
 */
public class HttpClientTest extends BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientTest.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    public void getSH300All() {
        String key = CacheUtil.getKey("SH300", "ORIGIN");
        BoundHashOperations<String, String, String> op = redisTemplate.boundHashOps(key);
        redisTemplate.delete(key); // clear all

        int endYear = DateUtils.getYear();
        String endYearStr = "" + endYear;
        endYear = Integer.valueOf(endYearStr.substring(2));

        for (int begin = 15; begin <= endYear; begin++) {
            String url = Const.URL_SH300_TEMPLATE.replace("{}", "" + begin);
            long start = System.currentTimeMillis();
            SH300Util.loadData(url, op);
            // loadData(url);
            long end = System.currentTimeMillis();
            logger.debug("year={},cost={}ms", begin, (end - start));
        }
    }

    @Test
    public void getSH300Latest() {
        String key = CacheUtil.getKey("SH300", "LATEST");
        BoundHashOperations<String, String, String> op = redisTemplate.boundHashOps(key);
        redisTemplate.delete(key);
        SH300Util.loadData(Const.URL_SH300_LATEST, op);
    }

}
