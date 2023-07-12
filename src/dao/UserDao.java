/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import model.Product;
import model.User;

/**
 *
 * @author Admin
 */
public class UserDao {

    public static void save(User user) {
        String query = "insert into user(name, email, mobileNumber, address, password, securityQuestion, answer, status) values('" + user.getName() + "','" + user.getEmail() + "','" + user.getMobileNumber() + "','" + user.getAddress() + "','" + user.getPassword() + "','" + user.getSecurityQuestion() + "','" + user.getAnswer() + "','false')";
        DbOperation.setDataOrDelete(query, "Registed Successfully! Wait for admin approval!");
    }

    public static boolean emailExists(String email) {
        try {
             ResultSet rs = DbOperation.getData("select *from user where email = '" + email + "'");
            if (rs.next()) {
                return true;
            }
            
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

   
    
    public static void updateToOnline(int ID) {
        try {
            String query = "UPDATE user\n"
                    + "SET active = 1\n"
                    + "WHERE id = "+ID;
            DbOperation.setDataOrDelete(query, "");
            System.err.println("Update to online");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
  
    
    public static User login(String email, String password) {
        User user = new User();
        try {
            ResultSet rs = DbOperation.getData("SELECT * FROM user WHERE email='" + email + "' AND password='" + password + "'");
            System.out.println("Selected user");
            while (rs.next()) {
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setMobileNumber(rs.getString("mobileNumber"));
                user.setAddress(rs.getString("address"));
                user.setSecurityQuestion(rs.getString("securityQuestion"));
                user.setStatus(rs.getString("status"));
            }
            return user;
        } catch (Exception e) {
            System.out.println("dao.UserDao.login()");
            JOptionPane.showMessageDialog(null, e);
        }
        return null;
    }



    public static User getSecurityQuestion(String email) {
        User user = new User();
        try {

            ResultSet rs = DbOperation.getData("select *from user where email = '" + email + "'");
            while (rs.next()) {
                user.setSecurityQuestion(rs.getString("securityQuestion"));
                user.setAnswer(rs.getString("answer"));

            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Incorrect Email");

        }
        return user;

    }
    
     
    

    public static void update(String email, String newPassword) {
        String query = "update user set password = '" + newPassword + "' where email = '" + email + "'";
        DbOperation.setDataOrDelete(query, "Password Changed Successfully");
    }

    public static ArrayList getAllRecords(String email) {
        ArrayList<User> arrayList = new ArrayList<>();
        try {
            ResultSet rs = DbOperation.getData("select *from user where email like '%" + email + "%'");
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setMobileNumber(rs.getString("mobileNumber"));
                user.setAddress(rs.getString("address"));
                user.setSecurityQuestion(rs.getString("securityQuestion"));
                user.setStatus(rs.getString("status"));
                arrayList.add(user);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }

        return arrayList;
    }
    
    public static User getUserByEmail(String email){
        try {
            ResultSet rs = DbOperation.getData("select *from user where email = '" + email + "'");
            User user = new User();
            while (rs.next()) {
                
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setMobileNumber(rs.getString("mobileNumber"));
                user.setAddress(rs.getString("address"));
                user.setSecurityQuestion(rs.getString("securityQuestion"));
                user.setStatus(rs.getString("status"));
            }
            return user;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
        return null;
    }

    public static void changeStatus(String email, String status) {
        String query = "update user set status='" + status + "' where email ='" + email + "'";
        DbOperation.setDataOrDelete(query, "Status changed succesfully");
    }

    public static void changePassword(String email, String oldPassword, String newPassword) {
        try {
            ResultSet rs = DbOperation.getData("select *from user where email='" + email + "' and password='" + oldPassword + "'");
            if (rs.next()) {
                update(email, newPassword);
            } else {
                JOptionPane.showMessageDialog(null, "Old password is wrong");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    public static void changeSecurityQuestion(String email, String password, String securityQuestion, String answer) {
        try {
            ResultSet rs = DbOperation.getData("select *from user where email='" + email + "' and password='" + password + "'");
            if (rs.next()) {
                DbOperation.setDataOrDelete("update user set securityQuestion= '" + securityQuestion + "', answer='" + answer + "' where email='" + email + "'", "Security question changed successfully");
            } else {
                JOptionPane.showMessageDialog(null, "Password is wrong");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    public static User getStatus(String email) {
        User user = null;
        try {
            ResultSet rs = DbOperation.getData("select * from user where email = '" + email + "'");
            while (rs.next()) {
                user = new User();

                user.setStatus(rs.getString("status"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
        return user;
    }
}
