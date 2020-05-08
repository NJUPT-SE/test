package com.wx.Controller;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@SpringBootApplication
@org.springframework.stereotype.Controller
public class User_Manage_Controller {


    @RequestMapping("api/user/SignIn")
    @ResponseBody
    public Map<String,Object> SignIn(){
        System.out.println("微信小程序正在调用...");

        Map<String,Object> map = new HashMap<String, Object>();

        System.out.println("微信小程序调用完成...");
        return map;

    }

    @RequestMapping("api/user/Register")
    @ResponseBody
    public String Register(){
            return "r";
    }

}
