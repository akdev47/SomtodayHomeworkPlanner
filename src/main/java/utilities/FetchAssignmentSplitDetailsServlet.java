package utilities;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import somtoday.model.Homework;
import somtoday.model.SplitHomework;

@WebServlet("/fetchAssignmentSplitDetails")
public class FetchAssignmentSplitDetailsServlet extends HttpServlet {
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
        String splitId = request.getParameter("splitId");

        Homework homeW = new Homework();
        SplitHomework sh = new SplitHomework();

        try {
            java.lang.Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            String sql;
            if ("teacher".equals(role) || "student".equals(role) || "admin".equals(role)) {
                sql = "SELECT h.homework_name, sh.split_name, h.description , sh.time_indication " +
                        "FROM homework h, splitted_homework sh " +
                         "WHERE sh.splitted_homework_id = " + splitId +
                        " AND sh.homework_id = h.homework_id ";
            }  else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid role");
                return;
            }

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                homeW.setHomeworkName(resultSet.getString("homework_name"));
                sh.setSplitName(resultSet.getString("split_name"));
                homeW.setDescription(resultSet.getString("description"));
                sh.setTimeIndication(resultSet.getTime("time_indication"));
            }

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
            return;
        }

        PrintWriter out = response.getWriter();
        out.write(toJson(homeW, sh));
        out.close();
    }

    private String toJson(Homework homeW, SplitHomework sh) {
        StringBuilder json = new StringBuilder();
        json.append("[");
            json.append("{");
            json.append("\"homework_name\":\"").append(homeW.getHomeworkName()).append("\",");
            json.append("\"split_name\":\"").append(sh.getSplitName()).append("\",");
            json.append("\"hw_description\":\"").append(homeW.getDescription()).append("\",");
            json.append("\"time_indication\":\"").append(sh.getTimeIndication()).append("\"");
            json.append("}");
        json.append("]");
        return json.toString();
    }
}
