package com.codi.jobservice.job;

import com.codi.base.spring.SpringContextHolder;
import com.codi.base.util.JsonUtil;
import com.codi.bus.constant.GlobalConstant;
import com.codi.bus.core.resultModel.FundListResult;
import com.codi.bus.core.service.FundInfoService;
import com.codi.bus.core.service.QueryService;
import com.codi.bus.core.service.model.FundModel;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.util.ArrayList;
import java.util.List;

/**
 * 定时更新基金信息
 *
 * @author song-jj
 */
@DisallowConcurrentExecution
public class UpdateFundInfoJob extends QuartzJobBean {

    private static final Logger logger = LoggerFactory.getLogger(UpdateFundInfoJob.class);

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        logger.debug("===========start update fund info job===============");
        // 取得service类
        QueryService queryService = SpringContextHolder.getBean("queryService");
        FundInfoService fundInfoService = SpringContextHolder.getBean("fundInfoService");

        FundListResult result = null;
        int lastRowCount = 1; // 上次的行数
        List<FundModel> insertFundList = new ArrayList<FundModel>();
        try {
            result = queryService.queryFunds(GlobalConstant.MAX_REQUEST_NUM, 1, lastRowCount, "1", "");

            // 插入数据库
            insertFundList = result.getFunds();
            if (insertFundList.size() > 0) {
                fundInfoService.insertFundInfo(insertFundList);
            }
            logger.info("==========insert 【{}】 funds===========", insertFundList.size());
        } catch (Exception e) {
            logger.error("发生错误啦：", e);
            logger.error("参数-恒生取得的基金列表：" + JsonUtil.list2json(insertFundList));
            logger.error("参数-lastRowCount = ：" + lastRowCount);
        }
        logger.debug("===========end update fund info job===============");
    }

}
