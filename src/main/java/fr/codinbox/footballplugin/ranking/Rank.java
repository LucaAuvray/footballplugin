package fr.codinbox.footballplugin.ranking;

import fr.codinbox.footballplugin.serialization.SerializableData;

public class Rank implements SerializableData<SerializableRank> {

    private String name;

    private String colorCode;

    private double minMmr;
    private double maxMmr;

    private int numberOfDivisions;

    private double divisionMmr;

    private double[] divisionsStartMmr;

    public Rank(String name, String colorCode, double minMmr, double maxMmr, int numberOfDivisions) {
        this.name = name;
        this.colorCode = colorCode.replaceAll("&", "§");
        this.minMmr = minMmr;
        this.maxMmr = maxMmr;
        this.numberOfDivisions = numberOfDivisions;
        this.divisionsStartMmr = new double[(int) numberOfDivisions];

        divisionMmr = (maxMmr - minMmr) / numberOfDivisions;
        for(int i = 0; i < numberOfDivisions; i++) {
            divisionsStartMmr[i] = minMmr + (i*divisionMmr);
        }
    }

    public Rank() {

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

    public double getDivisionMmr() {
        return divisionMmr;
    }

    public double[] getDivisionsStartMmr() {
        return divisionsStartMmr;
    }

    boolean isInRange(double mmr) {
        return minMmr <= mmr && maxMmr >= mmr;
    }

    public int getDivisionNumber(double mmr) {
        if(minMmr > mmr)
            return 1;

        if(maxMmr < mmr)
            return numberOfDivisions;

        int div = 1;
        for(int i = 0; i < numberOfDivisions; i++) {
            if(divisionsStartMmr[i] <= mmr)
                div = i+1;
        }
        return div;
    }

    @Override
    public SerializableRank serialize() {
        return new SerializableRank(name, colorCode, minMmr, maxMmr, numberOfDivisions);
    }

}
