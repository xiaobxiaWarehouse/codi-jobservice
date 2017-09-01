package com.codi.jobservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.codi.jobservice.service.QuartzService;

/**
 * SH300数据
 * 
 * @author shi.pengyan
 * @date 2016年11月9日 下午3:18:43
 */
@Controller
@RequestMapping("/sh300")
public class SH300Controller {

    private static final Logger logger = LoggerFactory.getLogger(SH300Controller.class);

    @Autowired
    private QuartzService quartzService;

    @RequestMapping("/reset")
    @ResponseBody
    public void resetAll() throws Exception {
        logger.debug("run load SH300 ALL data in 2015-now");
        quartzService.triggerJob("SH300_ALL_JOB", "CODI_JOBS");
    }

    @RequestMapping("/latest")
    @ResponseBody
    public void loadLatest() throws Exception {
        logger.debug("run load SH300 latest data ");
        quartzService.triggerJob("SH300_LATEST_JOB", "CODI_JOBS");
    }

}
