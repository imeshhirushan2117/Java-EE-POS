package servlet;

import javax.annotation.Resource;
import javax.json.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(urlPatterns = "/order")
public class OrderServlet extends HttpServlet {

    @Resource(name = "java:comp/env/jdbc/pool")
    DataSource ds;

    Connection connection = null;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        JsonObjectBuilder dataMsgBuilder = Json.createObjectBuilder();
        PrintWriter writer = resp.getWriter();

        Connection connection = null;

        try {
            connection = ds.getConnection();
            ResultSet rst;
            PreparedStatement pstm;

            String option = req.getParameter("option");

            switch (option) {
                case "cmb_load_cus_id":
                    rst = connection.prepareStatement("SELECT id FROM customer").executeQuery();
                    while (rst.next()) {
                        String id = rst.getString(1);
                        objectBuilder.add("id", id);
                        arrayBuilder.add(objectBuilder.build());
                    }
                    dataMsgBuilder.add("data", arrayBuilder.build());
                    dataMsgBuilder.add("message", "Done");
                    dataMsgBuilder.add("status", 200);
                    writer.print(dataMsgBuilder.build());
                    break;

                case "cmb_load_item_id":
                    rst = connection.prepareStatement("SELECT code FROM item").executeQuery();
                    while (rst.next()) {
                        String id = rst.getString(1);
                        objectBuilder.add("id", id);
                        arrayBuilder.add(objectBuilder.build());
                    }
                    dataMsgBuilder.add("data", arrayBuilder.build());
                    dataMsgBuilder.add("message", "Done");
                    dataMsgBuilder.add("status", 200);
                    writer.print(dataMsgBuilder.build());
                    break;


                case "SelectedCus":
                    String cusId = req.getParameter("cusId");
                    pstm = connection.prepareStatement("SELECT * FROM customer WHERE id=?");
                    pstm.setObject(1, cusId);
                    rst = pstm.executeQuery();
                    if (rst.next()) {
                        String cusName = rst.getString(2);
                        String cusAddress = rst.getString(3);
                        String cusSalary = rst.getString(4);
                        objectBuilder.add("cusName", cusName);
                        objectBuilder.add("cusAddress", cusAddress);
                        objectBuilder.add("cusSalary", cusSalary);
                        arrayBuilder.add(objectBuilder.build());
                    }
                    dataMsgBuilder.add("data", arrayBuilder.build());
                    dataMsgBuilder.add("message", "Done");
                    dataMsgBuilder.add("status", 200);
                    writer.print(dataMsgBuilder.build());
                    break;

                case "SelectedItem":
                    String itemId = req.getParameter("itemId");
                    pstm = connection.prepareStatement("SELECT * FROM item WHERE code=?");
                    pstm.setObject(1, itemId);
                    rst = pstm.executeQuery();
                    if (rst.next()) {
                        String itemName = rst.getString(2);
                        String itemQty = rst.getString(3);
                        String itemPrice = rst.getString(4);
                        objectBuilder.add("itemName", itemName);
                        objectBuilder.add("itemQty", itemQty);
                        objectBuilder.add("itemPrice", itemPrice);
                        arrayBuilder.add(objectBuilder.build());
                    }
                    dataMsgBuilder.add("data", arrayBuilder.build());
                    dataMsgBuilder.add("message", "Done");
                    dataMsgBuilder.add("status", 200);
                    writer.print(dataMsgBuilder.build());
                    break;

                case "GenOrderId":
                    rst = connection.prepareStatement("SELECT oid FROM orders ORDER BY oid DESC LIMIT 1").executeQuery();
                    if (rst.next()) {
                        int tempId = Integer.parseInt(rst.getString(1).split("-")[1]);
                        tempId += 1;
                        if (tempId < 10) {
                            objectBuilder.add("oid", "O00-00" + tempId);
                        } else if (tempId < 100) {
                            objectBuilder.add("oid", "O00-0" + tempId);
                        } else if (tempId < 1000) {
                            objectBuilder.add("oid", "O00-" + tempId);
                        }
                    } else {
                        objectBuilder.add("oid", "O00-001");
                    }
                    dataMsgBuilder.add("data", objectBuilder.build());
                    dataMsgBuilder.add("message", "Done");
                    dataMsgBuilder.add("status", 200);
                    writer.print(dataMsgBuilder.build());
                    break;

            }


        } catch (SQLException throwables) {
            throwables.printStackTrace();
            dataMsgBuilder.add("data", throwables.getLocalizedMessage());
            dataMsgBuilder.add("message", "Error");
            dataMsgBuilder.add("status", 400);
            writer.print(dataMsgBuilder.build());
        } finally {
            try {
                connection.close();
            } catch (SQLException throwables) {

            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter writer = resp.getWriter();
        JsonReader reader = Json.createReader(req.getReader());
        JsonObject jsonObject = reader.readObject();
        System.out.println(jsonObject);

        JsonObject order = jsonObject.getJsonObject("order");
        System.out.println(order);
        JsonArray orderDetail = jsonObject.getJsonArray("orderDetail");

        System.out.println("before if");
        JsonObjectBuilder response = Json.createObjectBuilder();
        if (saveOrder(order, orderDetail)) {
            System.out.println("true");
            resp.setStatus(HttpServletResponse.SC_CREATED);//201
            response.add("status", 200);
            response.add("message", "Order Successful");
            response.add("data", "");
        } else {
            System.out.println("false");
            resp.setStatus(HttpServletResponse.SC_OK);//201
            response.add("status", 400);
            response.add("message", "Order Not Successful");
            response.add("data", "");
        }
        writer.print(response.build());
    }

    public boolean saveOrder(JsonObject order, JsonArray orderDetail) {

        Connection connection = null;
        try {
            connection = ds.getConnection();
            connection.setAutoCommit(false);
            PreparedStatement pstm = connection.prepareStatement("INSERT INTO orders VALUES(?,?,?,?,?,?)");
            pstm.setObject(1, order.getString("orderId"));
            pstm.setObject(2, order.getString("orderDate"));
            pstm.setObject(3, order.getString("customer"));
            pstm.setObject(4, order.getInt("discount"));
            pstm.setObject(5, order.getString("total"));
            pstm.setObject(6, order.getString("subTotal"));

            System.out.println(order);

            if (pstm.executeUpdate() > 0) {
                if (saveOrderDetails(order.getString("orderId"), orderDetail)) {
                    connection.commit();
                    return true;
                } else {
                    connection.rollback();
                    return false;
                }
            } else {
                connection.rollback();
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(true);
                if (connection != null) {
                    connection.close();
                }
//                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean saveOrderDetails(String oid, JsonArray orderDetail) throws SQLException {
        for (JsonValue orderDetails : orderDetail) {
            JsonObject orderDetailJsonObj = orderDetails.asJsonObject();
            PreparedStatement pstm = connection.prepareStatement("INSERT INTO orderdetails VALUES(?,?,?,?,?)");
            pstm.setObject(1, oid);
            pstm.setObject(2, orderDetailJsonObj.getString("itemCode"));
            pstm.setObject(3, orderDetailJsonObj.getString("itemQty"));
            pstm.setObject(4, orderDetailJsonObj.getString("itemPrice"));
            pstm.setObject(5, orderDetailJsonObj.getString("itemTotal"));
            System.out.println("PAKAYA "+orderDetailJsonObj);

            if (pstm.executeUpdate() > 0) {
                if (updateItem(orderDetailJsonObj.getString("itemCode"), orderDetailJsonObj.getString("itemQty"))) {

                } else {
                    return false;
                }
            } else {
                return false;
            }

        }
        return true;
    }

    public boolean updateItem(String ItemCode, String itemQty) throws SQLException {
        
        PreparedStatement pstm = connection.prepareStatement("UPDATE item SET qtyOnHand=(qtyOnHand-?) WHERE code=?");
        pstm.setObject(1, itemQty);
        pstm.setObject(2, ItemCode);
        return pstm.executeUpdate() > 0;
    }

}
