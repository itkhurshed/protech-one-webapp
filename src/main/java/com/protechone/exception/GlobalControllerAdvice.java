package com.protechone.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * Renders a friendly HTML error page for exceptions that escape controller
 * try/catch blocks (most expected errors are instead handled per-controller
 * with RedirectAttributes flash messages so the user lands back on the form
 * with a clear message — this advice is the safety net for anything else).
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleNotFound(ResourceNotFoundException ex) {
        ModelAndView mv = new ModelAndView("error/404");
        mv.addObject("message", ex.getMessage());
        return mv;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAccessDenied(AccessDeniedException ex) {
        ModelAndView mv = new ModelAndView("error/403");
        mv.addObject("message", "You do not have permission to perform this action.");
        return mv;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneric(Exception ex, HttpServletRequest request) {
        ModelAndView mv = new ModelAndView("error/500");
        mv.addObject("message", ex.getMessage());
        return mv;
    }
}
