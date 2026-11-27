package es.safareyes.smartchef.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngredienteEscalado {
    private Integer idIngrediente;
    private String nombre;
    private BigDecimal cantidad;
    private BigDecimal cantidadGramos;
    private String unidadCodigo;
    private String unidadNombre;
    private String grupo;
    private String notas;
    private Boolean opcional;
}
