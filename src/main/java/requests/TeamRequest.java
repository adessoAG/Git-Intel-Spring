package requests;

import enums.RequestType;
import enums.ResponseProcessor;
import objects.Query;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TeamRequest extends Request {

    private String query;
    private ResponseProcessor responseProcessor;
    private RequestType requestType;
    private String organizationName;

    public TeamRequest(String organizationName, String endCursor) {
        this.organizationName = organizationName;
        this.query = "query {\n" +
                "organization(login: \"" + organizationName + "\") {\n" +
                "teams(first: 50, after: " + endCursor + ") {\n" +
                "pageInfo {\n" +
                "hasNextPage\n" +
                "endCursor\n" +
                "}\n" +
                "totalCount\n" +
                "nodes {\n" +
                "name\n" +
                "id\n" +
                "description\n" +
                "avatarUrl\n" +
                "url\n" +
                "repositories(first: 10) {\n" +
                "totalCount\n" +
                "nodes {\n" +
                "name\n" +
                "defaultBranchRef {\n" +
                "target {\n" +
                "... on Commit {\n" +
                "history(first: 25, since: \"" + getDateWeekAgoInISO8601UTC() + "\") {\n" +
                "totalCount\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "members {\n" +
                "totalCount\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "}\n" +
                "}";

        this.responseProcessor = ResponseProcessor.TEAM;
        this.requestType = RequestType.TEAM;
    }

    public Query generateQuery() {
        return new Query(this.organizationName, this.query, this.responseProcessor, this.requestType);
    }

    private String getDateWeekAgoInISO8601UTC() {
        long DAY_IN_MS = 1000 * 60 * 60 * 24;
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(tz);
        return df.format(new Date(System.currentTimeMillis() - (7 * DAY_IN_MS)));
    }
}
