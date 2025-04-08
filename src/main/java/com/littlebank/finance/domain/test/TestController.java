package com.littlebank.finance.domain.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping()
    public String test() {
        return "test";
    }

    @GetMapping()
    public String test2() {
        return "test2";
    }
}
