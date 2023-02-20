package io.quarkiverse.embedded.postgresql.it;

import java.util.Collection;
import java.util.Optional;

public interface EmbeddedRepository {

    Collection<EmbeddedVO> findAll();

    Optional<EmbeddedVO> findById(Long id);

    long create(EmbeddedBody vo);

    boolean createOrUpdate(EmbeddedVO vo);

    boolean delete(long id);
}
