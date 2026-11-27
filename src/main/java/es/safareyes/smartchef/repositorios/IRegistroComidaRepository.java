package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.dtos.CaloriasDiaView;
import es.safareyes.smartchef.dtos.GuardadaView;
import es.safareyes.smartchef.dtos.v1.RecetaPopularDto;
import es.safareyes.smartchef.modelos.MomentoComida;
import es.safareyes.smartchef.modelos.RegistroComida;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IRegistroComidaRepository extends JpaRepository<RegistroComida, Integer> {

    boolean existsByUsuarioIdAndFechaAndMomento(Integer idUsuario, LocalDate fecha, MomentoComida momento);

    @EntityGraph(attributePaths = {"receta", "receta.dificultad"})
    List<RegistroComida> findByUsuarioIdAndFechaBetweenOrderByFechaAsc(Integer idUsuario, LocalDate desde,
                                                                      LocalDate hasta);

    @Query(nativeQuery = true, value = "select to_char(r.fecha, 'YYYY-MM-DD') as \"fecha\", " +
            "coalesce(sum(r.calorias_kcal), 0) as \"calorias\", count(*) as \"comidas\" " +
            "from registro_comida r " +
            "where r.id_usuario = :idUsuario " +
            "and date_trunc('week', r.fecha) = date_trunc('week', cast(:fecha as date)) " +
            "group by r.fecha order by r.fecha")
    List<CaloriasDiaView> caloriasPorDia(@Param("idUsuario") Integer idUsuario, @Param("fecha") LocalDate fecha);

    @Query("select new es.safareyes.smartchef.dtos.v1.RecetaPopularDto(r.id, r.titulo, count(rc)) " +
            "from RegistroComida rc join rc.receta r " +
            "where rc.fecha between :desde and :hasta " +
            "group by r.id, r.titulo order by count(rc) desc, r.titulo")
    List<RecetaPopularDto> masCocinadas(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta,
                                        Pageable limite);

    @Query(nativeQuery = true, value = "select cr.id_receta as \"idReceta\", r.titulo as \"titulo\", " +
            "count(*) as \"total\" " +
            "from coleccion_receta cr join receta r on r.id = cr.id_receta " +
            "where cr.creado_en >= :desde and cr.creado_en < :hasta " +
            "group by cr.id_receta, r.titulo order by count(*) desc, r.titulo limit :limite")
    List<GuardadaView> masGuardadas(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta,
                                    @Param("limite") int limite);
}
