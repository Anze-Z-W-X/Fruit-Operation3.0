package com.fruit.controllers;

import com.fruit.dao.FruitDAO;
import com.fruit.dao.impl.FruitDAOImpl;
import com.fruit.pojo.Fruit;
import com.myssm.myspringmvc.ViewBaseServlet;
import com.myssm.util.StringUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class FruitController extends ViewBaseServlet {
    FruitDAO fruitDAO = new FruitDAOImpl();

    private String add(String fname,Integer price,Integer fcount,String remark) throws ServletException, IOException {
        fruitDAO.addFruit(new Fruit(0,fname,price,fcount,remark));
        return "redirect:fruit.do";
    }

    private String del(Integer fid)throws IOException, ServletException {
        if(fid!=null){
            fruitDAO.delFruit(fid);
            return "redirect:fruit.do";
        }
        return "error";
    }

    private String update(Integer fid,String fname,Integer price,Integer fcount,String remark){

        fruitDAO.updateFruit(new Fruit(fid,fname, price ,fcount ,remark ));

        //response.sendRedirect("fruit.do");
        return "redirect:fruit.do";
    }


    private String edit(Integer fid,HttpServletRequest request){
        if(fid!=null){
            Fruit fruit = fruitDAO.getFruitByFid(fid);
            request.setAttribute("fruit",fruit);
            //super.processTemplate("edit",req,resp);
            return "edit";
        }
        return "error";
    }

    private String index(String oper,String keyword,Integer pageNo,HttpServletRequest request) throws IOException {

            HttpSession session = request.getSession() ;
            if(pageNo==null)pageNo=1;

            if(!StringUtil.isEmpty(oper) && "search".equals(oper)){
                if(StringUtil.isEmpty(keyword))keyword="";
            }else {
                Object keywordObj = session.getAttribute("keyword");
                if(keywordObj!=null)keyword = (String)keywordObj;
                else keyword="";
            }

            session.setAttribute("pageNo",pageNo);
            session.setAttribute("keyword",keyword);
            FruitDAO fruitDAO = new FruitDAOImpl();
            List<Fruit> fruitList = fruitDAO.getFruitList(pageNo,keyword);

            session.setAttribute("fruitList",fruitList);

            //总记录条数
            int fruitCount = fruitDAO.getFruitCount(keyword);
            //总页数
            int pageCount = (fruitCount+5-1)/5 ;

            session.setAttribute("pageCount",pageCount);

            return "index";
    }
}
