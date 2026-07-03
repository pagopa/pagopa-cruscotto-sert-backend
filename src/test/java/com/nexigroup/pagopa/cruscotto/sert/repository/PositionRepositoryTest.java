package com.nexigroup.pagopa.cruscotto.sert.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
public class PositionRepositoryTest {

    @Autowired
    private PositionRepository positionRepository;

    @Test
    public void testEvents() {
        var page = positionRepository.findPositionWorkflows("301000014903654436", "80018460347", PageRequest.of(0, 10));
        System.out.println("--- JUNIT TEST OUTPUT START ---");
        System.out.println("TOTAL ELEMENTS: " + page.getTotalElements());
        for (Object[] row : page.getContent()) {
            System.out.println("ROW: timestamp=" + row[0] + ", evento=" + row[1] + ", tipo=" + row[2] + ", outcome=" + row[3] + ", eventId=" + row[4] + ", faultcode=" + row[5] + ", token=" + row[6]);
        }
        System.out.println("--- JUNIT TEST OUTPUT END ---");
    }
}
