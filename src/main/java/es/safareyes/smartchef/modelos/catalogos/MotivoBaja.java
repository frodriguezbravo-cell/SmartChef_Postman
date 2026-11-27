package es.safareyes.smartchef.modelos.catalogos;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cat_motivo_baja")
@NoArgsConstructor
public class MotivoBaja extends Catalogo {
}
