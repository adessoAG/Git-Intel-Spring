package enums;

/**
 * Enum used for the declaration of the suitable response processor for the query. Similar to the RequestType enum but slightly different in cases.
 */
public enum ResponseProcessor {
    ORGANIZATION_DETAIL,
    MEMBER_ID,
    MEMBER,
    MEMBER_PR,
    REPOSITORY,
    TEAM,
    EXTERNAL_REPO,
    CREATED_REPOS_BY_MEMBERS
}
