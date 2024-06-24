package controller;

import util.Util;
import util.Mapping;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.stream.Collectors;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import annotation.*;
import model.*;

public class FrontController extends HttpServlet {
    private List<String> controllers;
    private HashMap<String, Mapping> map;

    @Override
    public void init() throws ServletException {
        try {
            String packageName = this.getInitParameter("package_name");
            controllers = Util.getAllClassesSelonAnnotation(packageName, ControllerAnnotation.class);
            map = Util.getAllMethods(controllers);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException("Initialization failed: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();
    
        String url = req.getRequestURI();
    
        boolean urlExists = false;
        if (map.containsKey(url)) {
            urlExists = true;
            Mapping mapping = map.get(url);
    
            try {
                Class<?> c = Class.forName(mapping.getClassName());
                Method[] methods = c.getDeclaredMethods();
                Method m = null;
                for (Method method : methods) {
                    if (method.getName().equals(mapping.getMethodeName())) {
                        m = method;
                        break; // Exit loop once the method is found
                    }
                }
    
                if (m == null) {
                    throw new ServletException("Method " + mapping.getMethodeName() + " not found in class " + mapping.getClassName());
                }
    
                Parameter[] params = m.getParameters();
                Object instance = c.getDeclaredConstructor().newInstance();
                Object result;
    
                if (params.length < 1) {
                    result = m.invoke(instance);
                } else {
                    Object[] parameterValues = new Object[params.length];
    
                    for (int i = 0; i < params.length; i++) {
                        if (params[i].isAnnotationPresent(Param.class)) {
                            Param param = params[i].getAnnotation(Param.class);
                            String paramName = param.name();
                            String paramValue = req.getParameter(paramName);
                            parameterValues[i] = Util.convertParameterValue(paramValue, params[i].getType());
                        } else if (params[i].isAnnotationPresent(ParamObject.class)) {
                            ParamObject paramObject = params[i].getAnnotation(ParamObject.class);
                            String objName = paramObject.objName();
                            try {
                                Object paramObjectInstance = params[i].getType().getDeclaredConstructor().newInstance();
                                Field[] fields = params[i].getType().getDeclaredFields();
                                for (Field field : fields) {
                                    String fieldName = field.getName();
                                    String paramValue = req.getParameter(objName + "." + fieldName);
                                    field.setAccessible(true);
                                    field.set(paramObjectInstance, Util.convertParameterValue(paramValue, field.getType()));
                                }
                                parameterValues[i] = paramObjectInstance;
                            } catch (Exception e) {
                                e.printStackTrace();
                                throw new RuntimeException("Failed to create and populate parameter object: " + e.getMessage());
                            }
                        } else {
                            throw new RuntimeException("Parameter name could not be determined for parameter index " + i);
                        }
                    }
                    result = m.invoke(instance, parameterValues);
                }
    
                if (result instanceof ModelView) {
                    ModelView mv = (ModelView) result;
                    String jspPath = mv.getUrl();
                    ServletContext context = getServletContext();
                    String realPath = context.getRealPath(jspPath);
    
                    if (realPath == null || !new File(realPath).exists()) {
                        throw new ServletException("The JSP page " + jspPath + " does not exist.");
                    }
    
                    HashMap<String, Object> data = mv.getData();
                    for (Map.Entry<String, Object> entry : data.entrySet()) {
                        req.setAttribute(entry.getKey(), entry.getValue());
                    }
    
                    RequestDispatcher dispatch = req.getRequestDispatcher(jspPath);
                    dispatch.forward(req, res);
                } else if (result instanceof String) {
                    out.println(result.toString());
                } else {
                    throw new ServletException("Unknown return type: " + result.getClass().getSimpleName());
                }
            } catch (Exception e) {
                e.printStackTrace();
                out.println("Error: " + e.getMessage());
            }
        }
    
        if (!urlExists) {
            out.println("No method is associated with the URL: " + url);
        }
    }
    
}
