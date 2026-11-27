package es.safareyes.smartchef.repositorios;

import es.safareyes.smartchef.modelos.Notificacion;
import es.safareyes.smartchef.modelos.RecursoNotificacion;
import es.safareyes.smartchef.modelos.TipoNotificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface INotificacionRepository extends JpaRepository<Notificacion, Integer> {

    Page<Notificacion> findByDestinatarioId(Integer idUsuario, Pageable pageable);

    Page<Notificacion> findByDestinatarioIdAndLeida(Integer idUsuario, Boolean leida, Pageable pageable);

    boolean existsByDestinatarioIdAndTipoAndRecursoAndRecursoId(Integer idUsuario, TipoNotificacion tipo,
                                                               RecursoNotificacion recurso, Integer recursoId);
}
