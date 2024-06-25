package utilities;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.json.JSONArray;
import org.json.JSONObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/fetchNotifications")
public class FetchNotificationsServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String personId = request.getParameter("personId");

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            Statement statement = connection.createStatement();
            String query = "SELECT * FROM somtoday6.Notification WHERE person_id = " + personId;
            ResultSet resultSet = statement.executeQuery(query);

            JSONArray jsonArray = new JSONArray();

            while (resultSet.next()) {
                JSONObject notification = new JSONObject();
                notification.put("notification_id", resultSet.getInt("notification_id"));
                notification.put("date", resultSet.getDate("date").toString());
                notification.put("sender", resultSet.getString("sender"));
                notification.put("info", resultSet.getString("info"));
                notification.put("person_id", resultSet.getInt("person_id"));
                jsonArray.put(notification);
            }

            connection.close();

            out.print(jsonArray.toString());
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
        }
    }
}
