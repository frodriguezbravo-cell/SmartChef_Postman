package es.safareyes.smartchef.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumenNutricional {
    private BigDecimal caloriasKcal;
    private BigDecimal proteinasG;
    private BigDecimal hidratosG;
    private BigDecimal azucaresG;
    private BigDecimal grasasG;
    private BigDecimal grasasSaturadasG;
    private BigDecimal fibraG;
    private BigDecimal salG;
    private Boolean completo;
    private Integer lineasSinDatos;
}
