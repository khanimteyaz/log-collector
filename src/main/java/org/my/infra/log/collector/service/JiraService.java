package org.my.infra.log.collector.service;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import java.net.URI;
import org.springframework.stereotype.Service;

@Service
public class JiraService {

    private String username="demo";
    private String password="demo";
    private String jiraUrl="http://localhost:8050";
    private String projectKey="TEST";
    private long ISSUE_TYPE_BUG=10004L;
    private JiraRestClient restClient;

    public JiraService() {
        this.restClient = getJiraRestClient();
    }

    public String createNewIssue(String title, String description) {
        IssueRestClient issueClient = restClient.getIssueClient();
        IssueInput newIssue = new IssueInputBuilder(projectKey,ISSUE_TYPE_BUG,title)
            .setDescription(description)
            .build();
        return issueClient.createIssue(newIssue).claim().getKey();
    }

    public void updateIssue(String jiraId, String commentBody) {
        Issue issue=getIssue(jiraId);
        restClient.getIssueClient().addComment(issue.getCommentsUri(), Comment.valueOf(commentBody));
    }


    private Issue getIssue(String issueKey) {
        return restClient.getIssueClient().getIssue(issueKey).claim();
    }


    private JiraRestClient getJiraRestClient() {
        return new AsynchronousJiraRestClientFactory()
            .createWithBasicHttpAuthentication(getJiraUri(), this.username, this.password);
    }

    private URI getJiraUri() {
        return URI.create(this.jiraUrl);
    }
}
