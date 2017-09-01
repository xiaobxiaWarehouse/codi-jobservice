package com.codi.jobservice.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * 
 * @author shi.pengyan
 * @date 2016年11月1日 下午5:04:10
 */
public class TestJob extends QuartzJobBean {

    private static final Logger logger = LoggerFactory.getLogger(SimpleJob.class);

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        System.out.println("Simple Job has executed");
        logger.debug("Just Simple Job");
        Trigger trigger = context.getTrigger();
        TriggerKey triggerKey = trigger.getKey();
        System.out.println("group=" + triggerKey.getGroup() + ",key=" + triggerKey.getName());

    }

}
