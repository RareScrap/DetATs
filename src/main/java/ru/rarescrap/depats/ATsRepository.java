package ru.rarescrap.depats;

import ru.rarescrap.depats.data.DepAT;
import ru.rarescrap.depats.data.EmptyDepAT;
import ru.rarescrap.depats.data.ExcludedDepAT;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import static ru.rarescrap.depats.BasePlugin.LOGGER;

public class ATsRepository {
    private Set<DepAT> depATs = new HashSet<>();

    public void add(File cfgFile, File dependency) throws IOException {
        Set<String> allATs = parseCfg(cfgFile); // Все трансформеры из файла
        if (allATs.isEmpty()) {
            LOGGER.info("No ATs in " + cfgFile);
            return;
        }

        LOGGER.info("Founded ATs: " + cfgFile);
        for (String atPath : allATs) LOGGER.info("\t" + atPath);

        // Потому что не хочу тащить гуаву
        Set<String> actualATs = new HashSet<>(allATs); // Уникальные транформеры относительно других
        for (DepAT dep : depATs) {
            actualATs.removeAll(dep.ats);
            // Sets.difference из gradle-internal не канает, т.к. вываливается в рантайме
        }
        Set<String> excludedATs = new HashSet<>(allATs); // Исключенные уже существующие трансформеры
        excludedATs.removeAll(actualATs);

        // TODO: Тест на существование DepAT а мапе

        DepAT depAT;
        if (excludedATs.isEmpty())
            depAT = new DepAT(cfgFile.getName(), dependency.getName(), actualATs);
        else if (actualATs.isEmpty())
            depAT = new EmptyDepAT(cfgFile.getName(), dependency.getName(), excludedATs);
        else
            depAT = new ExcludedDepAT(cfgFile.getName(), dependency.getName(), actualATs, excludedATs);

        depATs.add(depAT);
    }

    /**
     * @param cfgFile cfg-файл с трансформерами
     * @return Построчно извлеченные трансформеры
     */
    private Set<String> parseCfg(File cfgFile) throws IOException {
        Set<String> ats = new HashSet<>();
        BufferedReader reader = new BufferedReader(new FileReader(cfgFile));
        String line = reader.readLine();
        while (line != null) {
            if (line.startsWith("public")) ats.add(line);
            line = reader.readLine();
        }
        return ats;
    }

    void saveInto(File file) {
        try(FileWriter fw = new FileWriter(file)) {
            // Сортируем список (на один проход), чтобы упростить цикл
            // Сверху трансформеры, в середине - эксклюдированные, а внизу - пустые
            TreeSet<DepAT> sortedDepATs = new TreeSet<>((o1, o2) -> {
                int result;
                if (o1.getClass() == o2.getClass()) {
                    result = Integer.compare(o2.ats.size(), o1.ats.size()); // TODO: Почему ревесаются?
                    return result;
                }

                int o1Priority;
                if (o1 instanceof EmptyDepAT) o1Priority = 2;
                else if (o1 instanceof ExcludedDepAT) o1Priority = 1;
                else o1Priority = 0; // o1 instanceof DepAT
                int o2Priority;

                if (o2 instanceof EmptyDepAT) o2Priority = 2;
                else if (o2 instanceof ExcludedDepAT) o2Priority = 1;
                else o2Priority = 0; // o2 instanceof DepAT

                result =  Integer.compare(o1Priority, o2Priority);
                return result;

//                int result;
//                if (o1 instanceof EmptyDepAT) result = 1; // TODO: Почему ревесаются?
//                else if (o1 instanceof ExcludedDepAT) result = 0;
//                else result = -1;
//                return result;
            });
            sortedDepATs.addAll(depATs);

            for (DepAT depAT : sortedDepATs) {
                depAT.printTo(fw);
                fw.write("\n");
            }

        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public boolean isEmpty() {
        return depATs.isEmpty();
    }

    /**
     * @return Количество уникальных трансформеров в хранилище
     */
    public int size() {
        int ats = 0;
        for (DepAT depAT : depATs) ats += depAT.ats.size();
        return ats;
    }
}
