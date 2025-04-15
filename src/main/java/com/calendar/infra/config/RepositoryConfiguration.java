package com.calendar.infra.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    value = "com.calendar.infra.persistence.repository",
    considerNestedRepositories = true)
public class RepositoryConfiguration {}
