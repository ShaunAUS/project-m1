package com.example.testSecurity.auth.controller;

import com.example.testSecurity.auth.dto.AuthToken;
import com.example.testSecurity.auth.dto.LoginForm;
import com.example.testSecurity.auth.service.AuthService;
import com.example.testSecurity.config.AppProperties;
import com.example.testSecurity.dto.MemberDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@Api("로그인")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/v1")
public class AuthController {

    final private AuthService authService;


    @ApiOperation(value = "testController", notes = "test")
    @GetMapping("/hello")
    public String hello(){
        return "hello";
    }

    @ApiOperation(value = "testController", notes = "test")
    @GetMapping("/hello/redis/{test-key}")
    @Cacheable(key = "#test-key", value = "test-value")
    public String helloRedis(){
        return "testData";
    }

    @ApiOperation(value = "createMember", notes = "멤버추가")
    @PostMapping("/create")
    public MemberDto.Info createMember(@RequestBody MemberDto.Create createDto){
        return authService.createMember(createDto);
    }


    @ApiOperation(value = "Login", notes = "JWT 토큰반환")
    @PostMapping("/sign-in")
        public AuthToken login(@RequestBody LoginForm loginForm){
        return authService.login(loginForm);
    }

    @ApiOperation(value = "Logout", notes = "로그아웃")
    @PostMapping("/sign-out")
    public void logout(
        @ApiIgnore @RequestHeader(AppProperties.AUTH_TOKEN_NAME) String token   //Header = key값 ,  value 는 token
    ){
        authService.logout(token);
    }


}
