package com.protechone.controller;

import com.protechone.service.GlobalSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final GlobalSearchService globalSearchService;

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("query", q);
        model.addAttribute("results", (q == null || q.trim().length() < 2) ? List.of() : globalSearchService.search(q.trim()));
        return "search/results";
    }
}
