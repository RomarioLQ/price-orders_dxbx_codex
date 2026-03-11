package ru.cource.priceorders.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.cource.priceorders.models.Nomenclature;

import java.util.UUID;

public interface NomenclatureRepository extends JpaRepository<Nomenclature, UUID> {
}
