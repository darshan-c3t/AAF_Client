package com.c3t.loginapi.service;

import com.c3t.loginapi.dto.ArticleDto;
import com.c3t.loginapi.entity.Articles;
import com.c3t.loginapi.exceptions.BadRequestException;
import com.c3t.loginapi.repository.ArticleRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticleService {

    @Autowired
    ArticleRepository articleRepository;

    public List<Articles> getAllArticles() {
        return articleRepository.findAll();
    }

    public Articles saveArticles(ArticleDto articleDto)  throws  Exception{
        if(StringUtils.isBlank(articleDto.getArticleName()) || StringUtils.isBlank(articleDto.getAuther())) {
            throw new BadRequestException("Article Name or Author is required field");
        }
        Articles articles = Articles.builder().articleName(articleDto.getArticleName()).auther(articleDto.getAuther()).build();
        articleRepository.saveAndFlush(articles);
        return articles;
    }
}
