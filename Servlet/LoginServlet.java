package com.example.healthcare.servlet;

import com.example.healthcare.model.User;
import com.example.healthcare.store.InMemoryStore;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final InMemoryStore store = InMemoryStore.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        User u = store.findByUsername(username);
        if (u != null && u.getPassword().equals(password)) {
            HttpSession session = req.getSession(true);
            session.setAttribute("userId", u.getId());
            if (u.getRole() == User.Role.PATIENT) {
                resp.sendRedirect(req.getContextPath() + "/patient");
            } else {
                resp.sendRedirect(req.getContextPath() + "/doctor");
            }
        } else {
            req.setAttribute("error", "Invalid username or password");
            req.getRequestDispatcher("/views/login.jsp").forward(req, resp);
        }
    }
}
