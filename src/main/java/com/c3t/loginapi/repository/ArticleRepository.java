package com.c3t.loginapi.repository;

import com.c3t.loginapi.entity.Articles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Articles, Long> {
}
