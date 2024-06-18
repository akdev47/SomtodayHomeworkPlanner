package utilities;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/addClass")
@MultipartConfig
public class AddClassServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=topicus6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String className = request.getParameter("className");
        String[] students = request.getParameterValues("students");
        String[] lessons = request.getParameterValues("lessons");
        Part filePart = request.getPart("classPicture");
        int teacherId = Integer.parseInt(request.getParameter("teacherId"));

        Connection connection = null;
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            connection.setAutoCommit(false); // Set auto-commit to false

            // Insert into class
            String sqlClass = "INSERT INTO topicus6.Class (class_name, class_capacity) VALUES (?, ?) RETURNING class_id";
            PreparedStatement preparedStatementClass = connection.prepareStatement(sqlClass);
            preparedStatementClass.setString(1, className);
            preparedStatementClass.setInt(2, Integer.parseInt(request.getParameter("classCapacity")));
            ResultSet rsClass = preparedStatementClass.executeQuery();
            int classId = 0;
            if (rsClass.next()) {
                classId = rsClass.getInt(1);
            }

            // Insert into course
            String sqlCourse = "INSERT INTO topicus6.Course (course_name, teacher_id) VALUES (?, ?) RETURNING course_id";
            PreparedStatement preparedStatementCourse = connection.prepareStatement(sqlCourse);
            preparedStatementCourse.setNull(1, java.sql.Types.VARCHAR);
            preparedStatementCourse.setInt(2, teacherId);
            ResultSet rsCourse = preparedStatementCourse.executeQuery();
            int courseId = 0;
            if (rsCourse.next()) {
                courseId = rsCourse.getInt(1);
            }

            // Insert into lesson
            String sqlLesson = "INSERT INTO topicus6.Lesson (course_id, class_id, school_year) VALUES (?, ?, ?)";
            PreparedStatement preparedStatementLesson = connection.prepareStatement(sqlLesson);
            preparedStatementLesson.setInt(1, courseId);
            preparedStatementLesson.setInt(2, classId);
            preparedStatementLesson.setInt(3, 2024);
            preparedStatementLesson.executeUpdate();

            // Handle associations with students
            if (students != null) {
                for (String studentId : students) {
                    String studentSql = "UPDATE topicus6.Student SET class_id = ? WHERE student_id = ?";
                    PreparedStatement studentStmt = connection.prepareStatement(studentSql);
                    studentStmt.setInt(1, classId);
                    studentStmt.setInt(2, Integer.parseInt(studentId));
                    studentStmt.executeUpdate();
                }
            }


            if (filePart != null && filePart.getSize() > 0) {
                InputStream fileContent = filePart.getInputStream();

            }

            connection.commit();
            connection.close();
            response.sendRedirect("classes.html?timestamp=" + System.currentTimeMillis());
        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (Exception rollbackException) {
                    rollbackException.printStackTrace();
                }
            }
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception closeException) {
                    closeException.printStackTrace();
                }
            }
        }
    }
}
