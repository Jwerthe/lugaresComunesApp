package com.example.lugarescomunes;

public enum PlaceType {
    CLASSROOM("Aula", "Aulas y salones de clase"),
    LABORATORY("Laboratorio", "Laboratorios y talleres"),
    LIBRARY("Biblioteca", "Bibliotecas y salas de estudio"),
    CAFETERIA("Cafetería", "Cafeterías y comedores"),
    OFFICE("Oficina", "Oficinas administrativas"),
    AUDITORIUM("Auditorio", "Auditorios y salas de eventos"),
    SERVICE("Servicio", "Servicios generales"),
    PARKING("Estacionamiento", "Área de estacionamiento"),
    RECREATION("Recreación", "Área recreativa o deportiva"),
    ENTRANCE("Entrada", "Entrada principal o secundaria");

    private final String displayName;
    private final String description;

    PlaceType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static PlaceType fromString(String type) {
        if (type == null || type.trim().isEmpty()) {
            return SERVICE; // Default seguro
        }

        String cleanType = type.trim().toUpperCase();

        for (PlaceType placeType : PlaceType.values()) {
            if (placeType.name().equals(cleanType) ||
                    placeType.displayName.equalsIgnoreCase(type)) {
                return placeType;
            }
        }

        // Si no se encuentra, retornar un valor por defecto seguro
        return SERVICE;
    }

    public boolean isAcademic() {
        return this == CLASSROOM || this == LABORATORY || this == LIBRARY;
    }

    public boolean isPublic() {
        return this == CAFETERIA || this == SERVICE || this == RECREATION || this == ENTRANCE;
    }

    public boolean isAccessible() {
        return this != PARKING; // Todos excepto parking son accesibles por defecto
    }

    @Override
    public String toString() {
        return displayName;
    }
}