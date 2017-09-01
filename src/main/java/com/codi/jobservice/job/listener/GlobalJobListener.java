package com.codi.jobservice.job.listener;

import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 全局JOB监听器
 * 
 * @author shi.pengyan
 * @date 2016年11月10日 上午9:56:05
 */
public class GlobalJobListener implements JobListener {

    private static final Logger logger = LoggerFactory.getLogger(GlobalJobListener.class);

    @Override
    public String getName() {
        return "GLOBAL_JOB_LISTENER";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        JobDetail jobDetail = context.getJobDetail();
        JobKey jobKey = jobDetail.getKey();
        logger.info("[JobName={},JobGroup={}] will be executed.", jobKey.getName(), jobKey.getGroup());
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        JobDetail jobDetail = context.getJobDetail();
        JobKey jobKey = jobDetail.getKey();
        logger.info("[JobName={},JobGroup={}] voted.", jobKey.getName(), jobKey.getGroup());
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        JobDetail jobDetail = context.getJobDetail();
        JobKey jobKey = jobDetail.getKey();
        logger.info("[JobName={},JobGroup={}] was executed.", jobKey.getName(), jobKey.getGroup());
    }

}
