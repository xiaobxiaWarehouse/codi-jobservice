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
import com.codi.base.util.DateUtils;
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
public class SH300AllJob extends QuartzJobBean {

    private static final Logger logger = LoggerFactory.getLogger(SH300AllJob.class);

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        logger.debug("execute SH300 Job");

        RedisTemplate<String, String> redisTemplate = SpringContextHolder.getBean("redisTemplate");
        StaticSH300Service staticSH300Service = SpringContextHolder.getBean("staticSH300Service");

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

            long end = System.currentTimeMillis();
            logger.debug("year={},cost={}ms", begin, (end - start));
        }

        // 调用业务服务器接口

        try {
            staticSH300Service.clearAll();
            staticSH300Service.addAll();
        } catch (BaseAppException e) {
            logger.error("fail to execute", e);
        }
        JobUtil.updateJobExecuteTime(CacheKeyConst.SH300);
    }
}
