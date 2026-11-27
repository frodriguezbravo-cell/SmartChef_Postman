package es.safareyes.smartchef.modelos.catalogos;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cat_dieta")
@NoArgsConstructor
public class Dieta extends Catalogo {
}
