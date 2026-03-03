package ru.practicum.dal.repository.compilation;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.dal.dao.compilation.Compilation;

import java.util.List;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    List<Compilation> findAllBy(Pageable pageable);

    List<Compilation> findByPinned(Boolean pinned, Pageable pageable);
}
