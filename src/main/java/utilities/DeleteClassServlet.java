package utilities;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/deleteClass")
public class DeleteClassServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String classIdStr = request.getParameter("class_id");
        if (classIdStr == null || classIdStr.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Class ID is required");
            return;
        }

        int classId;
        try {
            classId = Integer.parseInt(classIdStr);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Class ID format");
            return;
        }

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // Update students to set class_id to NULL
            String updateStudentsSql = "UPDATE somtoday6.Student SET class_id = NULL WHERE class_id = ?";
            try (PreparedStatement updateStudentsStmt = connection.prepareStatement(updateStudentsSql)) {
                updateStudentsStmt.setInt(1, classId);
                updateStudentsStmt.executeUpdate();
            }

            // delete lessons associated with the class
            String deleteLessonsSql = "DELETE FROM somtoday6.Lesson WHERE class_id = ?";
            try (PreparedStatement deleteLessonsStmt = connection.prepareStatement(deleteLessonsSql)) {
                deleteLessonsStmt.setInt(1, classId);
                deleteLessonsStmt.executeUpdate();
            }

            // delete the class
            String deleteClassSql = "DELETE FROM somtoday6.Class WHERE class_id = ?";
            try (PreparedStatement deleteClassStmt = connection.prepareStatement(deleteClassSql)) {
                deleteClassStmt.setInt(1, classId);
                deleteClassStmt.executeUpdate();
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
