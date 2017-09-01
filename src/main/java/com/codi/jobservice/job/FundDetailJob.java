package com.codi.jobservice.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.codi.base.spring.SpringContextHolder;
import com.codi.bus.core.service.FundDetailService;
import com.codi.bus.core.service.FundInfoService;

/**
 * 基金详情中使用的Cache
 * 
 * @author shi.pengyan
 * @date 2016年11月10日 上午11:26:52
 */
@DisallowConcurrentExecution
public class FundDetailJob extends QuartzJobBean {

    @SuppressWarnings("unused")
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        RedisTemplate<String, String> redisTemplate = SpringContextHolder.getBean("redisTemplate");

        FundInfoService fundInfoService = SpringContextHolder.getBean("fundInfoService");
        FundDetailService fundDetailService = SpringContextHolder.getBean("fundDetailService");

        // TODO

    }
}
