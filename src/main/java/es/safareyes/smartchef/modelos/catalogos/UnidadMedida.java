package es.safareyes.smartchef.modelos.catalogos;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cat_unidad_medida")
@NoArgsConstructor
public class UnidadMedida extends Catalogo {
}
