package org.my.infra.log.collector.service;

import org.springframework.stereotype.Service;

@Service
public class JiraService {


    public JiraService() {

    }

    public String createNewIssue(String title, String description) {
        return title;
    }
}
