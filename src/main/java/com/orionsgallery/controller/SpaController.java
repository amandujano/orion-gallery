package com.orionsgallery.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Catch-all controller that forwards any non-API, non-static-asset request
 * to index.html so that Angular's client-side router handles navigation.
 *
 * The regex {path:[^\\.]*} matches paths with no dot (i.e. not file extensions),
 * which prevents it from intercepting .js / .css / image asset requests.
 */
@Controller
public class SpaController {

//    @RequestMapping(value = { "/", "/{path:[^\\.]*}", "/{path:^(?!api).*$}/**/{path2:[^\\.]*}" })
    public String forward() {
        return "forward:/index.html";
    }
}
