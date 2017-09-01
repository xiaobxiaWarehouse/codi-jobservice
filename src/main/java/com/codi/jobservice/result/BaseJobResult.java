package com.codi.jobservice.result;

import com.codi.base.domain.BaseResult;

/**
 * 基础job返回值
 * 
 * @author shi.pengyan
 * @date 2016年11月25日 下午1:59:41
 */
public class BaseJobResult extends BaseResult {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
