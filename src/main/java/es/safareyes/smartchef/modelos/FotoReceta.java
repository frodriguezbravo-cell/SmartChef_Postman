package es.safareyes.smartchef.modelos;

import es.safareyes.smartchef.servicios.Fechas;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "foto_receta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "receta")
public class FotoReceta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_receta", nullable = false)
    private Receta receta;

    @Column(name = "nombre_original", nullable = false, length = 255)
    private String nombreOriginal;

    @Column(name = "clave", nullable = false, unique = true, length = 100)
    private String clave;

    @Column(name = "tipo_contenido", nullable = false, length = 50)
    private String tipoContenido;

    @Column(name = "tamano_bytes", nullable = false)
    private Long tamanoBytes;

    @Column(name = "orden", nullable = false)
    private Short orden = 0;

    @Column(name = "portada", nullable = false)
    private Boolean portada = false;

    @Column(name = "subida_en", nullable = false, updatable = false)
    private LocalDateTime subidaEn;

    @PrePersist
    void alSubir() {
        if (subidaEn == null) {
            subidaEn = LocalDateTime.now(Fechas.MADRID);
        }
    }
}
