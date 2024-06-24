package utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import org.json.JSONObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/deleteLesson")
public class DeleteLessonServlet extends HttpServlet {
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

        int lessonId = jsonRequest.getInt("lesson_id");

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // delete homework associated with the lesson
            String deleteHomeworkSql = "DELETE FROM somtoday6.Homework WHERE lesson_id = ?";
            try (PreparedStatement deleteHomeworkStmt = connection.prepareStatement(deleteHomeworkSql)) {
                deleteHomeworkStmt.setInt(1, lessonId);
                deleteHomeworkStmt.executeUpdate();
            }

            // delete the lesson
            String deleteLessonSql = "DELETE FROM somtoday6.Lesson WHERE lesson_id = ?";
            try (PreparedStatement deleteLessonStmt = connection.prepareStatement(deleteLessonSql)) {
                deleteLessonStmt.setInt(1, lessonId);
                deleteLessonStmt.executeUpdate();
            }

            connection.close();

            PrintWriter out = response.getWriter();
            out.write("{\"success\": true}");
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
        }
    }
}
