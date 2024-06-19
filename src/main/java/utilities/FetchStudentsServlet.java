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
import somtoday.model.Student;

@WebServlet("/fetchStudents")
public class FetchStudentsServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String unassigned = request.getParameter("unassigned");

        List<Student> studentList = new ArrayList<>();

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            String sql = "SELECT s.student_id, p.person_name " +
                    "FROM somtoday6.Student s " +
                    "JOIN somtoday6.Person p ON s.person_id = p.person_id";

            if ("true".equals(unassigned)) {
                sql += " WHERE s.class_id IS NULL";
            }

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                Student student = new Student();
                student.setStudentID(resultSet.getInt("student_id"));
                student.setPersonName(resultSet.getString("person_name"));
                studentList.add(student);
            }

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
            return;
        }

        PrintWriter out = response.getWriter();
        out.write(toJson(studentList));
        out.close();
    }

    private String toJson(List<Student> studentList) {
        StringBuilder json = new StringBuilder();
        json.append("[");

        for (int i = 0; i < studentList.size(); i++) {
            Student student = studentList.get(i);
            json.append("{");
            json.append("\"student_id\":").append(student.getStudentID()).append(",");
            json.append("\"person_name\":\"").append(student.getPersonName()).append("\"");
            json.append("}");

            if (i < studentList.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");
        return json.toString();
    }
}
