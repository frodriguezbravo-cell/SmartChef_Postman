package es.safareyes.smartchef.modelos.catalogos;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cat_categoria_articulo")
@NoArgsConstructor
public class CategoriaArticulo extends Catalogo {
}
