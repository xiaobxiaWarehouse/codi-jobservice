package com.codi.jobservice.job;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
// @WebAppConfiguration
@ContextConfiguration(locations = { "classpath:spring/spring-application.xml" })
public class BaseTest {

    protected ApplicationContext ctx;

    @Resource(name = "scheduler")
    protected Scheduler scheduler;

    protected Logger logger = LoggerFactory.getLogger(BaseTest.class);

    @Before
    public void init() {
    }

    @After
    public void destroy() throws SchedulerException {
        scheduler.shutdown(true);
    }
    
}
