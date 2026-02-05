package com.plc.recipe.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {

    @RequestMapping(value = {
        "/",
        "/{path:[^\\.]*}"
    })
    public String forwardSpa() {
        return "forward:/index.html";
    }
    
}
