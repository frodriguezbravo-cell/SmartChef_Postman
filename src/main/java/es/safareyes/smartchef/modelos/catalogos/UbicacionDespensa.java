package es.safareyes.smartchef.modelos.catalogos;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cat_ubicacion_despensa")
@NoArgsConstructor
public class UbicacionDespensa extends Catalogo {
}
