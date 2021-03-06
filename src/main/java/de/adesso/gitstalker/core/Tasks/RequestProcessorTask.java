package de.adesso.gitstalker.core.Tasks;

import de.adesso.gitstalker.core.REST.OrganizationController;
import de.adesso.gitstalker.core.config.Config;
import de.adesso.gitstalker.core.config.RateLimitConfig;
import de.adesso.gitstalker.core.enums.RequestStatus;
import de.adesso.gitstalker.core.enums.RequestType;
import de.adesso.gitstalker.core.exceptions.NoRemainingRateLimitException;
import de.adesso.gitstalker.core.objects.OrganizationWrapper;
import de.adesso.gitstalker.core.objects.Query;
import de.adesso.gitstalker.core.repositories.OrganizationRepository;
import de.adesso.gitstalker.core.repositories.ProcessingRepository;
import de.adesso.gitstalker.core.repositories.RequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Transient;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.Date;

public class RequestProcessorTask {

    @Autowired
    RequestRepository requestRepository;

    @Autowired
    OrganizationRepository organizationRepository;

    @Autowired
    ProcessingRepository processingRepository;

    @Transient
    private Logger logger = LoggerFactory.getLogger(RequestProcessorTask.class);

    /**
     * Scheduled task checking for queries without crawled information.
     * After picking one query the request starts with the specified information out of the selected query. After the request the query is saved in the repository with the additional response data.
     * The request can generate an exception because of no remaining Rate Limit.
     */
    @Scheduled(fixedRate = Config.PROCESSING_RATE_IN_MS, initialDelay = Config.PROCESSING_DELAY_IN_MS)
    private void crawlQueryData() {
        ArrayList<Query> queriesToProcess;
        String organizationName;

        if (!this.processingRepository.findAll().isEmpty()) {
            organizationName = this.processingRepository.findAll().get(0).getInternalOrganizationName();
            queriesToProcess = this.requestRepository.findByQueryStatusAndOrganizationName(RequestStatus.CREATED, organizationName);
        } else queriesToProcess = this.requestRepository.findByQueryStatus(RequestStatus.CREATED);

        if (!queriesToProcess.isEmpty()) {
            try {
                this.processQuery(this.findProcessableQueryByRequestCostAndPriority(queriesToProcess));
            } catch (NoRemainingRateLimitException e) {
                logger.error(e.getMessage());
                if(!RateLimitConfig.checkIfRateLimitWillReset()){
                    try {
                        Thread.sleep(RateLimitConfig.getResetRateLimitAt().getTime() - System.currentTimeMillis());
                    } catch (InterruptedException e1) {
                        logger.error("InterruptedException at Thread Sleep");
                    }
                }
            }
        }
    }

    /**
     * Check whether prioritized requests have already been completed. Prioritizes the application flow so that paused requests are not processed.
     * @param createdQuery Query to be checked
     * @return Boolean whether it is approved or not
     */
    private boolean checkIfPrioritizedRequestsAreFinished(Query createdQuery) {
        OrganizationWrapper organization = this.organizationRepository.findByOrganizationName(createdQuery.getOrganizationName());
        switch (createdQuery.getQueryRequestType()) {
            case TEAM:
                return organization.getFinishedRequests().contains(RequestType.MEMBER) && organization.getFinishedRequests().contains(RequestType.REPOSITORY);
            case MEMBER_PR:
                return organization.getFinishedRequests().contains(RequestType.REPOSITORY);
            case CREATED_REPOS_BY_MEMBERS:
                return organization.getFinishedRequests().contains(RequestType.MEMBER);
        }
        return true;
    }

    /**
     * Selects a suitable query based on the calculated query costs and prioritization.
     * @param processingQueries Array of multiple queries that need to be processed.
     * @return Suitable query based on rate limit and prioritization.
     * @throws NoRemainingRateLimitException Error if the rate limit is used up and no more queries can be processed.
     */
    private Query findProcessableQueryByRequestCostAndPriority(ArrayList<Query> processingQueries) throws NoRemainingRateLimitException {
        for (Query createdQuery : processingQueries) {
            if (RateLimitConfig.getRemainingRateLimit() - createdQuery.getEstimatedQueryCost() >= 0 && checkIfPrioritizedRequestsAreFinished(createdQuery)) {
                return createdQuery;
            }
        }
        throw new NoRemainingRateLimitException("Rate Limit exhausted. Processing is paused until " + RateLimitConfig.getResetRateLimitAt());
    }

    /**
     * Processing of a query by saving it again with the response.
     * @param queryToProcess Query to be processed
     */
    private void processQuery(Query queryToProcess) {
        this.requestRepository.delete(queryToProcess);
        queryToProcess.crawlQueryResponse();
        this.requestRepository.save(queryToProcess);
    }
}
