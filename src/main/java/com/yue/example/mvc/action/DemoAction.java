package com.yue.example.mvc.action;

import com.yue.example.service.IDemoService;
import com.yue.mvcframework.v1.annotation.YAutowired;
import com.yue.mvcframework.v1.annotation.YController;
import com.yue.mvcframework.v1.annotation.YRequestMapping;
import com.yue.mvcframework.v1.annotation.YRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@YController
@YRequestMapping("/demo")
public class DemoAction {

  	@YAutowired
	private IDemoService demoService;

	@YRequestMapping("/query")
	public void query(HttpServletRequest req, HttpServletResponse resp,
					  @YRequestParam("name") String name){
		String result = demoService.get(name);
//		String result = "My name is " + name;
		try {
			resp.getWriter().write(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@YRequestMapping("/add")
	public void add(HttpServletRequest req, HttpServletResponse resp,
					@YRequestParam("a") Integer a, @YRequestParam("b") Integer b){
		try {
			resp.getWriter().write(a + "+" + b + "=" + (a + b));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@YRequestMapping("/remove")
	public void remove(HttpServletRequest req, HttpServletResponse resp,
					   @YRequestParam("id") Integer id){
	}

}
