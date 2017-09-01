package com.codi.jobservice.service;

import org.quartz.JobDetail;
import org.quartz.SchedulerException;

import com.codi.base.exception.BaseAppException;

/**
 * JOB服务
 * 
 * @author shi.pengyan
 * @date 2016年11月2日 下午1:14:25
 */
public interface QuartzService {

    /**
     * 增加Job并关联CronTrigger
     * 
     * @param jobName
     *            job名称
     * @param jobGroupName
     *            job组
     * @param desc
     *            描述
     * @param cronTriggerStr
     *            cron表达式
     */
    public void addJob(String jobName, String jobGroupName, String desc, String clazz, String triggerName,
            String triggerGroupName, String cronTriggerStr) throws Exception;

    /**
     * 修改Job
     * 
     * @param jobName
     *            job名称
     * @param jobGroupName
     *            job组
     * @param cronTriggerStr
     */
    public void modifyJob(String jobName, String jobGroupName, String cronTriggerStr);

    /**
     * 获取Job信息
     * 
     * @param jobName
     *            job名称
     * @param jobGroupName
     *            job组
     * @return
     * @throws BaseAppException
     */
    public JobDetail getJob(String jobName, String jobGroupName) throws Exception;

    /**
     * 删除Job
     * 
     * @param jobName
     *            job名称
     * @param jobGroupName
     *            job组
     */
    public void removeJob(String jobName, String jobGroupName) throws Exception;

    /**
     * 暂停Job
     * 
     * @param jobName
     *            job名称
     * @param jobGroupName
     *            job组
     */
    public void pauseJob(String jobName, String jobGroupName) throws Exception;

    /**
     * 重新启动Job
     * 
     * @param jobName
     *            job名称
     * @param jobGroupName
     *            job组
     */
    public void resumeJob(String jobName, String jobGroupName) throws Exception;

    /**
     * 停止正在运行中的job
     * 
     * @param jobName
     *            job名称
     * @param jobGroupName
     *            job组
     * @throws Exception
     */
    public void stopJobOfRunning(String jobName, String jobGroupName) throws Exception;

    /**
     * 立即执行JOB
     * 
     * @param jobName
     *            job名称
     * @param jobGroupName
     *            job组
     * @throws Exception
     */
    public void triggerJob(String jobName, String jobGroupName) throws Exception;

    /**
     * 暂停触发器执行
     * 
     * @param triggerName
     *            触发器名称
     * @param triggerGroupName
     *            触发器组
     */
    public void pauseTrigger(String triggerName, String triggerGroupName) throws Exception;

    /**
     * 重新启动触发器
     * 
     * @param triggerName
     *            触发器名称
     * @param triggerGroupName
     *            触发器组
     */
    public void resumeTrigger(String triggerName, String triggerGroupName) throws Exception;

    /**
     * 移除触发器
     * 
     * @param triggerName
     *            触发器名称
     * @param triggerGroupName
     *            触发器组
     */
    public void removeTrigger(String triggerName, String triggerGroupName) throws Exception;

    /**
     * 启动任务调度
     */
    public void start() throws SchedulerException;

    /**
     * 停止任务调度
     * 
     * @param waitForJobsToComplete
     *            是否等待运行中的JOB完成
     */
    public void shutdown(boolean waitForJobsToComplete) throws SchedulerException;

}
