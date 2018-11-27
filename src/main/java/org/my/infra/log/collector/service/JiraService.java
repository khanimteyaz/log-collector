package org.my.infra.log.collector.service;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.BasicVotes;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Service;

@Service
public class JiraService {

    private String username="demo";
    private String password="demo";
    private String jiraUrl="http://localhost:8050";
    private String projectKey="TEST";
    private long ISSUE_TYPE_BUG=1L;
    private JiraRestClient restClient;

    public JiraService() {
        this.restClient = getJiraRestClient();
    }

    public String createNewIssue(String title, String description) {
        IssueRestClient issueClient = restClient.getIssueClient();
        BasicProject cpqProject = null;
        IssueType issueType = null;

        try {
            final Iterable<BasicProject> projects = restClient.getProjectClient().getAllProjects().claim();

            System.out.println("======================getting all projoects======================");

            for (BasicProject project : projects) {
                if(project.getKey().equalsIgnoreCase("cpq")) {
                    cpqProject = project;
                }
            }

            Promise<Project> project = restClient.getProjectClient().getProject(projectKey);

            for(IssueType type : (project.get()).getIssueTypes()) {
                if(type.getName().equalsIgnoreCase("Bug")){
                    issueType = type;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

//        IssueType issueType = new IssueType(getJiraIssueUri(),
//            3L, "Bug", false, "Testing the Issue now", null);
        System.out.println("<<<<=====>>>>>"+issueType.getId()+":"+issueType.getName()+":"+issueType.getSelf());
        IssueInput newIssue = new IssueInputBuilder(projectKey,10004L,title)
            .setDescription(description)
            //.setIssueType(issueType)
            .build();
        return issueClient.createIssue(newIssue).claim().getKey();
    }

    private Issue getIssue(String issueKey) {
        return restClient.getIssueClient().getIssue(issueKey).claim();
    }

    private void voteForAnIssue(Issue issue) {
        restClient.getIssueClient().vote(issue.getVotesUri()).claim();
    }

    private int getTotalVotesCount(String issueKey) {
        BasicVotes votes = getIssue(issueKey).getVotes();
        return votes == null ? 0 : votes.getVotes();
    }

    private void addComment(Issue issue, String commentBody) {
        restClient.getIssueClient().addComment(issue.getCommentsUri(), Comment.valueOf(commentBody));
    }

    private List<Comment> getAllComments(String issueKey) {
        return StreamSupport.stream(getIssue(issueKey).getComments().spliterator(), false)
            .collect(Collectors.toList());
    }

    private void updateIssueDescription(String issueKey, String newDescription) {
        IssueInput input = new IssueInputBuilder().setDescription(newDescription).build();
        restClient.getIssueClient().updateIssue(issueKey, input).claim();
    }

    private void deleteIssue(String issueKey, boolean deleteSubtasks) {
        restClient.getIssueClient().deleteIssue(issueKey, deleteSubtasks).claim();
    }

    private JiraRestClient getJiraRestClient() {
        return new AsynchronousJiraRestClientFactory()
            .createWithBasicHttpAuthentication(getJiraUri(), this.username, this.password);
    }

    private URI getJiraUri() {
        return URI.create(this.jiraUrl);
    }

    private URI getJiraIssueUri() {
        return URI.create("http://localhost:8050/rest/api/3/issue");
    }
}
