package ru.rarescrap.depats.data;

import org.apache.commons.lang3.builder.EqualsBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;

/**
 * Data-класс для хранения трансформеров
 */
public class DepAT {
    /** Имя файла, откуда были извлечены транформеры */
    public final String cfgFileName;
    /** Зависимость, из которой были извлечены трансформеры */
    public final String depName;
    /** Список трансформеров, которые будут применены */
    public Set<String> ats;

    /**
     * @param cfgFileName cfg-файл с трансформерами
     * @param depName Имя зависимсти, откуда был взят cfg-файл
     * @param ats Access-трансформеры, которые будут применены
     */
    public DepAT(String cfgFileName, String depName, Set<String> ats) {
        this.cfgFileName = cfgFileName;
        this.depName = depName;
        this.ats = ats;
    }

    public void printTo(FileWriter fw) throws IOException {
        fw.write("# " + cfgFileName + " (from " + depName + ")\n");
        for (String at : ats) fw.write(at + "\n");
    }

    @Override // final, т.к. не планируются что субклассы будут иметь immutable-поля
    public final boolean equals(Object obj) {
        if (obj instanceof DepAT) {
            DepAT depAT = (DepAT) obj;
            return new EqualsBuilder()
                    .append(depName, depAT.depName)
                    .append(cfgFileName, depAT.cfgFileName)
                    .isEquals();
        }
        return false;
    }

    @Override
    public final int hashCode() { // final по той же причине что и equals()
        return Objects.hash(cfgFileName, depName);
    }
}
