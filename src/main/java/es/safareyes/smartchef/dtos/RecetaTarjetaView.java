package es.safareyes.smartchef.dtos;

public interface RecetaTarjetaView {

    Integer getId();

    String getTitulo();

    String getSlug();

    String getImagenUrl();

    Short getRaciones();

    Short getTiempoPreparacionMin();

    Short getTiempoCoccionMin();

    DificultadView getDificultad();

    interface DificultadView {
        String getNombre();
    }
}
