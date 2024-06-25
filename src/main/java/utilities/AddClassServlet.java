package utilities;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

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
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String className = request.getParameter("className");
        String[] students = request.getParameterValues("students");
        Part filePart = request.getPart("classPicture");

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            connection.setAutoCommit(false);

            String sql = "INSERT INTO somtoday6.Class (class_name, class_capacity, profile_picture) VALUES (?, ?, ?) RETURNING class_id";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, className);
            preparedStatement.setInt(2, Integer.parseInt(request.getParameter("classCapacity")));
            if (filePart != null && filePart.getSize() > 0) {
                InputStream fileContent = filePart.getInputStream();
                preparedStatement.setBinaryStream(3, fileContent, (int) filePart.getSize());
            } else {
                preparedStatement.setNull(3, java.sql.Types.BINARY);
            }
            ResultSet rs = preparedStatement.executeQuery();
            int classId = 0;
            if (rs.next()) {
                classId = rs.getInt(1);
            }


            if (students != null) {
                for (String studentIdStr : students) {
                    int studentId = Integer.parseInt(studentIdStr);

                    String studentSql = "UPDATE somtoday6.Student SET class_id = ? WHERE student_id = ?";
                    PreparedStatement studentStmt = connection.prepareStatement(studentSql);
                    studentStmt.setInt(1, classId);
                    studentStmt.setInt(2, studentId);
                    studentStmt.executeUpdate();

                    // get person_id for the student
                    int personId = fetchPersonId(connection, studentId);

                    // send notification for the student
                    insertNotification(connection, personId, "Admin added you to a class: " + className);
                }
            }

            connection.commit(); // Commit the transaction
            connection.close();
            response.sendRedirect("classes.html?timestamp=" + System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
        }
    }

    private int fetchPersonId(Connection connection, int studentId) throws Exception {
        String sql = "SELECT person_id FROM somtoday6.Student WHERE student_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("person_id");
            } else {
                throw new Exception("Person ID not found for student ID: " + studentId);
            }
        }
    }

    private void insertNotification(Connection connection, int personId, String info) throws Exception {
        String sql = "INSERT INTO somtoday6.notification (date, sender, info, person_id) " +
                "VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(LocalDate.now()));
            pstmt.setString(2, "Admin");
            pstmt.setString(3, info);
            pstmt.setInt(4, personId);
            pstmt.executeUpdate();
        }
    }
}
