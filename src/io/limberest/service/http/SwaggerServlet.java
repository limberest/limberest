package io.limberest.service.http;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.limberest.api.ServiceApi;
import io.limberest.api.ServiceApi.Format;

/**
 * Scans a service path for Swagger annotations and generates the service spec in JSON or YAML.
 */
@WebServlet(urlPatterns={"/api-docs/*"})
public class SwaggerServlet extends HttpServlet {

    private static final String PRETTY_INDENT_PARAM = "prettyIndent";
    private static final String SWAGGER_PATH = "/swagger";

    private static final Logger logger = LoggerFactory.getLogger(SwaggerServlet.class);

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        ServletContext servletContext = config.getServletContext();
        logger.info("swagger context path: {}: " + servletContext.getContextPath());
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
    throws ServletException, IOException {

        try {
            Format format = Format.json;
            String path = request.getPathInfo();
            if (path != null) {
                int lastDot = path.lastIndexOf('.');
                if (lastDot > 0 && lastDot < path.length()) {
                    String f = path.substring(lastDot + 1);
                    path = path.substring(0, lastDot);
                    try {
                        format = Format.valueOf(f);
                    }
                    catch (IllegalArgumentException ex) {
                        logger.debug("Request for unsupported format: " + f);
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported format: " + f);
                        return;
                    }
                }
            }
            
            String servicePath = path == null || path.equals(SWAGGER_PATH) ? "/" : path;
            servicePath = servicePath.replace('.', '/');
            
            if (format == Format.yaml)
                response.setContentType("text/yaml");
            else
                response.setContentType("application/json");
            
            ServiceApi serviceApi = new ServiceApi();
            String indent = request.getParameter(PRETTY_INDENT_PARAM);
        
            if (indent != null) {
                int prettyIndent = Integer.parseInt(indent);
                response.getWriter().println(serviceApi.getSwaggerString(servicePath, format, prettyIndent));
            }
            else {
                response.getWriter().println(serviceApi.getSwaggerString(servicePath, format));
            }
        }
        catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }
}
