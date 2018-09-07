package de.adesso.gitstalker.core.resources.externalRepo_Resources;

import de.adesso.gitstalker.core.resources.rateLimit_Resources.RateLimit;

import java.util.ArrayList;

public class Data {

    private ArrayList<NodesRepositories> nodes;
    private RateLimit rateLimit;

    public Data() {}

    public ArrayList<NodesRepositories> getNodes() {
        return nodes;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }
}
