package com.example.healthcare.servlet;

import com.example.healthcare.model.User;
import com.example.healthcare.store.InMemoryStore;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private final InMemoryStore store = InMemoryStore.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/views/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String fullName = req.getParameter("fullName");
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String roleStr = req.getParameter("role");

        if (store.findByUsername(username) != null) {
            req.setAttribute("error", "Username already exists");
            req.getRequestDispatcher("/views/register.jsp").forward(req, resp);
            return;
        }

        User.Role role = "DOCTOR".equalsIgnoreCase(roleStr) ? User.Role.DOCTOR : User.Role.PATIENT;
        store.registerUser(username, password, fullName, role);
        resp.sendRedirect(req.getContextPath() + "/login");
    }
}
