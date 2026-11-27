package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.DespensaItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IDespensaItemRepository extends JpaRepository<DespensaItem, Integer> {

    List<DespensaItem> findByDespensaIdAndFechaBajaIsNull(Integer idDespensa);

    @Query("select di from DespensaItem di where di.despensa.usuario.id = :idUsuario " +
            "and di.fechaBaja is null and di.fechaCaducidad is not null " +
            "and di.fechaCaducidad <= :limite order by di.fechaCaducidad")
    List<DespensaItem> buscarQueCaducanAntesDe(@Param("idUsuario") Integer idUsuario,
                                              @Param("limite") LocalDate limite);

    @EntityGraph(attributePaths = {"ingrediente", "unidad"})
    @Query("select di from DespensaItem di where di.despensa.usuario.id = :idUsuario and di.fechaBaja is null " +
            "order by case when di.fechaCaducidad is null then 1 else 0 end, di.fechaCaducidad, di.ingrediente.nombre")
    List<DespensaItem> disponiblesDeUsuario(@Param("idUsuario") Integer idUsuario);

    @Query("select distinct di.ingrediente.id from DespensaItem di where di.despensa.usuario.id = :idUsuario " +
            "and di.fechaBaja is null and (di.fechaCaducidad is null or di.fechaCaducidad >= :hoy)")
    List<Integer> idsIngredientesDisponibles(@Param("idUsuario") Integer idUsuario, @Param("hoy") LocalDate hoy);

    @EntityGraph(attributePaths = {"ingrediente", "unidad"})
    @Query("select di from DespensaItem di where di.despensa.usuario.id = :idUsuario " +
            "and di.ingrediente.id = :idIngrediente and di.fechaBaja is null " +
            "order by case when di.fechaCaducidad is null then 1 else 0 end, di.fechaCaducidad, di.id")
    List<DespensaItem> activosDeIngrediente(@Param("idUsuario") Integer idUsuario,
                                            @Param("idIngrediente") Integer idIngrediente);

    @EntityGraph(attributePaths = {"ingrediente", "despensa", "despensa.usuario"})
    List<DespensaItem> findByFechaBajaIsNullAndFechaCaducidadBetween(LocalDate desde, LocalDate hasta);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from DespensaItem di where di.fechaBaja is null and di.fechaCaducidad < :limite")
    int eliminarCaducadosAntesDe(@Param("limite") LocalDate limite);
}
