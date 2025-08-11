package com.example.lugarescomunes;

public enum PlaceType {
    CLASSROOM("Aula", "Aulas y salones de clase"),
    LABORATORY("Laboratorio", "Laboratorios y talleres"),
    LIBRARY("Biblioteca", "Bibliotecas y salas de estudio"),
    CAFETERIA("Cafetería", "Cafeterías y comedores"),
    OFFICE("Oficina", "Oficinas administrativas"),
    AUDITORIUM("Auditorio", "Auditorios y salas de eventos"),
    SERVICE("Servicio", "Servicios generales");

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
        for (PlaceType placeType : PlaceType.values()) {
            if (placeType.displayName.equalsIgnoreCase(type) ||
                    placeType.name().equalsIgnoreCase(type)) {
                return placeType;
            }
        }
        return CLASSROOM; // Default
    }

    @Override
    public String toString() {
        return displayName;
    }
}