package com.ryml.handelmapping;

import java.lang.reflect.Method;

/**
 * description:
 *
 * @author 刘一博
 * @version V1.0
 * @date 2019/3/1
 */
public class HandelMapping {

    private Object controller;

    private Method method;

    private String url;

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
