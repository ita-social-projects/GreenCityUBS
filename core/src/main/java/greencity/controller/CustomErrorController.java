package greencity.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {
    /**
     * Controller to handle "/error" requests.
     *
     *
     * @return {@link String} HTML page with error message.
     * @author Stepan Tehlivets.
     */

    @RequestMapping(value = "/error", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE,
        RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.PATCH, RequestMethod.PUT, RequestMethod.TRACE})
    @ResponseBody
    public String handleError(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        Exception exception = (Exception) request.getAttribute("javax.servlet.error.exception");
        return String.format("<html><body><h2>Error Page</h2><div>Status code: <b>%s</b></div>"
            + "<div>Exception Message: <b>%s</b></div><body></html>",
            statusCode, exception == null ? "N/A" : exception.getMessage());
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}
