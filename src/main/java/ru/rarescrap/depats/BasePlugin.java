package ru.rarescrap.depats;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;

import java.util.ArrayList;

public class BasePlugin implements Plugin<Project> {

    static Logger LOGGER;

    @Override
    public void apply(Project project) {
        LOGGER = project.getLogger();
        DepATsPluginExtension e = project.getExtensions().create("DepATs", DepATsPluginExtension.class, project);
        GetDepATsTask t = project.getTasks().create("getDepATs", GetDepATsTask.class);

        ArrayList<Task> tasks = new ArrayList<>(project.getTasksByName("deobfuscateJar", false));
        if (tasks.isEmpty()) LOGGER.error("Can't find \"deobfuscateJar\" task. Make sure you apply the ForgeGradle plugin.");
        else if (tasks.size() > 1) LOGGER.warn("There are multiple \"deobfuscateJar\" tasks. Set \"getDepATs.dependsOn(deobfuscateJar)\" in buildscript by yourself.");
        else {
            Task deobfuscateJar = tasks.get(0);
            deobfuscateJar.dependsOn(t);
        }

        // Для проверки кастомных значений из билдскрипта
//        project.afterEvaluate(project1 -> {
//            DepATsPluginExtension e1 = project1.getExtensions().getByType(DepATsPluginExtension.class);
//            System.out.println(e1.getDepATs());
//        });

    }
}