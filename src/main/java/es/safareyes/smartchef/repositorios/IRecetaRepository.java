package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.dtos.NutricionReceta;
import es.safareyes.smartchef.dtos.RecetasPorTipoPlato;
import es.safareyes.smartchef.modelos.Receta;
import es.safareyes.smartchef.dtos.CoincidenciaView;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface IRecetaRepository extends JpaRepository<Receta, Integer>, JpaSpecificationExecutor<Receta> {

    Optional<Receta> findBySlug(String slug);

    Optional<Receta> findFirstByIngredientesIngredienteIdAndPublicadaTrueAndRetiradaFalseOrderByTituloAsc(
            Integer idIngrediente);

    List<Receta> findByPublicadaTrue();

    List<Receta> findByTituloContainingIgnoreCaseAndPublicadaTrue(String texto);

    List<Receta> findByDificultadCodigoAndPublicadaTrue(String codigoDificultad);

    @Query("select r from Receta r where r.publicada = true " +
            "and (coalesce(r.tiempoPreparacionMin, 0) + coalesce(r.tiempoCoccionMin, 0)) <= :minutos")
    List<Receta> buscarRapidas(@Param("minutos") Integer minutos);

    @Query("select distinct r from Receta r join r.dietas d " +
            "where r.publicada = true and d.codigo = :codigoDieta")
    List<Receta> buscarPorDieta(@Param("codigoDieta") String codigoDieta);

    @Query("select r from Receta r where r.publicada = true and not exists (" +
            "select ri.id from RecetaIngrediente ri join ri.ingrediente i join i.alergenos a " +
            "where ri.receta = r and a.codigo in :codigos)")
    List<Receta> buscarSinAlergenos(@Param("codigos") List<String> codigos);

    @Query("select r from Receta r where r.publicada = true and not exists (" +
            "select ri.id from RecetaIngrediente ri where ri.receta = r and ri.esOpcional = false " +
            "and not exists (select di.id from DespensaItem di where di.despensa.id = :idDespensa " +
            "and di.fechaBaja is null and di.ingrediente = ri.ingrediente))")
    List<Receta> buscarCocinablesConDespensa(@Param("idDespensa") Integer idDespensa);

    @Query("select new es.safareyes.smartchef.dtos.RecetasPorTipoPlato(tp.nombre, count(r)) " +
            "from Receta r join r.tipoPlato tp where r.publicada = true " +
            "group by tp.nombre, tp.orden order by tp.orden")
    List<RecetasPorTipoPlato> contarRecetasPorTipoPlato();

    @Query(nativeQuery = true, value = "select id_receta as \"idReceta\", titulo as \"titulo\", " +
            "raciones as \"raciones\", calorias_kcal as \"caloriasKcal\", proteinas_g as \"proteinasG\", " +
            "hidratos_g as \"hidratosG\", azucares_g as \"azucaresG\", grasas_g as \"grasasG\", " +
            "grasas_saturadas_g as \"grasasSaturadasG\", fibra_g as \"fibraG\", sal_g as \"salG\" " +
            "from v_receta_nutricion where id_receta = :idReceta")
    Optional<NutricionReceta> nutricionPorRacion(@Param("idReceta") Integer idReceta);

    @EntityGraph(attributePaths = {"ingredientes", "ingredientes.ingrediente", "ingredientes.unidad",
            "tipoPlato", "tipoCocina", "dificultad", "creadoPor"})
    Optional<Receta> findConIngredientesById(Integer id);

    @EntityGraph(attributePaths = {"pasos"})
    Optional<Receta> findConPasosById(Integer id);

    @EntityGraph(attributePaths = {"ingredientes", "ingredientes.ingrediente", "ingredientes.unidad", "dificultad"})
    List<Receta> findConIngredientesByIdIn(Collection<Integer> ids);

    @Query("select r.id from Receta r where not exists (" +
            "select ri.id from RecetaIngrediente ri where ri.receta = r and ri.ingrediente.aptoVegetariano = false)")
    List<Integer> idsVegetarianas();

    @Query("select r.id from Receta r where not exists (" +
            "select ri.id from RecetaIngrediente ri join ri.ingrediente i join i.alergenos a " +
            "where ri.receta = r and a.codigo = 'GLUTEN')")
    List<Integer> idsSinGluten();

    @Query("select r.id from Receta r join r.ingredientes ri join ri.ingrediente i " +
            "where ri.esOpcional = false " +
            "and not exists (select x.id from RecetaIngrediente x where x.receta = r and x.esOpcional = false " +
            "and (x.cantidadGramos is null or x.ingrediente.precioKg is null)) " +
            "group by r.id, r.raciones " +
            "having sum(ri.cantidadGramos * i.precioKg) / 1000 / r.raciones <= :maximo")
    List<Integer> idsEconomicas(@Param("maximo") BigDecimal maximo);

    @Query(nativeQuery = true, value = "select ri.id_receta as \"idReceta\", " +
            "count(distinct ri.id_ingrediente) as \"total\", " +
            "count(distinct ri.id_ingrediente) filter (where ri.id_ingrediente in (:ids)) as \"presentes\" " +
            "from receta_ingrediente ri join receta r on r.id = ri.id_receta " +
            "where r.publicada = true and r.retirada = false " +
            "group by ri.id_receta " +
            "having count(distinct ri.id_ingrediente) filter (where ri.id_ingrediente in (:ids)) * 2 " +
            "> count(distinct ri.id_ingrediente) " +
            "order by count(distinct ri.id_ingrediente) filter (where ri.id_ingrediente in (:ids)) * 1.0 " +
            "/ count(distinct ri.id_ingrediente) desc, ri.id_receta")
    List<CoincidenciaView> coincidenciasConIngredientes(@Param("ids") Collection<Integer> ids);
}
