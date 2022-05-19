package servlet;

import javax.annotation.Resource;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
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

@WebServlet(urlPatterns = "/customer")
public class CustomerServlet extends HttpServlet {
    @Resource(name = "java:comp/env/jdbc/pool")
    DataSource ds;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
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
                    ResultSet resultSet = connection.prepareStatement("select * from customer").executeQuery();
                    while (resultSet.next()) {
                        String id = resultSet.getString(1);
                        String name = resultSet.getString(2);
                        String address = resultSet.getString(3);
                        double salary = resultSet.getDouble(4);

                        resp.setStatus(HttpServletResponse.SC_OK);//201

                        objectBuilder.add("id", id);
                        objectBuilder.add("name", name);
                        objectBuilder.add("address", address);
                        objectBuilder.add("salary", salary);

                        arrayBuilder.add(objectBuilder.build());
                    }

                    dataMsgBuilder.add("data", arrayBuilder.build());
                    dataMsgBuilder.add("massage", "Done");
                    dataMsgBuilder.add("status", "200");

                    writer.print(dataMsgBuilder.build());
                    break;
                case "GenId":
//                    resp.addHeader("Access-Control-Allow-Origin","*");
                    ResultSet rst = connection.prepareStatement("SELECT id FROM customer ORDER BY id DESC LIMIT 1").executeQuery();
                    if (rst.next()) {
                        int tempId = Integer.parseInt(rst.getString(1).split("-")[1]);
                        tempId += 1;
                        if (tempId < 10) {
                            objectBuilder.add("id", "C00-00" + tempId);
                        } else if (tempId < 100) {
                            objectBuilder.add("id", "C00-0" + tempId);
                        } else if (tempId < 1000) {
                            objectBuilder.add("id", "C00-" + tempId);
                        }
                    } else {
                        objectBuilder.add("id", "C00-001");
                    }
                    dataMsgBuilder.add("data",objectBuilder.build());
                    dataMsgBuilder.add("massage","Done");
                    dataMsgBuilder.add("status",200);
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
        String cusId = req.getParameter("cusID");
        String cusName = req.getParameter("cusName");
        String cusAddress = req.getParameter("cusAddressA");
        String cusSalary = req.getParameter("cusSalary");

        PrintWriter writer = resp.getWriter();
        Connection connection = null;
        try {
            connection = ds.getConnection();
            PreparedStatement pstm = connection.prepareStatement("INSERT INTO customer VALUES(?,?,?,?)");
            pstm.setObject(1, cusId);
            pstm.setObject(2, cusName);
            pstm.setObject(3, cusAddress);
            pstm.setObject(4, cusSalary);

            if (pstm.executeUpdate() > 0) {
                JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
                resp.setStatus(HttpServletResponse.SC_CREATED);
                objectBuilder.add("status", 200);
                objectBuilder.add("message", "Customer Add Success");
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
            writer.print(objectBuilder.build());
        } finally {
            try {
                connection.close();
            } catch (SQLException throwables) {

            }
        }
    }


}

