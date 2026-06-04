package com.interview;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.interview.mapper")
public class AiInterviewCoachBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiInterviewCoachBackendApplication.class, args);
    }

}
