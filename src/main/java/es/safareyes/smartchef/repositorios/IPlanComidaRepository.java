package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.PlanComida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface IPlanComidaRepository extends JpaRepository<PlanComida, Integer> {

    List<PlanComida> findByUsuarioIdAndFechaBetweenOrderByFechaAscTipoComidaOrdenAsc(
            Integer idUsuario, LocalDate desde, LocalDate hasta);

    List<PlanComida> findByFechaAndNotificarTrueAndCompletadaFalse(LocalDate fecha);
}
