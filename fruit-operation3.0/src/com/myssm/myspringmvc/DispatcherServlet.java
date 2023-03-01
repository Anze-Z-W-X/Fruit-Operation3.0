package com.myssm.myspringmvc;

import com.myssm.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

@WebServlet("*.do")
public class DispatcherServlet extends ViewBaseServlet{

    private Map<String,Object> beanMap = new HashMap<>();

    public DispatcherServlet(){
    }

    public void init() throws ServletException {
        super.init();
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("applicationContext.xml");
            //1.创建DocumentBuilderFactory
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            //2.创建DocumentBuilder对象
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder() ;
            //3.创建Document对象
            Document document = documentBuilder.parse(inputStream);

            //4.获取所有的bean节点
            NodeList beanNodeList = document.getElementsByTagName("bean");
            for(int i = 0 ; i<beanNodeList.getLength() ; i++){
                Node beanNode = beanNodeList.item(i);
                if(beanNode.getNodeType() == Node.ELEMENT_NODE){
                    Element beanElement = (Element)beanNode ;
                    String beanId =  beanElement.getAttribute("id");
                    String className = beanElement.getAttribute("class");
                    Class controllerBeanClass = Class.forName(className);
                    Object beanObj = controllerBeanClass.newInstance() ;
                    beanMap.put(beanId , beanObj) ;
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException | IllegalAccessException |
                 InstantiationException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //设置编码
        request.setCharacterEncoding("UTF-8");
        //假设url是：  http://localhost:8080/pro15/hello.do
        //那么servletPath是：    /hello.do
        // 我的思路是：
        // 第1步： /hello.do ->   hello   或者  /fruit.do  -> fruit
        // 第2步： hello -> HelloController 或者 fruit -> FruitController
        String servletPath = request.getServletPath();
        servletPath = servletPath.substring(1);
        int lastDotIndex = servletPath.lastIndexOf(".do") ;
        servletPath = servletPath.substring(0,lastDotIndex);

        Object controllerBeanObj = beanMap.get(servletPath);

        String operate = request.getParameter("operate");
        if(StringUtil.isEmpty(operate)){
            operate = "index" ;
        }

        try {
            Method[] methods = controllerBeanObj.getClass().getDeclaredMethods();
            for(Method method:methods){
                if(operate.equals(method.getName())) {
                    Parameter[] parameters = method.getParameters();
                    Object[] parameterValues = new Object[parameters.length];
                    for(int i=0;i<parameters.length;i++){
                        Parameter parameter = parameters[i];
                        String parameterName = parameter.getType().getSimpleName();
                        if(parameterName.equals("request"))parameterValues[i]=request;
                        else if(parameterName.equals("response"))parameterValues[i]=response;
                        else if(parameterName.equals("seesion"))parameterValues[i]=request.getSession();
                        else {
                            String parameterValue = request.getParameter(parameterName);
                            String typeName = parameter.getType().getName();
                            Object parameterObj = parameterValue;
                            if(parameterObj!=null&&"java.lang.Integer".equals(typeName)){
                                parameterObj = Integer.parseInt(parameterValue);
                            }
                            parameterValues[i]=parameterObj;
                        }
                    }


                    method.setAccessible(true);
                    Object returnObj = method.invoke(controllerBeanObj, parameterValues);

                    //视图处理
                    String methodReturnStr = (String) returnObj;
                    if (methodReturnStr.startsWith("redirect:")) {        //比如：  redirect:fruit.do
                        String redirectStr = methodReturnStr.substring("redirect:".length());
                        response.sendRedirect(redirectStr);
                    }
                    else {
                        super.processTemplate(methodReturnStr, request, response);    // 比如：  "edit"
                    }
                }

            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}

// 常见错误： IllegalArgumentException: argument type mismatch