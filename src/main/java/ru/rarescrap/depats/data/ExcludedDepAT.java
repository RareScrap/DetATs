package ru.rarescrap.depats.data;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Data-класс для хранения трансформеров и трансформеров, не имеющих воздействия, т.к. уже
 * представлены другим объектом {@link DepAT}
 */
public class ExcludedDepAT extends DepAT {
    /** Существующие, но не примененные трансформеры */
    Set<String> excludedATs;

    /**
     * @param cfgFileName cfg-файл с трансформерами
     * @param depName Имя зависимсти, откуда был взят cfg-файл
     * @param actualATs Access-трансформеры, которые будут применены
     * @param excludedATs Существующие, но не примененные Access-трансформеры
     */
    public ExcludedDepAT(String cfgFileName, String depName, Set<String> actualATs, Set<String> excludedATs) {
        super(cfgFileName, depName, actualATs);
        this.excludedATs = excludedATs;
    }

    @Override
    public void printTo(FileWriter fw) throws IOException {
        fw.write("# " + cfgFileName + " (from " + depName + ")\n");
        fw.write("# Excluded " + excludedATs.size() + " access transformers (already used)\n");
        Set<String> actualATs = new HashSet<>(ats);
        actualATs.removeAll(excludedATs);
        for (String at : actualATs) fw.write(at + "\n");
    }

}
