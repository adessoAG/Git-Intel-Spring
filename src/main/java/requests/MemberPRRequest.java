package requests;

import enums.RequestType;
import enums.ResponseProcessor;
import objects.Query;

public class MemberPRRequest {

    private final int estimatedQueryCost = 1;
    private String query;
    private ResponseProcessor responseProcessor;
    private RequestType requestType;
    private String organizationName;

    public MemberPRRequest(String organizationName, String endCursor) {
        this.organizationName = organizationName;
        this.query = "query {\n" +
                "organization(login:\"" + organizationName + "\") {\n" +
                "members(first: 100, after: " + endCursor + ") {\n" +
                "pageInfo {\n" +
                "hasNextPage\n" +
                "endCursor\n" +
                "}\n" +
                "nodes {\n" +
                "id\n" +
                "pullRequests(last: 25, states: [MERGED, OPEN]) {\n" +
                "nodes {\n" +
                "updatedAt \n" +
                "repository {\n" +
                "id\n" +
                "}\n" +
                "}\n" +
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

        this.responseProcessor = ResponseProcessor.MEMBER_PR;
        this.requestType = RequestType.MEMBER_PR;
    }

    public Query generateQuery() {
        return new Query(this.organizationName, this.query, this.responseProcessor, this.requestType, this.estimatedQueryCost);
    }
}
