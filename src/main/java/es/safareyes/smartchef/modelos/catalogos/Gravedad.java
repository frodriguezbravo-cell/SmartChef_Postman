package es.safareyes.smartchef.modelos.catalogos;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cat_gravedad")
@NoArgsConstructor
public class Gravedad extends Catalogo {
}
