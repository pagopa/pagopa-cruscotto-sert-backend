package com.nexigroup.pagopa.cruscotto.sert.service.validation;

import jakarta.validation.groups.Default;

public class ValidationGroups {

    private ValidationGroups() {}

    public interface PlannedShutdownJob extends Default {}

    public interface RegistryJob extends Default {}

    public interface StationJob extends Default {}

    public interface KpiA1Job extends Default {}

    public interface KpiA2Job extends Default {}

    public interface KpiB2Job extends Default {}

    public interface KpiB9Job extends Default {}
}
