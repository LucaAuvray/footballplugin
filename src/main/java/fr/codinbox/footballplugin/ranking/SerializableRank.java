package fr.codinbox.footballplugin.ranking;

import fr.codinbox.footballplugin.serialization.SerializedData;

public class SerializableRank implements SerializedData<Rank> {

    private String name;

    private String colorCode;

    private double minMmr;
    private double maxMmr;

    private int numberOfDivisions;

    public SerializableRank(String name, String colorCode, double minMmr, double maxMmr, int numberOfDivisions) {
        this.name = name;
        this.colorCode = colorCode;
        this.minMmr = minMmr;
        this.maxMmr = maxMmr;
        this.numberOfDivisions = numberOfDivisions;
    }

    public SerializableRank() {

    }

    public String getName() {
        return name;
    }

    public String getColorCode() {
        return colorCode;
    }

    public double getMinMmr() {
        return minMmr;
    }

    public double getMaxMmr() {
        return maxMmr;
    }

    public int getNumberOfDivisions() {
        return numberOfDivisions;
    }

    @Override
    public Rank toLegacyData() {
        return new Rank(name, colorCode, minMmr, maxMmr, numberOfDivisions);
    }
}
