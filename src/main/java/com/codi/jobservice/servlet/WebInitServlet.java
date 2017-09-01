package com.codi.jobservice.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author shi.pengyan
 * @date 2016年11月1日 下午3:21:34
 */
public class WebInitServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(WebInitServlet.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public WebInitServlet() {
        super();
    }

    @Override
    public void init() throws ServletException {
        super.init();

        logger.debug("Web init Servlet");

        printVersion();
    }

    /**
     * print version
     */
    private void printVersion() {
        StringBuffer version = new StringBuffer();
        version.append("\n\n");
        version.append("\n      *************************************************");
        version.append("\n      *        Job Server successfully launched       *");
        version.append("\n      *************************************************");
        version.append("\n\n");

        logger.info(version.toString());
        System.out.println(version.toString());
    }

    @Override
    public void destroy() {
        super.destroy();
    }

}
