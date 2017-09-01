package com.codi.jobservice.job.listener;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerKey;
import org.quartz.TriggerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 全局触发器监听器
 * 
 * @author shi.pengyan
 * @date 2016年11月10日 上午10:26:09
 */
public class GlobalTriggerListener implements TriggerListener {

    private static final Logger logger = LoggerFactory.getLogger(GlobalTriggerListener.class);

    @Override
    public String getName() {
        return "GLOBAL_TRIGGER_LISTENER";
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        TriggerKey triggerKey = trigger.getKey();
        logger.info("[TiggerName={},TriggerGroupName={}] fired.", triggerKey.getName(), triggerKey.getGroup());
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        TriggerKey triggerKey = trigger.getKey();
        logger.info("[TiggerName={},TriggerGroupName={}] vetoJob.", triggerKey.getName(), triggerKey.getGroup());
        return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
        TriggerKey triggerKey = trigger.getKey();
        logger.info("[TiggerName={},TriggerGroupName={}] misfired.", triggerKey.getName(), triggerKey.getGroup());
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context,
            CompletedExecutionInstruction triggerInstructionCode) {
        TriggerKey triggerKey = trigger.getKey();
        logger.info("[TiggerName={},TriggerGroupName={}] complete.", triggerKey.getName(), triggerKey.getGroup());

    }

}
