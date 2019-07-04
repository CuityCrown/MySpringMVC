package com.ryml.dispatcher;

import com.ryml.annotation.MyAutoWriter;
import com.ryml.annotation.MyController;
import com.ryml.annotation.MyRequestMapping;
import com.ryml.annotation.MyService;
import com.ryml.handelmapping.HandelMapping;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * description:
 *
 * @author 刘一博
 * @version V1.0
 * @date 2019/2/28
 */
public class MyDispatcherServlet extends HttpServlet {

    private Properties properties = new Properties();

    private List<String> classNames = new ArrayList<String>();

    private Map<String, Object> beans = new HashMap<String, Object>();

    private Map<String, HandelMapping> handelMappingMap = new HashMap<String, HandelMapping>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        doConfig(config);

        doScanner(properties.getProperty("basePackage"));

        doInstance();

        initHandelMapping();
    }

    private void doConfig(ServletConfig config) {
        try {
            InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(config.getInitParameter("propertiesPath"));
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource(scanPackage.replaceAll("\\.", "/"));

        File file = new File(url.getFile());

        File[] files = file.listFiles();

        for (File file1 : files) {
            if (file1.isDirectory()) {
                doScanner(scanPackage + "." + file1.getName());
            } else {
                if (!file1.getName().endsWith(".class")) {
                    continue;
                }
                classNames.add(scanPackage + "." + file1.getName().replace(".class", ""));
            }
        }
    }

    private void doInstance() {
        if (classNames.isEmpty()) {
            return;
        }

        try {

            for (String className : classNames) {

                Class<?> beanClass = Class.forName(className);

                if (beanClass.isAnnotationPresent(MyController.class)) {
                    MyController annotation = beanClass.getAnnotation(MyController.class);
                    String value = annotation.value();
                    Object bean = beanClass.newInstance();
                    if (value.length() > 0) {
                        beans.put(value, bean);
                    } else {
                        String beanName = beanClass.getSimpleName();
                        beans.put(makeFirstLetterToLowercase(beanName), bean);
                    }
                } else if (beanClass.isAnnotationPresent(MyService.class)) {
                    MyService annotation = beanClass.getAnnotation(MyService.class);
                    String value = annotation.value();
                    Object bean = beanClass.newInstance();
                    if (value.length() > 0) {
                        beans.put(value, bean);
                    } else {
                        String beanName = beanClass.getSimpleName();
                        beans.put(makeFirstLetterToLowercase(beanName), bean);
                    }
                }
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }


    }

    public static String makeFirstLetterToLowercase(String s){
        if(Character.isLowerCase(s.charAt(0))){
            return s;
        }else{
            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
        }
    }
    private void initHandelMapping() {
        if (beans.isEmpty()) {
            return;
        }
        try {
            for (Object bean : beans.values()) {
                Class<?> aClass = bean.getClass();
                if (aClass.isAnnotationPresent(MyController.class)) {
                    doAutoWrite(bean, aClass);
                    String baseUrl = "";
                    if (aClass.isAnnotationPresent(MyRequestMapping.class)) {
                        baseUrl = aClass.getAnnotation(MyRequestMapping.class).value().replaceAll("/+","/");
                    }
                    Method[] declaredMethods = aClass.getMethods();
                    for (Method declaredMethod : declaredMethods) {
                        if (declaredMethod.isAnnotationPresent(MyRequestMapping.class)) {
                            MyRequestMapping annotation = declaredMethod.getAnnotation(MyRequestMapping.class);
                            HandelMapping handelMapping = new HandelMapping();
                            handelMapping.setMethod(declaredMethod);
                            handelMapping.setUrl(baseUrl);
                            handelMapping.setController(bean);
                            handelMappingMap.put(baseUrl + annotation.value().replaceAll("/+","/"), handelMapping);
                        }
                    }
                } else if (aClass.isAnnotationPresent(MyService.class)) {
                    doAutoWrite(bean, aClass);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void doAutoWrite(Object bean, Class aClass) throws IllegalAccessException {
        Field[] declaredFields = aClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            if (!declaredField.isAnnotationPresent(MyAutoWriter.class)) {continue;}
            MyAutoWriter annotation = declaredField.getAnnotation(MyAutoWriter.class);
            String value = annotation.value();
            if (value.length() > 0) {
                Object o = beans.get(value);
                declaredField.set(bean, o);
            } else {
                Object o1 = beans.get(declaredField.getName());
                if (o1 != null){
                    declaredField.set(bean,o1);
                }else{
                    for (Object o : beans.values()) {

                    }
                }
            }
        }
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String servletPath = request.getRequestURI().toString().replace(request.getContextPath(),"").replaceAll("/+","/");
        HandelMapping handelMapping = handelMappingMap.get(servletPath);
        PrintWriter writer = response.getWriter();
        try {
            if (handelMapping == null){
                writer.write("404 not found");
            }else{
                Method method = handelMapping.getMethod();
                Object name = method.invoke(handelMapping.getController(), request.getParameter("name"));
                writer.print(name);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }finally {
            writer.flush();
            writer.close();
        }
    }
}
