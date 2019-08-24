package ru.rarescrap.depats;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.file.FileTree;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.configuration.ShowStacktrace;
import org.gradle.api.specs.Specs;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class GetDepATsTask extends DefaultTask {
    private String dependenciesAT = getProject().getRootProject().getBuildDir() + "\\dependencies_at.cfg";
    // ALWAYS - при явном указании --stacktrace, INTERNAL_EXCEPTIONS - при отсуствии
    private boolean enableStacktrace = getProject().getGradle().getStartParameter().getShowStacktrace() == ShowStacktrace.ALWAYS;

    private Logger logger = getProject().getLogger();

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

        Set<String> ats;
        // Т.к. этот таск может выполнится и для сапроджектов, то мы предварительно выгрузим уже
        // сохраненные трансформеры, чтобы они не перезапиались
        try {
            ats = extractATs(dependenciesAT);
        } catch (IOException e) {
            ats = new HashSet<>();
        }

        for (String dependencyPath : collectDependencies()) {
            logger.lifecycle("Scanning dependency: " + dependencyPath);
            Set<String> cfgPaths = getCFGs(dependencyPath);
            if (cfgPaths.isEmpty()) continue; // Если у зависимоти нет cfg-файла - пропускаем ее
            logger.lifecycle("Found cfg files in dependency: " + dependencyPath);
            for (String cfg : cfgPaths) {
                try {
                    logger.lifecycle("Extracting ATs from: " + cfg);
                    Set<String> dependencyATs = extractATs(cfg);
                    if (dependencyATs.isEmpty()) logger.lifecycle("No ATs in " + cfg);
                    else {
                        logger.lifecycle("Founded ATs: " + dependencyATs);
                        ats.addAll(dependencyATs);
                    }
                } catch (IOException e) {
                    if (enableStacktrace) e.printStackTrace();
                }
            }
        }

        if (ats.isEmpty()) throw new RuntimeException("Dependencies not found. Are you called setupDecompWorkspace?");
        logger.lifecycle("Saving all founded ATs (" + ats.size() + ") in " + dependenciesAT);
        saveATs(ats);
        return dependenciesAT;
    }

    /**
     * @param cfgPath Путь до cfg-файла
     * @return Построчно извлеченные трансформеры
     * @throws IOException
     */
    private Set<String> extractATs(String cfgPath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(cfgPath));
        String line = reader.readLine();
        Set<String> ats = new HashSet<>();
        while (line != null) {
            if (line.startsWith("public")) ats.add(line);
            line = reader.readLine();
        }
        return ats;
    }

    /**
     * @return Пути к jar-никам зависимостей
     */
    private Set<String> collectDependencies() {
        Set<String> dependencies = new HashSet<>();

//        logger.lifecycle("Всего проектов: " + getProject().getAllprojects().size());
//        for (Project project : getProject().getAllprojects()) {
//            logger.lifecycle(project.getName());
//        }

        for (Project project : getProject().getAllprojects()) {
            logger.lifecycle("Project: " + project.getName());
            for (Configuration configuration : project.getConfigurations()) {
                logger.lifecycle("Configuration: " + configuration + ", resolved: " + configuration.isCanBeResolved());
                if (configuration.isCanBeResolved()) {
                    try {
                        Set<File> files = configuration.getResolvedConfiguration().getLenientConfiguration().getFiles(Specs.satisfyAll());

                        logger.lifecycle("Found " + files.size() + " files");
                        for (File file : files) {
                            logger.lifecycle("Pickup file: " + file.toString());
                            if ("jar".equals(getFileExtension(file))) dependencies.add(file.toString());
                        }

//                        Set<File> files = configuration.getFiles();
//                        logger.lifecycle("Found " + files.size() + " files");
//                        for (File file : files) {
//                            logger.lifecycle("Pickup file: " + file.toString());
//                            if ("jar".equals(getFileExtension(file))) dependencies.add(file.toString());
//                        }
                    } catch (Exception e) {
                        if (enableStacktrace) logger.lifecycle("pizda", e);
                    }
                }
            }
        }


        return dependencies;
    }

    /**
     * @param dependencyPath Путь до jar-ника зависимости
     * @return Пути до cfg-файлов для указанной зависимости
     */
    private Set<String> getCFGs(String dependencyPath) {
        Set<String> cfgs = new HashSet<>();
        try {

            FileTree zipTree = getProject().zipTree(dependencyPath);
            for (File file : zipTree.getFiles()) {
                if ("cfg".equals(getFileExtension(file))) cfgs.add(file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cfgs;
    }

    private void saveATs(Set<String> ats) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(dependenciesAT);
            for (String at : ats) {
                fw.write(at+"\n");
            }
        } catch (IOException e) {
            if (enableStacktrace) e.printStackTrace();
        } finally {
            try {
                if (fw != null) fw.close();
            } catch (IOException e) {
                if (enableStacktrace) e.printStackTrace();
            }
        }
    }

    // Вообще похуй на Files, апач комон и гуаву. С этими вашими апдейтами проще парсить расширение самому.
    private static String getFileExtension(File file) {
        String fileName = file.getName();
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }
}