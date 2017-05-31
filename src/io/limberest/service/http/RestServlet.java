package io.limberest.service.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.limberest.config.LimberestConfig;
import io.limberest.config.LimberestConfig.Settings;
import io.limberest.json.StatusResponse;
import io.limberest.service.Query;
import io.limberest.service.ResourcePath;
import io.limberest.service.Service;
import io.limberest.service.ServiceException;
import io.limberest.service.http.Request.HttpMethod;
import io.limberest.service.registry.Initializer;
import io.limberest.service.registry.ServiceRegistry;
import io.limberest.service.registry.ServiceRegistry.RegistryKey;
import io.limberest.util.ExecutionTimer;

/**
 * Initialization registers jax-rs @Path annotations in the ServiceRegistry.
 * 
 * TODO: can urlPatterns be overridden by declaring in web.xml?
 */
@WebServlet(urlPatterns={"/api/*"}, loadOnStartup=0)
public class RestServlet extends HttpServlet {

    // TODO yaml option instead of servlet init param
    public static final String SCAN_PACKAGES = "io.limberest.scan.packages";
    private static final Logger logger = LoggerFactory.getLogger(RestServlet.class);

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        ServletContext servletContext = config.getServletContext();
        logger.info("limberest context path: {}: " + servletContext.getContextPath());

        String warDeployPath = servletContext.getRealPath("/");
        logger.debug("warDeployPath: {}", warDeployPath);

        String webappContextPath = servletContext.getContextPath();
        logger.debug("webappContextPath: {}", webappContextPath);

        String scanPackagesParam = config.getInitParameter(SCAN_PACKAGES);
        if (scanPackagesParam != null) {
            List<String> scanPackages = Arrays.asList(scanPackagesParam.split(","));
            new Initializer().scan(scanPackages);
        }
        else {
            try {
                // TODO log
                new Initializer().scan();
            }
            catch (IOException ex) {
                logger.error("Unable to scan all packages", ex);
            }
        }

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        ExecutionTimer timer = new ExecutionTimer(true);
        try {
            HttpMethod method;
            try {
                method = HttpMethod.valueOf(request.getMethod());
            }
            catch (IllegalArgumentException ex) {
                throw new ServiceException(Status.NOT_IMPLEMENTED, request.getMethod() + " not supported");
            }

            String path = request.getPathInfo();
            if (path == null) {
                if (method == HttpMethod.GET) {
                    // TODO redirect to Swagger docs
                    return;
                }
                else {
                    throw new ServiceException(Status.BAD_REQUEST, "Missing path: " + request.getServletPath());
                }
            }
            
            ResourcePath resourcePath = new ResourcePath(path);
            ServiceRegistry registry = ServiceRegistry.getInstance();
            Service<?> service = null;
            RegistryKey registryKey = null;
            String accept = request.getHeader("Accept");
            if (accept != null) {
                for (String type : accept.split(",")) {
                    registryKey = new RegistryKey(resourcePath, type);
                    service = registry.get(registryKey);
                    if (service != null)
                        break;
                }
            }
            if (service == null) {
                // try the same as received
                String contentType = request.getContentType();
                if (contentType != null) {
                    registryKey = new RegistryKey(resourcePath, contentType);
                    service = registry.get(registryKey);
                }
            }
            if (service == null) {
                String fallback = getFallbackContentType(method);
                if (fallback != null) {
                    registryKey = new RegistryKey(resourcePath, fallback);
                    service = registry.get(registryKey);
                }
            }
            if (service == null) {
                logger.warn("Service not found: {}", path);
                throw new ServiceException(Status.NOT_FOUND, "Service not found: " + path);
            }

            // query parameters
            Map<String,String> parameters = new HashMap<String,String>();
            Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String paramName = paramNames.nextElement();
                parameters.put(paramName, request.getParameter(paramName));
            }
            Query query = new Query(parameters);

            // HTTP headers
            Map<String,String> headers = new HashMap<String,String>();
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                headers.put(headerName, request.getHeader(headerName));
            }
                
            Request serviceRequest = new Request(method, resourcePath, query, headers);
            if (service.isAuthenticationRequired(serviceRequest) || request.getHeader("Authorization") != null) {
                // TODO authenticate() populates response status message with tomcat default
                AuthenticationResponseWrapper authWrapper = new AuthenticationResponseWrapper(response);
                if (!request.authenticate(authWrapper)) {
                    throw new ServiceException(new Status(authWrapper.code, authWrapper.message));
                }
            }
            
            service.initialize(serviceRequest, request.getUserPrincipal(), r -> request.isUserInRole(r));
            
            if (service.authorize(serviceRequest)) {
                BufferedReader reader = request.getReader();
                StringBuffer requestBuffer = new StringBuffer(request.getContentLength() < 0 ? 0 : request.getContentLength());
                String line;
                while ((line = reader.readLine()) != null)
                    requestBuffer.append(line).append('\n');
                String requestString = requestBuffer.toString().trim();
                if (requestString.isEmpty())
                    requestString = null;
                serviceRequest.setText(requestString);
                timer.log("RestServlet: build ServiceRequest:");

                logger.info("Request: {}'", serviceRequest);
                logger.debug("Request Content:\n{}", serviceRequest, requestString);
                serviceRequest.setText(requestString);

                Response<?> serviceResponse = service.service(serviceRequest);
                logger.debug("Response for '{}':\n{}", serviceRequest, serviceResponse.getText());
                timer.log("RestServlet: invoke service():");
                response.setContentType(registryKey.getContentType());
                Map<String,String> responseHeaders = serviceResponse.getHeaders();
                if (responseHeaders != null) {
                    for (String responseHeaderName : responseHeaders.keySet())
                        response.setHeader(responseHeaderName, responseHeaders.get(responseHeaderName));
                }

                response.getOutputStream().print(serviceResponse.getText());
            }
            else {
                throw new ServiceException(Status.FORBIDDEN, "Not authorized");
            }
        }
        catch (ServiceException ex) {
            // TODO: yaml option: logging level based on response code
            logger.error("Service exception: " + ex.getCode(), ex);
            // TODO: customize error response
            response.setStatus(ex.getCode());
            response.getWriter().println(new StatusResponse(ex.getCode(), ex.getMessage()).toString());
        }
        catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            // TODO: customize error response
            Status status = Status.INTERNAL_ERROR;
            response.setStatus(status.getCode());
            response.getWriter().println(new StatusResponse(status, "Server error processing request").toString());
        }
        finally {
            if (timer.isEnabled())
                timer.log("RestServlet: http " + request.getMethod() + " completed in:");
        }
    }
    
    protected String getFallbackContentType(HttpMethod method) {
        Settings settings = LimberestConfig.getSettings();
        Map<?,?> reqMap = settings.getMap("request");
        if (reqMap != null) {
            Map<?,?> fallbackMap = settings.getMap("fallbackContentType", reqMap);
            if (fallbackMap != null) {
                return settings.get(method.toString().toLowerCase(), fallbackMap);
            }
        }
        return "application/json";
    }
    
    /**
     * Prevent authenticate() from committing the response with HTML instead of JSON.
     */
    private class AuthenticationResponseWrapper extends HttpServletResponseWrapper {

        int code = Status.UNAUTHORIZED.getCode();
        String message = "Authentication failure";
        
        public AuthenticationResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            this.code = sc;
            this.message = msg;
        }

        @Override
        public void sendError(int sc) throws IOException {
            this.code = sc;
        }
    }
}
