package controller;

import dao.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import model.Category;
import model.Product;
import model.User;

/**
 *
 * @author Admin
 */
public class ServerThread implements Runnable {

    private User user;
    private Socket socketOfServer;
    private int clientNumber;
    private BufferedReader is;
    private BufferedWriter os;
    private boolean isClosed;
    private String clientIP;

    public BufferedReader getIs() {
        return is;
    }

    public BufferedWriter getOs() {
        return os;
    }

    public int getClientNumber() {
        return clientNumber;
    }

    public User getUser() {
        return user;
    }

    public String getClientIP() {
        return clientIP;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ServerThread(Socket socketOfServer, int clientNumber) {
        this.socketOfServer = socketOfServer;
        this.clientNumber = clientNumber;
        System.out.println("Server thread number " + clientNumber + " Started");
        isClosed = false;

        //Trường hợp test máy ở server sẽ lỗi do hostaddress là localhost
        if (this.socketOfServer.getInetAddress().getHostAddress().equals("127.0.0.1")) {
            clientIP = "127.0.0.1";
        } else {
            clientIP = this.socketOfServer.getInetAddress().getHostAddress();
        }

    }

    public String getStringFromUser(User user1) {
        return "" + user1.getId() + "," + user1.getName()
                + "," + user1.getEmail() + "," + user1.getMobileNumber() + ","
                + user1.getAddress() + "," + user1.getPassword() + ","
                + user1.getSecurityQuestion() + "," + user1.getAnswer() + "," + user1.getStatus();
    }

    @Override
    public void run() {
        try {
            // Mở luồng vào ra trên Socket tại Server.
            is = new BufferedReader(new InputStreamReader(socketOfServer.getInputStream()));
            os = new BufferedWriter(new OutputStreamWriter(socketOfServer.getOutputStream()));
            System.out.println("Khời động luông mới thành công, ID là: " + clientNumber);
            write("server-send-id" + "," + this.clientNumber);
            String message;
            while (!isClosed) {
                message = is.readLine();
                if (message == null) {
                    break;
                }
                String[] messageSplit = message.split(",");
                //Xác minh
                if (messageSplit[0].equals("client-verify")) {
                    System.out.println(message);
                    User user1 = UserDao.login(messageSplit[1], messageSplit[2]);//email and password
                    if (user1 == null) {
                        write("wrong-user," + messageSplit[1] + "," + messageSplit[2]);
                        System.out.println("wrong-user," + messageSplit[1] + "," + messageSplit[2]);
                    } else {
                        write("login-success," + getStringFromUser(user1));
                        System.out.println(getStringFromUser(user1));
                        this.user = user1;
                        System.out.println();
                        UserDao.updateToOnline(this.user.getId());
                        Server.serverThreadBus.boardCast(clientNumber, "chat-server," + user1.getName() + " đang online");
                        Server.admin.addMessage("[" + user1.getId() + "] " + user1.getName() + " đang online");
                    }
                }

                //Xử lý đăng kí
                if (messageSplit[0].equals("register")) {
                    boolean checkdup = UserDao.emailExists(messageSplit[2]);
                    if (checkdup) {
                        write("duplicate-email,");
                    } else {
                        User userRegister = new User(messageSplit[1], messageSplit[2], messageSplit[3], messageSplit[4], messageSplit[4],
                                messageSplit[5], messageSplit[6], messageSplit[7]);
                        UserDao.save(userRegister);
                        User userRegistered = UserDao.getUserByEmail(userRegister.getEmail());
                        if (userRegistered != null) {
                            this.user = userRegistered;
                            UserDao.updateToOnline(this.user.getId());
                            Server.serverThreadBus.boardCast(clientNumber, "chat-server," + this.user.getName() + " đang online");
                            write("login-success," + getStringFromUser(this.user));
                        } else {
                            System.err.println("Fail to sign up");
                        }

                    }
                }
                if (messageSplit[0].equals("product-name-by-category")) {
                    ArrayList<Product> list = ProductDao.getAllRecordsByCategory(messageSplit[1]);
                    Iterator<Product> itr = list.iterator();
                    StringBuilder listProducts = new StringBuilder();

                    while (itr.hasNext()) {
                        Product productObj = itr.next();
                        listProducts.append(productObj.getName()).append(",");
                    }

                    // Remove the trailing comma if the string is not empty
                    if (listProducts.length() > 0) {
                        listProducts.setLength(listProducts.length() - 1);
                    }
                    System.out.println(listProducts);
                    write("product-name-by-category," + listProducts);
                }
                //Xử lý chat toàn server
                if (messageSplit[0].equals("chat-server")) {
                    Server.serverThreadBus.boardCast(clientNumber, messageSplit[0] + "," + user.getName() + " : " + messageSplit[1]);
                    Server.admin.addMessage("[" + user.getId() + "] " + user.getName() + " : " + messageSplit[1]);
                }
                
                //Get all category 
                if(messageSplit[0].equals("get-all-category")){
                   ArrayList<Category> list = CategoryDao.getAllRecords();
                    Iterator<Category> itr = list.iterator();
                    StringBuilder listCategorys = new StringBuilder();

                    while (itr.hasNext()) {
                        Category categoryObj = itr.next();
                        listCategorys.append(categoryObj.getName()).append(",");
                    }

                    // Remove the trailing comma if the string is not empty
                    if (listCategorys.length() > 0) {
                        listCategorys.setLength(listCategorys.length() - 1);
                    }
                    System.out.println(listCategorys);
                    write("get-all-category," +  listCategorys);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void write(String message) throws IOException {
        os.write(message);
        os.newLine();
        os.flush();
    }
}
