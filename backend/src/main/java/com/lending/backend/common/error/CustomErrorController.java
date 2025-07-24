package com.lending.backend.common.error;

import com.lending.backend.common.exception.ProblemDetailBuilder;
import com.lending.backend.common.exception.ApiProblemDetail;
import com.lending.backend.common.exception.ErrorConstants;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("${server.error.path:/error}")
public class CustomErrorController implements ErrorController {

    private final ErrorAttributes errorAttributes;

    public CustomErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping(produces = "application/json")
    @ResponseBody
    public ApiProblemDetail handleError(HttpServletRequest request) {
        var webRequest = new ServletWebRequest(request);
        Map<String, Object> attributes = errorAttributes.getErrorAttributes(webRequest,
                ErrorAttributeOptions.defaults());

        int statusCode = (int) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String message = (String) attributes.getOrDefault("error", "Unexpected Error");

        log.error("Handled by /error: status={}, message={}", statusCode, message);

        return ProblemDetailBuilder.createProblemDetail(
                HttpStatus.valueOf(statusCode),
                ErrorConstants.RESOURCE_NOT_FOUND,
                message,
                "req_" + UUID.randomUUID().toString().replace("-", ""));
    }
}
