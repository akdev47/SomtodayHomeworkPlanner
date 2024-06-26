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
import somtoday.model.Teacher;

@WebServlet("/fetchAllTeachers")
public class FetchAllTeachersServlet extends HttpServlet {
    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
    private static final String DB_USER = "dab_di23242b_168";
    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        List<Teacher> teacherList = new ArrayList<>();

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            String sql = "SELECT t.teacher_id, p.person_name " +
                    "FROM somtoday6.Teacher t " +
                    "JOIN somtoday6.Person p ON t.person_id = p.person_id";

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                Teacher teacher = new Teacher();
                teacher.setTeacherID(resultSet.getInt("teacher_id"));
                teacher.setPersonName(resultSet.getString("person_name"));
                teacherList.add(teacher);
            }

            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
            return;
        }

        PrintWriter out = response.getWriter();
        out.write(toJson(teacherList));
        out.close();
    }

    private String toJson(List<Teacher> teacherList) {
        StringBuilder json = new StringBuilder();
        json.append("[");

        for (int i = 0; i < teacherList.size(); i++) {
            Teacher teacher = teacherList.get(i);
            json.append("{");
            json.append("\"teacher_id\":").append(teacher.getTeacherID()).append(",");
            json.append("\"person_name\":\"").append(teacher.getPersonName()).append("\"");
            json.append("}");

            if (i < teacherList.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");
        return json.toString();
    }
}

