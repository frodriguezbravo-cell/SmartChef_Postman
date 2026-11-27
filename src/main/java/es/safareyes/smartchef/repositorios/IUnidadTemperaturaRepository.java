package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.catalogos.UnidadTemperatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUnidadTemperaturaRepository extends JpaRepository<UnidadTemperatura, Short> {

    Optional<UnidadTemperatura> findByCodigo(String codigo);
}
