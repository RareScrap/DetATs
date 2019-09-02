package ru.rarescrap.depats.data;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Data-класс, хранящий Access-трансформеры, которые не будут применены, т.к. не имеют
 * уникальных трансформеров по отношению к другим объектам {@link DepAT}
 */
public class EmptyDepAT extends ExcludedDepAT{
    public EmptyDepAT(String cfgFileName, String depName, Set<String> excludedATs) {
        super(cfgFileName, depName, new HashSet<>(), excludedATs);
    }

    @Override
    public void printTo(FileWriter fw) throws IOException {
        fw.write("# SKIPPED ATs from " + cfgFileName + " (from " + depName + ") - No unique ATs\n");
    }

}