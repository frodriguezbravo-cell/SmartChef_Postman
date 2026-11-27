package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.catalogos.RolUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IRolUsuarioRepository extends JpaRepository<RolUsuario, Short> {

    Optional<RolUsuario> findByCodigo(String codigo);
}
