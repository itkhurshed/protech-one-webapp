package com.protechone.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Flips the dark/light theme or LTR/RTL direction by setting a plain cookie
 * and redirecting back to the referring page. A link (<a href="/prefs/theme?...">)
 * is all that's needed on the client side — no JavaScript required.
 */
@RestController
public class PreferencesController {

    @GetMapping("/prefs/theme")
    public Object toggleTheme(@RequestParam String value, @RequestParam(defaultValue = "/dashboard") String redirect,
                               HttpServletResponse response) {
        setCookie(response, "protech_theme", value);
        return redirect(redirect);
    }

    @GetMapping("/prefs/dir")
    public Object toggleDir(@RequestParam String value, @RequestParam(defaultValue = "/dashboard") String redirect,
                             HttpServletResponse response) {
        setCookie(response, "protech_dir", value);
        return redirect(redirect);
    }

    private void setCookie(HttpServletResponse response, String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 365);
        response.addCookie(cookie);
    }

    private Object redirect(String url) {
        return new org.springframework.web.servlet.view.RedirectView(url);
    }
}
