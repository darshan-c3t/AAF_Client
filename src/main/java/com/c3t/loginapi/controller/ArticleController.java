package com.c3t.loginapi.controller;

import com.c3t.loginapi.dto.ArticleDto;
import com.c3t.loginapi.dto.LoginDto;
import com.c3t.loginapi.dto.RestResponse;
import com.c3t.loginapi.dto.TokenResponse;
import com.c3t.loginapi.entity.Articles;
import com.c3t.loginapi.service.ArticleService;
import com.c3t.loginapi.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value ="/article")
public class ArticleController {

    @Autowired
    ArticleService articleService;

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @RequestMapping( method = RequestMethod.GET)
    public RestResponse getApi () throws Exception {
        return RestResponse.builder().success(RestResponse.RESPONSE_SUCCESS).data(articleService.getAllArticles()).build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(method = RequestMethod.POST)
    public RestResponse writeApi (@RequestBody ArticleDto articleDto) throws Exception {
        return RestResponse.builder().success(RestResponse.RESPONSE_SUCCESS).data(articleService.saveArticles(articleDto)).build();
    }

}
