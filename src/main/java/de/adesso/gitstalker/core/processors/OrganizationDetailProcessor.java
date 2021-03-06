package de.adesso.gitstalker.core.processors;

import de.adesso.gitstalker.core.enums.RequestType;
import de.adesso.gitstalker.core.objects.OrganizationDetail;
import de.adesso.gitstalker.core.objects.OrganizationWrapper;
import de.adesso.gitstalker.core.objects.Query;
import de.adesso.gitstalker.core.parser.MemberGrowthChartJSParser;
import de.adesso.gitstalker.core.repositories.OrganizationRepository;
import de.adesso.gitstalker.core.repositories.ProcessingRepository;
import de.adesso.gitstalker.core.repositories.RequestRepository;
import de.adesso.gitstalker.core.resources.organisation_Resources.Data;
import de.adesso.gitstalker.core.resources.organisation_Resources.Organization;
import de.adesso.gitstalker.core.resources.organisation_Resources.ResponseOrganization;

import java.util.Objects;


public class OrganizationDetailProcessor extends ResponseProcessor {

    private MemberGrowthChartJSParser memberGrowthChartJSParser = new MemberGrowthChartJSParser();

    private RequestRepository requestRepository;
    private OrganizationRepository organizationRepository;
    private ProcessingRepository processingRepository;
    private Query requestQuery;
    private OrganizationWrapper organization;

    public OrganizationDetailProcessor(RequestRepository requestRepository, OrganizationRepository organizationRepository, ProcessingRepository processingRepository) {
        this.requestRepository = requestRepository;
        this.organizationRepository = organizationRepository;
        this.processingRepository = processingRepository;
    }

    /**
     * Performs the complete processing of an answer.
     *
     * @param requestQuery Query to be processed.
     */
    public void processResponse(Query requestQuery) {
        this.requestQuery = requestQuery;
        this.organization = this.organizationRepository.findByOrganizationName(requestQuery.getOrganizationName());
        Data responseData = ((ResponseOrganization) this.requestQuery.getQueryResponse()).getData();
        super.updateRateLimit(responseData.getRateLimit(), requestQuery.getQueryRequestType());
        this.processQueryResponse(responseData);
        super.doFinishingQueryProcedure(this.requestRepository, this.organizationRepository, this.processingRepository, this.organization, requestQuery, RequestType.ORGANIZATION_DETAIL);
    }

    /**
     * Processes the response of the requests.
     *
     * @param responseData Response information of the request
     */
    private void processQueryResponse(Data responseData) {
        Organization organization = responseData.getOrganization();
        OrganizationDetail organizationDetail;
        if (Objects.nonNull(this.organization.getOrganizationDetail())) {
            organizationDetail = this.organization.getOrganizationDetail();
        } else organizationDetail = new OrganizationDetail();

        this.organization.addMemberAmountHistory(organization.getMembersWithRole().getTotalCount());
        this.organization.setOrganizationDetail(
                organizationDetail.setName(organization.getName())
                        .setDescription(organization.getDescription())
                        .setWebsiteURL(organization.getWebsiteUrl())
                        .setGithubURL(organization.getGithubUrl())
                        .setLocation(organization.getLocation())
                        .setAvatarURL(organization.getAvatarUrl())
                        .setNumOfMembers(organization.getMembersWithRole().getTotalCount())
                        .setNumOfRepositories(organization.getRepositories().getTotalCount())
                        .setNumOfTeams(organization.getTeams().getTotalCount())
                        .setMemberAmountHistory(this.memberGrowthChartJSParser.parseInputToChartJSData(this.organization.getMemberAmountHistory())));

    }
}
