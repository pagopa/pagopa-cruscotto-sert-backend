package com.nexigroup.pagopa.cruscotto.sert.repository;

import com.nexigroup.pagopa.cruscotto.sert.domain.SearchPerimeterFile;
import com.nexigroup.pagopa.cruscotto.sert.domain.SearchInstance;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchPerimeterFileRepository extends JpaRepository<SearchPerimeterFile, UUID> {

    Optional<SearchPerimeterFile> findTopByInstanceOrderByCreatedAtDesc(SearchInstance instance);

    SearchPerimeterFile findByInstance(SearchInstance instance);

    @Query("select distinct file.instance.id from SearchPerimeterFile file " +
        "where file.instance.id in :instanceIds and file.content is not null and trim(file.content) <> ''")
    List<UUID> findInstanceIdsWithContent(@Param("instanceIds") Collection<UUID> instanceIds);
}
