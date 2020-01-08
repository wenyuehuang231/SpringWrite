package com.yue.mvcframework.v1.servlet;

import com.yue.mvcframework.v1.annotation.YAutowired;
import com.yue.mvcframework.v1.annotation.YController;
import com.yue.mvcframework.v1.annotation.YRequestMapping;
import com.yue.mvcframework.v1.annotation.YService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * created by Mr.huang on 2020/1/8
 */
public class YDispatcherServlet extends HttpServlet {

    /**
     * 放置className的容器
     */
    private Map<String,Object> mapping = new HashMap<String, Object>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req,resp);
        } catch (Exception e) {
            resp.getWriter().write("500 Exception " + Arrays.toString(e.getStackTrace()));
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");
        if(!this.mapping.containsKey(url)){resp.getWriter().write("404 Not Found!!");return;}

        Method method = (Method) this.mapping.get(url);
        Map<String,String[]> params = req.getParameterMap();
        method.invoke(this.mapping.get(method.getDeclaringClass().getName()),new Object[]{req,resp,params.get("name")[0]});
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        InputStream is = null;
        try {
            //1、读取到扫描目录
            is = this.getClass().getClassLoader().getResourceAsStream(config.getInitParameter("contextConfigLocat ion"));
            Properties configContext = new Properties();
            configContext.load(is);
            String scanPackage = configContext.getProperty("scanPackage");

            //2、读取到目录中所有的class并放置className到mapping容器中
            doScanner(scanPackage);

            for (String className : mapping.keySet()) {
                if(!className.contains(".")){continue;}
                Class<?> clazz = Class.forName(className);

                if(clazz.isAnnotationPresent(YController.class)){
                    mapping.put(className, clazz.getInterfaces());
                    String baseUrl = "";
                    //如果--类名--包含YRequestMapping，读取到YRequestMapping中的值，放到url的头部，最后放入mapping容器中
                    if (clazz.isAnnotationPresent(YRequestMapping.class)) {
                        YRequestMapping clazzRequestMapping = clazz.getAnnotation(YRequestMapping.class);
                        baseUrl = clazzRequestMapping.value();

                        //如果--方法--包含YRequestMapping，读取到YRequestMapping中的值，并拼接到url最后
                        //并把方法放入到value中
                        Method[] methods = clazz.getMethods();
                        for (Method method : methods) {
                            if (!method.isAnnotationPresent(YRequestMapping.class)) { continue; }
                            YRequestMapping methodRequestMapping = clazz.getAnnotation(YRequestMapping.class);
                            String url = (baseUrl + "/" + methodRequestMapping.value()).replaceAll("/+", "/");
                            mapping.put(url, method);
                            System.out.println("Mapped " + url + "," + method);
                        }
                    }
                }else if (clazz.isAnnotationPresent(YService.class)) {
                    YService service = clazz.getAnnotation(YService.class);
                    String beanName = service.value();
                    if("".equals(beanName)){
                        beanName = clazz.getName();
                    }
                    Object instance = clazz.newInstance();
                    mapping.put(beanName,instance);
                    for (Class<?> i : clazz.getInterfaces()) { mapping.put(i.getName(),instance); }
                }else {continue;}
            }


            for (Object object : mapping.values()) {
                if(object == null){continue;}
                Class clazz = object.getClass();
                if(clazz.isAnnotationPresent(YController.class)){
                    Field [] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        if(!field.isAnnotationPresent(YAutowired.class)){continue;}
                        YAutowired autowired = field.getAnnotation(YAutowired.class);
                        String beanName = autowired.value();
                        if("".equals(beanName)){beanName = field.getType().getName();}

                        //只要加了@Autowired 注解，都要强制赋值
                        field.setAccessible(true);
                        try {
                            //用反射机制，动态给字段赋值[即完成依赖注入]
                            field.set(mapping.get(clazz.getName()),mapping.get(beanName));
                        }catch (IllegalAccessException e) { e.printStackTrace(); }
                    }
                }
            }

        } catch (Exception e) { } finally {
            if(is != null){
                try { is.close(); }
                catch (IOException e) { e.printStackTrace(); }
            }
        }
        System.out.print("GP MVC Framework is init");
    }

    //读取目录
    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.","/"));
        File classDir = new File(url.getFile());
        for (File file : classDir.listFiles()) {
            if(file.isDirectory()){
                doScanner(scanPackage + "." + file.getName());
            } else {
                if(!file.getName().endsWith(".class")){continue;}
                //将所有的className放入mapping容器中
                String clazzName = (scanPackage + "." + file.getName().replace(".class",""));
                mapping.put(clazzName, null);
            }
        }

    }

}
