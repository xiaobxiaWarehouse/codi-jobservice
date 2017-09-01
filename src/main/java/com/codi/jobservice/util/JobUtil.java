package com.codi.jobservice.util;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import com.codi.base.cache.CacheUtil;
import com.codi.base.common.Const;
import com.codi.base.spring.SpringContextHolder;
import com.codi.base.util.DateUtils;

/**
 * job工具类
 * 
 * @author shi.pengyan
 * @date 2016年11月21日 下午12:58:29
 */
public final class JobUtil {

    private static final Logger logger = LoggerFactory.getLogger(JobUtil.class);

    /**
     * 更新Job执行时间
     * 
     * @param moduleName
     *            模块名称
     */
    public static void updateJobExecuteTime(String moduleName) {
        RedisTemplate<String, String> redisTemplate = SpringContextHolder.getBean("redisTemplate");
        String key = CacheUtil.getKey(moduleName, Const.LAST_UPDATE_TIME);

        logger.debug("JobKey={},update job execute time", key);

        CacheUtil.setStr(redisTemplate, key, DateUtils.formatDateTime(new Date()));
    }
}
