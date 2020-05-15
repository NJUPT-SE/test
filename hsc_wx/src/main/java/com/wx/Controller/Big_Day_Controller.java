package com.wx.Controller;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@SpringBootApplication
@org.springframework.stereotype.Controller
public class Big_Day_Controller {

    //新增一条纪念日记录
    //接受从前端传来的uid,title,date,notes,img存入数据库
    //新建成功返回err=1，失败err=0
    //url:http://localhost:8080/api/bigDay/build
    @RequestMapping("api/bigDay/build")
    @ResponseBody
    public Map<String, Object> bigday_build(int uid, String title,String date, String notes,int img)
            throws  ParseException {

        int err=0; //新建成功，err=1，新建失败，err=0

        //获取的String类型date转化为java.sql.Date类型
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
            String sql;

            //核心部分
            //为bigday分配id
            sql = "SELECT * FROM bigday";
            ResultSet rs = stmt.executeQuery(sql);
            int max_id=0;
            while (rs.next()) {
                if(uid==rs.getInt("uid")) {
                    int current_id = rs.getInt("id");
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


    //删除一条纪念日记录
    //接受从前端传来的uid,id在数据库中查找相应纪念日记录并删除
    //删除成功返回err=1，失败err=0
    //url:http://localhost:8080/api/bigDay/delete
    @RequestMapping("api/bigDay/delete")
    @ResponseBody
    public Map<String, Object> bigday_delete(int uid, int id) {

        int err=0; //删除成功，err=1，删除失败，err=0

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

            //核心部分
            //根据uid，id找到纪念日记录并删除
            stmt = conn.createStatement();
            String sql;
            sql = "DELETE FROM bigday WHERE id="+id+" AND uid="+uid;
            //System.out.println(sql);
            stmt.executeUpdate(sql);
            err=1;

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


    //查看用户的全部纪念日记录
    //接受从前端传来的uid在数据库中查找所有纪念日记录
    //按照date升序(2020-05-11,2020-05-12,2020-05-13)返回所有纪念日记录的id、title、date、notes、img
    //查看成功返回err=1，失败err=0
    //url:http://localhost:8080/api/bigDay/view
    @RequestMapping("api/bigDay/view")
    @ResponseBody
    public Map<String, Object> bigday_view(int uid) {

        int err=0; //删除成功，err=1，删除失败，err=0
        SimpleDateFormat f= new SimpleDateFormat("yyyy-MM-dd");

        //ArrayList，以Record类型来临时存储用户所有记录
        ArrayList<HashMap> all_record=new ArrayList<>();

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
            sql = "SELECT * FROM bigday ORDER BY date ASC";
            ResultSet rs = stmt.executeQuery(sql);

            //根据date

            //根据uid找到所有纪念日记录并储存在ArrayList中
            while (rs.next()) {
                if(uid==rs.getInt("uid")) {

                    HashMap<String, Object> map1 = new HashMap<String, Object>();

                    map1.put("id", rs.getInt("id"));
                    map1.put("title",rs.getString("title"));
                    java.sql.Date current_date=rs.getDate("date");
                    String current_date2=f.format(current_date);        //java.sql.Date转成String
                    map1.put("date",current_date2);
                    map1.put("notes",rs.getString("notes"));
                    map1.put("img",rs.getInt("img"));

                    all_record.add(map1);
                }
            }
            err=1;

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
        //返回数组中的数据
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("err", err);
        map.put("data",all_record);
        //System.out.println(all_record);
        return map;

    }


    //修改用户的一条纪念日记录
    //接受从前端传来的uid、id、title、date、notes、img，根据uid、id定位记录并更新
    //修改成功返回err=1,失败err=0
    //url:http://localhost:8080/api/bigDay/revise
    @RequestMapping("api/bigDay/revise")
    @ResponseBody
    public Map<String, Object> bigday_revise(int uid,int id, String title,String date, String notes,int img)
            throws  ParseException {

        int err=0; //修改成功，err=1，修改失败，err=0

        //获取的String类型date转化为java.sql.Date类型
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

            String sql;
            sql= "update bigday set title=?,date=?,notes=?,img=? where uid=? and id=?";
            stmt=conn.prepareStatement(sql);
            ((PreparedStatement) stmt).setString(1,title);
            ((PreparedStatement) stmt).setDate(2,date3);
            ((PreparedStatement) stmt).setString(3,notes);
            ((PreparedStatement) stmt).setInt(4,img);
            ((PreparedStatement) stmt).setInt(5,uid);
            ((PreparedStatement) stmt).setInt(6,id);
            ((PreparedStatement) stmt).executeUpdate();
            err=1;

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

