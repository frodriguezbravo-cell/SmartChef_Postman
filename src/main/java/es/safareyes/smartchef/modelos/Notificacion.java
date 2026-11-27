package es.safareyes.smartchef.modelos;

import es.safareyes.smartchef.servicios.Fechas;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario destinatario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoNotificacion tipo;

    @Column(name = "mensaje", nullable = false, length = 500)
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurso", nullable = false, length = 30)
    private RecursoNotificacion recurso;

    @Column(name = "recurso_id")
    private Integer recursoId;

    @Column(name = "leida", nullable = false)
    private Boolean leida = false;

    @Column(name = "creada_en", nullable = false, updatable = false)
    private LocalDateTime creadaEn;

    @PrePersist
    void alCrear() {
        if (creadaEn == null) {
            creadaEn = LocalDateTime.now(Fechas.MADRID);
        }
    }
}
