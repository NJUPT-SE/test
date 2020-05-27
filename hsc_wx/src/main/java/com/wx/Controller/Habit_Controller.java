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


public class Habit_Controller {

    //新增一条习惯打卡记录
    //接受从前端传来的uid,title,remind,time,img
    //为用户的新习惯分配id,再将这6项内容写入数据库
    //新建成功返回err=1，失败err=0
    //url:http://localhost:8080/api/habit/build
    @RequestMapping("api/habit/build")
    @ResponseBody
    public Map<String, Object> habit_build(int uid, String title, String time, int remind, int img) {

        int err = 0; //新建成功，err=1，新建失败，err=0

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
            String sql;

            //为habit分配id
            sql = "SELECT * FROM habit";
            ResultSet rs = stmt.executeQuery(sql);
            int max_id = 0;
            while (rs.next()) {
                if (uid == rs.getInt("uid")) {
                    int current_id = rs.getInt("id");
                    if (max_id < current_id) {
                        max_id = current_id;
                    }
                }
            }
            int id = max_id + 1;

            //写入数据库完成新建

            try {
                stmt = conn.prepareStatement("insert into habit (uid,id,title,time,remind,img," +
                        "lasted_clockin,continue_clockin,total_clockin) values(?,?,?,?,?,?,?,?,?)");
                ((PreparedStatement) stmt).setInt(1, uid);
                ((PreparedStatement) stmt).setInt(2, id);
                ((PreparedStatement) stmt).setString(3, title);
                ((PreparedStatement) stmt).setString(4, time);
                ((PreparedStatement) stmt).setInt(5, remind);
                ((PreparedStatement) stmt).setInt(6, img);
                ((PreparedStatement) stmt).setInt(7, 0); //lasted_clockin初始值为0
                ((PreparedStatement) stmt).setInt(8, 0); //continue_clockin初始值为0
                ((PreparedStatement) stmt).setInt(9, 0); //total_clockin初始值为0
                ((PreparedStatement) stmt).executeUpdate();
                err = 1;
            } catch (Exception e) {
                e.printStackTrace();
            }


            // 完成后关闭
            rs.close();
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

    //删除一条习惯打卡记录
    //接受从前端传来的uid和id,在数据库中查找相应习惯记录并删除
    //删除成功返回err=1，失败err=0
    //url:http://localhost:8080/api/habit/delete
    @RequestMapping("api/habit/delete")
    @ResponseBody
    public Map<String, Object> memo_delete(int uid, int id) {
        int err = 0;  //删除成功，err=1，删除失败，err=0

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

            //根据uid，id找到习惯记录并删除
            stmt = conn.createStatement();
            String sql;
            sql = "DELETE FROM habit WHERE id=" + id + " AND uid=" + uid;
            //System.out.println(sql);
            stmt.executeUpdate(sql);
            err = 1;

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

    //查看用户的全部习惯打卡记录
    //接受从前端传来的uid在数据库中查找所有习惯
    //today_clockin:今日是否打卡，0表示今日未打卡，1表示今日已打卡
    //按id升序返回所有记录的id,title,remind,time,img,lasted_clockin,continue_clockin,total_clockin,is_clockin
    //查看成功返回err=1，失败err=0
    //url:http://localhost:8080/api/habit/view
    @RequestMapping("api/habit/view")
    @ResponseBody
    public Map<String, Object> habit_view(int uid) {
        int err = 0; //查看成功，err=1，查看失败，err=0

        //ArrayList，以Record类型来临时存储用户所有记录
        ArrayList<HashMap> all_record = new ArrayList<>();

        //获得整型当前日期current_date
        Date time1 = new Date();
        SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd");//日期转换成想要的格式
        String string_date = date.format(time1);//日期转成字符串
        int current_date = Integer.parseInt(string_date);
        int current_date_1=current_date-1;

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

            String sql1,sql2;
            //判断continue_clockin是否需要置0
            //current_date-lasted_clockin>1:昨天未打卡，今天未打卡，continue_clockin置0（连续打卡中断）
            sql1 = "UPDATE habit SET continue_clockin=0 WHERE uid="+uid +" AND lasted_clockin<"+current_date_1 ;
            stmt = conn.prepareStatement(sql1);
            stmt.executeUpdate(sql1);

            sql2 = "SELECT * FROM habit WHERE uid="+uid+" ORDER BY id ASC";
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql2);

            int today_clockin = 0;
            while (rs.next()) {
                int lasted_clockin = rs.getInt("lasted_clockin");

                //current_date-lasted_clockin>1:昨天未打卡，今天未打卡，continue_clockin置0（连续打卡中断）
                if ((current_date - lasted_clockin) > 1) {
                    today_clockin = 0;
                }
                //current_date-lasted_clockin=1:昨天打了卡，今天未打卡，continue_clockin不变（昨天的值）
                else if ((current_date - lasted_clockin) == 1) {
                    today_clockin = 0;
                }
                //current_date-lasted_clockin=0:今天打了卡，continue_clockin不变（已在打卡接口中做了计算）
                else if ((current_date - lasted_clockin) == 0) {
                    today_clockin = 1;
                }

                HashMap<String, Object> map1 = new HashMap<String, Object>();

                map1.put("id", rs.getInt("id"));
                map1.put("title", rs.getString("title"));
                map1.put("remind", rs.getInt("remind"));
                map1.put("time", rs.getString("time"));
                map1.put("img", rs.getInt("img"));
                map1.put("lasted_clockin", rs.getInt("lasted_clockin"));
                map1.put("continue_clockin", rs.getInt("continue_clockin"));
                map1.put("total_clockin", rs.getInt("total_clockin"));
                map1.put("today_clockin", today_clockin);

                all_record.add(map1);
            }

            err = 1;
            // 完成后关闭
            stmt.close();
            //stmt.close();
            conn.close();
            //conn2.close();
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
        //返回数组中的数据
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("err", err);
        map.put("data", all_record);
        //System.out.println(all_record);
        return map;

    }

    //修改用户的一条habit记录
    //接受从前端传来的uid,id,title,remind,time,img，根据uid、id定位记录并更新
    //修改成功返回err=1,失败err=0
    //url:http://localhost:8080/api/habit/revise
    @RequestMapping("api/habit/revise")
    @ResponseBody
    public Map<String, Object> habit_revise(int uid, int id, String title, int remind, String time, int img)
            throws ParseException {

        int err = 0; //修改成功，err=1，修改失败，err=0
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
            String sql;
            sql = "update habit set title=?,remind=?,time=?,img=? where uid="+uid+" and id="+id;
            stmt = conn.prepareStatement(sql);
            ((PreparedStatement) stmt).setString(1, title);
            ((PreparedStatement) stmt).setInt(2, remind);
            ((PreparedStatement) stmt).setString(3, time);
            ((PreparedStatement) stmt).setInt(4, img);
            ((PreparedStatement) stmt).executeUpdate();
            err = 1;

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


