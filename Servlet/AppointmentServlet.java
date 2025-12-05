package com.example.healthcare.servlet;

import com.example.healthcare.model.User;
import com.example.healthcare.model.Appointment;
import com.example.healthcare.store.InMemoryStore;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet({"/patient", "/doctor", "/appointments"})
public class AppointmentServlet extends HttpServlet {

    private final InMemoryStore store = InMemoryStore.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // get user from session
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        long uid = (Long) session.getAttribute("userId");
        User u = store.findById(uid);
        if (u == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String path = req.getServletPath(); // /patient or /doctor or /appointments
        if ("/patient".equals(path)) {
            List<User> doctors = store.findDoctors();
            List<Appointment> myAppts = store.findAppointmentsByPatient(uid);
            req.setAttribute("doctors", doctors);
            req.setAttribute("appointments", myAppts);
            req.setAttribute("user", u);
            req.getRequestDispatcher("/views/patient.jsp").forward(req, resp);
        } else if ("/doctor".equals(path)) {
            List<Appointment> assigned = store.findAppointmentsByDoctor(uid);
            req.setAttribute("appointments", assigned);
            req.setAttribute("user", u);
            req.getRequestDispatcher("/views/doctor.jsp").forward(req, resp);
        } else {
            resp.sendRedirect(req.getContextPath() + "/login");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // actions: book (from patient), changeStatus (from doctor)
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        long uid = (Long) session.getAttribute("userId");
        User u = store.findById(uid);
        String action = req.getParameter("action");

        if ("book".equals(action) && u.getRole() == User.Role.PATIENT) {
            long doctorId = Long.parseLong(req.getParameter("doctorId"));
            String dateTime = req.getParameter("dateTime");
            store.createAppointment(uid, doctorId, dateTime);
            resp.sendRedirect(req.getContextPath() + "/patient");
            return;
        }

        if ("status".equals(action) && u.getRole() == User.Role.DOCTOR) {
            long apptId = Long.parseLong(req.getParameter("apptId"));
            String status = req.getParameter("status");
            Appointment a = store.findAppointmentById(apptId);
            if (a != null && a.getDoctorId() == uid) {
                if ("APPROVED".equalsIgnoreCase(status)) a.setStatus(Appointment.Status.APPROVED);
                else if ("REJECTED".equalsIgnoreCase(status)) a.setStatus(Appointment.Status.REJECTED);
            }
            resp.sendRedirect(req.getContextPath() + "/doctor");
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/login");
    }
}
