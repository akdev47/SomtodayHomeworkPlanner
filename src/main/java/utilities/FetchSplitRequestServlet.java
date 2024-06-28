//package utilities;
//
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.util.ArrayList;
//import java.util.List;
//import somtoday.model.Class;
//import somtoday.model.Homework;
//import somtoday.model.Person;
//import somtoday.model.SplitRequest;
//
//@WebServlet("/fetchSplitRequest")
//public class FetchSplitRequestServlet extends HttpServlet {
//    private static final String DB_URL = "jdbc:postgresql://bronto.ewi.utwente.nl/dab_di23242b_168?currentSchema=somtoday6";
//    private static final String DB_USER = "dab_di23242b_168";
//    private static final String DB_PASSWORD = "f39egyiyL6ph4m/k";
//
//    @Override
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        response.setContentType("application/json");
//        response.setCharacterEncoding("UTF-8");
//
//        String role = request.getParameter("role");
//        String homeworkId = request.getParameter("homeworkId");
//
//        List<SplitRequest> splitRequestList = new ArrayList<>();
//        List<Person> personList = new ArrayList<>();
//
//        try {
//            java.lang.Class.forName("org.postgresql.Driver");
//            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
//
//            String sql = "SELECT p.person_name, sr.split_request_id, sr.request_description " +
//                    "FROM split_request sr, student s, person p " +
//                    "WHERE teacher_homework_id = ? " +
//                    "AND sr.student_id = s.student_id AND s.person_id = p.person_id ";
//            PreparedStatement preparedStatement = connection.prepareStatement(sql);
//            preparedStatement.setInt(1, Integer.parseInt(homeworkId));
//
//
//            if (role.equals("student")) {
//                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid role");
//                return;
//            }
//
//            ResultSet resultSet = preparedStatement.executeQuery();
//
//            while (resultSet.next()) {
//                Person p = new Person();
//                p.setPersonName(resultSet.getString("person_name"));
//                SplitRequest sr = new SplitRequest();
//                sr.setSplittedRequestId(resultSet.getInt("split_request_id"));
//                sr.setRequestDescription(resultSet.getString("request_description"));
//                splitRequestList.add(sr);
//                personList.add(p);
//            }
//            connection.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An error occurred.");
//            return;
//        }
//        PrintWriter out = response.getWriter();
//        out.write(toJson(splitRequestList, personList));
//        out.close();
//    }
//
//    private String toJson(List<SplitRequest> splitRequestList, List<Person> personList) {
//        StringBuilder json = new StringBuilder();
//        json.append("[");
//        if (personList.size() == splitRequestList.size()) {
//            for (int i = 0; i < splitRequestList.size(); i++) {
//                SplitRequest sr = splitRequestList.get(i);
//                Person p = personList.get(i);
//                json.append("{");
//                json.append("\"person_name\":\"").append(p.getPersonName()).append("\",");
//                json.append("\"split_request_id\":").append(sr.getSplittedRequestId()).append(",");
//                json.append("\"request_description\":\"").append(sr.getRequestDescription()).append("\"");
//                json.append("}");
//                if (i < splitRequestList.size() - 1) {
//                    json.append(",");
//                }
//            }
//
//            json.append("]");
//            return json.toString();
//        } else {
//            return "ERROR IN THE SERVER";
//        }
//    }
//}
