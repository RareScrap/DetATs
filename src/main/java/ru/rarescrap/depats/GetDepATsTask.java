package ru.rarescrap.depats;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileTree;
import org.gradle.api.logging.configuration.ShowStacktrace;
import org.gradle.api.specs.Specs;
import org.gradle.api.tasks.TaskAction;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static ru.rarescrap.depats.BasePlugin.LOGGER;

public class GetDepATsTask extends DefaultTask {
    // ALWAYS - при явном указании --stacktrace, INTERNAL_EXCEPTIONS - при отсуствии
    private boolean enableStacktrace = getProject().getGradle().getStartParameter().getShowStacktrace() == ShowStacktrace.ALWAYS;

    //@OutputFile // TODO: Поменить файл как выходной артефакт
    //private File outputFile = new File(dependenciesAT);

    public GetDepATsTask() {
        setDescription("Search for access transformers in dependencies and save it to file");
    }

    /**
     * @return Путь до файла с итоговыми трансформерами
     */
    @TaskAction
    public void getDepATs() { // TODO: Почему я не могу запустить этот таск только для рутпроджекта?
        File depATs = getProject().getExtensions().getByType(DepATsPluginExtension.class).getDepATs(); // Не в конструкторе, т.к. не подберет кастомные значения т.к. запустится до evaluation

        ATsRepository ats = new ATsRepository();

        // TODO: Переработать парсер
        // Т.к. этот таск может выполнится и для сапроджектов, то мы предварительно выгрузим уже
        // сохраненные трансформеры, чтобы они не перезапиались
//        try {
//            ats = extractATs(depATs.getAbsolutePath());
//        } catch (IOException e) {
//            ats = new HashSet<>();
//        }

        for (File dependency : collectDependencies()) {
            LOGGER.info("Scanning dependency: " + dependency);
            Set<File> cfgPaths = getCFGs(dependency);
            if (cfgPaths.isEmpty()) continue; // Если у зависимоти нет cfg-файла - пропускаем ее
            LOGGER.info("Found cfg files in dependency: " + dependency);
            for (File cfg : cfgPaths) {
                try {
                    LOGGER.info("Extracting ATs from: " + cfg);
                    ats.add(cfg, dependency);
                } catch (IOException e) {
                    LOGGER.error(e.getMessage());
                }
            }
        }

        if (ats.isEmpty()) {
           LOGGER.error("You either haven't any ATs in your dependencies, ignore all exist ATs, or do not have any dependencies because you not called 'setupDecompWorkspace'."); // TODO: Вот с setupDecompWorkspace не знаю. Походу он всегда будет иметь хоть какие-то зависимости.
           return;
        }

        LOGGER.info("Saving all founded ATs (" + ats.size() + ") in " + depATs.getAbsolutePath());
        ats.saveInto(depATs);
    }

    /**
     * @return Пути к jar-никам зависимостей
     */
    private Set<File> collectDependencies() {
        Set<File> dependencies = new HashSet<>();

        for (Project project : getProject().getAllprojects()) {
            LOGGER.info("Project: " + project.getName());
            for (Configuration configuration : project.getConfigurations()) {
                if (configuration.isCanBeResolved()) {
                    try {
                        LOGGER.info("Analyse configuration: " + configuration.getName());
                        // Этим хистрым хаком мы получаем все зависимости, которые смогли разрешить.
                        // configuration.getFiles() обосрется есть хоть у одной зависимости будет
                        // неправильный путь из-за чего ее нельзя будет разрешить.
                        // А так как пользоваться этим плагином может конченный дибил,
                        // который не сможет нормально сконфигурить мультипроджекты,
                        // то нам следует игнорировать неразрешенные зависимости
                        Set<File> files = configuration.getResolvedConfiguration()
                                .getLenientConfiguration().getFiles(Specs.satisfyAll());

                        LOGGER.info("Found " + files.size() + " files");
                        for (File file : files) {
                            LOGGER.info("Pickup file: " + file.toString());
                            if ("jar".equals(getFileExtension(file))) dependencies.add(file);
                        }
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage());
                    }
                }
                else LOGGER.info("Skipping configuration: " + configuration.getName() + " - cant resolve.");
            }
        }

        return dependencies;
    }

    /**
     * @param dependency Путь до jar-ника зависимости
     * @return cfg-файлов для указанной зависимости
     */
    private Set<File> getCFGs(File dependency) {
        Set<File> cfgs = new HashSet<>();
        try {
            FileTree zipTree = getProject().zipTree(dependency);
            for (File file : zipTree.getFiles()) {
                if ("cfg".equals(getFileExtension(file)) && !shouldIgnoreCFG(file, dependency))
                    cfgs.add(file);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return cfgs;
    }

    /**
     * Определяет, стоит ли игнорировать трансформер
     * @param cfgFile cfg-файл, содержащий трансформеры
     * @param dependency jar'ник, в котором хранитс cfg-файл
     * @return True.
     */
    private boolean shouldIgnoreCFG(File cfgFile, File dependency) {
        List<String> ignoredATs = DepATsPluginExtension.get(getProject()).ignoredATs;
        for (String ignoredAT : ignoredATs) {
            String[] parts = ignoredAT.split(":");

            String ignoredCfgName = parts[0];
            String ignoredDepName = "";
            if (parts.length > 1) ignoredDepName = parts[1];

            if (ignoredCfgName.equals(cfgFile.getName())) {
                if (ignoredDepName.isEmpty()) return true;

                return ignoredDepName.equals(dependency.getName());
            }
        }

        return false;
    }

    // Вообще похуй на Files, апач комон и гуаву. С этими вашими апдейтами проще парсить расширение самому.
    private static String getFileExtension(File file) {
        String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }
}