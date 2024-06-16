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
        String classCapacityStr = request.getParameter("classCapacity");
        int classCapacity = Integer.parseInt(classCapacityStr);
        String[] students = request.getParameterValues("students");
        String[] lessons = request.getParameterValues("lessons");
        Part filePart = request.getPart("classPicture");
        int teacherId = Integer.parseInt(request.getParameter("teacherId"));

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            String sql = "INSERT INTO topicus6.Class (class_name, class_capacity) VALUES (?, ?) RETURNING class_id";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, className);
            preparedStatement.setInt(2, classCapacity);
            ResultSet rs = preparedStatement.executeQuery();
            int classId = 0;
            if (rs.next()) {
                classId = rs.getInt(1);
            }

            // Handle file upload
            if (filePart != null && filePart.getSize() > 0) {
                InputStream fileContent = filePart.getInputStream();

            }

            // Handle associations with students and lessons
            if (students != null) {
                for (String studentId : students) {
                    String studentSql = "INSERT INTO topicus6.Student (person_id, class_id) VALUES (?, ?)";
                    PreparedStatement studentStmt = connection.prepareStatement(studentSql);
                    studentStmt.setInt(1, Integer.parseInt(studentId));
                    studentStmt.setInt(2, classId);
                    studentStmt.executeUpdate();
                }
                System.out.println("students are null");
            }

            if (lessons != null) {
                for (String lessonId : lessons) {
                    String lessonSql = "INSERT INTO topicus6.Lesson (course_id, class_id, school_year) VALUES (?, ?, ?)";
                    PreparedStatement lessonStmt = connection.prepareStatement(lessonSql);
                    lessonStmt.setInt(1, Integer.parseInt(lessonId));
                    lessonStmt.setInt(2, classId);
                    lessonStmt.setInt(3, 2024);
                    lessonStmt.executeUpdate();
                }
                System.out.println("lessons are null");
            }

            // Associate the class with the teacher
            String teacherSql = "INSERT INTO topicus6.Course (course_name, teacher_id) VALUES (?, ?)";
            PreparedStatement teacherStmt = connection.prepareStatement(teacherSql);
            teacherStmt.setString(1, className);
            teacherStmt.setInt(2, teacherId);
            teacherStmt.executeUpdate();

            connection.close();
            response.sendRedirect("classes.html");
            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
        }
    }
}
