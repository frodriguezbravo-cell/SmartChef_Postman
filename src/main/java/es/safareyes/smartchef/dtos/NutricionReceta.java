package es.safareyes.smartchef.dtos;

import java.math.BigDecimal;

public interface NutricionReceta {
    Integer getIdReceta();

    String getTitulo();

    Integer getRaciones();

    BigDecimal getCaloriasKcal();

    BigDecimal getProteinasG();

    BigDecimal getHidratosG();

    BigDecimal getAzucaresG();

    BigDecimal getGrasasG();

    BigDecimal getGrasasSaturadasG();

    BigDecimal getFibraG();

    BigDecimal getSalG();
}
