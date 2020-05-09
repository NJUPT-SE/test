package com.wx.Controller;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@SpringBootApplication
@org.springframework.stereotype.Controller
public class Get_Time_Controller {

    //获得时间
    //url : http://localhost:8080/api/GetTime
    @RequestMapping("api/GetTime")
    @ResponseBody
    public Map<String,Object> getTime(){

        //获得日期和具体时间，分别转换成字符串，分别发送

        System.out.println("微信小程序正在调用...");

        Map<String,Object> map = new HashMap<String, Object>();

        Date current_time = new Date();//获得时间
        SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd");//日期转换成想要的格式
        SimpleDateFormat time = new SimpleDateFormat("HHmmss");//具体时间转换成想要的格式

        String string_date = date.format(current_time);//日期转成字符串
        String string_time = time.format(current_time);//具体时间转成字符串

        List<String> list1 = new ArrayList<String>();//发送日期，格式:20200507
        list1.add(string_date);
        map.put("list1",list1);

        List<String> list2 = new ArrayList<String>();//发送具体时间，格式220159
        list2.add(string_time);
        map.put("list2",list2);

        System.out.println(string_date);
        System.out.println(string_time);
        System.out.println("微信小程序调用完成...");
        return map;

    }

    @RequestMapping("")
    public String Test(){
        return "Hello world";
    }

}
