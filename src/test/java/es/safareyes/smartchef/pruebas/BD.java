package es.safareyes.smartchef.pruebas;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.time.LocalDate;

public final class BD {

    private final EntityManager em;

    public BD(EntityManager em) {
        this.em = em;
    }

    private Query consulta(String sql, Object[] parametros) {
        Query consulta = em.createNativeQuery(sql);
        for (int i = 0; i < parametros.length; i++) {
            consulta.setParameter(i + 1, parametros[i]);
        }
        return consulta;
    }

    public void ejecutar(String sql, Object... parametros) {
        consulta(sql, parametros).executeUpdate();
    }

    public Object valor(String sql, Object... parametros) {
        return consulta(sql, parametros).getSingleResult();
    }

    public int numero(String sql, Object... parametros) {
        return ((Number) valor(sql, parametros)).intValue();
    }

    public int insertar(String tabla, String sql, Object... parametros) {
        ejecutar(sql, parametros);
        return numero("SELECT currval(pg_get_serial_sequence('" + tabla + "', 'id'))");
    }

    public int idCatalogo(String tabla, String codigo) {
        return numero("SELECT id FROM " + tabla + " WHERE codigo = ?1", codigo);
    }

    public int idPor(String tabla, String columna, String valor) {
        return numero("SELECT id FROM " + tabla + " WHERE " + columna + " = ?1", valor);
    }

    public LocalDate hoy() {
        Object fecha = valor("SELECT CURRENT_DATE");
        if (fecha instanceof java.sql.Date sql) {
            return sql.toLocalDate();
        }
        if (fecha instanceof LocalDate local) {
            return local;
        }
        return LocalDate.parse(fecha.toString().substring(0, 10));
    }

    public int usuario(String username) {
        return insertar("usuario", "INSERT INTO usuario (email, username, password_hash, nombre, objetivo_calorias_dia) "
                + "VALUES (?1, ?2, 'hash', 'Prueba', 2000)", username + "@prueba.es", username);
    }

    public int despensa(int idUsuario, String nombre) {
        return insertar("despensa", "INSERT INTO despensa (id_usuario, nombre) VALUES (?1, ?2)", idUsuario, nombre);
    }

    public int producto(int idDespensa, String ingrediente, Integer diasParaCaducar, boolean dadoDeBaja) {
        String caducidad = diasParaCaducar == null ? "NULL" : "CURRENT_DATE + " + diasParaCaducar;
        String baja = dadoDeBaja ? "CURRENT_DATE" : "NULL";
        String motivo = dadoDeBaja ? "(SELECT id FROM cat_motivo_baja WHERE codigo = 'CONSUMIDO')" : "NULL";
        return insertar("despensa_item", "INSERT INTO despensa_item (id_despensa, id_ingrediente, cantidad, id_unidad, fecha_caducidad, fecha_baja, id_motivo_baja) "
                + "VALUES (?1, (SELECT id FROM ingrediente WHERE nombre = ?2), 500, (SELECT id FROM cat_unidad_medida WHERE codigo = 'G'), "
                + caducidad + ", " + baja + ", " + motivo + ")", idDespensa, ingrediente);
    }
}
