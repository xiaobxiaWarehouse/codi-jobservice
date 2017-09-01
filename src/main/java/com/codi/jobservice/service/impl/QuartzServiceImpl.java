package com.codi.jobservice.service.impl;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import javax.annotation.Resource;

import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.codi.base.exception.BaseAppException;
import com.codi.base.util.EqualsUtil;
import com.codi.jobservice.service.QuartzService;

/**
 * 
 * @author shi.pengyan
 * @date 2016年11月7日 下午1:53:36
 */
@Service("quartzService")
public class QuartzServiceImpl implements QuartzService {

    private static final Logger logger = LoggerFactory.getLogger(QuartzServiceImpl.class);

    @Resource(name = "scheduler")
    private Scheduler scheduler;

    @SuppressWarnings("unchecked")
    @Override
    public void addJob(String jobName, String groupName, String desc, String clazz, String triggerName,
            String triggerGroupName, String cronTriggerStr) throws BaseAppException {
        logger.debug("jobName={},groupName={}", jobName, groupName);
        logger.debug("clazz={},cron={}", clazz, cronTriggerStr);

        JobDetail job;
        try {
            job = JobBuilder.newJob((Class<? extends Job>) Class.forName(clazz)).withIdentity(jobName, groupName)
                    .build();

            CronTrigger trigger = newTrigger().withIdentity(triggerName, triggerGroupName)
                    .withSchedule(cronSchedule(cronTriggerStr)).build();
            // TODO 在指定时间段内执行 startAt, endAt

            scheduler.scheduleJob(job, trigger);

        } catch (ClassNotFoundException e1) {
            logger.error("Class not funded", e1);

        } catch (SchedulerException e) {
            logger.error("Fail to Scheduler Job", e);
        }
    }

    @Override
    public void modifyJob(String jobName, String groupName, String cronTriggerStr) {
        logger.debug("modifyJob jobName={},groupName={}", jobName, groupName);

        // TODO Auto-generated method stub

    }

    @Override
    public JobDetail getJob(String jobName, String jobGroupName) throws Exception {
        logger.debug("getJob jobName={},groupName={}", jobName, jobGroupName);

        JobKey jobKey = getJobKey(jobName, jobGroupName);
        if (jobKey == null) {
            return null;
        }

        return scheduler.getJobDetail(jobKey);
    }

    @Override
    public void removeJob(String jobName, String jobGroupName) throws Exception {
        logger.debug("removeJob jobName={},groupName={}", jobName, jobGroupName);

        JobKey jobKey = getJobKey(jobName, jobGroupName);
        scheduler.deleteJob(jobKey);
    }

    @Override
    public void pauseJob(String jobName, String groupName) throws Exception {
        logger.debug("pauseJob jobName={},groupName={}", jobName, groupName);

        JobKey jobKey = getJobKey(jobName, groupName);
        scheduler.pauseJob(jobKey);
    }

    @Override
    public void resumeJob(String jobName, String groupName) throws Exception {
        logger.debug("interrupt jobName={},groupName={}", jobName, groupName);

        JobKey jobKey = getJobKey(jobName, groupName);
        scheduler.resumeJob(jobKey);
    }

    @Override
    public void stopJobOfRunning(String jobName, String jobGroupName) throws Exception {
        logger.debug("stopJobOfRunning jobName={},groupName={}", jobName, jobGroupName);

        JobKey jobKey = getJobKey(jobName, jobGroupName);
        scheduler.interrupt(jobKey);
    }

    @Override
    public void triggerJob(String jobName, String jobGroupName) throws Exception {
        logger.debug("triggerJob jobName={},groupName={}", jobName, jobGroupName);

        JobKey jobKey = getJobKey(jobName, jobGroupName);
        if (jobKey != null) {
            scheduler.triggerJob(jobKey);
        } else {
            logger.warn("this is nojob,jobName={},jobGroupName={}", jobName, jobGroupName);
        }
    }

    @Override
    public void pauseTrigger(String triggerName, String triggerGroupName) throws Exception {
        logger.debug("pauseTrigger triggerName={},triggerGroupName={}", triggerName, triggerGroupName);

        TriggerKey triggerKey = getTriggerKey(triggerName, triggerGroupName);
        if (triggerKey != null) {
            // Trigger trigger = scheduler.getTrigger(triggerKey);
            TriggerState state = scheduler.getTriggerState(triggerKey);

            if (state != TriggerState.PAUSED) {
                scheduler.pauseTrigger(triggerKey);
            } else {
                logger.warn("Trigger[name={},group={}] has bean already stopped", triggerName, triggerGroupName);
            }
        }
    }

    @Override
    public void resumeTrigger(String triggerName, String triggerGroupName) throws Exception {
        logger.debug("resumeTrigger triggerName={},triggerGroupName={}", triggerName, triggerGroupName);

        TriggerKey triggerKey = getTriggerKey(triggerName, triggerGroupName);
        if (triggerKey != null) {
            TriggerState state = scheduler.getTriggerState(triggerKey);

            if (state == TriggerState.PAUSED) {
                scheduler.resumeTrigger(triggerKey);
            } else {
                logger.warn("Trigger[name={},group={}] has bean already stopped", triggerName, triggerGroupName);
            }
        }
    }

    @Override
    public void removeTrigger(String triggerName, String triggerGroupName) throws Exception {
        // TODO
        logger.debug("removeTrigger triggerName={},triggerGroupName={}", triggerName, triggerGroupName);

    }

    @Override
    public void start() throws SchedulerException {
        logger.debug("scheduler start");

        if (scheduler.isStarted()) {
            logger.warn("Scheduler has started");
        } else {
            scheduler.start();
        }
    }

    @Override
    public void shutdown(boolean waitForJobsToComplete) throws SchedulerException {
        logger.debug("scheduler shutdown");

        if (scheduler.isShutdown()) {
            logger.warn("Scheduler has shutdown!");
        } else {
            scheduler.shutdown(waitForJobsToComplete);
        }
    }

    /**
     * 获取指定job的key
     * 
     * @param jobName
     *            任务名称
     * @param jobGroupName
     *            任务组
     * @return
     * @throws SchedulerException
     */
    private JobKey getJobKey(String jobName, String jobGroupName) throws SchedulerException {
        for (String groupName : scheduler.getJobGroupNames()) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                if (EqualsUtil.equals(jobName, jobKey.getName()) && EqualsUtil.equals(jobGroupName, jobKey.getGroup())) {
                    return jobKey;
                }
            }
        }
        return null;
    }

    /**
     * 查询指定的触发器
     * 
     * @param triggerName
     *            触发器名称
     * @param triggerGroupName
     *            触发器组
     * @return
     * @throws SchedulerException
     */
    private TriggerKey getTriggerKey(String triggerName, String triggerGroupName) throws SchedulerException {
        for (String groupName : scheduler.getTriggerGroupNames()) {
            for (TriggerKey triggerKey : scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(groupName))) {
                if (EqualsUtil.equals(triggerName, triggerKey.getName())
                        && EqualsUtil.equals(triggerGroupName, triggerKey.getGroup())) {
                    return triggerKey;
                }
            }
        }
        return null;
    }
}
