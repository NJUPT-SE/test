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
import java.util.*;


@RestController
@SpringBootApplication
@org.springframework.stereotype.Controller
public class User_Manage_Controller {

    //接受从前端传来的code（code）、性别（gender）、用户昵称（nickname）、头像url（avaUrl）
    //根据uid查询数据库判断是否注册，已注册flag返回1，未注册flag返回0,并返回用户uid
    //若未注册，则将用户信息写入数据库
    //url : http://localhost:8080/api/UserManage
    @RequestMapping("api/UserManage")
    @ResponseBody
    public Map<String, Object> user_manage(String code, String gender, String nickname, String avaUrl)
            throws SQLException {
        System.out.println("微信小程序正在调用...");

        String flag = "0";  //判断用户注册or登录，0代表注册，1代表登录
        int uid=0;  //用户uid，为自增长的主键

        //POST获取用户openid
        String url="https://api.weixin.qq.com/sns/jscode2session";
        String realurl=url+"?"+"appid=wx1c1d22415dd9aa90&secret=93298a938c8363cab3b408b5f0bbd956&js_code="
                +code+"&grant_type=authorization_code";
        String R=sendGet(realurl);

        //提取openid
        JSONObject object=JSONObject.fromObject(R);
        String openid= (String) object.get("openid");
        System.out.println("openid:"+openid);

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

            //检索数据库中是否有该openid
            sql1 = "SELECT openid FROM tb1";
            ResultSet rs1 = stmt.executeQuery(sql1);
            while (rs1.next()) {
                String exist_openid_1 = rs1.getString("openid");
                if (openid.equals(exist_openid_1)) {
                    flag = "1";
                    System.out.println("登录成功");
                    break;
                }
            }

            //数据库中没有该openid，则写入数据库完成注册
            if (flag.equals("0")) {
                try {
                    stmt = conn.prepareStatement("insert into tb1 (openid,gender,nickname,avaUrl) values(?,?,?,?)");
                    ((PreparedStatement) stmt).setString(1, openid);
                    ((PreparedStatement) stmt).setString(2, gender);
                    ((PreparedStatement) stmt).setString(3, nickname);
                    ((PreparedStatement) stmt).setString(4, avaUrl);
                    ((PreparedStatement) stmt).executeUpdate();
                    System.out.println("注册成功");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //根据openid获取uid
            sql2="SELECT * FROM tb1";
            ResultSet rs2 = stmt.executeQuery(sql2);
            while (rs2.next()) {
                String exist_openid_2 = rs2.getString("openid");
                if (openid.equals(exist_openid_2)) {
                    uid=rs2.getInt("uid");
                    break;
                }
            }

            // 完成后关闭
            rs1.close();
            rs2.close();
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

        Map<String, Object> map = new HashMap<String, Object>();

        System.out.println("微信小程序调用完成...");
        map.put("uid",uid);
        map.put("flag", flag);
        return map;
    }

    //GET请求
    public static String sendGet(String url) {
        String result = "";
        BufferedReader in = null;
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            // 遍历所有的响应头字段
            for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送GET请求出现异常！" + e);
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }
}
