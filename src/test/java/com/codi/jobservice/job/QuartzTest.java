package com.codi.jobservice.job;

import static org.quartz.CronScheduleBuilder.cronSchedule;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.quartz.DateBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.matchers.GroupMatcher;

import com.codi.base.util.ListUtil;

/**
 * Job Test
 * 
 * @author shi.pengyan
 * @date 2016年11月1日 下午4:48:35
 */
public class QuartzTest extends BaseTest {

    @SuppressWarnings("unchecked")
    @Test
    public void getJobs() throws SchedulerException {
        StringBuffer sb = new StringBuffer();
        sb.append("JOB_GROUP").append("\t").append("JOB_NAME").append("\t").append("NEXT_FIRE_TIME").append("\n");

        for (String groupName : scheduler.getJobGroupNames()) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                String jobName = jobKey.getName();
                String jobGroup = jobKey.getGroup();

                sb.append(jobGroup).append("\t").append(jobName);

                // get job's trigger
                List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);

                if (ListUtil.isNotEmpty(triggers)) {
                    Date nextFireTime = triggers.get(0).getNextFireTime();
                    sb.append("\t").append(nextFireTime);
                }
                sb.append("\n");
            }
        }
        System.out.println(sb.toString());
    }

    @Test
    public void addJob() throws SchedulerException, InterruptedException {
        if (scheduler.isStarted()) {
            System.out.println("scheduler started.");
        }
        // computer a time that is on the next round minute
        Date runTime = DateBuilder.evenMinuteDate(new Date());

        JobDetail job = JobBuilder.newJob(TestJob.class).withIdentity("job1", "group1").storeDurably()
                .withIdentity("Qrtz_Job_Detail").withDescription("Invoke Sample Job service...").build();

        Trigger trigger = TriggerBuilder.newTrigger().withIdentity("trigger1", "group1").startAt(runTime).build();

        // Tell quartz to schedule the job using our trigger
        scheduler.scheduleJob(job, trigger);
        System.out.println(job.getKey());

        if (scheduler.isStarted()) {
            System.out.println("scheduler started.");
        } else {
            scheduler.start();
        }

        Thread.sleep(10L * 1000L);
    }

    /**
     * cronTrigger 示例
     * 
     * @throws SchedulerException
     * @throws InterruptedException
     */
    @Test
    public void addJob2() throws SchedulerException, InterruptedException {

        JobDetail jobDetail = JobBuilder.newJob(TestJob.class).withIdentity("SIMPLE_JOB", "JOB_GROUP1").storeDurably()
                .withDescription("Invoke Sample Job service...").build();

        Trigger trigger = TriggerBuilder.newTrigger().withIdentity("SPY_TEST_TIRGGER", "TRIGGER_GROUP1")
                .withDescription("每隔10s执行一次").withSchedule(cronSchedule("0/10 * * * * ?")).build();
        scheduler.scheduleJob(jobDetail, trigger);
        Thread.sleep(60L * 1000L);

    }

    @Test
    public void deleteAllJob() throws SchedulerException {
        for (String groupName : scheduler.getJobGroupNames()) {

            // Set<JobKey> jobKeys =
            // scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName));

            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                scheduler.deleteJob(jobKey);
            }
        }
    }

    @Test
    public void runJobInDB() throws IOException {
        System.in.read();
    }

}