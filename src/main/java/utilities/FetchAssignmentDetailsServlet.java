package utilities;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import somtoday.model.Class;
import somtoday.model.Homework;

@WebServlet("/fetchAssignmentDetails")
public class FetchAssignmentDetailsServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String role = request.getParameter("role");
        String personId = request.getParameter("personId");
        String assignmentId = request.getParameter("id");

        Homework homeW = new Homework();

        try {
            java.lang.Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            String sql;
            if ("teacher".equals(role)) {
                sql = "SELECT DISTINCT h.homework_type, h.description " +
                        "FROM homework h " +
                        "WHERE h.homework_id = " + assignmentId;
            } else if ("student".equals(role)) {
                sql = "SELECT DISTINCT h.homework_type, h.description " +
                        "FROM homework h " +
                        "WHERE h.homework_id = " + assignmentId; // No goals and their time indication are added yet!
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid role");
                return;
            }

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                homeW.setHomeworkType(resultSet.getString("homework_type"));
                homeW.setDescription(resultSet.getString("description"));
            }

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
            return;
        }

        PrintWriter out = response.getWriter();
        out.write(toJson(homeW));
        out.close();
    }

    private String toJson(Homework homeW) {
        StringBuilder json = new StringBuilder();
        json.append("[");
            json.append("{");
            json.append("\"homework_type\":\"").append(homeW.getHomeworkType()).append("\",");
            json.append("\"hw_description\":\"").append(homeW.getDescription()).append("\"");
            json.append("}");
        json.append("]");
        return json.toString();
    }
}
