package com.codegym.controller;

import com.codegym.dao.UserDAO;
import com.codegym.model.Product;
import com.codegym.model.User;
import com.codegym.utils.ValidateUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "UserServlet", urlPatterns = "/users")
public class UserServlet extends HttpServlet {

    private  UserDAO userDAO;

    public void init() {
        userDAO = new UserDAO();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }
        try {
            switch (action) {
                case "create":
                    insertUser(request, response);
                    break;
                case "edit":
                    updateUser(request, response);
                    break;
                case "delete":
                    deleteUser(request, response);
                    break;
            }
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }

        try {
            switch (action) {
                case "create":
                    showNewForm(request, response);
                    break;
                case "edit":
                    showEditForm(request, response);
                    break;
                case "delete":
                    deleteUser(request, response);
                    break;
                default:
                    listNumberPage(request, response);
                    break;
            }
        } catch (SQLException ex) {
            throw new ServletException(ex);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void listNumberPage(HttpServletRequest req, HttpServletResponse resp) throws SQLException, ClassNotFoundException, ServletException, IOException {
        System.out.println("numberPage");
        int page = 1;
        int recordsPerPage = 5;
        if (req.getParameter("page") != null) {
            page = Integer.parseInt(req.getParameter("page"));
        };
        String name = "";
        if (req.getParameter("searchuser") != null) {
            name = req.getParameter("searchuser");
        }
        List<User> listUser = userDAO.getNumberPage((page - 1) * recordsPerPage, recordsPerPage, name);
        int noOfRecords = userDAO.getNoOfRecords();
        int noOfPages = (int) Math.ceil(noOfRecords * 1.0 / recordsPerPage);
        req.setAttribute("listUser", listUser);
        req.setAttribute("noOfPages", noOfPages);
        req.setAttribute("currentPage", page);
        req.setAttribute("searchuser" , name);



        RequestDispatcher requestDispatcher = req.getRequestDispatcher("/WEB-INF/user/list.jsp");
        requestDispatcher.forward(req, resp);

    }
    private void showNewForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = new User();
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/user/add.jsp");
        request.setAttribute("user", user);
        dispatcher.forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {
        String id = request.getParameter("id");
        User existingUser = userDAO.selectUser(id);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/user/edit.jsp");
        request.setAttribute("user", existingUser);
        dispatcher.forward(request, response);

    }


    private void insertUser(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException, ServletException {

        User user;
        String userName = request.getParameter("username").replaceAll(" ", "").toLowerCase();
        String password = request.getParameter("password").trim();
        String fullName = request.getParameter("fullname").trim();
        String phone = request.getParameter("phone").trim();
        String email = request.getParameter("email").trim();
        String address = request.getParameter("address").trim();
        List<String> errors = new ArrayList<>();
        boolean isPassword = ValidateUtils.isPasswordVailid(password);
        boolean isPhone = ValidateUtils.isNumberPhoneVailid(phone);
        boolean isEmail = ValidateUtils.isEmailValid(email);

        user = new User(userName, password, fullName, phone, email, address);
        if (userName.isEmpty() ||
                password.isEmpty() ||
                fullName.isEmpty() ||
                phone.isEmpty() ||
                email.isEmpty() ||
                address.isEmpty() ){
            errors.add("Vui l??ng ??i???n ?????y ????? th??ng tin");
        }
        if (userName.isEmpty()) {
            errors.add("UserName kh??ng ???????c ????? tr???ng");
        }
        if (password.isEmpty()) {
            errors.add("Password kh??ng ???????c ????? tr???ng");
        }
        if (fullName.isEmpty()) {
            errors.add("Fullname kh??ng ???????c ????? tr???ng");
        }
        if (phone.isEmpty()) {
            errors.add("Phone Nh???p v??o kh??ng ????ng");
        }
        if (!isPhone) {
            errors.add("Phone kh??ng ????ng ?????nh d???ng");
        }
        if (userDAO.existByPhone(phone)) {
            errors.add("Phone ???? t???n t???i!");
        }
        if (email.isEmpty()) {
            errors.add("Email nh???p v??o kh??ng ????ng");
        }
        if (!isEmail) {
            errors.add("Email nh???p v??o kh??ng ????ng d???nh d???ng");
        }
        if (address.isEmpty()) {
            errors.add("Address kh??ng ???????c ????? tr???ng");
        }

        if (userDAO.existsByEmail(email)) {
            errors.add("Email ???? t???n t???i");
        }

        if (userDAO.existByUsername(userName)) {
            errors.add("Username n??y ???? t???n t???i!");
        }
        if (!isPassword) {
            errors.add("Password kh??ng ????ng ?????nh d???ng");
        }

        if (errors.size() == 0) {
            user = new User(userName, password, fullName, phone, email, address);
            boolean success = false;
            success = userDAO.insertUser(user);

            if (success) {
                request.setAttribute("success", true);
            } else {
                request.setAttribute("errors", true);
                errors.add("D??? li???u kh??ng h???p l???, Vui l??ng ki???m tra l???i!");
            }

        }
        if (errors.size() > 0) {
            request.setAttribute("errors", errors);
            request.setAttribute("user", user);
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/user/add.jsp");
        request.setAttribute("user", user);
        dispatcher.forward(request, response);
    }

    private void updateUser(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException, ServletException {
        List<String> errors = new ArrayList<>();
        User user = null;
        String id= request.getParameter("id");
        boolean isId = ValidateUtils.isIntValid(id);
        int checkId=0;
        if (isId) {
            checkId=Integer.parseInt(id);
        }else {
            errors.add("ID ph???i l?? s??? nguy??n d????ng !");
        }
        if (!userDAO.existByUserId(checkId)) {
            errors.add("ID ph???i c?? th???t!");
        }

        String fullName = request.getParameter("fullname");
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");
        String address = request.getParameter("address");
        boolean isPhone = ValidateUtils.isNumberPhoneVailid(phone);
        boolean isEmail = ValidateUtils.isEmailValid(email);
        user = new User(id, fullName, phone,email, address);
        User userEmail = userDAO.selectUser(id);
        String checkEmail = userEmail.getEmail();
        User userPhone = userDAO.selectUser(id);
        String checkPhone = userPhone.getPhone();
        if (fullName.isEmpty() ||
                phone.isEmpty() ||
                email.isEmpty() ||
                address.isEmpty()) {
            errors.add("H??y nh???p ?????y ????? th??ng tin");
        }

        if (fullName.isEmpty()) {
            errors.add("Full name kh??ng ???????c ????? tr???ng");
        }
        if (phone.isEmpty()) {
            errors.add("Phone kh??ng ???????c ????? tr???ng");
        }
        if (email.isEmpty()) {
            errors.add("Email kh??ng ???????c b??? tr???ng");
        }
        if (!isEmail) {
            errors.add("Email kh??ng ????ng ?????nh d???ng");
        }

        if (userDAO.existsByEmail(email) && !email.equals(checkEmail)){
            errors.add("Email ???? t???n t???i");
        }
        if (!isPhone) {
            errors.add("Phone kh??ng ????ng ?????nh d???ng");
        }
        if (userDAO.existByPhone(phone) && !phone.equals(checkPhone)){
            errors.add("Phone ???? t???n t???i");
        }
        if (address.isEmpty()) {
            errors.add("Address kh??ng ???????c ????? tr???ng");
        }


        if (errors.size() == 0) {
            user = new User(id, fullName, phone,email, address);
            boolean success = false;
            success = userDAO.updateUser(user);

            if (success) {
                request.setAttribute("success", true);
            } else {
                request.setAttribute("errors", true);
                errors.add("Invalid data, Please check again!");
            }
        }
        else {
            request.setAttribute("errors", errors);
            request.setAttribute("user", user);
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/user/edit.jsp");

        dispatcher.forward(request, response);
    }

    private void deleteUser(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException, ServletException {
        int id = Integer.parseInt(request.getParameter("id"));
        userDAO.deleteUser(id);
        response.sendRedirect("/users");
    }

    private void search(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<User> listUser = null;
        String name = "";
        if (req.getParameter("searchuser") != null) {
            name = req.getParameter("searchuser");

            listUser = userDAO.searchUser(name);
        } else {
            listUser = userDAO.selectAllUsers();
        }
        req.setAttribute("listUser", listUser);
        RequestDispatcher dispatcher = req.getRequestDispatcher("/WEB-INF/user/list.jsp");
        dispatcher.forward(req, resp);
    }
}
