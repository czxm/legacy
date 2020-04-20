package com.intel.cedar.service.server;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intel.cedar.feature.util.FeatureUtil;

public class ResourceExplorerServlet extends HttpServlet {
    private final static Logger LOG = LoggerFactory
            .getLogger(ResourceExplorerServlet.class);

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String uri = request.getRequestURI();

        String[] spices = uri.split("/");
        if (spices.length < 3) {
            LOG.error("the requested uri {} is not correct", uri);
            // write empty response to client;
        }
        if (!spices[1].equalsIgnoreCase("features")) {
            LOG.error("the requested uri {} is not correct", uri);
            // write empty response to client;
        }
        String fid = spices[2];
        int fin = uri.indexOf(fid);
        int resourcein = fin + fid.length() + 1;
        String resourceLoc = uri.substring(resourcein);
        byte[] res = FeatureUtil.getResource(fid, resourceLoc);
        if (res.length <= 0) {
            LOG.debug("the requested resource {} is empty", uri);
            res = new byte[0];
        }

        response.setContentType("image/png");
        response.setContentLength(res.length);
        response.setStatus(HttpServletResponse.SC_OK);
        ServletOutputStream sos = response.getOutputStream();
        sos.write(res);
        sos.flush();
        sos.close();
    }

}
