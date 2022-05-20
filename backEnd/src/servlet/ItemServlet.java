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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(urlPatterns = "/item")
public class ItemServlet extends HttpServlet {
    @Resource(name = "java:comp/env/jdbc/pool")
    DataSource ds;

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
            String option = req.getParameter("option");
            switch (option) {
                case "GetAll":
                    ResultSet resultSet = connection.prepareStatement("select * from item").executeQuery();
                    while (resultSet.next()) {
                        String id = resultSet.getString(1);
                        String name = resultSet.getString(2);
                        int qty = resultSet.getInt(3);
                        double price = resultSet.getDouble(4);

                        resp.setStatus(HttpServletResponse.SC_OK);//201

                        objectBuilder.add("itemId", id);
                        objectBuilder.add("itemName", name);
                        objectBuilder.add("itemQty", qty);
                        objectBuilder.add("itemPrice", price);

                        arrayBuilder.add(objectBuilder.build());
                    }

                    dataMsgBuilder.add("data", arrayBuilder.build());
                    dataMsgBuilder.add("massage", "Done");
                    dataMsgBuilder.add("status", "200");

                    writer.print(dataMsgBuilder.build());
                    break;

                case "GenId":
                    ResultSet rst = connection.prepareStatement("SELECT code FROM item ORDER BY code DESC LIMIT 1").executeQuery();
                    if (rst.next()) {
                        int tempId = Integer.parseInt(rst.getString(1).split("-")[1]);
                        tempId += 1;
                        if (tempId < 10) {
                            objectBuilder.add("code", "I00-00" + tempId);
                        } else if (tempId < 100) {
                            objectBuilder.add("code", "I00-0" + tempId);
                        } else if (tempId < 1000) {
                            objectBuilder.add("code", "I00-" + tempId);
                        }
                    } else {
                        objectBuilder.add("code", "I00-001");
                    }
                    dataMsgBuilder.add("data", objectBuilder.build());
                    dataMsgBuilder.add("message", "Done");
                    dataMsgBuilder.add("status", 200);
                    writer.print(dataMsgBuilder.build());
                    break;

            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        String itemId = req.getParameter("itemId");
        String itemName = req.getParameter("itemName");
        String itemQty = req.getParameter("itemQty");
        String itemPrice = req.getParameter("itemPrice");

        PrintWriter writer = resp.getWriter();
        Connection connection = null;
        try {
            connection = ds.getConnection();
            PreparedStatement pstm = connection.prepareStatement("INSERT INTO item VALUES(?,?,?,?)");
            pstm.setObject(1, itemId);
            pstm.setObject(2, itemName);
            pstm.setObject(3, itemQty);
            pstm.setObject(4, itemPrice);

            if (pstm.executeUpdate() > 0) {
                JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
                resp.setStatus(HttpServletResponse.SC_CREATED);
                objectBuilder.add("status", 200);
                objectBuilder.add("message", "Item Add Success");
                objectBuilder.add("data", "");
                writer.print(objectBuilder.build());
            }


        } catch (SQLException throwables) {
            throwables.printStackTrace();
            JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
            resp.setStatus(HttpServletResponse.SC_OK);
            objectBuilder.add("status", 400);
            objectBuilder.add("message", "Error");
            objectBuilder.add("data", throwables.getLocalizedMessage());
        } finally {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        JsonReader reader = Json.createReader(req.getReader());
        JsonObject jsonObject = reader.readObject();

        String itemIDUpdate = jsonObject.getString("itemId");
        String itemNameUpdate = jsonObject.getString("itemName");
        String itemQtyUpdate = jsonObject.getString("itemQty");
        String itemPriceUpdate = jsonObject.getString("itemPrice");
        PrintWriter writer = resp.getWriter();
        System.out.println(itemIDUpdate + " " + itemNameUpdate + " " + itemQtyUpdate + " " + itemPriceUpdate);

        Connection connection = null;
        try {
            connection= ds.getConnection();
            PreparedStatement pstm = connection.prepareStatement("UPDATE item SET description=?, qtyOnHand=?, unitPrice=? WHERE code=?");
            pstm.setObject(1, itemNameUpdate);
            pstm.setObject(2, itemQtyUpdate);
            pstm.setObject(3, itemPriceUpdate);
            pstm.setObject(4, itemIDUpdate);

            if(pstm.executeUpdate()>0){
                JsonObjectBuilder respones = Json.createObjectBuilder();
                resp.setStatus(HttpServletResponse.SC_CREATED);
                respones.add("ststus",200);
                respones.add("message", "Successfully Updated");
                respones.add("data", "");
                writer.print(respones.build());
            }

        } catch (SQLException throwables) {
            JsonObjectBuilder response = Json.createObjectBuilder();
            response.add("status", 400);
            response.add("message", "Error");
            response.add("data", throwables.getLocalizedMessage());
            writer.print(response.build());
            resp.setStatus(HttpServletResponse.SC_OK); //200
        }finally {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }


    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        String itemID = req.getParameter("txtItemId");
        JsonObjectBuilder dataMsgBuilder = Json.createObjectBuilder();
        PrintWriter writer = resp.getWriter();

        Connection connection = null;
        try {
            connection = ds.getConnection();
            PreparedStatement pstm = connection.prepareStatement("DELETE FROM item WHERE code=?");
            pstm.setObject(1,itemID);

            if (pstm.executeUpdate()>0){
                resp.setStatus(HttpServletResponse.SC_OK);
                dataMsgBuilder.add("data","");
                dataMsgBuilder.add("massage","Item Deleted");
                dataMsgBuilder.add("status","200");
                writer.print(dataMsgBuilder.build());
            }
        } catch (SQLException throwables) {
            dataMsgBuilder.add("status",400);
            dataMsgBuilder.add("massage","Error");
            dataMsgBuilder.add("data", throwables.getLocalizedMessage());
            writer.print(dataMsgBuilder.build());
            resp.setStatus(HttpServletResponse.SC_OK);
        }finally {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }
}
