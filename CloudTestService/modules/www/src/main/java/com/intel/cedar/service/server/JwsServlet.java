package com.intel.cedar.service.server;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.intel.cedar.feature.util.FileUtils;
import com.intel.cedar.util.CedarConfiguration;
import com.intel.cedar.util.SubDirectory;

public class JwsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private String baseURL = CedarConfiguration.getServiceURL() + "jws";

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        String url = request.getRequestURI();
        int index = url.lastIndexOf("/");
        String jwsAction = url.substring(index + 1);
        try {
            if (jwsAction.endsWith(".jar")
                    && request.getParameterMap().isEmpty()) {
                InputStream is = new FileInputStream(SubDirectory.LIBS
                        .toString()
                        + jwsAction);
                FileUtils.copyStream(is, response.getOutputStream());
                is.close();
            } else if (jwsAction.length() > 0) {
                response.setContentType("application/x-java-jnlp-file");
                InputStream is = getClass().getClassLoader()
                        .getResourceAsStream(jwsAction + ".jnlp");
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                FileUtils.copyStream(is, os);
                String template = os.toString();
                template = template.replace("@BASEURL@", baseURL);
                for (Object e : request.getParameterMap().entrySet()) {
                    if (e instanceof Map.Entry<?, ?>) {
                        Map.Entry<?, ?> entry = (Map.Entry<?, ?>) e;
                        String key = (String) entry.getKey();
                        String value = ((String[]) entry.getValue())[0];
                        template = template.replace("@" + key + "@", value);
                    }
                }
                response.getWriter().print(template);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void init() throws ServletException {
    }
}
