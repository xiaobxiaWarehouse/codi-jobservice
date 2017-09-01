package com.codi.jobservice.job;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.codi.jobservice.job.BaseTest;
import com.codi.jobservice.service.QuartzService;

/**
 * 
 * @author shi.pengyan
 * @date 2016年11月15日 下午3:10:36
 */
public class JobTest extends BaseTest {

    @Autowired
    private QuartzService quartzService;

    @Test
    public void fundCodeRelationshipTest() throws Exception {
        executeCodiJob("FUND_CODE_RELATIONSHIP_JOB");
    }

    @Test
    public void fundSortJobTest() throws Exception {
        executeCodiJob("FUND_SORT_JOB");
    }

    @Test
    public void fundTypeJobTest() throws Exception {
        executeCodiJob("FUND_TYPE_JOB");
    }

    @Test
    public void fundRatingJobTest() throws Exception {
        executeCodiJob("FUND_RATING_JOB");
    }

    @Test
    public void deleteSimpleTest() throws Exception {
        quartzService.removeJob("SIMPLE_JOB", "CODI_JOBS");
    }

    @Test
    public void secuMainJobTest() throws Exception {
        executeCodiJob("SECU_MAIN_JOB");
    }

    private void executeCodiJob(String jobName) throws Exception {
        executeJob(jobName, "CODI_JOBS");
    }

    private void executeJob(String jobName, String groupName) throws Exception {
        quartzService.triggerJob(jobName, groupName);
        System.in.read();
    }
}
