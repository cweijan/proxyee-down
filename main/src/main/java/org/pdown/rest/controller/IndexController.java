package org.pdown.rest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author cweijan
 * @version 2019/8/16 11:41
 */
@Controller
public class IndexController {

    @RequestMapping("/")
    public String index(){
        return "forward:/index.html";
    }

}
