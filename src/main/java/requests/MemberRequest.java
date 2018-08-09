package requests;

import config.Config;
import enums.RequestType;
import enums.ResponseProcessor;
import objects.Query;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MemberRequest {

    private final int estimatedQueryCost = 1;
    private String query;
    private ResponseProcessor responseProcessor;
    private String organizationName;
    private RequestType requestType;

    public MemberRequest(String organizationName, String memberID) {
        this.organizationName = organizationName;
        this.query = "{\n" +
                "node(id: \"" + memberID + "\") {\n" +
                "... on User {\n" +
                "name\n" +
                "id\n" +
                "login\n" +
                "url\n" +
                "avatarUrl\n" +
                "repositoriesContributedTo(first: 100, contributionTypes: COMMIT, includeUserRepositories: true) {\n" +
                "nodes {\n" +
                "id\n" +
                "defaultBranchRef {\n" +
                "target {\n" +
                "... on Commit {\n" +
                "history(first: 100, since: \"" + getDateToStartCrawlingInISO8601UTC() + "\"  ,author: {id: \"" + memberID + "\"}) {\n" +
                "nodes {\n" +
                "committedDate\n" +
                "url\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "issues(last: 25) {\n" +
                "nodes {\n" +
                "createdAt\n" +
                "url\n" +
                "}\n" +
                "}\n" +
                "pullRequests(last: 25) {\n" +
                "nodes {\n" +
                "createdAt\n" +
                "url\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "rateLimit {\n" +
                "cost\n" +
                "remaining\n" +
                "resetAt\n" +
                "}\n" +
                "}";

        this.responseProcessor = ResponseProcessor.MEMBER;
        this.requestType = RequestType.MEMBER;
    }

    private String formatMemberIDs(List<String> memberIDs) {
        String formattedString = "";
        for (String memberID : memberIDs) {
            formattedString += "\"" + memberID.toString() + "\",";
        }
        return formattedString;
    }

    private String getDateToStartCrawlingInISO8601UTC() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(tz);
        return df.format(new Date(System.currentTimeMillis() - Config.PAST_DAYS_TO_CRAWL_IN_MS));
    }

    public Query generateQuery() {
        return new Query(this.organizationName, this.query, this.responseProcessor, this.requestType, this.estimatedQueryCost);
    }
}
