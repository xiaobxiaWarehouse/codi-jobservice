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

import com.codi.base.spring.SpringContextHolder;
import com.codi.base.util.ListUtil;
import com.codi.bus.constant.CacheKeyConst;
import com.codi.bus.core.domain.FundInfo;
import com.codi.bus.core.service.FundDetailService;
import com.codi.bus.core.service.FundInfoService;
import com.codi.jobservice.common.Const;
import com.codi.jobservice.util.JobUtil;

/**
 * 刷新基金类型
 * 
 * @author shi.pengyan
 * @date 2016年11月17日 上午9:32:46
 */
@DisallowConcurrentExecution
public class FundTypeJob extends QuartzJobBean {

    private static final Logger logger = LoggerFactory.getLogger(FundTypeJob.class);

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        RedisTemplate<String, String> redisTemplate = SpringContextHolder.getBean("redisTemplate");

        FundInfoService fundInfoService = SpringContextHolder.getBean("fundInfoService");
        FundDetailService fundDetailService = SpringContextHolder.getBean("fundDetailService");

        Set<String> keys = redisTemplate.keys(CacheKeyConst.MF_FUND_TYPE + ":*");
        if (keys != null && !keys.isEmpty()) {
            logger.debug("fund type keys length={}", keys.size());
            redisTemplate.delete(keys);
        }

        int pageIndex = 1;
        while (true) {
            logger.debug("loading fund pageIndex={}", pageIndex);

            List<FundInfo> funds = fundInfoService.queryPage(Const.PAGE_SIZE, pageIndex);
            if (ListUtil.isNotEmpty(funds)) {
                for (FundInfo fund : funds) {
                    fundDetailService.getFundDetail(fund.getFundCode());
                }
                if (funds.size() < Const.PAGE_SIZE) {
                    break;
                }
            } else {
                break;
            }

            pageIndex++;
        }
        
        JobUtil.updateJobExecuteTime(CacheKeyConst.MF_FUND_TYPE);
    }
}
