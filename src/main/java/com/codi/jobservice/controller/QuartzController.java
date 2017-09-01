package com.codi.jobservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.codi.base.domain.BaseResult;
import com.codi.jobservice.result.BaseJobResult;
import com.codi.jobservice.service.QuartzService;

/**
 * 执行任务
 * 
 * @author shi.pengyan
 * @date 2016年11月15日 上午9:52:08
 */
@Controller
@RequestMapping("/quartz")
public class QuartzController {

    private static final Logger logger = LoggerFactory.getLogger(QuartzController.class);
    @Autowired
    private QuartzService quartzService;

    /**
     * 暂时手工调用 TODO
     * 
     * @param jobName
     * @param groupName
     * @throws Exception
     */
    @RequestMapping(value = "/execute", method = RequestMethod.GET)
    @ResponseBody
    public BaseResult executeJob(@RequestParam(value = "jobName") String jobName,
            @RequestParam(value = "groupName") String groupName) throws Exception {

        logger.info("jobName={},groupName={}", jobName, groupName);

        quartzService.triggerJob(jobName, groupName);

        BaseJobResult result = new BaseJobResult();
        result.setSuccess(true);
        result.setDescription("Please check Redis KEY, after 5s.");

        return result;
    }

}
