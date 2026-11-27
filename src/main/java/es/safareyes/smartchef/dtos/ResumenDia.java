package es.safareyes.smartchef.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumenDia {
    private LocalDate fecha;
    private Integer totalComidas;
    private BigDecimal caloriasPlanificadas;
    private Integer comidasSinDatos;
    private Integer objetivoCalorias;
    private BigDecimal diferencia;
}
