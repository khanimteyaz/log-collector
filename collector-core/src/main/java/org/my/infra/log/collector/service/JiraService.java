package org.my.infra.log.collector.service;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import java.net.URI;
import org.my.infra.log.collector.entity.ProjectMetadata;
import org.my.infra.log.collector.repository.ProjectMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JiraService {

    private final String username;
    private final String password;
    private final String jiraUrl;
    private final long jiraIssueTypeId;
    private final ProjectMetadataRepository projectMetadataRepository;

    private final JiraRestClient restClient;

    public JiraService(@Value("${app.jira.username}")final String username
        ,@Value("${app.jira.password}")final String password
        ,@Value("${app.jira.url}")final String jiraUrl
        ,@Value("${app.jira.issue.type.id}") final String jiraTypeIdStr
        ,@Autowired final ProjectMetadataRepository projectMetadataRepository) {
        this.username=username;
        this.password=password;
        this.jiraUrl=jiraUrl;
        this.jiraIssueTypeId=Long.parseLong(jiraTypeIdStr);
        this.projectMetadataRepository=projectMetadataRepository;
        this.restClient = getJiraRestClient();
    }

    public String createNewIssue(String source,String title,String description) {
        IssueRestClient issueClient = restClient.getIssueClient();
        String projectKey=projectId(source);
        IssueInput newIssue = new IssueInputBuilder(projectKey, jiraIssueTypeId, title)
            .setDescription(description)
            .build();
        return issueClient.createIssue(newIssue).claim().getKey();
    }

    public void updateIssue(String jiraId, String commentBody) {
        Issue issue = getIssue(jiraId);
        restClient.getIssueClient().addComment(issue.getCommentsUri(), Comment.valueOf(commentBody));
    }


    private Issue getIssue(String issueKey) {
        return restClient.getIssueClient().getIssue(issueKey).claim();
    }

    private String projectId(String source) {
         ProjectMetadata metadata=projectMetadataRepository.findBySource(source);
         return metadata==null?null:metadata.getProjectKey();
    }
    private JiraRestClient getJiraRestClient() {
        return new AsynchronousJiraRestClientFactory()
            .createWithBasicHttpAuthentication(getJiraUri(), this.username, this.password);
    }

    private URI getJiraUri() {
        return URI.create(this.jiraUrl);
    }
}
