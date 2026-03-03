package ru.practicum.dal.repository.category;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.dal.dao.category.Category;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Boolean existsByName(String name);

    Optional<Category> findByName(String name);
}
