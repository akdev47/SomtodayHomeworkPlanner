package utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/editStudentsInClass")
public class EditStudentsInClassServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        JSONObject jsonRequest = new JSONObject(sb.toString());

        int classId = jsonRequest.getInt("classId");
        JSONArray studentIdsJsonArray = jsonRequest.getJSONArray("studentIds");
        List<Integer> studentIds = new ArrayList<>();
        for (int i = 0; i < studentIdsJsonArray.length(); i++) {
            studentIds.add(studentIdsJsonArray.getInt(i));
        }

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // remove existing students from the class by setting class_id to NULL
            String removeStudentsSql = "UPDATE somtoday6.Student SET class_id = NULL WHERE class_id = ?";
            PreparedStatement removeStmt = connection.prepareStatement(removeStudentsSql);
            removeStmt.setInt(1, classId);
            removeStmt.executeUpdate();

            // assign new students to the class
            String assignStudentsSql = "UPDATE somtoday6.Student SET class_id = ? WHERE student_id = ?";
            PreparedStatement assignStmt = connection.prepareStatement(assignStudentsSql);

            for (Integer studentId : studentIds) {
                assignStmt.setInt(1, classId);
                assignStmt.setInt(2, studentId);
                assignStmt.addBatch();
            }
            assignStmt.executeBatch();

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
            return;
        }

        PrintWriter out = response.getWriter();
        out.write("{\"success\": true}");
        out.close();
    }
}
