package com.codi.jobservice.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.codi.base.cache.CacheUtil;
import com.codi.base.exception.BaseAppException;
import com.codi.base.spring.SpringContextHolder;
import com.codi.bus.constant.CacheKeyConst;
import com.codi.bus.core.service.StaticSH300Service;
import com.codi.jobservice.common.Const;
import com.codi.jobservice.util.JobUtil;
import com.codi.jobservice.util.SH300Util;

/**
 * 沪深300 指数采集定时任务
 * 
 * @author shi.pengyan
 * @date 2016年11月1日 下午3:15:46
 */
@DisallowConcurrentExecution
public class SH300LatestJob extends QuartzJobBean {

    private static final Logger logger = LoggerFactory.getLogger(SH300LatestJob.class);

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        logger.debug("execute SH300 Latest Job");

        RedisTemplate<String, String> redisTemplate = SpringContextHolder.getBean("redisTemplate");
        StaticSH300Service staticSH300Service = SpringContextHolder.getBean("staticSH300Service");

        String key = CacheUtil.getKey("SH300", "LATEST");
        BoundHashOperations<String, String, String> op = redisTemplate.boundHashOps(key);
        redisTemplate.delete(key);
        SH300Util.loadData(Const.URL_SH300_LATEST, op);

        // 调用业务服务器接口

        try {
            staticSH300Service.addLatestData();

            // 1,3,6,12个月
            int[] months = { 1, 3, 6, 12 };
            for (int month : months) {
                String key1 = CacheUtil.getKey("SH300", "STATIC_MONTH", month);
                redisTemplate.delete(key1);
                staticSH300Service.getStatisticData(month, null);
            }

            // 最新一条记录
            String key2 = CacheUtil.getKey("SH300", "MAX_TRADING_DATE_RECORD");
            redisTemplate.delete(key2);
            staticSH300Service.getLatestRecord();

        } catch (BaseAppException e) {
            logger.error("Fail to add latestData", e);
        }

        JobUtil.updateJobExecuteTime(CacheKeyConst.SH300_LATEST);
    }
}
