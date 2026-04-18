package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import org.jspecify.annotations.NonNull;

public class HumanBeing implements Comparable<HumanBeing>, Serializable {
    @NonNull
    private UUID id; // Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    @NonNull
    private String name; //Поле не может быть null, Строка не может быть пустой
    @NonNull
    private Coordinates coordinates; //Поле не может быть null
    private final java.time.LocalDateTime creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private boolean realHero;
    @NonNull
    private Boolean hasToothpick; //Поле не может быть null
    private Integer impactSpeed; //Значение поля должно быть больше -117, Поле может быть null
    @NonNull
    private WeaponType weaponType; //Поле не может быть null
    private Mood mood; //Поле может быть null
    private Car car; //Поле может быть null
    public HumanBeing(@NonNull UUID id, @NonNull String name, @NonNull Coordinates coordinates, boolean realHero,
                      @NonNull Boolean hasToothpick, Integer impactSpeed, @NonNull WeaponType weaponType,
                      Mood mood, Car car) {
        this.id = id;
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name не может быть пустым");
        }
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = LocalDateTime.now();
        this.realHero = realHero;
        this.hasToothpick = hasToothpick;
        if (impactSpeed != null && impactSpeed <= -117) {
            throw new IllegalArgumentException("ImpactSpeed должен быть больше -117");
        }
        this.impactSpeed = impactSpeed;
        this.weaponType = weaponType;
        this.mood = mood;
        this.car = car;
    }
    public HumanBeing(@NonNull String name, @NonNull Coordinates coordinates, boolean realHero,
                      @NonNull Boolean hasToothpick, Integer impactSpeed, @NonNull WeaponType weaponType,
                      Mood mood, Car car) {
        this(UUID.randomUUID(), name, coordinates, realHero, hasToothpick, impactSpeed, weaponType, mood, car);
    }
    public @NonNull UUID getId() {
        return id;
    }
    public void setId(@NonNull UUID id) {
        this.id = id;
    }
    public @NonNull String getName() {
        return name;
    }
    public @NonNull Coordinates getCoordinates() {
        return coordinates;
    }
    public boolean isRealHero() {
        return realHero;
    }
    public @NonNull Boolean getHasToothpick() {
        return hasToothpick;
    }
    public Integer getImpactSpeed() {
        return impactSpeed;
    }
    public @NonNull WeaponType getWeaponType() {
        return weaponType;
    }
    public Mood getMood() {
        return mood;
    }
    public Car getCar() {
        return car;
    }
    public void setName(@NonNull String name) {
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name не может быть пустым");
        }
        this.name = name;
    }
    public void setCoordinates(@NonNull Coordinates coordinates) {
        this.coordinates = coordinates;
    }
    public void setRealHero(boolean realHero) {
        this.realHero = realHero;
    }
    public void setHasToothpick(boolean hasToothpick) {
        this.hasToothpick = hasToothpick;
    }
    public void setImpactSpeed(Integer impactSpeed) {
        if (impactSpeed != null && impactSpeed <= -117) {
            throw new IllegalArgumentException("ImpactSpeed должен быть больше -117");
        }
        this.impactSpeed = impactSpeed;
    }
    public void setWeaponType(@NonNull WeaponType weaponType) {
        this.weaponType = weaponType;
    }
    public void setMood(Mood mood) {
        this.mood = mood;
    }
    public void setCar(Car car) {
        this.car = car;
    }

    @Override
    public int compareTo(@NonNull HumanBeing o) {
        return this.name.compareTo(o.name);
    }
    @Override
    public String toString() {
        String carName = car == null ? "null" : car.getName();
        String carCool = car == null ? "null" : car.getCool().toString();
        return "HumanBeing{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", coordinatesX=" + coordinates.getX() +
                ", coordinatesY=" + coordinates.getY() +
                ", creationDate=" + creationDate +
                ", realHero=" + realHero +
                ", hasToothpick=" + hasToothpick +
                ", impactSpeed=" + impactSpeed +
                ", mood='" + mood + '\'' +
                ", weaponType=" + weaponType +
                ", carName=" + carName +
                ", carCool=" + carCool +
                '}';
    }
}
