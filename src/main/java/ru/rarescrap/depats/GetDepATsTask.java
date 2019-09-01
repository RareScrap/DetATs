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
    public String getDepATs() { // TODO: Почему я не могу запустить этот таск только для рутпроджекта?
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
            LOGGER.lifecycle("Scanning dependency: " + dependency);
            Set<File> cfgPaths = getCFGs(dependency);
            if (cfgPaths.isEmpty()) continue; // Если у зависимоти нет cfg-файла - пропускаем ее
            LOGGER.lifecycle("Found cfg files in dependency: " + dependency);
            for (File cfg : cfgPaths) {
                try {
                    LOGGER.lifecycle("Extracting ATs from: " + cfg);
                    ats.add(cfg, dependency);
                } catch (IOException e) {
                    if (enableStacktrace) e.printStackTrace();
                }
            }
        }

        if (ats.isEmpty()) throw new RuntimeException("Dependencies not found. Are you called setupDecompWorkspace?");
        LOGGER.lifecycle("Saving all founded ATs (" + ats.size() + ") in " + depATs.getAbsolutePath());

        ats.saveInto(depATs);
        return depATs.getAbsolutePath();
    }

    /**
     * @return Пути к jar-никам зависимостей
     */
    private Set<File> collectDependencies() {
        Set<File> dependencies = new HashSet<>();

        for (Project project : getProject().getAllprojects()) {
            LOGGER.lifecycle("Project: " + project.getName());
            for (Configuration configuration : project.getConfigurations()) {
                LOGGER.lifecycle("Configuration: " + configuration + ", resolved: " + configuration.isCanBeResolved());
                if (configuration.isCanBeResolved()) {
                    try {
                        // Этим хистрым хаком мы получаем все зависимости, которые смогли разрешить.
                        // configuration.getFiles() обосрется есть хоть у одной зависимости будет
                        // неправильный путь из-за чего ее нельзя будет разрешить.
                        // А так как пользоваться этим плагином может конченный дибил,
                        // который не сможет нормально сконфигурить мультипроджекты,
                        // то нам следует игнорировать неразрешенные зависимости
                        Set<File> files = configuration.getResolvedConfiguration()
                                .getLenientConfiguration().getFiles(Specs.satisfyAll());

                        LOGGER.lifecycle("Found " + files.size() + " files");
                        for (File file : files) {
                            LOGGER.lifecycle("Pickup file: " + file.toString());
                            if ("jar".equals(getFileExtension(file))) dependencies.add(file);
                        }
                    } catch (Exception e) {
                        if (enableStacktrace) LOGGER.lifecycle("pizda", e);
                    }
                }
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
                if ("cfg".equals(getFileExtension(file))) cfgs.add(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cfgs;
    }

    // Вообще похуй на Files, апач комон и гуаву. С этими вашими апдейтами проще парсить расширение самому.
    private static String getFileExtension(File file) {
        String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }
}