package de.adesso.gitstalker.core.resources.organization_validation;


import de.adesso.gitstalker.core.resources.rateLimit_Resources.RateLimit;
import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
public class Data {

    private Organization organization;
    private RateLimit rateLimit;
}
