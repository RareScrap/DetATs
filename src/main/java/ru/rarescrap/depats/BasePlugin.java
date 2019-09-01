package ru.rarescrap.depats;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;

public class BasePlugin implements Plugin<Project> {

    static Logger logger;

    @Override
    public void apply(Project project) {
        DepATsPluginExtension e = project.getExtensions().create("DepATs", DepATsPluginExtension.class, project);
        GetDepATsTask t = project.getTasks().create("getDepATs", GetDepATsTask.class);
        //t.dependsOn("deobfuscateJar"); // TODO: Протестировать

        // Для проверки кастомных значений из билдскрипта
//        project.afterEvaluate(project1 -> {
//            DepATsPluginExtension e1 = project1.getExtensions().getByType(DepATsPluginExtension.class);
//            System.out.println(e1.getDepATs());
//        });

        logger = project.getLogger();
    }
}