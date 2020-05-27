package com.wx.Controller;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@SpringBootApplication
@org.springframework.stereotype.Controller

public class Habit_Clockin_Controller {
    //打卡
    //接受从前端传来的uid、id，根据uid、id定位记录并进行打卡
    //打卡成功返回err=1,今日已打卡即打卡失败err=0
    //url:http://localhost:8080/api/habit/clockin
    @RequestMapping("api/habit/clockin")
    @ResponseBody
    public Map<String, Object> habit_clockin(int uid,int id)
    {
        //获得整型当前日期current_date
        Date time1 = new Date();
        SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd");//日期转换成想要的格式
        String string_date = date.format(time1);//日期转成字符串
        int current_date = Integer.parseInt(string_date);

        int err=0; //打卡成功，err=1，打卡失败，err=0
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

            //更新continue_clockin
            int lasted_clockin=0,continue_clockin=0,total_clockin=0;
            String sql= "SELECT * FROM habit WHERE uid="+uid+" AND id="+id;
            stmt = conn.createStatement();
            ResultSet rs=stmt.executeQuery(sql);
            while (rs.next()) {
                 lasted_clockin = rs.getInt("lasted_clockin");
                 continue_clockin = rs.getInt("continue_clockin");
                 total_clockin = rs.getInt("total_clockin");}


            stmt = conn.prepareStatement("UPDATE habit set continue_clockin=? WHERE uid="+uid+" AND id="+id);

            if(current_date-lasted_clockin==1)  {          //昨天打了卡，continue_clockin加一
                ((PreparedStatement) stmt).setInt(1, continue_clockin+1);
                ((PreparedStatement) stmt).executeUpdate();
                err=1;
                System.out.println("case1");
            }
            else if(current_date-lasted_clockin>1) {      //昨天未打卡，continue_clockin置一
                ((PreparedStatement) stmt).setInt(1, 1);
                ((PreparedStatement) stmt).executeUpdate();
                err = 1;
                System.out.println("case2");
            }
            else if(current_date-lasted_clockin==0) {   //今天打过卡了，continue_clockin不变
                err = 0;
                System.out.println("case3");
            }

            if(err==1) { //今天未打卡
                //更新lasted_clockin
                stmt = conn.prepareStatement("UPDATE habit set lasted_clockin=? WHERE uid=" + uid + " AND id=" + id);
                ((PreparedStatement) stmt).setInt(1, current_date);
                ((PreparedStatement) stmt).executeUpdate();

                //更新total_clockin
                stmt = conn.prepareStatement("UPDATE habit set total_clockin=? WHERE uid=" + uid + " AND id=" + id);
                ((PreparedStatement) stmt).setInt(1, total_clockin + 1);
                ((PreparedStatement) stmt).executeUpdate();
            }

            // 完成后关闭
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
