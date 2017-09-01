package com.codi.jobservice.job;

import java.util.List;

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
import com.codi.bus.core.service.FundInfoService;
import com.codi.fundData.domain.SecurityProduct;
import com.codi.fundData.service.SecurityProductService;
import com.codi.jobservice.util.JobUtil;

/**
 * 
 * @author shi.pengyan
 * @date 2016年11月21日 下午1:29:49
 */
public class SecuMainJob extends QuartzJobBean {

    private static final Logger logger = LoggerFactory.getLogger(SecuMainJob.class);

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        updateSecuMainCache();

        JobUtil.updateJobExecuteTime(CacheKeyConst.MF_SECUMAIN);
    }

    private void updateSecuMainCache() {
        RedisTemplate<String, String> redisTemplate = SpringContextHolder.getBean("redisTemplate");

        FundInfoService fundInfoService = SpringContextHolder.getBean("fundInfoService");
        SecurityProductService productService = SpringContextHolder.getBean("securityProductService");

        int pageSize = 100, pageIndex = 1, delay = 2 * 1000;
        while (true) {
            logger.debug("loading fund pageIndex={}", pageIndex);

            List<FundInfo> funds = fundInfoService.queryPage(pageSize, pageIndex);

            if (ListUtil.isEmpty(funds)) {
                break;
            }

            for (FundInfo fund : funds) {
                try {
                    SecurityProduct product = productService.querySecurityProduct(fund.getFundCode());
                    if (product == null) {
                        continue;
                    }
                    String key = CacheUtil.getKey(CacheKeyConst.SECU_MAIN, fund.getFundCode());
                    CacheUtil.setStrObj(redisTemplate, key, product);
                } catch (BaseAppException e) {
                    logger.error("fail to query rating", e);
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
    }
}
