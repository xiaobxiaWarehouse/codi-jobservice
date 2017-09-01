package com.codi.jobservice.job;

import java.util.List;
import java.util.Set;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.codi.base.cache.CacheUtil;
import com.codi.base.exception.BaseAppException;
import com.codi.base.spring.SpringContextHolder;
import com.codi.base.util.ListUtil;
import com.codi.bus.constant.CacheKeyConst;
import com.codi.bus.core.domain.FundInfo;
import com.codi.bus.core.service.FundDetailService;
import com.codi.bus.core.service.FundInfoService;
import com.codi.fundData.domain.FundDetail;
import com.codi.fundData.domain.FundRating;
import com.codi.fundData.service.FundRatingService;
import com.codi.jobservice.util.JobUtil;

/**
 * 刷新基金评级
 * 
 * @author shi.pengyan
 * @date 2016年11月17日 上午10:32:46
 */
@DisallowConcurrentExecution
public class FundRatingJob extends QuartzJobBean {

    private static final Logger logger = LoggerFactory.getLogger(FundRatingJob.class);

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        RedisTemplate<String, String> redisTemplate = SpringContextHolder.getBean("redisTemplate");

        FundInfoService fundInfoService = SpringContextHolder.getBean("fundInfoService");
        FundDetailService fundDetailService = SpringContextHolder.getBean("fundDetailService");
        FundRatingService fundRatingService = SpringContextHolder.getBean("fundRatingService");

        Set<String> keys = redisTemplate.keys(CacheKeyConst.MF_FUNDRATING + ":*");
        if (keys != null && !keys.isEmpty()) {
            logger.debug("fund type keys length={}", keys.size());
            redisTemplate.delete(keys);
        }

        int pageSize = 100, pageIndex = 1, delay = 2 * 1000;
        while (true) {
            logger.debug("loading fund pageIndex={}", pageIndex);

            List<FundInfo> funds = fundInfoService.queryPage(pageSize, pageIndex);

            if (ListUtil.isEmpty(funds)) {
                break;
            }

            for (FundInfo fund : funds) {
                FundDetail fundDetail = fundDetailService.getFundDetail(fund.getFundCode());
                if (fundDetail != null) {
                    try {
                        int innerCode = fundDetail.getInnerCode();
                        FundRating fundRating = fundRatingService.queryFundRatingByInnerCode(innerCode);
                        String key = CacheUtil.getKey(CacheKeyConst.MF_FUNDRATING, innerCode);
                        CacheUtil.setStrObj(redisTemplate, key, fundRating);
                    } catch (BaseAppException e) {
                        logger.error("fail to query rating", e);
                    }
                }

            }

            if (funds.size() < pageSize) {
                break;
            }

            pageIndex++;

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                logger.error("fail to sleep", e);
            }
        }
        JobUtil.updateJobExecuteTime(CacheKeyConst.MF_FUNDRATING);
    }
}
