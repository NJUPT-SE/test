package com.wx.Controller;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;
import java.util.*;

@RestController
@SpringBootApplication
@org.springframework.stereotype.Controller
public class User_Manage_Controller {

    //接受从前端传来的唯一微信用户识别码（uid）、用户昵称（nickname）、头像url（avatar_url）
    //根据uid查询数据库判断是否注册，已注册返回1，未注册返回0
    //若未注册，则将用户信息写入数据库
    //url : http://localhost:8080/api/UserManage
    @RequestMapping("api/UserManage")
    @ResponseBody
    public Map<String, Object> user_manage(String uid,String nickname,String avatar_url) throws SQLException {
        System.out.println("微信小程序正在调用...");

        String flag = "0";  //判断用户注册or登录，0代表注册，1代表登录

        final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
        final String DB_URL = "jdbc:mysql://localhost:3306/test_db";
        // 数据库的用户名与密码，需要根据自己的设置
        final String USER = "root";
        final String PASS = "222777hhh";

        Connection conn = null;
        Statement stmt = null;
        try{
            // 注册JDBC驱动
            Class.forName(JDBC_DRIVER);

            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL,USER,PASS);

            stmt = conn.createStatement();
            String sql;
            sql = "SELECT uid FROM tb1";
            ResultSet rs = stmt.executeQuery(sql);

            //检索数据库中是否有该uid
            while (rs.next()) {
                String id = rs.getString("uid");
                if (uid.equals(id)) {
                    flag = "1";
                    System.out.println("登录成功");
                    break;
                }
            }

            //数据库中没有该uid，则写入数据库完成注册
            if(flag.equals("0")) {
                try {
                    stmt = conn.prepareStatement("insert into tb1 (uid,nickname,avatar_url) values(?,?,?)");
                    ((PreparedStatement) stmt).setString(1, uid);
                    ((PreparedStatement) stmt).setString(2, nickname);
                    ((PreparedStatement) stmt).setString(3, avatar_url);
                    ((PreparedStatement) stmt).executeUpdate();
                    System.out.println("登录成功");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }

        Map<String, Object> map = new HashMap<String, Object>();

        System.out.println("微信小程序调用完成...");
        map.put("flag", flag);
        return map;
        }

}

