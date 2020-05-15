package com.wx.Controller;

import net.sf.json.JSONObject;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.*;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;


@RestController
@SpringBootApplication
@org.springframework.stereotype.Controller
public class Big_Day_Controller {

    //新增一条纪念日记录
    //接受从前端传来的uid,title,date,notes,img,存入数据库
    //新建成功返回err=1，失败err=0
    //url:http://localhost:8080/api/bigDay/build
    @RequestMapping("api/bigDay/build")
    @ResponseBody
    public Map<String, Object> bigday_build(int uid, String title,String date, String notes,int img)
            throws  ParseException {

        int err=0; //新建成功，err=1，新建失败，err=0

        DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date date2 = null;
        date2 =format1.parse(date);
        java.sql.Date date3 = new java.sql.Date(date2.getTime());

        //数据库部分
        final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
        final String DB_URL = "jdbc:mysql://localhost:3306/user";
        //数据库的用户名与密码，需要根据自己的设置
        final String USER = "root";
        final String PASS = "123";


        Connection conn = null;
        Statement stmt = null;
        try {
            // 注册JDBC驱动
            Class.forName(JDBC_DRIVER);

            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            stmt = conn.createStatement();
            String sql1,sql2;

            //为bigday分配id
            sql1 = "SELECT * FROM bigday";
            ResultSet rs1 = stmt.executeQuery(sql1);
            int max_id=0;
            while (rs1.next()) {
                if(uid==rs1.getInt("uid")) {
                    int current_id = rs1.getInt("id");
                    if (max_id < current_id) {
                        max_id = current_id;
                    }
                }
            }
            int id=max_id+1;

            //写入数据库完成新建
            if (err==0) {
                try {
                    stmt = conn.prepareStatement("insert into bigday (uid,id,title,date,notes,img) values(?,?,?,?,?,?)");
                    ((PreparedStatement) stmt).setInt(1, uid);
                    ((PreparedStatement) stmt).setInt(2, id);
                    ((PreparedStatement) stmt).setString(3, title);
                    ((PreparedStatement) stmt).setDate(4, date3);
                    ((PreparedStatement) stmt).setString(5, notes);
                    ((PreparedStatement) stmt).setInt(6, img);
                    ((PreparedStatement) stmt).executeUpdate();
                    err=1;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // 完成后关闭
            rs1.close();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        } finally {
            // 关闭资源
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException se2) {
            }// 什么都不做
            try {
                if (conn != null) conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        //返回err
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("err", err);
        return map;

    }

}
