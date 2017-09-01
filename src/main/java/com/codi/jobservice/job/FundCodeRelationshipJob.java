package com.codi.jobservice.job;

import java.util.List;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.codi.base.spring.SpringContextHolder;
import com.codi.base.util.ListUtil;
import com.codi.bus.constant.CacheKeyConst;
import com.codi.bus.core.domain.FundInfo;
import com.codi.bus.core.service.FundDetailService;
import com.codi.bus.core.service.FundInfoService;
import com.codi.fundData.domain.FundDetail;
import com.codi.fundData.service.FundCodeRelationshipService;
import com.codi.jobservice.common.Const;
import com.codi.jobservice.util.JobUtil;

/**
 * 刷新基金编码关联
 * 
 * @author shi.pengyan
 * @date 2016年11月23日 上午11:26:52
 */
@DisallowConcurrentExecution
public class FundCodeRelationshipJob extends QuartzJobBean {

    private static final Logger logger = LoggerFactory.getLogger(FundCodeRelationshipJob.class);

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

        updateCache(context);
        JobUtil.updateJobExecuteTime(CacheKeyConst.MF_FUND_CODE_RELATIONSHIP_NEW);
    }

    private void updateCache(JobExecutionContext context) {
        FundInfoService fundInfoService = SpringContextHolder.getBean("fundInfoService");
        FundDetailService fundDetailService = SpringContextHolder.getBean("fundDetailService");
        FundCodeRelationshipService fundCodeRelationshipService = SpringContextHolder
                .getBean("fundCodeRelationshipService");

        int pageIndex = 1;

        while (true) {
            logger.debug("loading fund pageIndex={}", pageIndex);

            List<FundInfo> funds = fundInfoService.queryPage(Const.PAGE_SIZE, pageIndex);
            if (ListUtil.isNotEmpty(funds)) {
                for (FundInfo fund : funds) {
                    FundDetail detail = fundDetailService.getFundDetail(fund.getFundCode());
                    if (detail != null) {
                        fundCodeRelationshipService.refreshCodeRelationshipCache(detail.getInnerCode());
                    }
                }
                if (funds.size() < Const.PAGE_SIZE) {
                    break;
                }
            } else {
                break;
            }

            pageIndex++;
        }
    }
}
