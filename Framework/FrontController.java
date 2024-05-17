package controller;

import util.Util;
import java.util.List;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import annotation.*;

public class FrontController extends HttpServlet {
    private Boolean isVoatety = false;

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processedRequest(req, res);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processedRequest(req, res);
    }

    protected void processedRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        PrintWriter out = res.getWriter();
        out.println("Tongasoa ato am FrontController");
        out.println("Votre url : " + req.getRequestURL().toString());
        List<String> controllerNames = new ArrayList<>();
        if (!isVoatety) {
            try {
                String package_name = this.getInitParameter("package_name");
                controllerNames = Util.getAllClassesSelonAnnotation(package_name,ControllerAnnotation.class);
                isVoatety = true;
            } catch (Exception e) {
                e.printStackTrace();
                out.println("Erreur lors du scan du package : " + e.getMessage());
            }
        }

        out.println("Liste des contrôleurs : ");
        for (String controllerName : controllerNames) {
            out.println(controllerName);
        }
    }
}
